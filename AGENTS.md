# Agent & Engineering Guidelines: Glasses Glance Assistant

Dieses Dokument dient als Arbeitsvertrag und Harness für autonome KI-Agenten und Entwickler, die an diesem Repository arbeiten. Es definiert die architektonischen Leitplanken, Designregeln für Smart Glasses und die schrittweise Implementierungs-Roadmap.

---

## 🧭 Projektmission & Kontext

* **Ziel:** Erstellung einer Referenzimplementierung und eines Blogbeitrags für **Android Display Glasses** mit **Jetpack Compose Glimmer** und **Android 16 AppFunctions**.
* **Zielplattform:** Android XR Projected (`compileSdk 37`, `minSdk 36`).
* **Package:** `de.julianbiedermann.app_functions_glasses_sample`

---

## 📐 Nicht-verhandelbare Architektur- & Design-Prinzipien

### 1. Jetpack Compose Glimmer (Display Glasses UI)
* **Additive Displays:** Reines Schwarz (`Color.Black` / `#000000`) ist auf transparenten AR-Brillen 100 % transparent. Der Root-Container muss immer einen rein schwarzen Hintergrund haben (`Modifier.background(Color.Black)`). **Niemals Material 3 im Brillen-Context verwenden!**
* **Ergonomie:** Alle UI-Container gehören an den unteren Bildschirmrand (`Alignment.BottomCenter`). Das Sichtfeld des Nutzers in der realen Welt darf niemals im Zentrum blockiert werden.
* **"One Thing at a Time":** Verwende `VerticalStack` für Benachrichtigungen und Karten. Zeige immer nur eine primäre Information im Fokus. Nachfolgende Karten werden per Tiefeneffekt dezent angedeutet.
* **Touchpad-Navigation (1D-Interaktion):** Eingaben erfolgen über das Brillen-Touchpad (Wischen für sequentiellen Fokuswechsel, Tippen für Aktionen). In der `Activity.onCreate` muss `ComposeUiFlags.isInitialFocusOnFocusableAvailable = true` gesetzt sein.
* **Typografie:** Verwende ausschließlich `createGoogleSansFlexTypography()` aus `androidx.xr.glimmer:glimmer-google-fonts`. Mindestschriftgröße 18sp. Keine dünnen Schriftschnitte (verhindert Aliasing/Shimmering).
* **Multimodalität:** Optische UI-Zustände sollten mit diskretem Audiofeedback (`AudioInterface` / TTS) begleitet werden.

### 2. Android 16 AppFunctions (AI Agent Workflows)
* **Service Entry Point:** Implementierung über eine abstrakte Klasse, die von `AppFunctionService` erbt und mit `@AppFunctionServiceEntryPoint` annotiert ist (ab Version 1.0.0-alpha10+).
* **KSP & Serializables:** `@AppFunctionSerializable`-Datenklassen müssen für jede Property zwingend **Inline-KDocs** verwenden (KSP ignoriert `@param`-Tags auf Klassenebene).
* **Threading:** AppFunction-Implementierungen müssen immer auf Hintergrund-Dispatchern (`withContext(Dispatchers.IO)`) laufen.
* **Agent-Optimierte KDocs:** KDocs für Funktionen müssen mit starken Imperativen beginnen ("Erstelle...", "Rufe ab..."), Parametervalidierungen beschreiben und für LLMs verständlich sein.

---

## 🗺️ Implementierungs-Roadmap (Von 0 auf 100)

Wenn das Projekt von Grund auf neu aufgebaut oder erweitert wird, sind folgende Schritte chronologisch zu durchlaufen:

### Phase 1: Dependency Setup & Toolchain
- [] Gradle Version Catalog (`libs.versions.toml`): Einbinden von `androidx.xr.glimmer`, `androidx.xr.projected`, `androidx.appfunctions` und dem `ksp`-Plugin.
- [] Modul-Build (`build.gradle.kts`): KSP-Plugin aktivieren, `compileSdk 37`, `minSdk 36` konfigurieren.
- [] Manifest (`AndroidManifest.xml`): Projection-Category (`android.hardware.display.category.XR_PROJECTED`) für Brillen-Aktivitäten definieren.

### Phase 2: State-Management & Datenmodell
- [] `GlanceCard.kt`: Serialisierbare Datenklasse mit Inline-KDocs für AI-Agenten (`id`, `title`, `content`, `category`, `priority`, `timestamp`).
- [] `GlanceCardRepository.kt`: Reaktiver State-Holder (`MutableStateFlow<List<GlanceCard>>`) als Single Source of Truth für Phone und Brille.

### Phase 3: Display Glasses UI (Compose Glimmer)
- [] `GlassesMainActivity.kt`: Initialisierung der Glimmer-Fokussteuerung und des `AudioInterface`.
- [] `VerticalStack`-Layout am unteren Rand mit rein schwarzem Hintergrund.
- [] Zusammenspiel aus `TitleChip`, `ActionCard` und `Button` mit `Modifier.itemDecoration(CardDefaults.shape)`.
- [] Touchpad-Interaktion (Wischen durch den Stapel, Tippen zum Erledigen) mit TTS-Bestätigung.

### Phase 4: AppFunctions für System-Agenten
- [] `GlassesAppFunctionService.kt`: Deklaration von `addGlanceCard`, `getGlanceCards` und `removeGlanceCard`.
- [] `app_metadata.xml`: Globale App-Funktionsbeschreibungen und Constraints für LLMs.
- [] `AndroidManifest.xml`: Registrierung des `AppFunctionService` mit Permission `BIND_APP_FUNCTION_SERVICE`.
- [] ADB-Testskripte zur Validierung von Agenten-Aufrufen auf dem Android 16 System.

### Phase 5: Phone Companion UI & Demo-Cockpit
- [] `MainActivity.kt`: Prüfung des Brillen-Verbindungsstatus via `ProjectedContext`.
- [] Button zum Projizieren auf die Brille.
- [] Quick-Presets (*Meeting*, *Navigation*, *Wetter*) und manuelles Erstellungsformular für Live-Demos.

### Phase 6: Testing, Dokumentation & Blogbeitrag
- [] Umfassende Dokumentation in `README.md`.
- [ ] Screenshot- und Videoerstellung für den Blogbeitrag (Display Glasses Simulator + Companion).
- [ ] Blogpost-Erstellung (Fokus: UX-Paradigmenwechsel bei Smart Glasses & agentische Workflows).
