package de.julianbiedermann.app_functions_glasses_sample.appfunctions

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.appfunctions.AppFunctionDeclaration
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import androidx.core.content.ContextCompat.startActivity
import androidx.xr.projected.ProjectedContext
import androidx.xr.projected.experimental.ExperimentalProjectedApi
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import de.julianbiedermann.app_functions_glasses_sample.appfunctions.models.NoteAppFunctionResult
import de.julianbiedermann.app_functions_glasses_sample.appfunctions.models.NoteListAppFunctionResult
import de.julianbiedermann.app_functions_glasses_sample.domain.models.Note
import de.julianbiedermann.app_functions_glasses_sample.domain.notes.NotesRepository
import de.julianbiedermann.app_functions_glasses_sample.domain.sentnotes.SentNotesRepository
import de.julianbiedermann.app_functions_glasses_sample.extensions.toNoteAppFunctionResult
import de.julianbiedermann.app_functions_glasses_sample.glasses.GlassesMainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * App Function service that exposes notes-to-glasses capabilities to on-device AI agents.
 *
 * Reads notes from the existing [NotesRepository], enqueues them into the shared
 * [SentNotesRepository], and launches the glasses activity via [ProjectedContext].
 */
@AndroidEntryPoint
@AppFunctionServiceEntryPoint(
    serviceName = "NotesGlasses",
    appFunctionXmlFileName = "notes_glasses_app_function_service",
)
abstract class NotesGlassesAppFunctionService : AppFunctionService() {

