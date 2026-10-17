# Agent Instructions

## Project Goal & Scope
* **Core Focus:** Demonstrate App Functions and Compose Glimmer for display glasses through focused, minimal use cases.
* **Simplicity Rule:** Keep the implementation lightweight. Do not include local databases, complex persistence, or tests unless explicitly requested.
* **Spec-Driven Development:** Before writing any implementation code, always create an execution plan and save it in `./.agents/plans/`.
* **Clarification First:** Never start implementing if there are ambiguous requirements or open questions. Always clarify everything first.

## Tech Stack & Library Versions
* **Language & Standards:** Kotlin (latest stable). Strictly follow official Kotlin Coding and Naming Guidelines.
* **Build Tool:** Gradle (Kotlin DSL / .kts)
* **UI Framework:** Compose Glimmer
* **Navigation:** Navigation 3
* **Dependency Injection:** Hilt
* **Integration:** Android App Functions / AppSearch
* **Dependencies:** Always use the latest stable versions of libraries, unless experimental or canary releases are strictly required for XR/Glimmer features.

## Workflow & Execution Process
1. **Analyze & Clarify:** Read the user prompt. If anything is unclear, ask questions immediately.
2. **Plan:** Create a step-by-step markdown plan under `./.agents/plans/` outlining files to create or modify.
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
    * ViewModels must end with the `ViewModel` suffix (e.g., `StatusViewModel`).
    * Repositories must end with the `Repository` suffix (e.g., `DeviceStateRepository`).
    * Glimmer UI components must use descriptive suffixes like `Panel`, `Overlay`, or `Screen` (e.g., `StatusOverlay`).
* **Visibility Modifiers (Strict):** Always restrict access visibility. Only make classes, functions, or properties `public` or `internal` when strictly required. Prefer `private` or package-private scope by default.
* **Self-Documenting Code:** Code must be clear and readable so that KDoc/comments are unnecessary. Only add KDoc or inline comments for non-obvious workarounds or complex business logic.

## Anti-Patterns & Forbidden Practices
* **No Legacy Views:** Do not use XML layouts, ViewBinding, or legacy Fragment APIs. Everything must use Jetpack Compose and Compose Glimmer.
* **No Business Logic in UI:** Do not implement business logic, heavy calculations, or data processing inside Composable functions or Activities. Always delegate to ViewModels or UseCases.
* **No Hardcoded Navigation:** Do not trigger screen transitions via raw Intents or direct Activity startups. Always rely on the central Navigation 3 graph.
* **No Unplanned Code:** Do not write or modify code outside of the approved execution plan in `./.agents/plans/`.