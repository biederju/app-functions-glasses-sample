# Send Notes to the Display Glasses (Queue of Sent Notes)

## Goal

Add the ability to **send** notes from the phone notes list to the connected display
glasses. Each phone list item gets a trailing **send** button. Tapping it sends the note
to the glasses, where the user sees the notes sent during the session. Each sent note is
shown as a **card with a Close button**, with the same vertical order as the phone list
item (**timestamp → title → text**). Content that is too long must be scrollable within
the card; the Close button dismisses that note from the glasses.

This plan follows `AGENTS.md` (spec-driven development, plan under
`.agents/executionplans/`) and the `display-glasses-with-jetpack-compose-glimmer` skill.

## Clarified Decisions (from user)

| Question | User Decision |
| --- | --- |
| How the note reaches the separate `GlassesMainActivity` | Launch/bring-up `GlassesMainActivity` via `ProjectedContext`. Both activities run **in the same process**, so share the sent-note queue through a **`@Singleton` in-memory repository** (no IPC serialization needed). |
| Glasses presentation of sent notes | Queue / show a **list** of the notes sent this session (not a single card). Each queued note is its own card with a Close button. |
| Phone list item reorder | timestamp → **overline**, title stays **headline**, text becomes **subline** on the phone list (mirrors the glasses card). |
| Glasses layout of the sent-note cards | **Glimmer `HorizontalStack`**: one full note card visible at a time (scrollable + Close button), **horizontal** swipe between notes. |

## Resolved Design — Glimmer HorizontalStack of note cards

The glasses show the sent notes in a **Glimmer `HorizontalStack`**
(`androidx.xr.glimmer.stack`). The stack displays **one full note card at a time** in the
foreground with a hint of the next note revealed beside it; the user **swipes horizontally**
to the next/previous sent note.

**Why horizontal (not vertical):** each card has its own **vertical** scrollable content for
long notes. If the stack also advanced on **vertical** swipes, a single one-dimensional swipe
would be ambiguous — does it scroll the card text or move to the next note? A `HorizontalStack`
separates the axes cleanly: **vertical swipe scrolls within the card**, while **horizontal
swipe moves between notes**. This follows the Glimmer guidance to avoid nested/ambiguous
scrolling while still supporting scroll-within-card.

Each card has its own **Close** button and its own **scrollable content** area (so long notes
scroll within the card).

## Proposed Changes

### Data / Shared State Layer

#### [NEW] [repositories/SentNotesRepository.kt] (interface)
```
app/src/main/java/de/julianbiedermann/app_functions_glasses_sample/repositories/SentNotesRepository.kt
```
```kotlin
internal interface SentNotesRepository {
    val sentNotes: Flow<List<Note>>
    fun addNote(note: Note)      // enqueue
    fun dismissNote(noteId: String) // dequeue
}
```

#### [NEW] [repositories/SentNotesRepositoryImpl.kt]
In-memory `@Singleton` implementation backed by a `MutableStateFlow<List<Note>>`.
`addNote` appends, `dismissNote` filters out the id. Internal visibility.

#### [MODIFY] [di/RepositoryModule.kt]
Add `@Binds` binding `SentNotesRepositoryImpl` → `SentNotesRepository`
(alongside the existing `NotesRepository` binding).

> [!NOTE]
> The queue is intentionally **in-memory** (session-scoped) and not persisted — the user
> wants a list of notes "sent this session", and `AGENTS.md` keeps things lightweight.
> If it should survive relaunches, say so and we can hook it to DataStore.

### Phone (Notes Overview) Layer

#### [MODIFY] [ui/notesoverview/NotesOverviewViewModel.kt]
- Inject `SentNotesRepository` (and the app `Context`/dispatcher for the projection
  launch on `Dispatchers.IO`).
- Add `fun sendNoteToGlasses(note: Note, launchOnGlasses: (Context) -> Unit)` that
  calls `sentNotesRepository.addNote(note)` then triggers the Projected launch.
  Logging via `Log.d`; `try-catch` around the launch.

#### [MODIFY] [ui/notesoverview/NotesOverviewScreen.kt]
- Add a trailing **send** `IconButton` (send icon) in each note row.
- Reorder each row: **timestamp** (overline) → **topic** (headline) → **text** (subline).
- Hoist `onSendNote: (Note) -> Unit` through `NotesOverviewContent`/`NotesList`/`NoteItem`
  and wire it to enqueue the note + bring up the glasses via `ProjectedContext`
  (`createProjectedActivityOptions`, singleTop launch).
- Reuse the existing `isConnected` state to gate/perform the projection.

### Glasses Layer

