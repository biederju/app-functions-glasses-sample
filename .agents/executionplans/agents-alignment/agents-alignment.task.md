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
- [ ] 
    10. Dependency bumps → **ALL REVERTED** (kotlin-stdlib 2.4.10 pull blocks all bumps until Kotlin
        version upgrade)
- [x] 
    11. Verification: `./gradlew assembleDebug` ✅
- [x] 
    12. Verification: `./gradlew lintDebug` ✅
