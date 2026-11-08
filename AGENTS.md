# Agent Instructions

## Project Context & Scope

* **App Overview:** A sample Android application centered around **Notes**. It features a phone
  interface for managing notes (viewing a list, adding via FAB, deleting) and allows users to
  interact hands-free via display glasses and an AI assistant via app functions.
* **Core Focus:** Demonstrate App Functions and Compose Glimmer for display glasses through focused
  use cases (creating notes via assistant voice command, and querying recent or day-specific notes
  to display on the glasses).
* **Simplicity Rule:** Keep the implementation lightweight.
    * Do not write tests unless explicitly requested.
    * Do not include a local databases but use **Preferences DataStore** (key-value), storing the
      notes list as JSON under a single preference key.
    * Navigation should also be kept as simple as possible, avoiding complex navigation graphs or
      the use of navigation libraries unless strictly required.
* **Spec-Driven Development:** Before writing any implementation code for a new feature, always
  create an execution plan package and save the plan under
  `.agents/executionplans/<execution-plan-name>/<execution-plan-name>.execution-plan.md`.
* **Clarification First:** Never start implementing if there are ambiguous requirements or open
  questions. Always clarify everything first.

## Tech Stack & Library Versions

* **Language & Standards:** Kotlin (latest stable). Strictly follow official Kotlin Coding and
  Naming Guidelines.
* **Build Tool:** Gradle (Kotlin DSL / .kts)
* **UI Frameworks:** Jetpack Compose (phone app), Jetpack Compose Glimmer (display glasses app),
  Material 3
* **Storage:** Preferences DataStore
* **Dependency Injection:** Hilt
* **Integration:** Android App Functions / AppSearch
* **Gradle:** Kotlin Script (kts)
* **Dependencies:** Prefer the latest **stable** versions of libraries. Pre-releases
  (experimental/canary/alpha) are allowed when necessary, since this is a sample app for pre-release
  features (e.g. XR, Glimmer, Projected, App Functions).

## Language & Documentation Rules

* **App & Code Language:** All code identifiers, strings, variable names, messages, and UI text
  (unless explicitly internationalized) must be in **English**.
* **Comments & Documentation:** Code comments, inline explanations, and KDoc must be written in
  **English**.
* **Logging:** Include meaningful, structured log messages (using `Log.d` or standard logging
  utilities) where it makes sense to trace key events, state changes, or App Function triggers.

## Workflow & Execution Process for new Features

1. **Analyze & Clarify:** Read the user prompt. If anything is unclear, ask questions immediately.
   When the feature involves Glimmer or app functions, first consult the relevant skill under
   `.agents/skills/` (e.g. `display-glasses-with-jetpack-compose-glimmer`, `appfunctions`) for
   domain guidance.
2. **Plan (New Features):**
    * Create a dedicated subdirectory under `.agents/executionplans/` named after the execution plan
      (e.g., `phone-notes-overview`), and place the plan file inside it as
      `<execution-plan-name>.execution-plan.md` (e.g.,
      `.agents/executionplans/phone-notes-overview/phone-notes-overview.execution-plan.md`).
    * Any new library/dependency that does not yet exist in the project (e.g. Hilt, DataStore, etc.)
      **must always be explicitly listed in the execution plan** before it is added, including its
      version and the files (version catalog, build scripts) affected.
3. **Execute & Task Tracking:**

* When an execution plan for a feature already exists and the prompt instructs you to
  execute/implement it, **proceed directly to execution** — do NOT create a new execution plan. Only
  create the corresponding task tracking file (see below) and start implementing.
* Create a corresponding task tracking file named `<execution-plan-name>.task.md` inside the exact
  same subdirectory (e.g.,
  `.agents/executionplans/phone-notes-overview/phone-notes-overview.task.md`) to track progress
  using Markdown checklists.
* Implement changes strictly adhering to the architecture, style, and safety rules below. Code can
  be modified, added, and deleted as needed to fulfill the plan.
* If in the execution plan an architectural decision was taken, update the `AGENTS.md` File
  accordingly.
* If the prompt describes improvements or fixes of an already implemented feature, no new execution
  plan is needed and implementation can start directly. But the existing plan and especially task
  file should be updated accordingly.

4. **Build & Verify:**
    * Run `./gradlew assembleDebug` to confirm the project builds successfully without compilation
      errors.
    * After a successful build also always run `./gradlew lintDebug` without errors (warnings are
      ok).
    * The task is only complete when the build and lint pass.

## Architecture & Design Patterns

* **Single-Activity Architecture:** The **phone app** uses a single-Activity structure. The
  **glasses experience is an intentional exception**: it runs in a separate XR Activity (see
  `GlassesMainActivity`).
