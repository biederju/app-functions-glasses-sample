package de.julianbiedermann.app_functions_glasses_sample.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.julianbiedermann.app_functions_glasses_sample.repositories.NotesRepository
import de.julianbiedermann.app_functions_glasses_sample.repositories.NotesRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNotesRepository(impl: NotesRepositoryImpl): NotesRepository
}
