package de.julianbiedermann.app_functions_glasses_sample.ai

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
import de.julianbiedermann.app_functions_glasses_sample.domain.models.Note
import de.julianbiedermann.app_functions_glasses_sample.domain.notes.NotesRepository
import de.julianbiedermann.app_functions_glasses_sample.domain.sentnotes.SentNotesRepository
import de.julianbiedermann.app_functions_glasses_sample.glasses.GlassesMainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.coroutineContext

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
     * Show the N most recent notes on the connected display glasses.
     *
     * Reads the latest [count] notes (sorted by timestamp descending) from the notes repository,
     * enqueues them into the glasses queue, and launches the glasses projected activity.
     *
     * @param count The number of latest notes to display. Defaults to 3 when omitted or when
     *              a value less than 1 is supplied.
     * @return [ShowNotesResult] describing how many notes were enqueued, or 0 if the repository is empty.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun showLatestNotes(
        count: Int = 3,
    ): ShowNotesResult = withContext(Dispatchers.IO) {
        // When the agent omits [count], the library passes the empty value (0), not the
        // declared default of 3. Resolve the meaningful default here.
        val effectiveCount = count.takeIf { it > 0 } ?: 3

        val (notesRepo, sentNotesRepo) = getInjectors()
        val allNotes = notesRepo.notes.first()
        if (allNotes.isEmpty()) {
            Timber.w("Cannot show latest notes: repository is empty")
            return@withContext ShowNotesResult(count = 0)
        }

        val selected = allNotes.take(effectiveCount)
        Timber.d(
            "showLatestNotes: enqueueing %d notes (requested %d)",
            selected.size,
            effectiveCount
        )

        enqueueNotes(selected, sentNotesRepo)
        ShowNotesResult(count = selected.size)
    }

    /**
     * Search and display notes whose title or text contains the given query on the connected
     * display glasses.
     *
     * Performs a case-insensitive substring match on each note's title and text, enqueues all
     * matching notes into the glasses queue, and launches the glasses projected activity.
     *
     * @param query The search string to match against note titles and text. Must be non-empty.
     * @return [ShowNotesResult] describing how many matching notes were enqueued, or 0 if none match.
     * @throws AppFunctionInvalidArgumentException If [query] is blank.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun searchNotesOnGlasses(
        query: String,
    ): ShowNotesResult = withContext(Dispatchers.IO) {
        if (query.isBlank()) {
            throw AppFunctionInvalidArgumentException("query must not be blank")
        }

        val (notesRepo, sentNotesRepo) = getInjectors()
        val allNotes = notesRepo.notes.first()
        if (allNotes.isEmpty()) {
            Timber.w("Cannot search notes: repository is empty")
            return@withContext ShowNotesResult(count = 0)
        }

        val queryLower = query.lowercase()
        val matching = allNotes.filter { note: Note ->
            note.topic.contains(queryLower, ignoreCase = true) ||
                    note.text.contains(queryLower, ignoreCase = true)
        }

        if (matching.isEmpty()) {
            Timber.w("searchNotesOnGlasses: no notes match query '$query'")
            return@withContext ShowNotesResult(count = 0)
        }

        Timber.d("searchNotesOnGlasses: enqueueing %d notes matching '%s'", matching.size, query)
        enqueueNotes(matching, sentNotesRepo)
        ShowNotesResult(count = matching.size)
    }

    @OptIn(ExperimentalProjectedApi::class)
    private suspend fun enqueueNotes(
        notes: List<Note>,
        sentNotesRepo: SentNotesRepository
    ) {
        notes.forEach { note: Note ->
            sentNotesRepo.addNote(note)
        }

        try {
            val connected = ProjectedContext.isProjectedDeviceConnected(
                applicationContext,
                coroutineContext
            ).first { it }

            if (connected) {
                val options =
                    ProjectedContext.createProjectedActivityOptions(applicationContext).toBundle()
                val intent = Intent(applicationContext, GlassesMainActivity::class.java)
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                startActivity(this@NotesGlassesAppFunctionService, intent, options)
                Timber.d("Launched glasses activity with %d notes", notes.size)
            } else {
                Timber.w("Projected device not connected; notes enqueued but glasses not launched")
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
internal interface AppFunctionEntryPoint {
    fun notesRepository(): NotesRepository
    fun sentNotesRepository(): SentNotesRepository
}
