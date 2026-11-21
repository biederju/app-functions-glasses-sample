# App Functions: Notes to Glasses — Task Tracking

## Phase 1: Library Setup & Dependencies

- [x] 
    1. Added `appfunctions = "1.0.0-alpha12"` version to `libs.versions.toml`
- [x] 
    2. Added `androidx-appfunctions` and `androidx-appfunctions-compiler` library aliases to
       `libs.versions.toml`
- [x] 
    3. Added `implementation(libs.androidx.appfunctions)` and
       `ksp(libs.androidx.appfunctions.compiler)` to `app/build.gradle.kts`
- [x] 
    4. Verified KSP plugin is present in `libs.plugins.ksp`

## Phase 2: AppMetadata XML

- [x] 
    5. Created `app/src/main/res/xml/app_metadata.xml` with agent-facing capabilities description

    - Operational patterns: `showLatestNotes` and `searchNotesOnGlasses`
    - Constraints: requires at least one existing note

## Phase 3: Serializable Result Type

- [x] 
    6. Created `ai/ShowNotesResult.kt` — `@AppFunctionSerializable(isDescribedByKDoc = true)` data
       class

    - Inline KDoc on `count` property
    - Also annotated with `@Serializable` for kotlinx.serialization

## Phase 4: AppFunctionService Implementation

- [x] 
    7. Created `ai/NotesGlassesAppFunctionService.kt`

    - Abstract class extending `AppFunctionService`
    - Annotated with `@AndroidEntryPoint` and `@AppFunctionServiceEntryPoint`
    - `serviceName = "NotesGlasses"`,
      `appFunctionXmlFileName = "notes_glasses_app_function_service"`
    - Two `@AppFunction` methods:
        - `showLatestNotes(count: Int = 3)` — shows N most recent notes
        - `searchNotesOnGlasses(query: String)` — searches notes by query (case-insensitive)
    - Both methods: suspend, use `withContext(Dispatchers.IO)`, throw typed `AppFunctionException`
      subclasses
    - Hilt EntryPoint pattern for DI (`AppFunctionEntryPoint` interface)
    - `enqueueNotes()` helper: enqueues notes to `SentNotesRepository` and launches glasses via
      `ProjectedContext`

## Phase 5: Manifest Configuration

- [x] 
    8. Added `BIND_APP_FUNCTION_SERVICE` permission to `AndroidManifest.xml`
- [x] 
    9. Added `android.app.appfunctions.app_metadata` property pointing to `@xml/app_metadata`
- [x] 
    10. Registered KSP-generated service (`NotesGlasses`) with:

    - `BIND_APP_FUNCTION_SERVICE` permission
    - `android:exported="true"`
    - `android.app.appfunctions.AppFunctionService` intent filter
    - Schema and schema_v2 properties

## Phase 6: Build Verification & Bug Fixes

- [x] 
    11. Fixed KSP "Class must have at least one AppFunction" error

    - Changed `serviceName` from `"NotesGlassesAppFunctionService"` to `"NotesGlasses"` to avoid
      naming collision
    - Changed abstract class visibility from `internal` to `public` (KSP generates public subclass)
    - Changed `NoteListAppFunctionResult` visibility from `internal` to `public`
- [x] 
    12. Fixed `coroutineContext` resolution in `ProjectedContext.isProjectedDeviceConnected()` call

    - Added `import kotlin.coroutines.coroutineContext`
- [x] 
    13. Fixed deprecated `startActivity` call — replaced with
        `ContextCompat.startActivity(this@..., intent, options)`
- [x] 
    14. Fixed `ActionCard` `header` parameter error in `GlassesMainActivity.kt`

    - Glimmer `1.0.0-alpha22` removed `header` parameter from `ActionCard`
    - Moved date display to `subtitle` parameter instead
- [x] 
    15. Verified `./gradlew assembleDebug` — compiles successfully
- [x] 
    16. Verified `./gradlew lintDebug` — passes with no errors

## Phase 7: Library Version Bumps (via agents-alignment plan)

- [x] 
    17. Kotlin `2.2.10` → `2.4.21` (resolves kotlin-stdlib 2.4.x metadata compatibility)
- [x] 
    18. AGP `9.5.0` → `9.5.1`
- [x] 
    19. Compose BOM `2026.03.01` → `2026.09.00`
- [x] 
    20. Glimmer `1.0.0-alpha15` → `1.0.0-alpha22`
- [x] 
    21. Projected `1.0.0-alpha10` → `1.0.0-alpha14`
- [x] 
    22. KSP `2.2.10-2.0.2` → `2.3.11`
- [x] 
    23. coreKtx `1.10.1` → `1.19.1`
- [x] 
    24. activityCompose `1.8.0` → `1.13.0`
- [x] 
    25. datastorePreferences `1.3.0` (already current)
- [x] 
    26. kotlinxSerialization `1.11.0` → `1.12.0`
