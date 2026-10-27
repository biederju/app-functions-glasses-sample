package de.julianbiedermann.app_functions_glasses_sample.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.julianbiedermann.app_functions_glasses_sample.repositories.notes.NotesRepository
import de.julianbiedermann.app_functions_glasses_sample.repositories.notes.NotesRepositoryImpl
import de.julianbiedermann.app_functions_glasses_sample.repositories.sentnotes.SentNotesRepository
import de.julianbiedermann.app_functions_glasses_sample.repositories.sentnotes.SentNotesRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNotesRepository(impl: NotesRepositoryImpl): NotesRepository

    @Binds
    @Singleton
    abstract fun bindSentNotesRepository(impl: SentNotesRepositoryImpl): SentNotesRepository
}
