# AGENTS.md Verification, Alignment & UI-Testing Verification Plan (v2)

## Goal

Bring `AGENTS.md` into agreement with the actual project state and fix the concrete
deviations that were found during analysis. This plan:

1. **Updates `AGENTS.md`** to:
   - Add a **UI verification** workflow (from the `android-cli` skill) under "Mandatory
     Post-Change Verification".
   - Correct the **storage** description (migrate off key-value Preferences DataStore to a
     typed, JSON-based DataStore) and the **logging** description (adopt Timber as documented).
   - Correct the **module package structure** section to match the target structure after the
     package refactor.
2. **Changes the code** to match AGENTS.md:
   - **Storage:** Migrate `NotesRepositoryImpl` from key-value
     `DataStore<Preferences>` to a typed `DataStore<NoteList>` with a custom JSON `Serializer`
     (no key-value map).
   - **Logging:** Add Timber dependency; replace all `android.util.Log` usages with Timber.
   - **Package structure:** Refactor `repositories/`, `models/`, `ui/notesoverview/` into
     `domain/`, `data/storage/`, `ui/screens/` etc. per AGENTS.md.

> [!NOTE] **Scope of this plan**
> This is a **hygiene / conformance** pass (no new product features). App Functions,
> AppSearch, Navigation 3 graph on the phone, and the glasses experience itself are **out of
> scope** for code changes beyond what is needed for the storage/logging/package alignment.

## User Decisions (confirmed)

| Item                               | Decision                                                                            |
|------------------------------------|-------------------------------------------------------------------------------------|
| **Storage**                        | Migrate away from key-value Preferences DataStore → **typed `DataStore<NoteList>` with JSON serializer** (no key-value map). |
| **Logging**                        | **Add Timber** dependency and refactor `android.util.Log` → Timber (matches AGENTS.md intent). |
| **Package structure**              | **Refactor code** to AGENTS.md structure (`domain/`, `data/storage/`, `ui/screens/`, etc.). |
| **`AGENTS.md` module structure**   | Update to describe the target structure after refactor. Include `glasses/` and `ai/` packages. |
| **UI verification**                | Add `android-cli`-based UI verification to "Mandatory Post-Change Verification". |

---

## Part A — Findings: Clarify AGENTS.md

The following AGENTS.md statements were ambiguous or did not match the project. Each is
addressed in this plan:

| # | AGENTS.md statement                                                              | Finding                                                                        | Resolution |
|---|----------------------------------------------------------------------------------|--------------------------------------------------------------------------------|------------|
| A1 | "Logging: Timber for meaningful, structured trace events" | No Timber dependency; code uses `android.util.Log` with `TAG` constants. | Adopt Timber (user decision) & refactor code. |
| A2 | "DataStore (storing notes list with kotlinx.serialization)" | Actually key-value `DataStore<Preferences>` (single `stringPreferencesKey` holding the whole JSON list). | Migrate to typed `DataStore<NoteList>` JSON serializer. |
| A3 | "Module Package Structure: `di`, `data/storage`, `domain/`, `ui/screens`, `ui/components`, `ui/navigation`, `extensions`" | Real code: `repositories/notes`, `repositories/sentnotes`, `models/`, `ui/notesoverview`, `glasses/`, `ai/`; no `domain/`, `data/storage`, `ui/screens`, `ui/components`, `ui/navigation`, `extensions`. | Refactor packages + update AGENTS.md (incl. `glasses/` & `ai/`). |
| A4 | "Mandatory Post-Change Verification: assembleDebug, lintDebug" | No UI-level verification step described. | Add `android-cli` UI verification (from Skill). |

---

## Part B — Proposed Changes

### Component: Storage (non-key-value DataStore)

#### [MODIFY] [NotesRepositoryImpl.kt](file:///Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/app/src/main/java/de/julianbiedermann/app_functions_glasses_sample/repositories/notes/NotesRepositoryImpl.kt)

- Migrate from `DataStore<Preferences>` to `DataStore<NoteList>` (typed).
- Add a `NoteListSerializer` (a `Serializer<NoteList>`) that serializes the list to a JSON file
  via `readFrom` / `writeTo` (using `kotlinx.serialization.json.Json`).
- `NoteList` is a tiny serializable wrapper type (e.g. `@Serializable data class NoteList(val
  notes: List<Note> = emptyList())`) so we avoid a raw top-level `List<Note>` serializer.
- Update `addNote` / `deleteNote` / `notes` to operate on the typed DataStore (`.updateData`,
  `.data`) instead of the `Preferences` key-value map.
- Keep data contract identical (same JSON shape as today so existing stored data is compatible
  or gracefully reset on decode failure).
- Replace `android.util.Log` with Timber (`Timber.d` / `Timber.e`).

