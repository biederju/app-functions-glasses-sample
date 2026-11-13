package de.julianbiedermann.app_functions_glasses_sample.data.storage

import androidx.datastore.core.Serializer
import de.julianbiedermann.app_functions_glasses_sample.domain.models.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/**
 * Wrapper for a typed, JSON-based [DataStore] that stores a list of [Note]s.
 *
 * Unlike the previous key-value [androidx.datastore.preferences.core.Preferences]
 * approach, this serializer writes a plain JSON file containing the full list of notes.
 * If the file is missing or cannot be parsed, an empty list is returned (graceful reset).
 */
@Serializable
internal data class NoteList(
    val notes: List<Note> = emptyList()
)

/**
 * A [Serializer] for [NoteList] that reads/writes JSON to a file.
 *
 * Uses [Json] with lenient mode to tolerate missing or unknown keys,
 * ensuring forward compatibility if the [Note] schema changes.
 */
internal object NoteListSerializer : Serializer<NoteList> {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override val defaultValue: NoteList = NoteList(emptyList())

    override suspend fun readFrom(input: InputStream): NoteList = withContext(Dispatchers.IO) {
        val content = input.buffered().use { it.readBytes().decodeToString() }
        if (content.isBlank()) {
            defaultValue
        } else {
            try {
                json.decodeFromString<NoteList>(content)
            } catch (e: Exception) {
                defaultValue
            }
        }
    }

    override suspend fun writeTo(t: NoteList, output: OutputStream) = withContext(Dispatchers.IO) {
        output.buffered().use { it.write(json.encodeToString(t).toByteArray()) }
    }
}
