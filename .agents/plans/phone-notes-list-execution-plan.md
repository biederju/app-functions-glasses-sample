# Phone UI — Notes List Screen Execution Plan

## Goal

Implement the initial **Phone UI feature: Notes List Screen** for the sample app. This is the
phone-side management surface for notes: list notes (topic, text, timestamp), add a note via a
Floating Action Button (FAB), and delete a note via a modern Swipe-to-Dismiss interaction.

Because the project currently has **no** Hilt, Navigation 3, ViewModel, Repository, or persistence
layer, this feature also establishes those foundational building blocks (chosen by the user):

- **Persistence:** Preferences **DataStore** (lightweight key-value, storing the notes list as JSON)
  — NOT a database, respecting the AGENTS.md "no local databases / complex persistence" rule.
- **DI:** **Hilt** set up from scratch (Gradle plugin, KSP, `@HiltAndroidApp` Application class).
- **Navigation:** Navigation 3 graph for the phone UI.

## Clarified Decisions (from user)

| Question | Decision |
| --- | --- |
| Persistence approach | Preferences **DataStore** (JSON-encoded list, non-database) |
| Hilt + Navigation 3 now or later | **Set up both now** to align with AGENTS.md architecture |
| ConnectionScreen fate | **Removed** — Notes List becomes the primary phone destination |
| Glasses connection indicator | Shown as **icons in the top app bar (right side)** of the Notes List |
| Add Note interaction | Material 3 **AlertDialog** with topic + text fields |
| Delete interaction | Material 3 **SwipeToDismissBox** |

## Resolved Design

- The phone UI is a Navigation 3 graph whose **primary destination is the Notes List Screen**.
  The previous "ConnectionScreen" (a full-screen button to launch glasses) is **removed**.
- **Glasses connectivity indicator:** Moved into the **top app bar** on the **right side** via
  icons (e.g. a status icon that reflects connected/disconnected state), instead of the old
  full-screen button + status text. The glasses-launch capability becomes an action/icon in the
  top app bar rather than a dedicated screen.
- **DataStore JSON serialization:** The notes list is serialized to JSON
  (`kotlinx.serialization` or Gson) and stored under a single Preference key. Proto DataStore is
  avoided to keep it lightweight.
- **Deletion:** Swipe-to-Dismiss (Material 3 `SwipeToDismissBox`) — chosen "state-of-the-art"
  interaction.
- **Add Note:** Material 3 `AlertDialog` with topic + text fields (no separate navigation
  destination), keeping navigation minimal.

## Proposed Changes

### Build & Dependency Layer

#### [MODIFY] [libs.versions.toml](/Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/gradle/libs.versions.toml)
Add version + library entries for:
- Hilt (Dagger) — `com.google.dagger:hilt-android` + `hilt-compiler` (stable ~2.60.x)
- Hilt Navigation Compose — `androidx.hilt:hilt-navigation-compose` (1.4.0)
- KSP plugin — version matched to Kotlin `2.2.10` (e.g. `2.2.10-2.0.x`)
- Navigation 3 — `androidx.navigation3:navigation3-runtime` + `navigation3-ui` (1.2.0)
- DataStore Preferences — `androidx.datastore:datastore-preferences` (1.2.1)
- JSON serialization — `kotlinx-serialization-json` (or Gson)
- Lifecycle ViewModel Compose — `androidx.lifecycle:lifecycle-viewmodel-compose`

#### [MODIFY] [app/build.gradle.kts](/Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/app/build.gradle.kts)
Apply plugins: `com.google.devtools.ksp`, `dagger.hilt.android.plugin`,
`org.jetbrains.kotlin.plugin.serialization`.
Add dependencies (implementation + `ksp` for `hilt-compiler`).

#### [MODIFY] [settings.gradle.kts](/Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/settings.gradle.kts)
Declare the plugin versions in `pluginManagement` `plugins` block (or rely on version catalog
`[plugins]` + top-level plugin declaration).

---

### Application & DI

#### [NEW] [AppFunctionsGlassesSampleApplication.kt](/Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/app/src/main/java/de/julianbiedermann/app_functions_glasses_sample/AppFunctionsGlassesSampleApplication.kt)
`@HiltAndroidApp class AppFunctionsGlassesSampleApplication : Application()`.

#### [MODIFY] [AndroidManifest.xml](/Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/app/src/main/AndroidManifest.xml)
Reference the new Application class via `android:name`.

#### [NEW] [di/DatabaseModule.kt] (DataStore Provider)
`@Module @InstallIn(SingletonComponent::class)` providing the
`DataStore<Preferences>` via `preferencesDataStore` delegate at `applicationContext`.

---

### Domain / Data Layer

#### [NEW] [data/model/Note.kt]
`data class Note(id, topic, text, timestamp)` — immutable model. English identifiers.