#### [MODIFY] [StorageModule.kt](file:///Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/app/src/main/java/de/julianbiedermann/app_functions_glasses_sample/di/StorageModule.kt)

- Replace the `DataStore<Preferences>` provider with a typed `DataStore<NoteList>` provider using
  `preferencesDataStore`-free typed store: `PreferenceDataStoreFactory.create` is for
  `Preferences`; for custom types use a file-backed store built with the `DataStoreFactory` /
  `dataStore` delegate for the custom `Serializer`.
- **Dependency note:** the typed custom-type DataStore API ships in
  `androidx.datastore:datastore` (core) — this is added via the existing `datastore-preferences`
  transitively, but we should confirm the exact artifact. If needed, add
  `androidx.datastore:datastore` (or `datastore-core`) to the catalog.
- Provide the `NoteListSerializer` as a singleton.

#### [MODIFY] [libs.versions.toml](file:///Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/gradle/libs.versions.toml)

- Keep `datastorePreferences` if still referenced; add typed DataStore artifact if required
  (e.g. `androidx.datastore:datastore`), plus add **Timber**.
- No hardcoded versions in `build.gradle.kts` (per AGENTS.md).

### Component: Logging (Timber)

#### [MODIFY] [libs.versions.toml](file:///Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/gradle/libs.versions.toml)

- Add `timber = "5.0.1"` (latest stable) + `timber = { group = "com.jakewharton.timber", name =
  "timber", version.ref = "timber" }`.

#### [MODIFY] [app/build.gradle.kts](file:///Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/app/build.gradle.kts)

- Add `implementation(libs.timber)` (type-safe accessor).

#### [MODIFY] Multiple source files — replace `android.util.Log` with Timber

