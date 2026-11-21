# AGENTS.md Alignment — Code & Style Compliance Execution Plan

## Goal

Bring the current project into alignment with the coding, naming, visibility, logging, and
structure rules defined in `AGENTS.md`. This is a **hygiene / conformance** pass: it fixes
source files that deviate from the AGENTS.md conventions **without adding new features**.

> [!NOTE] **Task file is created at execution time, not plan time.**
> Per AGENTS.md workflow step 3, the `<plan>.task.md` file is only created **when the plan is
> being executed** (alongside implementation). It is **not** created when writing the plan.
> This plan therefore ships with the `.execution-plan.md` only; the `.task.md` will be generated
> only when execution begins.

## Scope Decision (confirmed with user)

| Item                          | Decision                                                                |
|-------------------------------|-------------------------------------------------------------------------|
| **App Functions / AppSearch** | **Deferred.** Not implemented in this plan (still absent from project). |
| **Navigation 3 (phone)**      | **Deferred.** Keep the single-screen phone UI without a nav graph.      |

> [!NOTE]
> Although `AGENTS.md` lists **App Functions / AppSearch** as a core focus and in the tech
> stack, and states the phone app uses Navigation 3, the user explicitly chose to defer both.
> No App Functions dependency and no Navigation 3 graph will be added in this plan. This is a
> known, dangling spec item to be revisited in a future feature plan.

## Identified Misalignments

The following concrete deviations from AGENTS.md were found during analysis:

| # | File                                        | Issue                                                                                                     | AGENTS.md Rule                                                                        |
|---|---------------------------------------------|-----------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------|
| 1 | `audioInterface.kt`                         | File name is lowercase; Kotlin class files should be PascalCase matching the class                        | Language & Standards (Kotlin Naming Guidelines)                                       |
| 2 | `AudioInterface`                            | `class AudioInterface` is `public` by default                                                             | Visibility: prefer `private`, then `internal`                                         |
| 3 | `GlassesMainActivity.kt`                    | `GlassesMainActivity` is not annotated `@AndroidEntryPoint`                                               | DI: "Annotate Activities with `@AndroidEntryPoint`"                                   |
| 4 | `GlassesMainActivity.kt` — `HomeScreen`     | `HomeScreen` composable is `public` (no modifier), only used within this file                             | Composable structure: single-screen composables should be `private`                   |
| 5 | `GlassesMainActivity.kt` — `DefaultPreview` | Preview is `public`                                                                                       | Previews must be `private` and at end of file (it is at end, but visibility is wrong) |
| 6 | `Color.kt`                                  | `Purple40`, `Purple80`, etc. are `public`; only used within the theme package                             | Visibility: prefer `internal` over `public`                                           |
| 7 | `libs.versions.toml`                        | `coreKtx`, `activityCompose`, `datastorePreferences`, `kotlinxSerialization` are **not** on latest stable | Dependencies: "Prefer the latest **stable** versions"                                 |

## Proposed Changes

### Component: Glasses Experience (GlassesMainActivity.kt + audio file)

#### [MODIFY] [GlassesMainActivity.kt](/Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/app/src/main/java/de/julianbiedermann/app_functions_glasses_sample/GlassesMainActivity.kt)