#### [NEW] [data/NotesRepository.kt] (interface)
```kotlin
interface NotesRepository {
    val notes: Flow<List<Note>>
    suspend fun addNote(topic: String, text: String)
    suspend fun deleteNote(noteId: String)
}
```

#### [NEW] [data/NotesRepositoryImpl.kt]
`@Singleton` / Hilt-injected implementation backed by the `DataStore<Preferences>`.
Persists the list as JSON under a single key; exposes the list as `Flow<List<Note>>`
(from the DataStore `data` flow, mapping + sorting by timestamp desc).
Handles empty/failed decode gracefully (returns empty list).

#### [NEW] [di/RepositoryModule.kt]
`@Module @InstallIn(SingletonComponent::class)` binding `NotesRepository`
to `NotesRepositoryImpl` (abstract binding via `@Binds`).

---

### UI / Presentation Layer

#### [NEW] [ui/notes/NotesUiState.kt]
Immutable `UiState`:
```kotlin
data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
```
Optional: add-note dialog state fields.

#### [NEW] [ui/notes/NotesViewModel.kt]
`@HiltViewModel class NotesViewModel @Inject constructor(repo: NotesRepository) : ViewModel()`.
- Exposes `val uiState: StateFlow<NotesUiState>` (combining repo flow with loading/error).
- User events (functions): `loadNotes()`, `addNote(topic, text)`, `deleteNote(note)`.
- All background work on `viewModelScope` + `Dispatchers.IO`; explicit `try-catch` error handling.

#### [NEW] [ui/notes/NotesListScreen.kt]
Composable(s):
- `NotesListScreen(uiState, onAddNote, onDeleteNote, ...)` — Scaffold + `FloatingActionButton`.
- `NotesList(list, onDelete)` — `LazyColumn` of `NoteItem`s.
- `NoteItem(note, onDelete)` — card with topic, text, timestamp + Swipe-to-Dismiss
  (`SwipeToDismissBox`).
- `AddNoteDialog(...)` — Material 3 `AlertDialog` with topic + text `OutlinedTextField`s.
- Preview composables for each reusable component in **light and dark** mode (per AGENTS.md).

#### [NEW] [ui/navigation/PhoneNavHost.kt]
Navigation 3 graph (`NavHost` / `NavBackStack`) with the Notes List as the primary destination.

#### [MODIFY] [MainActivity.kt](/Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/app/src/main/java/de/julianbiedermann/app_functions_glasses_sample/MainActivity.kt)
`@AndroidEntryPoint`. `setContent { theme { PhoneNavHost() } }`.
Remove the existing inline `ConnectionScreen` composable entirely (per user decision); the
Notes List becomes the primary destination and the glasses-launch + connection status move
into the Notes List top app bar.

---

### Resources

#### [MODIFY] [strings.xml](/Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/app/src/main/res/values/strings.xml)
Add English UI strings: screen title, FAB content description, add-note dialog labels,
note topic/text/timestamp hints, empty-state text, delete confirmation.

#### [MODIFY] [themes.xml] (if needed)
Ensure a Material 3 theme/window background compatible with the Notes UI (likely already fine).

---

## UI Structure Summary

```
PhoneNavHost (Navigation 3)
└── NotesListScreen (Scaffold)
    ├── TopAppBar (title "Notes")
    │   └── actions (right side):
    │       ├── Glasses connection indicator icon (connected/disconnected)
    │       └── Launch-glasses action icon
    ├── LazyColumn
    │   └── NoteItem (Card) [SwipeToDismissBox → delete]
    │       ├── Topic (titleMedium)
    │       ├── Text (bodyMedium)
    │       └── Timestamp (labelSmall)
    ├── Empty state (when no notes)
    ├── FloatingActionButton (Add) → opens AddNoteDialog
    └── AddNoteDialog (AlertDialog: topic + text fields)
```

**State management (UDF):**
- *State flows down:* `NotesUiState` exposed on `StateFlow`, collected with
  `collectAsStateWithLifecycle`.
- *Events flow up:* direct VM calls `viewModel.addNote(...)`, `viewModel.deleteNote(...)`.

---

## Verification Plan

### Build
- Run `./gradlew assembleDebug` and confirm it compiles with no errors (required by AGENTS.md).
- Run `./gradlew :app:compileDebugKotlin` if needed to isolate Kotlin errors faster.

### Automated Tests
- None required for this feature (AGENTS.md: no tests unless explicitly requested).
  If desired later: a plain JVM unit test for `NotesRepositoryImpl` JSON round-trip (no device).

### Manual Verification
- Deploy the app to a phone emulator via `deploy`.
- Verify:
  1. Notes list renders (topic, text, timestamp) when notes exist.
  2. FAB opens the Add dialog; entering topic/text creates a note that appears in the list.
  3. Swipe-to-Dismiss removes a note; the list updates and persists (re-launch keeps notes,
     per DataStore).
  4. Empty state shown when no notes.
  5. Light & dark mode previews render correctly.
