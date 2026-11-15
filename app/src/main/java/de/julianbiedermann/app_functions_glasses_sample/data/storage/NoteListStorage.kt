package de.julianbiedermann.app_functions_glasses_sample.data.storage

import de.julianbiedermann.app_functions_glasses_sample.domain.models.Note
import kotlinx.serialization.Serializable

/**
 * Wrapper for a typed, JSON-based [DataStore] that stores a list of [de.julianbiedermann.app_functions_glasses_sample.domain.models.Note]s.
 *
 * Unlike the previous key-value [androidx.datastore.preferences.core.Preferences]
 * approach, this serializer writes a plain JSON file containing the full list of notes.
 * If the file is missing or cannot be parsed, an empty list is returned (graceful reset).
 */
@Serializable
internal data class NoteListStorage(
    val notes: List<Note> = emptyList()
)