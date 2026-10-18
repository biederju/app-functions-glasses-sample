package de.julianbiedermann.app_functions_glasses_sample.ui.notesoverview

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.julianbiedermann.app_functions_glasses_sample.repositories.NotesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "NotesOverviewViewModel"

@HiltViewModel
internal class NotesOverviewViewModel @Inject constructor(
    private val repository: NotesRepository
) : ViewModel() {

    val uiState: StateFlow<NotesOverviewUiState> = repository.notes
        .onStart {
            Log.d(TAG, "Loading notes from DataStore")
        }
        .catch { e ->
            Log.e(TAG, "Error loading notes", e)
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
                Log.e(TAG, "Error adding note", e)
            }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            try {
                repository.deleteNote(noteId)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting note", e)
            }
        }
    }
}
