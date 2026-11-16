# Add Note Delete Button Execution Plan

## Goal

Add a dedicated delete button next to the send button on each note item card in the smartphone app (`NotesOverviewScreen`). When clicked, it deletes the note from the local app repository (`NotesRepository`) and also removes/dismisses the note from the glasses queue (`SentNotesRepository`).

## Context & Requirements

- Currently, notes can be deleted via swipe-to-dismiss or created/managed, but users requested a clear, visible delete button next to the send button in each note row for better UX clarity.
- Deleting a note must perform two actions:
  1. Delete the note from `NotesRepository` (smartphone app storage).
  2. Dismiss the note from `SentNotesRepository` (`sentNotesRepository.dismissNote(noteId)`), removing it from the glasses display queue.

## Proposed Changes

### Component: UI & ViewModel (`NotesOverviewScreen.kt` & `NotesOverviewViewModel.kt`)

#### [MODIFY] [NotesOverviewViewModel.kt](file:///Users/Rump/Documents/AndroidStudioProjects/app-functions-glasses-sample/app/src/main/java/de/julianbiedermann/app_functions_glasses_sample/ui/screens/notesoverview/NotesOverviewViewModel.kt)
- Update `deleteNote(noteId: String)` to:
  - Call `repository.deleteNote(noteId)`
  - Call `sentNotesRepository.dismissNote(noteId)`

#### [MODIFY] [NotesOverviewScreen.kt](file:///Users/Rump/Documents/AndroidStudioProjects/app-functions-glasses-sample/app/src/main/java/de/julianbiedermann/app_functions_glasses_sample/ui/screens/notesoverview/NotesOverviewScreen.kt)
- Update `NoteItemForeground` to include an `IconButton` with `Icons.Filled.Delete` next to the Send button in a horizontal arrangement (`Row`).
- Update `NoteItem` to pass `onDeleteNote` to `NoteItemForeground` (or directly invoke `onDeleteNote(note.id)`).

## Dependencies

No new dependencies or Gradle plugins required.

## Verification Plan

### Automated Build Checks
- Run `./gradlew assembleDebug`
- Run `./gradlew lintDebug`

### UI Verification
- Verify the delete button appears next to the send button in the phone UI.
- Verify clicking delete removes the note from the phone list and dismisses it from the glasses queue.
