# AGENTS.md Alignment — Task Tracking

- [x] 
    1. Rename `audioInterface.kt` → `AudioInterface.kt` (PascalCase)
- [x] 
    2. `AudioInterface` visibility → `internal`
- [x] 
    3. `GlassesMainActivity` → add `@AndroidEntryPoint`
- [x] 
    4. `GlassesMainActivity` → change `HomeScreen` visibility → `private`
- [x] 
    5. `GlassesMainActivity` → change `DefaultPreview` visibility → `private`
- [x] 
    6. Add structured `Log.d` logging to glasses lifecycle
- [x] 
    7. `Color.kt` color vals → `internal`
- [x] 
    8. Named parameters → add to all single-argument calls (GlassesMainActivity.kt +
       NotesOverviewScreen.kt)
- [x] 
    9. Dependency version audit (libs.versions.toml)
- [x] 
    10. Dependency bumps → **COMPLETED** (Kotlin upgraded to 2.4.21, all AndroidX libs bumped to
        latest stable including appfunctions 1.0.0-alpha12)
- [x] 
    11. Verification: `./gradlew assembleDebug` ✅
- [x] 
    12. Verification: `./gradlew lintDebug` ✅
