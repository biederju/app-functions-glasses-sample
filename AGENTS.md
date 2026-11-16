# Agent Instructions

## Project Overview

- **App Name:** GlassyNotes
- **Description:** A sample Android application centered around notes, featuring a phone interface
  for managing notes (viewing a list, adding via FAB, deleting) and a hands-free display glasses
  experience via an AI assistant and App Functions.
- **Core Capabilities / Main Features:**
    - Indicator in the app if glasses are connected or not
    - List all notes in the app (Timestamp, title, text)
    - Create and delete notes in the app
    - Show notes on the connected glasses via button press in app
    - Show recent notes or notes which match a certain query on connected glasses
- **Supported App Languages:** English

## Tech Stack

- **Programming Language:** Kotlin
- **Build System:** Gradle Kotlin DSL (`.kts`)
- **UI:**
    - **Phone UI:** Jetpack Compose + Material 3
    - **Glasses UI:** Jetpack Compose Glimmer
- **Dependency Injection:** Hilt
- **Integration:** Android App Functions / AppSearch
- **Storage:** DataStore (file-backed, key-value-free `DataStore<NoteList>` with custom JSON
  serializer; **No Preferences DataStore**, Room, or SQLite)
- **Navigation:** Simplified navigation (avoiding complex navigation graphs or navigation libraries
  unless strictly required)
- **Unit Testing:** None by default (Do not write tests unless explicitly requested)
- **Logging:** Timber for meaningful, structured trace events, state changes, or App
  Function triggers.

## Dependency Management

- When adding new dependencies or Gradle plugins, update the version catalog under
  `gradle/libs.versions.toml` first.
- Reference dependencies or Gradle plugins via type-safe accessors (e.g., `libs.example`) in
  `build.gradle.kts`.
- **NEVER** hardcode version strings directly inside `build.gradle.kts` files.

## Build Variants

- **Debug:**
    - R8 deactivated
    - Signed with debug keystore
    - Logging activated
- **Release:** No for now since this is a sample project.

## Workflow, Execution & Verification

- Ask for clarification whenever requirements or tasks are ambiguous. Never start implementing if
  there are ambiguous requirements.
- **Domain Guidance:** When features involve Glimmer or App Functions, first consult the relevant
  skill under `.agents/skills/` (e.g., `display-glasses-with-jetpack-compose-glimmer`,
  `appfunctions`).
- **Task Categorization & Planning:**
    - **Feature Implementation / New Features:**
        - Create a detailed **Execution Plan** in
          `.agents/executionplans/<execution-plan-name>/<execution-plan-name>.execution-plan.md`
          before touching code.
        - Any new library/dependency must be explicitly listed in the execution plan, including its
          version and affected files.
        - Wait for user confirmation before proceeding.
        - Log execution progress in
          `.agents/executionplans/<execution-plan-name>/<execution-plan-name>.task.md` using
          Markdown checklists.
    - **Execution of Existing Plan:** Proceed directly to execution (do not create a new plan),
      create the task tracking file, and implement.
    - **Minor Fixes / Improvements / Refinements:** Proceed directly to code implementation without
      a new plan, but update existing task tracking files accordingly.

## Mandatory Post-Change Verification

- Run `./gradlew assembleDebug` to confirm successful build.
- Run `./gradlew lintDebug` (warnings are ok, errors are not).
- **UI Verification (`android` CLI):** For changes that affect the UI (screens, components,
  glasses experience, or any feature verifiable through the UI), verify on a connected
  device/emulator using the `android` CLI / `adb`:
    1. Deploy & launch the app (e.g. `android run`, or `adb shell am start`).
    2. Inspect the UI: `android layout` to dump the UI tree (text, resource-ids,
       content-desc, interactions, bounds), and `android screen capture -o <file>`
       (optionally `--annotate`) to visually confirm content not present in the dump.
    3. Interact via `adb shell input <tap|swipe|text|keyevent>` (focus text fields first,
       use `%s` for spaces).
    4. Confirm expected UI state appears (elements/text visible, no crash/anr).
    5. For glasses (XR) flows, use a glasses-enabled AVD with the same
       layout/screen/input primitives.
