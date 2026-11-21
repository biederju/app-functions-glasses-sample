# Phone UI — Notes Overview Screen Execution Plan

## Goal

Implement the initial **Phone UI feature: Notes Overview Screen** for the sample app. This is the
phone-side management surface for notes: list notes (topic, text, timestamp), add a note via a
Floating Action Button (FAB), and delete a note via a modern Swipe-to-Dismiss interaction.

Because the project currently has **no** Hilt, ViewModel, Repository, or persistence layer, this
feature also establishes those foundational building blocks:

- **Persistence:** Preferences **DataStore** (lightweight key-value, storing the notes list as JSON)
  — NOT a database, respecting the AGENTS.md "no local databases / complex persistence" rule.
- **DI:** **Hilt** set up from scratch (Gradle plugin, KSP, `@HiltAndroidApp` Application class).

> [!NOTE] **Navigation 3 not needed.**
> Since the phone UI consists of a single screen (Notes Overview), and the add-note interaction
> uses a `Dialog` (no separate destination) while delete uses Swipe-to-Dismiss, there is no
> navigation graph to manage. Navigation 3 is intentionally **left out** of this feature to keep
> the implementation simpler and is not a dependency of this plan.

## Clarified Decisions (from user)

| Question                         | Decision                                                                                                 |
|----------------------------------|----------------------------------------------------------------------------------------------------------|
| Persistence approach             | Preferences **DataStore** (JSON-encoded list, non-database)                                              |
| Hilt + Navigation 3 now or later | Hilt **now**; Navigation 3 **not needed** for single-screen UI                                           |
| ConnectionScreen fate            | **Removed** — Notes Overview becomes the primary phone destination                                       |
| Glasses connection indicator     | **Single icon** in the top app bar (right side) that changes based on connection status                  |
| Add Note interaction             | Material 3 **AlertDialog** with topic + text fields                                                      |
| Delete interaction               | Material 3 **SwipeToDismissBox**                                                                         |
| Screen naming                    | `NotesOverviewScreen` + `NotesOverviewViewModel` + `NotesOverviewUiState` in `ui/notesoverview`          |
| Package layout                   | `models/Note.kt` and `repositories/NotesRepository.kt` (not `data/...`)                                  |
| Visibility                       | All new classes/interfaces use `internal` visibility modifier                                            |
| Unit tests                       | Not needed for `NotesRepositoryImpl` (no plain JVM tests required)                                       |
| Composable structure             | `private` composables within a screen file; `internal`/`public` reusable composables in `ui/components/` |

## Resolved Design

- The phone UI consists of a **single Activity with one primary destination:
  `NotesOverviewScreen`**.
  The previous "ConnectionScreen" (a full-screen button to launch glasses) is **removed**.
- **Glasses connectivity indicator:** Moved into the **top app bar** on the **right side** as a
  **single status icon** that reflects connected/disconnected state. The icon uses the **wifi
  icon** (`Icons.Filled.Wifi`) when glasses are **connected**, and a **strikethrough wifi icon**
  (`Icons.Filled.WifiOff`) when **not connected**. There is no separate launch-glasses action
  icon — the status icon serves that purpose (tapping launches glasses when connected). The old
  full-screen ConnectionScreen button is removed.
- **DataStore JSON serialization:** The notes list is serialized to JSON (`kotlinx.serialization`)
  and stored under a single Preference key. Proto DataStore is
  avoided to keep it lightweight.
- **Deletion:** Swipe-to-Dismiss (Material 3 `SwipeToDismissBox`) — chosen "state-of-the-art"
  interaction.
- **Add Note:** Material 3 `AlertDialog` with topic + text fields (no separate navigation
  destination).
- **List refresh:** The list refreshes automatically and correctly after adding (and after
  deleting) a note because it flows directly from the DataStore `data` flow — see the
  `NotesRepositoryImpl` and ViewModel wiring below. No manual reload is required.
- **No loading indicator:** Since there are no backend/network requests (all data is local in
  DataStore), the UI does **not** show a loading spinner or loading state anywhere.
