package de.julianbiedermann.app_functions_glasses_sample.data.storage

import androidx.datastore.core.Serializer
import de.julianbiedermann.app_functions_glasses_sample.domain.models.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/**
 * A [Serializer] for [NoteListStorage] that reads/writes JSON to a file.
 *
 * Uses [Json] with lenient mode to tolerate missing or unknown keys,
 * ensuring forward compatibility if the [Note] schema changes.
 */
internal object NoteListStorageSerializer : Serializer<NoteListStorage> {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override val defaultValue: NoteListStorage = NoteListStorage(emptyList())

    override suspend fun readFrom(input: InputStream): NoteListStorage =
        withContext(Dispatchers.IO) {
            val content = input.buffered().use { it.readBytes().decodeToString() }
            if (content.isBlank()) {
                defaultValue
            } else {
                try {
                    json.decodeFromString<NoteListStorage>(content)
                } catch (e: Exception) {
                    defaultValue
                }
            }
        }

    override suspend fun writeTo(t: NoteListStorage, output: OutputStream) =
        withContext(Dispatchers.IO) {
            output.buffered().use { it.write(json.encodeToString(t).toByteArray()) }
        }
}