    /**
     * Create a new note with a topic and body text.
     *
     * Persists the note to the repository and optionally enqueues it into the glasses queue
     * and launches the glasses projected activity if [showOnGlasses] is true.
     *
     * @param topic The title or topic of the note. Must not be blank.
     * @param text The body content of the note. Must not be blank.
     * @param showOnGlasses Whether to immediately display the created note on connected display glasses. Defaults to false.
     * @return [NoteAppFunctionResult] containing the created note details.
     * @throws AppFunctionInvalidArgumentException If [topic] or [text] is blank.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun createNote(
        topic: String,
        text: String,
        showOnGlasses: Boolean = false,
    ): NoteAppFunctionResult = withContext(Dispatchers.IO) {
        if (topic.isBlank()) {
            throw AppFunctionInvalidArgumentException("topic must not be blank")
        }
        if (text.isBlank()) {
            throw AppFunctionInvalidArgumentException("text must not be blank")
        }

        val (notesRepo, sentNotesRepo) = getInjectors()
        notesRepo.addNote(topic = topic.trim(), text = text.trim())

        // The newly created note is at the front (sorted by timestamp descending)
        val createdNote = notesRepo.notes.first().first { it.topic == topic.trim() && it.text == text.trim() }
        Timber.d("createNote: successfully created note with id %s, topic '%s'", createdNote.id, createdNote.topic)

        if (showOnGlasses) {
            enqueueNotes(listOf(createdNote), sentNotesRepo)
        }

        createdNote.toNoteAppFunctionResult()
    }

    /**
     * Edit an existing note matching the given topic.
     *
     * Finds an existing note by its topic (case-insensitive), updates its text and optionally its topic,
     * and persists the changes. Optionally enqueues the updated note into the glasses queue and launches
     * the glasses projected activity if [showOnGlasses] is true.
     *
     * @param topic The topic of the existing note to find and edit. Must not be blank.
     * @param text The new body content of the note. Must not be blank.
     * @param newTopic Optional new topic name if renaming the note.
     * @param showOnGlasses Whether to immediately display the updated note on connected display glasses. Defaults to false.
     * @return [NoteAppFunctionResult] containing the updated note details.
     * @throws AppFunctionInvalidArgumentException If [topic] or [text] is blank, or if no note is found matching [topic].
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun editNote(
        topic: String,
        text: String,
        newTopic: String? = null,
        showOnGlasses: Boolean = false,
    ): NoteAppFunctionResult = withContext(Dispatchers.IO) {
        if (topic.isBlank()) {
            throw AppFunctionInvalidArgumentException("topic must not be blank")
        }
        if (text.isBlank()) {
            throw AppFunctionInvalidArgumentException("text must not be blank")
        }

        val (notesRepo, sentNotesRepo) = getInjectors()
        val allNotes = notesRepo.notes.first()
        val topicTrimmed = topic.trim()
        val targetNote = allNotes.firstOrNull { it.topic.equals(topicTrimmed, ignoreCase = true) }
            ?: throw AppFunctionInvalidArgumentException("No note found with topic: $topicTrimmed")

        val effectiveTopic = newTopic?.takeIf { it.isNotBlank() }?.trim() ?: targetNote.topic
        val effectiveText = text.trim()

        notesRepo.updateNote(
            noteId = targetNote.id,
            topic = effectiveTopic,
            text = effectiveText
        )

        val updatedNote = notesRepo.notes.first().first { it.id == targetNote.id }
        Timber.d("editNote: successfully updated note with id %s, topic '%s'", updatedNote.id, updatedNote.topic)

        if (showOnGlasses) {
            enqueueNotes(listOf(updatedNote), sentNotesRepo)
        }

        updatedNote.toNoteAppFunctionResult()
    }

    /**
     * Return the N most recent notes.
     *
     * Reads the latest notes (sorted by timestamp descending) from the notes repository,
     * enqueues them into the glasses queue, and optionally launches the glasses projected
     * activity if a device is connected.
     *
     * The calling agent always receives the requested notes in the result, regardless of
     * whether glasses are connected.
     *
     * @param count The number of latest notes to return. Defaults to 3 when omitted or when
     *              a value less than 1 is supplied.
     * @return [de.julianbiedermann.app_functions_glasses_sample.appfunctions.models.NoteListAppFunctionResult] containing the returned notes, or an empty list if the
     *         repository is empty.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun showLatestNotes(
        count: Int = 3,
    ): NoteListAppFunctionResult = withContext(Dispatchers.IO) {
        // When the agent omits [count], the library passes the empty value (0), not the
        // declared default of 3. Resolve the meaningful default here.
        val effectiveCount = count.takeIf { it > 0 } ?: 3

        val (notesRepo, sentNotesRepo) = getInjectors()
        val allNotes = notesRepo.notes.first()
        if (allNotes.isEmpty()) {
            Timber.w("Cannot show latest notes: repository is empty")
            return@withContext NoteListAppFunctionResult(notes = emptyList())
        }

        val selected = allNotes.take(effectiveCount)
        Timber.d(
            "showLatestNotes: returning %d notes (requested %d)",
            selected.size,
            effectiveCount
        )

        enqueueNotes(selected, sentNotesRepo)
        NoteListAppFunctionResult(notes = selected.map { it.toNoteAppFunctionResult() })
    }

    /**
     * Search and return notes whose title or text contains the given query.
     *
     * Performs a case-insensitive substring match on each note's title and text. All
     * matching notes are enqueued into the glasses queue and optionally launched on
     * connected glasses. The calling agent always receives the matching notes in the result.
     *
     * @param query The search string to match against note titles and text. Must be non-empty.
     * @return [NoteListAppFunctionResult] containing the matching notes, or an empty list if none match.
     * @throws AppFunctionInvalidArgumentException If [query] is blank.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun searchNotesOnGlasses(
        query: String,
    ): NoteListAppFunctionResult = withContext(Dispatchers.IO) {
        if (query.isBlank()) {
            throw AppFunctionInvalidArgumentException("query must not be blank")
        }

        val (notesRepo, sentNotesRepo) = getInjectors()
        val allNotes = notesRepo.notes.first()
        if (allNotes.isEmpty()) {
            Timber.w("Cannot search notes: repository is empty")
            return@withContext NoteListAppFunctionResult(notes = emptyList())
        }

        val queryLower = query.lowercase()
        val matching = allNotes.filter { note: Note ->
            note.topic.contains(queryLower, ignoreCase = true) ||
                    note.text.contains(queryLower, ignoreCase = true)
        }

        if (matching.isEmpty()) {
            Timber.w("searchNotesOnGlasses: no notes match query '%s'", query)
            return@withContext NoteListAppFunctionResult(notes = emptyList())
        }

        Timber.d("searchNotesOnGlasses: returning %d notes matching '%s'", matching.size, query)
        enqueueNotes(matching, sentNotesRepo)
        NoteListAppFunctionResult(notes = matching.map { it.toNoteAppFunctionResult() })
    }

    @OptIn(ExperimentalProjectedApi::class)
    private suspend fun enqueueNotes(
        notes: List<Note>,
        sentNotesRepo: SentNotesRepository
    ) {
        try {
            // Non-blocking check: launch glasses only if a device is currently connected.
            // Notes are already enqueued, so they will be visible on next glasses open regardless.
            val isConnected = ProjectedContext.isProjectedDeviceConnected(
                applicationContext,
                currentCoroutineContext()
            ).firstOrNull()

            if (isConnected == true) {
                notes.forEach { note: Note ->
                    sentNotesRepo.addNote(note = note)
                }
                val options =
                    ProjectedContext.createProjectedActivityOptions(applicationContext).toBundle()
                val intent = Intent(applicationContext, GlassesMainActivity::class.java)
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                startActivity(this@NotesGlassesAppFunctionService, intent, options)
                Timber.d("Launched glasses activity with %d notes", notes.size)
            } else {
                Timber.d("Projected device not connected; notes enqueued but glasses not launched")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to launch glasses activity")
            // Notes are already enqueued; graceful degradation - the glasses will see them on next open.
        }
    }

    private fun getInjectors(): Pair<NotesRepository, SentNotesRepository> {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            AppFunctionEntryPoint::class.java
        )
        return Pair(entryPoint.notesRepository(), entryPoint.sentNotesRepository())
    }
}

/** Hilt EntryPoint for accessing DI from App Functions. */
@EntryPoint
@InstallIn(SingletonComponent::class)
private interface AppFunctionEntryPoint {
    fun notesRepository(): NotesRepository
    fun sentNotesRepository(): SentNotesRepository
}
