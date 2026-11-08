package de.julianbiedermann.app_functions_glasses_sample.ai

import android.app.PendingIntent
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.util.Log
import androidx.appfunctions.AppFunction
import androidx.appfunctions.AppFunctionElementNotFoundException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import androidx.core.content.ContextCompat.startActivity
import androidx.xr.projected.ProjectedContext
import androidx.xr.projected.experimental.ExperimentalProjectedApi
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.components.SingletonComponent
import de.julianbiedermann.app_functions_glasses_sample.glasses.GlassesMainActivity
import de.julianbiedermann.app_functions_glasses_sample.models.Note
import de.julianbiedermann.app_functions_glasses_sample.repositories.notes.NotesRepository
import de.julianbiedermann.app_functions_glasses_sample.repositories.sentnotes.SentNotesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
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

    private companion object {
        const val TAG = "NotesGlassesAppFuncSvc"
    }

    /**
     * Show the N most recent notes on the connected display glasses.
     *
     * Reads the latest [count] notes (sorted by timestamp descending) from the notes repository,
     * enqueues them into the glasses queue, and launches the glasses projected activity.
     *
     * @param count The number of latest notes to display. Must be >= 1. Defaults to 3 when omitted.
     * @return [ShowNotesResult] describing how many notes were enqueued.
     * @throws AppFunctionInvalidArgumentException If [count] is less than 1.
     * @throws AppFunctionElementNotFoundException If no notes exist in the repository.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun showLatestNotes(
        count: Int = 3,
    ): ShowNotesResult = withContext(Dispatchers.IO) {
        if (count < 1) {
            throw AppFunctionInvalidArgumentException("count must be >= 1, got $count")
        }

        val (notesRepo, sentNotesRepo) = getInjectors()
        val allNotes = notesRepo.notes.first()
        if (allNotes.isEmpty()) {
            Log.w(TAG, "Cannot show latest notes: repository is empty")
            throw AppFunctionElementNotFoundException("No notes exist in the repository")
        }

        val selected = allNotes.take(count)
        Log.d(TAG, "showLatestNotes: enqueueing ${selected.size} notes (requested $count)")

        enqueueNotes(selected, notesRepo, sentNotesRepo)
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
     * @return [ShowNotesResult] describing how many matching notes were enqueued.
     * @throws AppFunctionInvalidArgumentException If [query] is blank.
     * @throws AppFunctionElementNotFoundException If no notes match the query.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun searchNotesOnGlasses(
        query: String,
    ): ShowNotesResult = withContext(Dispatchers.IO) {
        if (query.isBlank()) {
            throw AppFunctionInvalidArgumentException("query must not be blank")
        }

        val (notesRepo, sentNotesRepo) = getInjectors()
        val allNotes = notesRepo.notes.first()
        if (allNotes.isEmpty()) {
            Log.w(TAG, "Cannot search notes: repository is empty")
            throw AppFunctionElementNotFoundException("No notes exist in the repository")
        }

        val queryLower = query.lowercase()
        val matching = allNotes.filter { note: Note ->
            note.topic.contains(queryLower, ignoreCase = true) ||
                    note.text.contains(queryLower, ignoreCase = true)
        }

        if (matching.isEmpty()) {
            Log.w(TAG, "searchNotesOnGlasses: no notes match query '$query'")
            throw AppFunctionElementNotFoundException("No notes match the query '$query'")
        }

        Log.d(TAG, "searchNotesOnGlasses: enqueueing ${matching.size} notes matching '$query'")
        enqueueNotes(matching, notesRepo, sentNotesRepo)
        ShowNotesResult(count = matching.size)
    }

    @OptIn(ExperimentalProjectedApi::class)
    private suspend fun enqueueNotes(
        notes: List<Note>,
        notesRepo: NotesRepository,
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
                Log.d(TAG, "Launched glasses activity with ${notes.size} notes")
            } else {
                Log.w(
                    TAG,
                    "Projected device not connected; notes enqueued but glasses not launched"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch glasses activity", e)
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