* **Unidirectional Data Flow (UDF):** State flows down from ViewModels as immutable data structures
  (`StateFlow`), and user or system actions flow up via direct ViewModel function calls or lambdas.
* **Dependency Injection (Hilt):**
    * Always annotate ViewModels with `@HiltViewModel` and use `@Inject constructor`.
    * Annotate Activities with `@AndroidEntryPoint`.
    * Inject interfaces (e.g., Repositories) rather than concrete implementations.
    * Inject the dispatcher in the constructor of the class as a `CoroutineDispatcher` with a
      default of `Dispatchers.IO` (e.g. `dispatcher: CoroutineDispatcher = Dispatchers.IO`) if used.
    * Define explicit Hilt modules (`@Module`, `@InstallIn`) for providing dependencies.

## Concurrency & Error Handling

* **Structured Concurrency:** Always use structured concurrency with Coroutines and Flow.
* **No `GlobalScope`:** Never use `GlobalScope`. Always tie coroutine lifecycles to `viewModelScope`
  or defined `CoroutineScope` instances with proper `SupervisorJob`.
* **Dispatchers:** Always execute background work on appropriate dispatchers (e.g.,
  `Dispatchers.IO`).
* **Exception Handling:** Implement explicit exception handling using `try-catch` blocks, `catch`
  operators on Flows, or `CoroutineExceptionHandler`.
* **Graceful Degradation:** The UI must handle edge cases and failures gracefully (e.g., display
  subtle fallback states or non-blocking error indicators on the glasses UI instead of crashing).

## Coding Style, Naming & Visibility Conventions

* **Naming Suffixes:**
    * ViewModels must end with the `ViewModel` suffix (e.g., `NotesViewModel`).
    * Repositories must end with the `Repository` suffix (e.g., `NotesRepository`).
    * Screen-Composables must end with the `Screen` suffix (e.g., `NotesOverviewScreen`).
    * Glimmer UI components must use descriptive suffixes like `Panel`, `Overlay`, or `Screen`
      (e.g., `NotesOverlay`).
    * Generally every parameter name should represent its type and every composable function name
      its main composable if there is one, e.g. `val sentNotesRepository: SentNotesRepository`,
      `val viewModel: NotesOverviewViewModel`, `@Composable fun NoteActionCard` using `ActionCard`
      or `@Composable fun SentNotesHorizontalPager` using `GlimmerHorizontalPager`.
* **Visibility Modifiers (Strict):** Always restrict access visibility. Only make classes,
  functions, or properties `public` or `internal` when strictly required. Prefer `private` scope by
  default and prefer `internal` over `public`.
* **Avoid Deprecated APIs:** Always try to avoid using deprecated functions, classes, or
  annotations. Prefer their modern replacements or seek the latest non-deprecated API.
* **Self-Documenting Code:** Code must be clear and readable so that comments are unnecessary. Only
  add comments or KDoc for non-obvious workarounds or complex business logic.
* **Composable Structure & Previews:**
    * Every screen composable (e.g., `NotesOverviewScreen`) must have a `private` content composable
      (e.g., `NotesOverviewContent`) that takes only plain parameters (no ViewModel injection) — the
      preview renders this content composable, not the screen wrapper.
    * All composables used only within a single screen should be `private` in that screen's file.
    * Reusable composables (used by two or more screens) live in their own files under
      `ui/components/`, are `internal` or `public`.
    * Every composable preview must display **both light and dark mode** via the preview annotation
      on a single preview composable (e.g., the `@Preview` annotation with `uiMode` /
      `@PreviewNightMode`, or a multipreview annotation combining light & dark). Do **not** create
      separate preview composables for each mode.
    * Previews must always be declared `private` and positioned at the end of the file.
* **Data Down, Events Up:**
    * Avoid passing classes like ViewModels or Repositories down to sub-composables. Instead, pass
      plain data as parameters and use callback lambdas for events.
    * Inject dependencies (ViewModels, Repositories) at the highest level possible — typically the
      screen composable or Activity (e.g., `GlassesMainActivity`).
    * This pattern improves testability, reduces coupling, and makes the data flow explicit and
      predictable.
* **Named Parameters:**
    * Always use the Kotlin language feature `named parameters/arguments`.
    * Use named parameters for **all** calls, including single-argument calls (e.g.
      `Text(text = stringResource(...))`), for consistency and readability.

## Anti-Patterns & Forbidden Practices

* **No Legacy Views:** Do not use XML layouts, ViewBinding, or legacy Fragment APIs. Everything must
  use Jetpack Compose and Compose Glimmer.
* **No Business Logic in UI:** Do not implement business logic, heavy calculations, or data
  processing inside Composable functions or Activities. Always delegate to ViewModels or UseCases.