The following files currently use `android.util.Log` and will be migrated to Timber
(`Timber.d/Timber.e`, with informative messages):
- [NotesRepositoryImpl.kt](file:///Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/app/src/main/java/de/julianbiedermann/app_functions_glasses_sample/repositories/notes/NotesRepositoryImpl.kt)
- [SentNotesRepositoryImpl.kt](file:///Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/app/src/main/java/de/julianbiedermann/app_functions_glasses_sample/repositories/sentnotes/SentNotesRepositoryImpl.kt)
- [NotesGlassesAppFunctionService.kt](file:///Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/app/src/main/java/de/julianbiedermann/app_functions_glasses_sample/ai/NotesGlassesAppFunctionService.kt)
- [AudioInterface.kt](file:///Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/app/src/main/java/de/julianbiedermann/app_functions_glasses_sample/glasses/AudioInterface.kt)
- [GlassesMainActivity.kt](file:///Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/app/src/main/java/de/julianbiedermann/app_functions_glasses_sample/glasses/GlassesMainActivity.kt)
- [NotesOverviewScreen.kt](file:///Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/app/src/main/java/de/julianbiedermann/app_functions_glasses_sample/ui/notesoverview/NotesOverviewScreen.kt)
- [NotesOverviewViewModel.kt](file:///Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/app/src/main/java/de/julianbiedermann/app_functions_glasses_sample/ui/notesoverview/NotesOverviewViewModel.kt)

#### [MODIFY] [AppFunctionsGlassesSampleApplication.kt](file:///Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/app/src/main/java/de/julianbiedermann/app_functions_glasses_sample/AppFunctionsGlassesSampleApplication.kt)

- Initialize Timber in `onCreate()`: `Timber.plant(DebugTree())` for debug builds
  (`BuildConfig.DEBUG`). This is the standard Timber setup point.

> [!NOTE] Remove `TAG` constants and `private const val TAG` from each migrated file — Timber
> infers the tag from the class name automatically.

### Component: Package Structure Refactor

Refactor source packages to match AGENTS.md, updating imports across the app:

| Current                                                    | Target (AGENTS.md)                                        |
|------------------------------------------------------------|-----------------------------------------------------------|
| `repositories/notes/NotesRepository[Impl]`                 | `domain/notes/NotesRepository[Impl]`                      |
| `repositories/sentnotes/SentNotesRepository[Impl]`         | `domain/sentnotes/SentNotesRepository[Impl]`              |
| `models/Note`                                              | `domain/models/Note` (or `domain/notes/Note`)             |
| `ui/notesoverview/NotesOverviewScreen/ViewModel/UiState`   | `ui/screens/notesoverview/...` (+ `ui/screens/notesoverview/`) |
| (new)                                                      | `data/storage/` (DataStore + serializers: move `NoteListSerializer` here) |
| (new)                                                      | `ui/components/` (shared composables, if any)             |
| (new)                                                      | `ui/navigation/` (only if needed)                         |
| `glasses/`, `ai/`                                          | keep, but document in AGENTS.md package structure         |
| (retained)                                                 | `di/`, `extensions/` (document; create if needed)         |

Files to move/rename (git `mv` to preserve history) and update imports:
- `repositories/notes/*` → `domain/notes/*`
- `repositories/sentnotes/*` → `domain/sentnotes/*`
- `models/Note.kt` → `domain/models/Note.kt`
- `ui/notesoverview/*` → `ui/screens/notesoverview/*`
- Move DataStore serializer to `data/storage/*`.

**Affected imports to update:**
- [RepositoryModule.kt](file:///Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/app/src/main/java/de/julianbiedermann/app_functions_glasses_sample/di/RepositoryModule.kt)
- [StorageModule.kt](file:///Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/app/src/main/java/de/julianbiedermann/app_functions_glasses_sample/di/StorageModule.kt)
- Any file importing `models.Note`, repositories, `NotesOverview*`.

> [!IMPORTANT] This is a large mechanical refactor. It is kept separate from the
> storage/logging changes so each can be reviewed and verified independently. All three may be
> done in one commit if the user prefers, but they are listed as separate tasks.

### Component: `AGENTS.md` Updates

#### [MODIFY] [AGENTS.md](file:///Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/AGENTS.md)

1. **Tech Stack → Storage:** Update to "DataStore with a typed JSON serializer for the notes
   list (`kotlinx.serialization`); **no local databases (Room)**; **not** a key-value
   Preferences store".
2. **Tech Stack → Logging:** Clarify "Timber" is the chosen logging framework (dep already
   added); trace structured events, state changes, App Function triggers.
3. **Module Package Structure:** Align with the post-refactor structure:
   - `di`, `domain/` (repos + models), `data/storage` (DataStore + serializers), `ui/screens/`,
     `ui/components/`, `ui/navigation/`, `glasses/` (glasses XR experience), `ai/` (App Function
     service), `extensions/`.
4. **Mandatory Post-Change Verification:** Add the **UI verification** step (see Part C).

---

## Part C — UI Verification via `android-cli` (update to AGENTS.md)

Per the `android-cli` skill, add the following to "Mandatory Post-Change Verification". This
should be a **new subsection** describing how an agent verifies UI behavior on a device after a
change.

> **UI Verification (device):** After code changes that affect the UI, verify behavior on a
> connected device/emulator using the `android` CLI / `adb`:
> 1. Deploy & launch the app (e.g. `android run` / deploy the APK, or `adb shell am start`).
> 2. Inspect the UI:
>    - `android layout` (or `adb shell uiautomator dump`) to dump the UI tree (text, resource-ids,
>      content-desc, interactions, bounds/center).
>    - `android screen capture -o <file>` (also `--annotate`) to visually inspect the screen and
>      locate elements by appearance when not present in the layout/dump.
> 3. Interact: `adb shell input tap <x> <y>`, `adb shell input swipe ...`, type text via
>    `adb shell input text "..."` (focus the field first; use `%s` for spaces).
> 4. Confirm expected UI state appears (elements/text visible, no crash/anr).
> 5. For glasses (XR) flows, the device must be a glasses-enabled AVD; use the same
>    layout/screen/input primitives.

> [!NOTE] The existing `./gradlew assembleDebug` and `./gradlew lintDebug` steps remain
> mandatory first; UI device verification is an **additional** post-change check for
> UI-affecting changes (per the `android-cli` skill guidance).

---

## Part D — Open Questions

> [!IMPORTANT]
> - **Typed DataStore artifact:** Confirm the exact artifact to add. The custom-type
>   DataStore `Serializer` API may require `androidx.datastore:datastore-core` / `datastore`
>   (separate from `datastore-preferences`). I will verify against the project's resolved
>   dependencies during execution and add the correct catalog entry.
> - **JSON compatibility:** Existing stored notes are under a single `notes_list` key in
>   Preferences DataStore. After switching to the typed file-backed store, old data will not be
>   auto-migrated (different file layout). Given this is a sample app, simplest correct behavior
>   is to start fresh (treat missing/undecodable file as empty list). Confirm acceptable.
> - **`NoteList` wrapper vs raw `List<Note>`:** I'll use a `@Serializable data class NoteList`
>   wrapper (cleaner top-level serializer). Confirm OK.

---

## Verification Plan

### Automated / Build
1. `./gradlew assembleDebug` — must compile with no errors.
2. `./gradlew lintDebug` — no errors (warnings ok).

### UI / Device (new, from `android-cli`)
3. Deploy & run the app on the phone (emulator).
4. `android layout` to confirm the notes overview screen renders with any existing notes.
5. Interact (add a note via FAB) and re-run `android layout`/`screen capture` to confirm the new
   note appears.
6. Delete the note and confirm it disappears.
7. If a glasses device is available, verify the glasses notes flow (App Function trigger →
   notes displayed) — otherwise note as not-verifiable on current device.

### Manual
- Confirm no regressions in the glasses experience or phone notes flow.
- Confirm package refactor kept `Git` history (moves, not deletes).
