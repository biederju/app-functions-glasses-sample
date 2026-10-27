package de.julianbiedermann.app_functions_glasses_sample.repositories.notes

import de.julianbiedermann.app_functions_glasses_sample.models.Note
import kotlinx.coroutines.flow.Flow

internal interface NotesRepository {
    val notes: Flow<List<Note>>
    suspend fun addNote(topic: String, text: String)
    suspend fun deleteNote(noteId: String)
}
