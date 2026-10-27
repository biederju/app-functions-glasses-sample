package de.julianbiedermann.app_functions_glasses_sample.repositories.sentnotes

import android.util.Log
import de.julianbiedermann.app_functions_glasses_sample.models.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "SentNotesRepositoryImpl"

@Singleton
internal class SentNotesRepositoryImpl @Inject constructor() : SentNotesRepository {

    private val _sentNotes = MutableStateFlow<List<Note>>(emptyList())
    override val sentNotes: StateFlow<List<Note>> = _sentNotes.asStateFlow()

    override fun addNote(note: Note) {
        _sentNotes.update { current -> current + note }
        Log.d(TAG, "Added note to glasses queue: ${note.topic}")
    }

    override fun dismissNote(noteId: String) {
        _sentNotes.update { current -> current.filterNot { it.id == noteId } }
        Log.d(TAG, "Dismissed note from glasses queue: $noteId")
    }
}
