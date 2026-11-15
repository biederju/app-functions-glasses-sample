# App Functions: Notes to the Display Glasses

## Goal

Expose the existing **send notes to the display glasses** capability to on-device AI agents via
**App Functions**. Since notes can already be sent to the display glasses by tapping a **send**
button in the phone app, AI assistants should be able to trigger the same functionality.

Two App Functions are added:

1. **Show the `X` latest notes** (by timestamp) on the glasses.
2. **Show all notes** whose **title** or **text** contains a given search string on the glasses.

This is a **new feature**, so per `AGENTS.md` a dedicated execution plan and task file are created
under `.agents/executionplans/app-functions-notes-to-glasses/`. The feature builds on the existing
`send-note-to-glasses` implementation (in-memory `SentNotesRepository` + shared-repo glasses
display) and follows the `appfunctions` skill under `.agents/skills/appfunctions/`.

> [!NOTE] **Canonical plan location**
> This file (`app-functions-notes-to-glasses.execution-plan.md`) is the **canonical** plan per
> `AGENTS.md`. An identical mirror is kept at
> `implementation_plan.artifact.md` in the IDE artifact cache; that copy exists **only** because the
> agent planning workflow requires it there to track the REFINING/EXECUTING lifecycle. Keep the two
> in
> sync during review; treat this file as the source of truth.

## Clarified Decisions (from user)

| Question                                                           | User Decision                                                                                                                                                              |
|--------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Should the App Function launch/bring up the glasses activity?      | **Yes** — enqueue the selected notes into `SentNotesRepository` AND launch/bring up `GlassesMainActivity` via `ProjectedContext`, mirroring the existing send-button flow. |
| Default value for `X` (latest-note count) when the agent omits it? | **3**                                                                                                                                                                      |

## Context & Current Behavior

- Notes are stored via `NotesRepository` (`NotesRepositoryImpl`), backed by Preferences DataStore,
  exposed as `notes: Flow<List<Note>>`, **already sorted by timestamp descending**.
- Sent notes are added to an in-memory `SentNotesRepository` (`SentNotesRepositoryImpl`, a
  `@Singleton` `MutableStateFlow<List<Note>>`) which the separate `GlassesMainActivity` observes via
  `collectAsStateWithLifecycle` and renders in a Glimmer `HorizontalStack` pager.
- The phone send flow (in `NotesOverviewScreen`): `viewModel.sendNoteToGlasses(note)` →
  `sentNotesRepository.addNote(note)`, then if `ProjectedContext.isProjectedDeviceConnected`, launch
  `GlassesMainActivity` with `ProjectedContext.createProjectedActivityOptions(context)`.
- **App Functions is not yet configured** in the project: no `androidx.appfunctions` dependency, no
  `app_metadata.xml`, no `AppFunctionService`. This plan performs the full first-time setup.

## Proposed Changes

### Dependencies / Build (new library — explicitly listed per `AGENTS.md`)

#### [MODIFY] [libs.versions.toml](/Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/gradle/libs.versions.toml)

- Add version entries and library aliases:
    - `appfunctions = "1.0.0-rc01"` (fallback `1.0.0-alpha12` if resolution fails)
    -
  `androidx-appfunctions = { group = "androidx.appfunctions", name = "appfunctions", version.ref = "appfunctions" }`
  -
  `androidx-appfunctions-compiler = { group = "androidx.appfunctions", name = "appfunctions-compiler", version.ref = "appfunctions" }`
- The KSP plugin is already present (`libs.plugins.ksp`), so no new plugin is required.

#### [MODIFY] [app/build.gradle.kts](/Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/app/build.gradle.kts)

- Add `implementation(libs.androidx.appfunctions)` and `ksp(libs.androidx.appfunctions.compiler)`.

### App Functions Service & Metadata

#### [NEW] [AppFunctionService.kt] (app meta) — `app/src/main/res/xml/app_metadata.xml`

Describe the app's agent-facing capabilities and operational patterns (per the kdoc-refinement
reference):

