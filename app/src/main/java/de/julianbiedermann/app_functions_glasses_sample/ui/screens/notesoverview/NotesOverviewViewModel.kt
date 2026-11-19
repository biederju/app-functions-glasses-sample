package de.julianbiedermann.app_functions_glasses_sample.ui.screens.notesoverview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.julianbiedermann.app_functions_glasses_sample.domain.models.Note
import de.julianbiedermann.app_functions_glasses_sample.domain.notes.NotesRepository
import de.julianbiedermann.app_functions_glasses_sample.domain.sentnotes.SentNotesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
internal class NotesOverviewViewModel @Inject constructor(
    private val repository: NotesRepository,
    private val sentNotesRepository: SentNotesRepository
) : ViewModel() {

    val uiState: StateFlow<NotesOverviewUiState> = repository.notes
        .onStart {
            Timber.d("Loading notes from DataStore")
        }
        .catch { e ->
            Timber.e(e, "Error loading notes")
            emit(emptyList())
        }
        .map { notes ->
            NotesOverviewUiState(notes = notes)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = NotesOverviewUiState()
        )

    fun addNote(topic: String, text: String) {
        viewModelScope.launch {
            try {
                repository.addNote(topic, text)
            } catch (e: Exception) {
                Timber.e(e, "Error adding note")
            }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            try {
                repository.deleteNote(noteId)
                sentNotesRepository.dismissNote(noteId)
            } catch (e: Exception) {
                Timber.e(e, "Error deleting note")
            }
        }
    }

    fun sendNoteToGlasses(note: Note) {
        try {
            sentNotesRepository.addNote(note)
        } catch (e: Exception) {
            Timber.e(e, "Error sending note to glasses")
        }
    }
}
