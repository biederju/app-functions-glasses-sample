package de.julianbiedermann.app_functions_glasses_sample.domain.notes

import de.julianbiedermann.app_functions_glasses_sample.domain.models.Note
import kotlinx.coroutines.flow.Flow

internal interface NotesRepository {
    val notes: Flow<List<Note>>
    suspend fun addNote(topic: String, text: String)
    suspend fun deleteNote(noteId: String)
}
