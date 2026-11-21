package de.julianbiedermann.app_functions_glasses_sample.domain.sentnotes

import de.julianbiedermann.app_functions_glasses_sample.domain.models.Note
import kotlinx.coroutines.flow.Flow

internal interface SentNotesRepository {
    val sentNotes: Flow<List<Note>>
    fun addNote(note: Note)
    fun dismissNote(noteId: String)
}
