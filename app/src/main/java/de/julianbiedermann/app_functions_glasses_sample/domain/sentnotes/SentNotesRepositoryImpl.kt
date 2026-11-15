package de.julianbiedermann.app_functions_glasses_sample.domain.sentnotes

import de.julianbiedermann.app_functions_glasses_sample.domain.models.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SentNotesRepositoryImpl @Inject constructor() : SentNotesRepository {

    private val _sentNotes = MutableStateFlow<List<Note>>(value = listOf())
    override val sentNotes: StateFlow<List<Note>> = _sentNotes.asStateFlow()

    override fun addNote(note: Note) {
        _sentNotes.update { current -> (current.filterNot { it.id == note.id }) + note }
        Timber.d("Added note to glasses queue: %s", note.topic)
    }

    override fun dismissNote(noteId: String) {
        _sentNotes.update { current -> current.filterNot { it.id == noteId } }
        Timber.d("Dismissed note from glasses queue: %s", noteId)
    }
}