- **List item dividers:** Note items in the `LazyColumn` are visually separated by
  `HorizontalDivider` composables between items (Material 3 dividers).

## Proposed Changes

### Build & Dependency Layer

#### [MODIFY] [libs.versions.toml](/Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/gradle/libs.versions.toml)

Add version + library entries for:

- Hilt (Dagger) — `com.google.dagger:hilt-android` + `hilt-compiler` (stable ~2.60.x)
- Hilt Navigation Compose — `androidx.hilt:hilt-navigation-compose` (1.4.0) — **only for
  `hiltViewModel()`**
- KSP plugin — version matched to Kotlin `2.2.10` (e.g. `2.2.10-2.0.x`)
- DataStore Preferences — `androidx.datastore:datastore-preferences` (1.2.1)
- JSON serialization — `kotlinx-serialization-json`
- Lifecycle ViewModel Compose — `androidx.lifecycle:lifecycle-viewmodel-compose`

> [!IMPORTANT]
> **Navigation 3** (`androidx.navigation3:*`) is **NOT** added — see note above.

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

`@HiltAndroidApp internal class AppFunctionsGlassesSampleApplication : Application()`.

#### [MODIFY] [AndroidManifest.xml](/Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/app/src/main/AndroidManifest.xml)

Reference the new Application class via `android:name`.

#### [NEW] [di/DatabaseModule.kt] (DataStore Provider)

`@Module @InstallIn(SingletonComponent::class)` providing the
`DataStore<Preferences>` via `preferencesDataStore` delegate at `applicationContext`.
Internal visibility.

---

### Domain / Data Layer

#### [NEW] [models/Note.kt]

```
app/src/main/java/de/julianbiedermann/app_functions_glasses_sample/models/Note.kt
```

```kotlin
@Serializable
internal data class Note(
    val id: String,
    val topic: String,
    val text: String,
    val timestamp: Long
)
```

Immutable model with English identifiers.

#### [NEW] [repositories/NotesRepository.kt] (interface)

```
app/src/main/java/de/julianbiedermann/app_functions_glasses_sample/repositories/NotesRepository.kt
```

```kotlin
internal interface NotesRepository {
    val notes: Flow<List<Note>>
    suspend fun addNote(topic: String, text: String)
    suspend fun deleteNote(noteId: String)
}
```

#### [NEW] [repositories/NotesRepositoryImpl.kt]

```
app/src/main/java/de/julianbiedermann/app_functions_glasses_sample/repositories/NotesRepositoryImpl.kt
```

`@Singleton` / Hilt-injected implementation backed by the `DataStore<Preferences>`.
Persists the list as JSON under a single key; exposes the list as `Flow<List<Note>>`
(from the DataStore `data` flow, mapping + sorting by timestamp desc).
Handles empty/failed decode gracefully (returns empty list).
Internal visibility. No plain unit tests required.

#### [NEW] [di/RepositoryModule.kt]

`@Module @InstallIn(SingletonComponent::class)` binding `NotesRepository`
to `NotesRepositoryImpl` (abstract binding via `@Binds`).
Internal visibility.

---

### UI / Presentation Layer

#### [NEW] [ui/notesoverview/NotesOverviewUiState.kt]

```
app/src/main/java/de/julianbiedermann/app_functions_glasses_sample/ui/notesoverview/NotesOverviewUiState.kt
```

```kotlin
internal data class NotesOverviewUiState(
    val notes: List<Note> = emptyList(),
    val error: String? = null
)
```

> [!NOTE] **No `isLoading` field.** There are no backend/network requests (all data is local in
> DataStore), so a loading indicator is not needed. The plan intentionally omits any loading
> state/spinner to keep the screen direct and instant.

Optional: add-note dialog state fields.

#### [NEW] [ui/notesoverview/NotesOverviewViewModel.kt]

`@HiltViewModel internal class NotesOverviewViewModel @Inject constructor(repo: NotesRepository) : ViewModel()`.

