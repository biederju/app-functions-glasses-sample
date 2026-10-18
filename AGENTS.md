# Agent Instructions

## Project Context & Scope
* **App Overview:** A sample Android application centered around **Notes**. It features a phone interface for managing notes (viewing a list, adding via FAB, deleting) and allows users to interact hands-free via display glasses and an AI assistant.
* **Core Focus:** Demonstrate App Functions and Compose Glimmer for display glasses through focused use cases (creating notes via assistant voice command, and querying recent or day-specific notes to display on the glasses).
* **Simplicity Rule:** Keep the implementation lightweight. Do not include local databases, complex persistence, or tests unless explicitly requested.
* **Persistence Decision:** Notes persistence uses **Preferences DataStore** (key-value), storing the notes list as JSON under a single preference key. No local database (Room) or Proto DataStore should be introduced for note persistence.
* **Spec-Driven Development:** Before writing any implementation code, always create an execution plan and save it in `.agents/executionplans`.
* **Clarification First:** Never start implementing if there are ambiguous requirements or open questions. Always clarify everything first.

## Tech Stack & Library Versions
* **Language & Standards:** Kotlin (latest stable). Strictly follow official Kotlin Coding and Naming Guidelines.
* **Build Tool:** Gradle (Kotlin DSL / .kts)
* **UI Frameworks:** Jetpack Compose (phone app), Jetpack Compose Glimmer (display glasses app), Material 3
* **Navigation:** Navigation 3
* **Storage:** DataStore
* **Dependency Injection:** Hilt
* **Integration:** Android App Functions / AppSearch
* **Dependencies:** Always use the latest stable versions of libraries, unless experimental or canary releases are strictly required for XR/Glimmer features.

## Language & Documentation Rules
* **App & Code Language:** All code identifiers, strings, variable names, messages, and UI text (unless explicitly internationalized) must be in **English**.
* **Comments & Documentation:** Code comments, inline explanations, and KDoc must be written in **English**.
* **Logging:** Include meaningful, structured log messages (using `Log.d` or standard logging utilities) where it makes sense to trace key events, state changes, or App Function triggers.

## Workflow & Execution Process
1. **Analyze & Clarify:** Read the user prompt. If anything is unclear, ask questions immediately.
2. **Plan:** Create a step-by-step markdown plan under `.agents/executionplans` outlining files to create or modify. Any new library/dependency that does not yet exist in the project (e.g. Hilt, Navigation, DataStore, a new Gradle plugin, or a new artifact) **must always be explicitly listed in the execution plan** before it is added, including its version and the files (version catalog, build scripts) affected.
3. **Execute:** Implement changes strictly adhering to the architecture, style, and safety rules below.
4. **Verify & Build:** After implementation, the task is only complete once the project builds successfully without compilation errors. Run `./gradlew assembleDebug` (or equivalent build command) to confirm.

## Architecture & Design Patterns
* **Single-Activity Architecture:** Use a single-Activity structure with Navigation 3.
* **Unidirectional Data Flow (UDF):** State flows down from ViewModels as immutable data structures (`StateFlow`), and user or system actions flow up via direct ViewModel function calls or lambdas.
* **Dependency Injection (Hilt):**
  * Always annotate ViewModels with `@HiltViewModel` and use `@Inject constructor`.
  * Inject interfaces (e.g., Repositories) rather than concrete implementations.
  * Define explicit Hilt modules (`@Module`, `@InstallIn`) for providing dependencies.

## Concurrency & Error Handling
* **Structured Concurrency:** Always use structured concurrency with Coroutines and Flow.
* **No `GlobalScope`:** Never use `GlobalScope`. Always tie coroutine lifecycles to `viewModelScope` or defined `CoroutineScope` instances with proper `SupervisorJob`.
* **Dispatchers:** Always execute background work on appropriate dispatchers (e.g., `Dispatchers.IO`).
* **Exception Handling:** Implement explicit exception handling using `try-catch` blocks, `catch` operators on Flows, or `CoroutineExceptionHandler`.
* **Graceful Degradation:** The UI must handle edge cases and failures gracefully (e.g., display subtle fallback states or non-blocking error indicators on the glasses UI instead of crashing).

## Coding Style, Naming & Visibility Conventions
* **Naming Suffixes:**
  * ViewModels must end with the `ViewModel` suffix (e.g., `NotesViewModel`).
  * Repositories must end with the `Repository` suffix (e.g., `NotesRepository`).
  * Glimmer UI components must use descriptive suffixes like `Panel`, `Overlay`, or `Screen` (e.g., `NotesOverlay`).
* **Visibility Modifiers (Strict):** Always restrict access visibility. Only make classes, functions, or properties `public` or `internal` when strictly required. Prefer `private` or package-private scope by default.
* **Self-Documenting Code:** Code must be clear and readable so that comments are unnecessary. Only add comments or KDoc for non-obvious workarounds or complex business logic.
* **Composable Structure & Previews:**
  * Every screen composable (e.g., `NotesOverviewScreen`) must have a `private` content composable (e.g., `NotesOverviewContent`) that takes only plain parameters (no ViewModel injection) — the preview renders this content composable, not the screen wrapper.
  * All composables used only within a single screen should be `private` in that screen's file.
  * Reusable composables (used by two or more screens) live in their own files under `ui/components/`, are `internal` or `public`, and must have compose previews in light and dark mode.

## Anti-Patterns & Forbidden Practices
* **No Legacy Views:** Do not use XML layouts, ViewBinding, or legacy Fragment APIs. Everything must use Jetpack Compose and Compose Glimmer.
* **No Business Logic in UI:** Do not implement business logic, heavy calculations, or data processing inside Composable functions or Activities. Always delegate to ViewModels or UseCases.
* **No Hardcoded Navigation:** Do not trigger screen transitions via raw Intents or direct Activity startups. Always rely on the central Navigation 3 graph.
* **No Unplanned Code:** Do not write or modify code outside of the approved execution plan in `.agents/executionplans`.