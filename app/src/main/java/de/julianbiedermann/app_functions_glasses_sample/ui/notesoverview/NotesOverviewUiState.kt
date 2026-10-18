package de.julianbiedermann.app_functions_glasses_sample.ui.notesoverview

import de.julianbiedermann.app_functions_glasses_sample.models.Note

internal data class NotesOverviewUiState(
    val notes: List<Note> = emptyList(),
    val error: String? = null
)