#### [MODIFY] [GlassesMainActivity.kt]
- Replace the static `HomeScreen` with a **`SentNotesOverlay`** (suffix per AGENTS.md)
  that:
  - Collects `SentNotesRepository.sentNotes` via `collectAsStateWithLifecycle`.
  - Wraps the sent notes in a **Glimmer `HorizontalStack`** (`androidx.xr.glimmer.stack`)
    that displays **one note card at a time**; the user swipes **horizontally** between notes
    (vertical swipe stays available to scroll long card content).
  - **Each note is a Glimmer `ActionCard`** with vertical order **timestamp (overline) →
    title (headline) → text (subline)**, and a **Close** `Button` that calls
    `dismissNote(note.id)`. Use `StackScope.item`/`items` to add the note cards by key
    (note id).
  - Each card's **content is scrollable** for long text (a `Column`/text area with a
    scroll modifier sized to the card), so long notes scroll within the card.
  - **Empty state**: when the queue is empty, skip the stack and show a lightweight
    Glimmer fallback message (graceful degradation per AGENTS.md).
  - Recommend `TextStyle.textMotion = TextMotion.Animated` for stack text per Glimmer
    guidance to avoid pixel-snapping artifacts.
- Keep `GlimmerTheme` + `createGoogleSansFlexTypography`, mandatory pure-black root
  background, `isInitialFocusOnFocusableAvailable = true`, and the `AudioInterface`
  greeting.
- `GlassesMainActivity` stays a single instance that observes the shared repository (no
  intent-extra parsing needed for the note data under the shared-repo decision).

### Manifest / Resources

#### [MODIFY] [AndroidManifest.xml]
- Set `GlassesMainActivity` `android:launchMode="singleTop"` so repeated sends bring the
  already-open glasses screen forward without recreating it.

#### [MODIFY] [res/values/strings.xml]
- Add English strings: send-button content description (`send_note`), **Dismiss** button label
  (`dismiss_note_card`), audio phrases (`note_displayed`, `note_dismissed`), and an empty-queue
  fallback message.

### Bug Fixes & Enhancements (Post-Implementation)

#### [MODIFY] [GlassesMainActivity.kt]

**Problem:** The Close button on `NoteCard` does nothing (`onClick = {}`). The composable has no
access to `SentNotesRepository` or audio feedback.

**Problem:** When a second note is sent, the `GlimmerHorizontalPager` stays on page 0 and the user
cannot see the newly sent note — the pager state resets on each recomposition.

**Problem:** The `LaunchedEffect(notes.size)` was triggering on both add AND dismiss events,
incorrectly speaking "Note displayed" when notes were removed.

**Solution:**

1. **Dismiss callback**: Add `onDismiss: () -> Unit` parameter to `NoteCard`, pass it through
   `SentNotesPager` → `SentNotesOverlay` → `GlassesMainActivity`. The callback calls
   `sentNotesRepository.dismissNote(noteId)` and plays a "Note dismissed" TTS via
   `audioInterface.speak()`.

2. **Pager auto-scroll**: Use `LaunchedEffect(notes.size)` in `SentNotesPager` to call
   `pagerState.animateScrollToPage(notes.size - 1)` whenever a new note is added, ensuring the
   user always sees the latest note.

3. **Smart state tracking**: Track `previousSize` using `mutableStateOf<Int>` and only trigger
   the "Note displayed" TTS when `notes.size > previousSize` (i.e., notes were added, not
   dismissed). This prevents false audio triggers when notes are removed.

4. **Audio feedback for note display**: When notes arrive on the glasses, play a "Note displayed"
   TTS via `audioInterface.speak(stringResource(id = R.string.note_displayed))` inside the
   conditional `LaunchedEffect` block.

5. **Button text change**: Rename `R.string.close_note_card` → `R.string.dismiss_note_card` with
   value "Dismiss". Also add `R.string.note_displayed` ("Note displayed") and
   `R.string.note_dismissed` ("Note dismissed") for TTS feedback.

6. **Refactored parameter flow**: Extract `dismissedText` in `SentNotesOverlay` and pass it
   through to `SentNotesPager` → `NoteCard` to avoid calling `stringResource()` inside
   non-Composable lambdas.

## Notes & Constraints

- **No IPC serialization** — shared in-memory singleton repository (same process).
- **Glimmer only in `GlassesMainActivity`**: `GlimmerTheme`, Glimmer `ActionCard`/`Card`,
  Glimmer `Button`, Glimmer `Text`, and the Glimmer **`HorizontalStack`** for the one-at-a-time
  note queue; pure-black background. No Material in the glasses activity.
- **Scrolling**: one card at a time in a Glimmer `HorizontalStack`. Vertical swipe scrolls long
  card content; horizontal swipe moves between notes — avoids the ambiguous nested-scroll
  interaction and follows Glimmer's "one primary piece of info" guidance.
- **No new dependencies expected.** Any new dependency required (icon, scroll) will be
  listed with an explicit version before being added, per `AGENTS.md`.
- Naming suffixes (`SentNotesRepository`, `SentNotesOverlay`), `private`/`internal`
  visibility, English identifiers/KDoc, structured logging, and explicit error handling
  per `AGENTS.md`.

## Verification Plan

### Build / Lint
- `./gradlew assembleDebug` — must compile without errors.
- `./gradlew lintDebug` — must pass without errors (warnings ok).

### Manual Verification
- Deploy phone app with display glasses connected.
- Add a couple of notes; tap **send** on each → glasses shows the sent notes.
- Verify each card shows timestamp/title/text in order.
- Select a note with very long text → confirm scrolling works within the card.
- Tap **Close** on a card → that note is removed from the glasses.
