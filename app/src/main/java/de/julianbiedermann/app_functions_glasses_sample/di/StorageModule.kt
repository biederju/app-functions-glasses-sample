package de.julianbiedermann.app_functions_glasses_sample.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.julianbiedermann.app_functions_glasses_sample.data.storage.NoteList
import de.julianbiedermann.app_functions_glasses_sample.data.storage.NoteListSerializer
import javax.inject.Singleton

/** Type alias for the typed, file-backed notes DataStore. */
internal typealias NotesDataStore = DataStore<NoteList>

private const val NOTE_LIST_DATA_STORE_FILE = "note_list.json"

/**
 * Provides a file-backed [DataStore] for [NoteList] using a custom JSON [Serializer].
 *
 * Unlike the previous Preferences DataStore, this stores notes as a plain JSON file,
 * giving us a typed, key-value-free approach aligned with [AGENTS.md](AGENTS.md).
 */
@Module
@InstallIn(SingletonComponent::class)
internal object StorageModule {

    @Provides
    @Singleton
    fun provideNotesDataStore(
        @ApplicationContext context: Context
    ): NotesDataStore = DataStoreFactory.create(
        serializer = NoteListSerializer,
    ) {
        context.dataStoreFile(NOTE_LIST_DATA_STORE_FILE)
    }
}