```xml

<AppFunctionAppMetadata xmlns:appfn="http://schemas.android.com/apk/androidx.appfunctions"
    appfn:description="This app manages personal notes and can display them on the user's
    connected display glasses.

    Operational Patterns:
    - Use 'showLatestNotes' to display the N most recent notes on the glasses.
    - Use 'searchNotesOnGlasses' to display notes whose title or text contains a given query.

    Constraints:
    - Both functions require at least one existing note; otherwise no notes are shown."
    appfn:displayDescription="@string/user_visible_app_function_description" />
```

#### [NEW] [ai/NotesGlassesAppFunctionService.kt]

A `@RequiresApi(36)` `@AndroidEntryPoint` abstract `AppFunctionService` annotated with
`@AppFunctionServiceEntryPoint(serviceName = ..., appFunctionXmlFileName = ...)`. It injects
`NotesRepository`, `SentNotesRepository`, `@ApplicationContext Context`, and a
`CoroutineDispatcher` (default `Dispatchers.IO`) to:

- Enqueue the selected notes into `SentNotesRepository` and launch `GlassesMainActivity` via
  `ProjectedContext` (mirrors the button flow; wrapped in try-catch with structured `Log.d`).
- Two `@AppFunction(isDescribedByKDoc = true)` suspend functions (below).

#### [MODIFY] [AndroidManifest.xml](/Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/app/src/main/AndroidManifest.xml)

- Add the `<property android:name="android.app.appfunctions.app_metadata" ...>` to `<application>`.
- Declare the generated App Function service (KSP generates the concrete class; the abstract one
  referenced here) with `android:permission="android.permission.BIND_APP_FUNCTION_SERVICE"`,
  `android:exported="true"`, the `android.app.appfunctions.AppFunctionService` intent-filter, and
  the `android.app.appfunctions.schema` / `v2` property pointing at the generated files (per the
  skill's manifest example).

### App Function #1 — Show Latest Notes

`showLatestNotes(count: Int = 3): ShowNotesResult`

- Reads the latest `count` notes from `NotesRepository.notes` (already timestamp-descending).
- `count` clamped to `1..*`; empty list accepted (graceful empty result).
- Enqueues them into `SentNotesRepository` and launches the glasses activity.
- Returns a summary result (`NoteListAppFunctionResult`) describing how many notes were shown.
- KDoc per the kdoc-refinement reference: strong imperative verb, `@param count` with default,
  `@return` description, recovery-oriented `@throws` mapping.

### App Function #2 — Search Notes on Glasses

`searchNotesOnGlasses(query: String): ShowNotesResult`

- Filters `NotesRepository.notes` for notes whose **title** or **text** contains `query`
  (case-insensitive).
- Enqueues matching notes into `SentNotesRepository` and launches the glasses activity.
- Returns a summary result. Empty matches produce a graceful empty result.
- KDoc per the kdoc-refinement reference.

### Shared Serializable Result Types

#### [NEW] ai/ShowNotesResult.kt

`@AppFunctionSerializable(isDescribedByKDoc = true) data class ShowNotesResult(/** count */ val count: Int)`.

### KDoc Refinement

- Refine the KDoc of both `@AppFunction` methods and the serializable (`NoteListAppFunctionResult`)
  inline
  property docs so AI agents / MCP correctly understand the tools: imperative verb, explicit
  `@param` with default, error-recovery `@throws`, and the app-level `app_metadata.xml`
  operational-pattern description.

> [!NOTE] The two App Functions enqueue straight into the shared `SentNotesRepository`; the
> existing `GlassesMainActivity` automatically renders whatever is in the queue. No changes to the
> glasses UI are required. The existing phone UI is unchanged.

## Verification Plan

### Build / Lint (required by `AGENTS.md`)

- `./gradlew assembleDebug` — must compile without errors (KSP generates the service + XML).
- `./gradlew lintDebug` — must pass without errors (warnings ok).

### ADB / Manual Verification

- Confirm the App Function service is registered:
  `adb shell cmd app_function list-app-functions`
- Invoke the latest-notes function:
  `adb shell cmd app_function execute-app-function --package de.julianbiedermann.app_functions_glasses_sample --function <service>#showLatestNotes --args '{"count":3}'`
- Invoke the search function:
  `adb shell cmd app_function execute-app-function --package ... --function <service>#searchNotesOnGlasses --args '{"query":"meeting"}'`
- With display glasses connected, confirm the notes appear on the glasses (mirroring the send-button
  behavior) and that an empty result is handled gracefully.
