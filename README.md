# Glasses Glance Assistant: Android Display Glasses & AppFunctions Sample

> Ein modernes Android XR Starterprojekt, das zeigt, wie **Jetpack Compose Glimmer** für Augmented Reality Display Glasses mit den neuen **Android 16 AppFunctions** für AI-Agenten zusammenspielt.

---

## 👓 Die Idee & Fachliches Konzept

Smart Display Glasses verändern das Verhältnis zwischen Mensch und digitaler Information fundamental: **Ambient & Glanceable Computing** statt Smartphone-Dauerablenkung.

Wenn wir unterwegs sind – beim Gehen, Fahrradfahren, Kochen, Handwerken oder am Flughafen mit vollen Händen – ist das Smartphone oft eine Barriere: Es muss aus der Tasche gezogen, entsperrt und durch Apps navigiert werden.

Der **Glasses Glance Assistant** löst genau das: Er bringt mundgerechte, kontextbezogene Informations-Kärtchen („Glances“) hands-free in das untere Sichtfeld des Nutzers – gesteuert durch Sprache, KI-Agenten oder die Brillen-Sensorik.

### 🌟 Was soll der Glance Assistant fachlich können?

#### 1. Hands-Free Alltag & Micro-Tasks (Im Alltag im Fluss bleiben)
* **Rezept- & Montage-Schritte:** Beim Kochen oder Reparieren bleiben die Hände bemehlt oder am Werkzeug. Ein Sprachbefehl („Nächster Schritt“) oder ein kurzer Wisch am Brillenbügel schaltet den nächsten Handgriff ins Sichtfeld.
* **Erinnerungen & Notizen:** „Erinnere mich an die Einkaufsliste“ blendet die anstehenden Besorgungen ein. Sobald ein Punkt erledigt ist: ein kurzer Tap ans Touchpad, und der Punkt ist abgehakt und verschwindet mit diskretem Audio-Feedback („Erledigt“).

#### 2. Reiseszenarien & On-the-Go Updates (Immer zur richtigen Zeit informiert)
* **Flughafen / Bahnhof:** Wenn sich das Abfluggate oder das Gleis ändert, muss niemand panisch aufs Smartphone starren. Ein KI-Agent projiziert im Hintergrund:
    * *[Reise] Flug LH123: Gate B42 (Boarding in 10 Min)*
* **Navigation & Wegpunkte:** Kurze, diskrete Richtungswechsel („In 100m rechts abbiegen“), die nur wenige Sekunden aufblitzen, damit der Blick auf den Verkehr frei bleibt.

#### 3. Proaktive AI-Assistenz (Der KI-Agent als Co-Pilot)
* **Termin-Radar:** 5 Minuten vor dem nächsten Standup blendet der Assistent proaktiv das Meeting und den Raum ein.
* **Wetter- & Umweltwarnungen:** Zieht ein Regenschauer auf, erscheint ein unaufdringlicher Hinweis: *[Wetter] Regen in 10 Minuten erwartet (90 %)*.
* **Bidirektionaler Kontext:** Sprachassistenten (wie Gemini) können per AppFunction abfragen, was der Nutzer gerade auf der Brille sieht (`getGlanceCards`), und darauf aufbauen („Lies mir die Details zur zweiten Notiz vor“).

#### 4. "One Thing at a Time" – Entlastung statt Overload
* Der Nutzer wird nicht mit Dashboards, Tabellen oder Push-Fluten überhäuft.
* Informationen liegen in einem virtuellen Stapel (**VerticalStack**): Immer nur **eine** Karte steht im Fokus.
* Mit einer Wischgeste nach vorne/hinten auf dem Brillen-Touchpad blättert man durch anstehende Karten; mit einem Tap hakt man die aktuelle Karte ab.

---

## 🏛️ Architektur & Systemübersicht

