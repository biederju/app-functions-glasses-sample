# Edit Note App Function (`editNote`) Execution Plan

## Goal

Add a new Android App Function `editNote` allowing AI agents (such as Google Assistant / Gemini) or system workflows to edit existing notes by matching their topic and updating their body text (and optionally renaming the topic), with an optional flag to immediately display the updated note on connected Android Display Glasses.

## Context & User Stories

- As an AI agent or voice assistant, I want to edit an existing note by specifying the note's topic and providing the new text content.
- Optionally, if the user requested to rename the note, the agent can supply `newTopic`.
- Optionally, if the user requested to see the updated note on the glasses, the agent can specify `showOnGlasses = true` to project the updated note directly onto the connected display glasses.

## Proposed Changes

### Component: Domain & Data Storage

#### [MODIFY] [NotesRepository.kt](file:///Users/Rump/Documents/AndroidStudioProjects/app-functions-glasses-sample/app/src/main/java/de/julianbiedermann/app_functions_glasses_sample/domain/notes/NotesRepository.kt)
- Add `suspend fun updateNote(noteId: String, topic: String, text: String)` to the interface.

#### [MODIFY] [NotesRepositoryImpl.kt](file:///Users/Rump/Documents/AndroidStudioProjects/app-functions-glasses-sample/app/src/main/java/de/julianbiedermann/app_functions_glasses_sample/domain/notes/NotesRepositoryImpl.kt)
- Implement `updateNote(noteId: String, topic: String, text: String)`:
  - Updates the matching note in `DataStore<NoteListStorage>` with the new topic, text, and an updated timestamp (`System.currentTimeMillis()`).

### Component: App Functions Service

#### [MODIFY] [NotesGlassesAppFunctionService.kt](file:///Users/Rump/Documents/AndroidStudioProjects/app-functions-glasses-sample/app/src/main/java/de/julianbiedermann/app_functions_glasses_sample/appfunctions/NotesGlassesAppFunctionService.kt)
- Add `@AppFunctionDeclaration(isDescribedByKDoc = true)` suspend function `editNote`:
  - Parameters:
    - `topic: String` (required, non-blank: topic of the existing note to find and edit)
    - `text: String` (required, non-blank: new body text of the note)
    - `newTopic: String? = null` (optional: new topic name if renaming)
    - `showOnGlasses: Boolean = false` (optional: whether to launch on connected display glasses)
  - Validation:
    - Throw `AppFunctionInvalidArgumentException` if `topic` or `text` is blank.
    - Find existing note matching `topic` (case-insensitive). If none found, throw `AppFunctionInvalidArgumentException("No note found with topic: $topic")`.
  - Execute update:
    - Call `notesRepo.updateNote(...)`.
    - Fetch updated note and return `NoteAppFunctionResult`.
    - If `showOnGlasses == true`, enqueue the updated note into `sentNotesRepo` and launch the glasses activity via `enqueueNotes()`.

### Component: App Metadata & Instructions

#### [MODIFY] [app_metadata.xml](file:///Users/Rump/Documents/AndroidStudioProjects/app-functions-glasses-sample/app/src/main/res/xml/app_metadata.xml)
- Update `appfn:instructions`:
  - Add operational pattern: `Use 'editNote' to update the text or topic of an existing note, optionally displaying it on connected glasses.`
  - Update constraints: `'editNote' requires an existing note with matching topic and non-blank text.`

### Component: Developer Guidelines & Testing Documentation

#### [MODIFY] [AGENTS.md](file:///Users/Rump/Documents/AndroidStudioProjects/app-functions-glasses-sample/AGENTS.md)
- Add documentation and ADB command example for testing `editNote` without removing any existing content.

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
2. Execute `editNote`:
   ```bash
   adb -s emulator-5556 shell 'cmd app_function execute-app-function --package de.julianbiedermann.app_functions_glasses_sample --function "de.julianbiedermann.app_functions_glasses_sample.appfunctions.NotesGlassesAppFunctionService#editNote" --parameters "{\"topic\": \"Shopping\", \"text\": \"Buy milk, bread, and coffee\", \"showOnGlasses\": true}"'
   ```
3. Inspect Phone UI and Glasses UI:
   - Confirm the note is updated in the Phone app.
   - Confirm updated card appears on the glasses emulator (`emulator-5554`).
