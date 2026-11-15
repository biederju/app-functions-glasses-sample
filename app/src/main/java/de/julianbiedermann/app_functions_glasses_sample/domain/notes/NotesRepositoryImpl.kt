package de.julianbiedermann.app_functions_glasses_sample.domain.notes

import de.julianbiedermann.app_functions_glasses_sample.di.NotesDataStore
import de.julianbiedermann.app_functions_glasses_sample.domain.models.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class NotesRepositoryImpl @Inject constructor(
    private val dataStore: NotesDataStore
) : NotesRepository {

    override val notes: Flow<List<Note>> = dataStore.data
        .map { noteList ->
            noteList.notes.sortedByDescending { it.timestamp }
        }
        .catch { e ->
            emit(emptyList())
        }

    override suspend fun addNote(topic: String, text: String) {
        val note = Note(
            id = UUID.randomUUID().toString(),
            topic = topic,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        dataStore.updateData { current ->
            current.copy(notes = current.notes + note)
        }
    }

    override suspend fun deleteNote(noteId: String) {
        dataStore.updateData { current ->
            current.copy(notes = current.notes.filterNot { it.id == noteId })
        }
    }
}
