# agents-alignment-v2 Tasks

## Part A: Dependency Updates

- [x] Add `androidx.datastore:datastore` to libs.versions.toml
- [x] Add `Timber` to libs.versions.toml
- [x] Add `implementation(libs.timber)` to app/build.gradle.kts
- [x] Add `implementation(libs.kotlinx.io.core)` to app/build.gradle.kts

## Part B: Storage Migration (typed DataStore<NoteList>)

- [x] Create `NoteListStorage` wrapper data class
- [x] Create `NoteListStorageSerializer` (Serializer<NoteList>) in `data/storage/`
- [x] Update `StorageModule.kt` to provide `DataStore<NoteList>`
- [x] Migrate `NotesRepositoryImpl` to use typed DataStore (remove key-value map)
- [x] Update `SentNotesRepository` interface to use Flow (match existing)

## Part C: Logging Migration (android.util.Log → Timber)

- [x] Initialize Timber in `AppFunctionsGlassesSampleApplication`
- [x] Migrate `NotesRepositoryImpl`
- [x] Migrate `SentNotesRepositoryImpl`
- [x] Migrate `NotesGlassesAppFunctionService`
- [x] Migrate `AudioInterface`
- [x] Migrate `GlassesMainActivity`
- [x] Migrate `NotesOverviewScreen`
- [x] Migrate `NotesOverviewViewModel`

## Part D: Package Structure Refactor

- [x] Move `models/Note.kt` → `domain/models/Note.kt` (make `public`)
- [x] Move `repositories/notes/*` → `domain/notes/*`
- [x] Move `repositories/sentnotes/*` → `domain/sentnotes/*`
- [x] Move `ui/notesoverview/*` → `ui/screens/notesoverview/*`
- [x] Move `NoteListStorageSerializer` → `data/storage/`
- [x] Update all imports across the app
- [x] Update Hilt bindings in `RepositoryModule`

## Part E: AGENTS.md Updates

- [x] Update Tech Stack → Storage description
- [x] Update Tech Stack → Logging description
- [x] Update Module Package Structure section
- [x] Add UI Verification subsection

## Part F: Verification

- [x] Run `./gradlew assembleDebug`
- [x] Run `./gradlew lintDebug`