- **Test App Functions:** Verify the functions are registered and working if changes were made

```bash
# List all registered functions (should return all 3 functions)
adb -s <device> shell cmd app_function list-app-functions --package de.julianbiedermann.app_functions_glasses_sample

# Create a new note (optionally showOnGlasses: true to project immediately)
adb -s <device> shell 'cmd app_function execute-app-function \
  --package de.julianbiedermann.app_functions_glasses_sample \
  --function "de.julianbiedermann.app_functions_glasses_sample.appfunctions.NotesGlassesAppFunctionService#createNote" \
  --parameters "{\"topic\": \"Shopping\", \"text\": \"Buy milk and bread\", \"showOnGlasses\": true}"'

# Show the 3 latest notes (omit count for default)
adb -s <device> shell cmd app_function execute-app-function \
  --package de.julianbiedermann.app_functions_glasses_sample \
  --function 'de.julianbiedermann.app_functions_glasses_sample.appfunctions.NotesGlassesAppFunctionService#showLatestNotes' \
  --parameters '{}'

# Search notes containing "groceries"
adb -s <device> shell cmd app_function execute-app-function \
  --package de.julianbiedermann.app_functions_glasses_sample \
  --function 'de.julianbiedermann.app_functions_glasses_sample.appfunctions.NotesGlassesAppFunctionService#searchNotesOnGlasses' \
  --parameters '{"query":"groceries"}'
```

- Self-Correction: Fix root causes precisely if verification fails. Do not apply blind
  workarounds.
- Keep `AGENTS.md` updated whenever new architectural decisions are made.

## Version Control & Git Hygiene

- Version control system: Git.
- Use **Conventional Commits** (`feat:`, `fix:`, `refactor:`, `test:`, `chore:`, `docs:`) for all
  commit messages.
- Suggest a commit message and ask for confirmation before committing.
- Create atomic, focused commits after successful completion of an execution plan or a successful
  fix/refactoring, only when all verification checks pass.

## Architecture & State Management

- **Architecture Pattern:** Single-Activity Architecture for the phone app; intentional exception
  for glasses which run in a separate XR Activity (`GlassesMainActivity`).
- **Unidirectional Data Flow (UDF):** State flows down from ViewModels as immutable data structures
  (`StateFlow`), and user actions flow up via direct ViewModel function calls or lambdas.
- **Dependency Injection (Hilt):**
    - Annotate ViewModels with `@HiltViewModel` and use `@Inject constructor`.
    - Annotate Activities with `@AndroidEntryPoint`.
    - Inject interfaces (e.g., Repositories) rather than concrete implementations.
    - Define explicit Hilt modules (`@Module`, `@InstallIn`) for providing dependencies.
    - Inject dispatchers in the constructor as a `CoroutineDispatcher` with a default of
      `Dispatchers.IO` (e.g., `dispatcher: CoroutineDispatcher = Dispatchers.IO`).
- **Concurrency:**
    - Structured concurrency with Coroutines and `Flow`.
    - Always tie coroutine lifecycles to `viewModelScope` or defined `CoroutineScope` instances with
      proper `SupervisorJob`.
    - Execute background work on appropriate dispatchers (`Dispatchers.IO`).
- **Error Handling:** Implement explicit exception handling (`try-catch`, `catch` operators, or
  `CoroutineExceptionHandler`) with graceful degradation on UI (non-blocking fallback states).

## Module Package Structure

Organize package structures logically by feature:

- `appfunctions` – App Functions services and entry points.
- `di` – Hilt dependency injection modules.
- `extensions` - Extension functions of classes.
- `data/storage` – File-backed DataStore serializer (`NoteListStorageSerializer`) and
  `NoteListStorage` data
  class.