- Exposes `val uiState: StateFlow<NotesOverviewUiState>` (combining repo flow with loading/error).
- User events (functions): `loadNotes()`, `addNote(topic, text)`, `deleteNote(note)`.
- All background work on `viewModelScope` + `Dispatchers.IO`; explicit `try-catch` error handling.

#### [NEW] [ui/notesoverview/NotesOverviewScreen.kt]

- `NotesOverviewScreen()` — `internal` screen wrapper that collects the ViewModel state and
  delegates to `NotesOverviewContent`.
- `private NotesOverviewContent(uiState, onAddNote, onDeleteNote, ...)` — the actual screen
  UI: Scaffold + `FloatingActionButton`. `private` because it is only used within this file.
- `private NotesList(list, onDelete)` — `LazyColumn` of `NoteItem`s with a
  `HorizontalDivider` between each item.
- `private NoteItem(note, onDelete)` — card with topic, text, timestamp + Swipe-to-Dismiss
  (`SwipeToDismissBox`). On dismissal, `onDelete` is invoked and the list refreshes automatically
  via the DataStore flow.
- `private AddNoteDialog(...)` — Material 3 `AlertDialog` with topic + text `OutlinedTextField`s.
- **Preview** `@Preview` composable renders `NotesOverviewContent` (not the screen wrapper)
  in **light and dark** mode, since `NotesOverviewContent` has no ViewModel injection.

#### [MODIFY] [MainActivity.kt](/Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/app/src/main/java/de/julianbiedermann/app_functions_glasses_sample/MainActivity.kt)

`@AndroidEntryPoint`. `setContent { theme { NotesOverviewScreen() } }`.
Remove the existing inline `ConnectionScreen` composable entirely (per user decision); the
Notes Overview becomes the primary destination and the glasses-launch + connection status move
into the Notes Overview top app bar.

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
MainActivity (single Activity, @AndroidEntryPoint)
└── NotesOverviewScreen (Scaffold)
    ├── TopAppBar (title "Notes")
    │   └── actions (right side):
    │       └── Glasses connection status icon (icon which changes based on connection status - use the wifi icon for it)
    ├── LazyColumn
    │   ├── NoteItem (Card) [SwipeToDismissBox → delete]
    │   │   ├── Topic (titleMedium)
    │   │   ├── Text (bodyMedium)
    │   │   └── Timestamp (labelSmall)
    │   └── HorizontalDivider (between each NoteItem)
    ├── Empty state (when no notes)
    ├── FloatingActionButton (Add) → opens AddNoteDialog
    └── AddNoteDialog (AlertDialog: topic + text fields)
```

**State management (UDF):**

- *State flows down:* `NotesOverviewUiState` exposed on `StateFlow`, collected with
  `collectAsStateWithLifecycle`.
- *Events flow up:* direct VM calls `viewModel.addNote(...)`, `viewModel.deleteNote(...)`.

---

## Verification Plan

### Build

- Run `./gradlew assembleDebug` and confirm it compiles with no errors (required by AGENTS.md).
- Run `./gradlew :app:compileDebugKotlin` if needed to isolate Kotlin errors faster.

### Manual Verification (functional requirements)

- **Swipe-to-delete:** Swipe a note; confirm it is removed from the screen and the list refreshes
  correctly after deletion (DataStore flow emits the updated list).
- **Glasses connection icon:** Confirm the top app bar shows the **wifi icon** when connected and
  the **strikethrough wifi icon** (`WifiOff`) when not connected.
- **List refresh after add:** Add a note via the FAB/dialog; confirm the new note appears in the
  list without a manual reload (DataStore flow re-emits).
- **No loading indicator:** Confirm no loading spinner/state appears at any point during add or
  delete (local-only data, no backend requests).
- **Dividers:** Confirm note items are visually separated by `HorizontalDivider`s.

### Automated Tests

- **None** required for this feature. The user explicitly stated no plain unit tests are needed
  for `NotesRepositoryImpl`. AGENTS.md: no tests unless explicitly requested.