- Annotate `GlassesMainActivity` with `@AndroidEntryPoint` (Hilt DI consistency; no injection
  yet but keeps the app's DI binding contract for Activities).
- Change `HomeScreen` visibility from public → `private`.
- Change `DefaultPreview` visibility from public → `private`.
- Add structured `Log.d` trace for key lifecycle events (e.g., creation, `onStart`, `onStop`,
  glasses close action) to satisfy the logging guideline.
- Keep the existing `onClose` lambda and layout unchanged.

#### [RENAME] [audioInterface.kt](/Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/app/src/main/java/de/julianbiedermann/app_functions_glasses_sample/audioInterface.kt) →

`AudioInterface.kt`

- Rename file to `AudioInterface.kt` (PascalCase matching the class).
- Record the rename via git (file moved, not duplicated).
- No logic changes.

#### [MODIFY] [AudioInterface.kt](/Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/app/src/main/java/de/julianbiedermann/app_functions_glasses_sample/AudioInterface.kt)

- Change `class AudioInterface` visibility from public → `internal`.

### Component: Theme

#### [MODIFY] [Color.kt](/Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/app/src/main/java/de/julianbiedermann/app_functions_glasses_sample/ui/theme/Color.kt)

- Change the six color `val`s from `public` → `internal` (they are only consumed within the
  `ui/theme` package, so private would also suffice; `internal` is chosen per "prefer internal
  over public").

---

### Component: Dependency Versions (latest stable)

The following catalog entries have been **successfully bumped** to their latest stable release
(verified via `version_lookup` and `./gradlew assembleDebug`):

| Catalog entry (`libs.versions.toml`) | Current in catalog | Latest stable | Action                                                               |
|--------------------------------------|--------------------|---------------|----------------------------------------------------------------------|
| `coreKtx`                            | `1.10.1`           | `1.19.1`      | **Bumped** — kotlin-stdlib compatibility resolved with Kotlin 2.4.21 |
| `activityCompose`                    | `1.8.0`            | `1.13.0`      | **Bumped** — kotlin-stdlib compatibility resolved with Kotlin 2.4.21 |
| `datastorePreferences`               | `1.2.1`            | `1.3.0`       | **Bumped** — kotlin-stdlib compatibility resolved with Kotlin 2.4.21 |
| `kotlinxSerialization`               | `1.11.0`           | `1.12.0`      | **Bumped** — kotlin-stdlib compatibility resolved with Kotlin 2.4.21 |
| `hilt`                               | `2.60.1`           | `2.60.1`      | Already current                                                      |
| `lifecycleViewModelCompose`          | `2.11.0`           | `2.11.0`      | Already current                                                      |
| `hiltNavigationCompose`              | `1.4.0`            | `1.4.0`       | Already current                                                      |
| `hilt`                               | `2.60.1`           | `2.60.1`      | Already current                                                      |
| `lifecycleViewModelCompose`          | `2.11.0`           | `2.11.0`      | Already current                                                      |
| `hiltNavigationCompose`              | `1.4.0`            | `1.4.0`       | Already current                                                      |

#### [MODIFY] [libs.versions.toml](/Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/gradle/libs.versions.toml)

- **All version bumps applied successfully.** The project migrated from Kotlin `2.2.10` to `2.4.21`,
  which resolves the kotlin-stdlib 2.4.x metadata compatibility issue.
- Additional bumps: AGP `9.5.0` → `9.5.1`, Compose BOM `2026.03.01` → `2026.09.00`, Glimmer
  `1.0.0-alpha15` → `1.0.0-alpha22`, Projected `1.0.0-alpha10` → `1.0.0-alpha14`, KSP
  `2.2.10-2.0.2` → `2.3.11`.

#### [MODIFY] [app/build.gradle.kts](/Users/julian.biedermann/AndroidStudioProjects/Private/app-functions-glasses-sample/app/build.gradle.kts)

- No direct edit required for these bumps (they flow through the version catalog); the file is
  listed for completeness since it consumes the affected libraries.

### Component: Named Parameters

The Kotlin naming rule in AGENTS.md ("Always use named parameters/arguments") was audited.
All **multi-parameter** composable calls already use named arguments (e.g., `AlertDialog(
  onDismissRequest = ..., title = ..., text = ..., confirmButton = ..., dismissButton = ...)`,
`OutlinedTextField(value = ..., onValueChange = ..., label = ..., modifier = ...)`, and the
`NotesOverviewContent(...)` invocation).

- Single-argument calls such as `Text(stringResource(...))` and `Button(onClick = ...)`
  reference only one parameter, so positional form is fine and requires no change.
- **No changes required** — this item is confirmed conformant. It is listed here only to
  record that the audit was performed.

## Explicitly NOT Changed (scope exclusions)

- **App Functions / AppSearch** — deferred by user decision.
- **Navigation 3 phone graph** — deferred by user decision.
- **Business logic location** — already conformant (repos/ViewModels hold logic, not UI).
- **Naming suffixes** (`NotesOverviewViewModel`, `NotesRepository`, `NotesOverviewScreen`,
  `NotesOverviewUiState`) — already conformant.
- **Visibility of Domain/UI layer** (`internal` interfaces, repos, ViewModels, screens) —
  already conformant.
- **Composable content/preview pattern** — `NotesOverviewContent` (private) + `@PreviewLightDark`
  single preview already conforms.
- **Named parameters** — already conformant (audited, no changes needed).
- **`MainActivity` / `GlassesMainActivity` visibility** — left `public` as they are system
  entry points launched from the manifest (a legitimate "strictly required" case).

## Verification Plan

### Build & Lint (required by AGENTS.md)

1. Run `./gradlew assembleDebug` — must compile with no errors.
2. Run `./gradlew lintDebug` — no errors (warnings ok).

### Dependency Bump Verification

- After bumping `coreKtx` / `activityCompose` / `datastorePreferences` / `kotlinxSerialization`,
  run `./gradlew assembleDebug` and confirm compilation still succeeds.
- **Result:** All bumps applied successfully. Kotlin upgraded to `2.4.21` which resolves the
  kotlin-stdlib 2.4.x metadata compatibility issue. `./gradlew assembleDebug` and
  `./gradlew lintDebug` both pass.

### Manual Verification

- Confirm `GlassesMainActivity` still launches the glasses experience without crashing.
- Confirm the TTS `AudioInterface` still initializes and speaks on `onStart`.
- Confirm no behavior/functionality regressions; the code changes are visibility/naming/logging
  only, plus dependency version bumps.