```text
┌───────────────────────────────┐              ┌───────────────────────────────┐
│     Nutzer / AI-Assistent     │              │     Companion App (Phone)     │
│        (z. B. Gemini)         │              │       (MainActivity.kt)       │
└───────────────┬───────────────┘              └───────────────┬───────────────┘
                │ Sprachbefehl / Prompt                        │ Manuelle Eingabe / Presets
                ▼                                              ▼
┌───────────────────────────────┐              ┌───────────────────────────────┐
│    Android 16 AppFunctions    │              │       Phone Dashboard &       │
│  (GlassesAppFunctionService)  │              │       Live-Karten-Monitor     │
└───────────────┬───────────────┘              └───────────────┬───────────────┘
                │ addGlanceCard()                              │ addCard()
                └──────────────────────┬───────────────────────┘
                                       │
                                       ▼
                    ┌─────────────────────────────────────┐
                    │        GlanceCardRepository         │
                    │      (Shared StateFlow Engine)      │
                    └──────────────────┬──────────────────┘
                                       │
                                       ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                    Display Glasses Experience (Compose Glimmer)              │
│                              (GlassesMainActivity.kt)                        │
├──────────────────────────────────────────────────────────────────────────────┤
│  • Additives Display: Color.Black (#000000 ist 100% transparent im Raum)     │
│  • Ergonomie: Ausrichtung am unteren Rand (Alignment.BottomCenter)           │
│  • One Thing at a Time: VerticalStack zeigt immer genau eine Karte im Fokus  │
│  • Typografie: Google Sans Flex (ab 18sp, kein Aliasing/Shimmering)          │
│  • 1D-Touchpad: Wischen zum Blättern, Tippen für "Erledigt"                  │
│  • Multimodalität: Sprachrückmeldung via AudioInterface (TTS)                │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## ✨ Features & Technische Kernkonzepte

### 1. Jetpack Compose Glimmer (AR UI)
* **Additive Displays:** Reines Schwarz (`#000000`) schaltet Pixel ab und wird vom optischen Wellenleiter als transparent gerendert. Das Sichtfeld bleibt frei.
* **Ergonomisches Blickfeld:** Alle UI-Komponenten sind am unteren Rand (`Alignment.BottomCenter`) verankert.
* **One Thing at a Time mit `VerticalStack`:** Statt überladener Dashboards wird immer genau eine aktive Karte im Fokus gezeigt; nachfolgende Karten werden räumlich angedeutet.
* **Google Sans Flex Typografie:** Variable Fonts mit angepasster Roundness und Mindestgröße (18sp+), um Aliasing und Shimmering auf AR-Gläsern zu verhindern.
* **1D-Interaktionsmodell:** Fokus-Navigation per Touchpad (Wischen zum Wechseln zwischen Karten/Buttons, Tap zum Bestätigen).

### 2. Android 16 AppFunctions (AI Workflows)
* **`addGlanceCard`:** Projiziert kontextuelle Karten (z. B. Gate-Änderungen, Meeting-Erinnerungen, Navigation) direkt auf die Brille.
* **`getGlanceCards`:** Liest für AI-Modelle aus, welche Karten aktuell im Sichtfeld des Nutzers aktiv sind.
* **`removeGlanceCard`:** Entfernt erledigte oder obsolete Karten.
* **Agent-Optimierte KDocs:** Ermöglicht semantische Werkzeugauswahl durch LLMs.

### 3. Phone Companion App
* Überwachung des Brillen-Verbindungsstatus via `androidx.xr.projected.ProjectedContext`.
* Dashboard zum manuellen Absenden von Presets und individuellen Test-Karten für Live-Demos.

---

## 🚀 Erste Schritte

### Voraussetzungen
* **Android Studio** (aktuelle Preview mit Android XR SDK-Unterstützung)
* **Target SDK:** 37 (Android 16) | **Min SDK:** 36
* **Geräte:** Android Display Glasses via USB/Wireless oder Android XR Emulator

### Installation & Ausführung
1. Repository in Android Studio klonen und öffnen.
2. Gradle Sync durchführen.
3. Auf dem Zielgerät / Emulator installieren:
   ```bash
   ./gradlew installDebug
   ```

---

## 🧪 AI-Workflows via ADB testen

Um zu simulieren, wie ein On-Device LLM (Gemini) AppFunctions aufruft, können folgende ADB-Befehle im Terminal genutzt werden:

```bash
# 1. Registrierte AppFunctions prüfen:
adb shell cmd app_function list-app-functions | grep julianbiedermann

# 2. Neue Karte auf die Brille pushen:
adb shell cmd app_function execute-app-function \
  --package de.julianbiedermann.app_functions_glasses_sample \
  --function 'de.julianbiedermann.app_functions_glasses_sample.ai.GlassesAppFunctionService#addGlanceCard' \
  --parameters '{"title":"Flugsteig B42","content":"Boarding beginnt in 10 Minuten.","category":"Reise","priority":"HIGH"}'

# 3. Aktive Karten abfragen:
adb shell cmd app_function execute-app-function \
  --package de.julianbiedermann.app_functions_glasses_sample \
  --function 'de.julianbiedermann.app_functions_glasses_sample.ai.GlassesAppFunctionService#getGlanceCards' \
  --parameters '{}'
```

---

## 📚 Blogbeitrag & Referenzen
* [Android XR – Intelligent Eyewear Documentation](https://developer.android.com/develop/xr/jetpack-xr-sdk/ai-glasses/first-activity)
* [Jetpack Compose Glimmer Guide](https://developer.android.com/jetpack/androidx/releases/xr-glimmer)
* [Android AppFunctions Framework](https://developer.android.com/about/versions/16)