- `domain/models` – Domain models (e.g., `Note`).
- `domain/notes` – Notes domain logic (`NotesRepository`, `NotesRepositoryImpl`).
- `domain/sentnotes` – Sent notes domain logic (`SentNotesRepository`, `SentNotesRepositoryImpl`).
- `glasses` – Glasses-specific activities (`GlassesMainActivity`), composables, and helpers
  (`AudioInterface`).
- `ui/screens` – Screen-level composables and ViewModels (e.g., `ui/screens/notesoverview/`).
- `ui/components` – Reusable UI composables shared across screens.

## UI & Composables

- **Screen Architecture:**
    - Every screen lives in its own file named as a PascalCase noun with `Screen` suffix (e.g.,
      `NotesOverviewScreen`).
    - Every screen exposes a top-level route/screen composable and delegates UI layout to a
      **private** `Content` composable (taking only plain parameters, no ViewModel injection).
    - All composables used only within a single screen must be `private`.
    - Reusable composables used by two or more screens live under `ui/components/` and are
      `internal` or `public`.
    - Every screen or component preview must be `private`, declared at the end of the file, and
      display **both light and dark mode** via a single preview annotation or combination.
- **Accessibility Standards:**
    - Every non-decorative `Icon` or `Image` **must** provide a localized `contentDescription` (use
      `null` explicitly only for purely decorative elements).
    - Ensure all interactive elements maintain a minimum touch target size of **48dp × 48dp** and
      expose clear semantic states.

### Naming & Style

- **Naming Conventions:**
    - Feature classes use standard suffixes (`ViewModel`, `Repository`, `Screen`).
    - Glimmer UI components use descriptive suffixes like `Panel`, `Overlay`, or `Screen` (e.g.,
      `NotesOverlay`).
    - Parameter and function names must clearly represent their type and purpose (e.g.,
      `viewModel: NotesOverviewViewModel`, `@Composable fun NoteActionCard`).
- **Immutability:**
    - Default to `val` and read-only collections (`List`, `Map`). Keep mutable state private
      (`_uiState`) and expose read-only streams (`uiState`).
    - All UI state holders, domain models, and navigation data must be immutable `data class` or
      sealed interfaces/classes utilizing Kotlin's `copy()` for state updates.
- **Visibility Modifiers (Strict):**
    - Prefer `private` scope by default.
    - Prefer `internal` over `public`. Only make classes, functions, or properties `public` or
      `internal` when strictly required.
- **Function/Class Calls & Parameters:**
    - Always use the Kotlin language feature **named parameters/arguments** for **all** calls,
      including single-argument calls (e.g., `Text(text = stringResource(...))`).

### Anti-Patterns & Forbidden Practices

- **NEVER** use XML layouts, ViewBinding, or legacy Fragment APIs (No Legacy Views). Everything must
  use Jetpack Compose and Compose Glimmer.
- **NEVER** place business logic, heavy calculations, or data processing inside Composable functions
  or Activities.
- **NEVER** use `GlobalScope`.
- **NEVER** use the not-null assertion operator (`!!`). Handle nullability safely via safe calls
  (`?.`), Elvis operators (`?:`), or explicit early returns/`requireNotNull()`.
- **NEVER** pass classes like ViewModels or Repositories down to sub-composables. Pass plain data
  down and events up via callbacks.
- **NEVER** hardcode version strings directly inside `build.gradle.kts` files.
- **NEVER** write unit tests unless explicitly requested by the user.
- **NEVER** include local databases (Room) or complex navigation libraries in this sample app.
- Avoid deprecated functions, classes and apis

## Documentation & Logging

- Code identifiers, strings, variable names, messages, and UI text must be in **English**.
- Code comments, inline explanations, and KDoc must be written in **English**. Avoid redundant
  comments; code should be self-documenting.
- Log meaningful, structured log messages (using Timber) to trace key
  events, state changes, or App Function triggers.
- Use Timber format-string placeholders (`%s`, `%d`) for string interpolation in log messages
  to avoid unnecessary string concatenation when logging is disabled.
