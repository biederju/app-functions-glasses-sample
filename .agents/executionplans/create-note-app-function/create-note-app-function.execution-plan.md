# Create Note App Function (`createNote`) Execution Plan

## Goal

Add a new Android App Function `createNote` to allow AI agents (such as Google Assistant / Gemini) or system workflows to create notes with a topic and body text hands-free, with an optional flag to immediately display the created note on connected Android Display Glasses.

## Context & User Stories

- As an AI agent or voice assistant, I want to create a note for the user by specifying a topic and text.
- Optionally, if the user requested to see the note on the glasses (or is currently in a hands-free workflow), the agent can specify `showOnGlasses = true` to project the note directly onto the connected display glasses.

## Proposed Changes

### Component: App Functions Service

#### [MODIFY] [NotesGlassesAppFunctionService.kt](file:///Users/Rump/Documents/AndroidStudioProjects/app-functions-glasses-sample/app/src/main/java/de/julianbiedermann/app_functions_glasses_sample/appfunctions/NotesGlassesAppFunctionService.kt)
- Add `@AppFunctionDeclaration(isDescribedByKDoc = true)` suspend function `createNote`:
  - Parameters:
    - `topic: String` (required, non-blank)
    - `text: String` (required, non-blank)
    - `showOnGlasses: Boolean = false` (optional, whether to launch on connected display glasses)
  - Validation: throw `AppFunctionInvalidArgumentException` if `topic` or `text` is blank.
  - Call `notesRepo.addNote(topic, text)`.
  - Fetch the newly created note and return `NoteAppFunctionResult`.
  - If `showOnGlasses` is true, enqueue the note to `sentNotesRepo` and launch the glasses activity via `enqueueNotes()`.

### Component: App Metadata & Instructions

#### [MODIFY] [app_metadata.xml](file:///Users/Rump/Documents/AndroidStudioProjects/app-functions-glasses-sample/app/src/main/res/xml/app_metadata.xml)
- Update `appfn:instructions`:
  - Add operational pattern: `Use 'createNote' to create a new note with a topic and body text, optionally displaying it on connected glasses.`
  - Update constraints: `Topic and text must be non-blank when creating a note.`

### Component: Developer Guidelines & Testing Documentation

#### [MODIFY] [AGENTS.md](file:///Users/Rump/Documents/AndroidStudioProjects/app-functions-glasses-sample/AGENTS.md)
- Add documentation and ADB command example for testing `createNote`.

## Dependencies

No new dependencies or Gradle plugins required.

## Verification Plan

### Automated Build Checks
- Run `./gradlew assembleDebug`
- Run `./gradlew lintDebug`

### App Function & Device Verification (via ADB)
1. Verify function registration:
   ```bash
   adb -s emulator-5556 shell cmd app_function list-app-functions --package de.julianbiedermann.app_functions_glasses_sample
   ```
2. Execute `createNote`:
   ```bash
   adb -s emulator-5556 shell 'cmd app_function execute-app-function --package de.julianbiedermann.app_functions_glasses_sample --function "de.julianbiedermann.app_functions_glasses_sample.appfunctions.NotesGlassesAppFunctionService#createNote" --parameters "{\"topic\": \"Shopping\", \"text\": \"Buy milk and bread\", \"showOnGlasses\": true}"'
   ```
3. Inspect Phone UI and Glasses UI:
   - Confirm note appears in the Notes list on phone emulator (`emulator-5556`).
   - Confirm note card is displayed on the glasses emulator (`emulator-5554`).
