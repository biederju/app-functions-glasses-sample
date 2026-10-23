package de.julianbiedermann.app_functions_glasses_sample.repositories

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.julianbiedermann.app_functions_glasses_sample.models.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "NotesRepositoryImpl"

@Singleton
internal class NotesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : NotesRepository {

    companion object {
        private val NOTES_KEY = stringPreferencesKey("notes_list")
        private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    }

    override val notes: Flow<List<Note>> = dataStore.data
        .map { preferences ->
            val jsonStr = preferences[NOTES_KEY]
            if (jsonStr.isNullOrEmpty()) emptyList() else {
                try {
                    json.decodeFromString<List<Note>>(jsonStr)
                        .sortedByDescending { it.timestamp }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to decode notes from DataStore", e)
                    emptyList()
                }
            }
        }
        .catch { e ->
            Log.e(TAG, "Error reading notes from DataStore", e)
            emit(emptyList())
        }

    override suspend fun addNote(topic: String, text: String) {
        val note = Note(
            id = java.util.UUID.randomUUID().toString(),
            topic = topic,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        dataStore.edit { preferences ->
            val current = decodeNotes(preferences)
            val updated = current + note
            preferences[NOTES_KEY] = json.encodeToString(updated)
        }
        Log.d(TAG, "Added note: $topic")
    }

    override suspend fun deleteNote(noteId: String) {
        dataStore.edit { preferences ->
            val current = decodeNotes(preferences)
            val updated = current.filterNot { it.id == noteId }
            preferences[NOTES_KEY] = json.encodeToString(updated)
        }
        Log.d(TAG, "Deleted note: $noteId")
    }

    private fun decodeNotes(preferences: Preferences): List<Note> {
        val jsonStr = preferences[NOTES_KEY]
        return if (jsonStr.isNullOrEmpty()) emptyList() else {
            try {
                json.decodeFromString<List<Note>>(jsonStr)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to decode notes during edit", e)
                emptyList()
            }
        }
    }
}
