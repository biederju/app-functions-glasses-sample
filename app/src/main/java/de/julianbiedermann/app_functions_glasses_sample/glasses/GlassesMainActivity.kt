package de.julianbiedermann.app_functions_glasses_sample.glasses

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ComposeUiFlags
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.xr.glimmer.ActionCard
import androidx.xr.glimmer.Button
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Text
import androidx.xr.glimmer.TitleChip
import androidx.xr.glimmer.list.GlimmerLazyColumn
import androidx.xr.glimmer.list.items
import androidx.xr.glimmer.list.rememberGlimmerLazyListState
import dagger.hilt.android.AndroidEntryPoint
import de.julianbiedermann.app_functions_glasses_sample.R
import de.julianbiedermann.app_functions_glasses_sample.domain.models.Note
import de.julianbiedermann.app_functions_glasses_sample.domain.sentnotes.SentNotesRepository
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
internal class GlassesMainActivity : ComponentActivity() {
    private lateinit var audioInterface: AudioInterface

    @Inject
    lateinit var sentNotesRepository: SentNotesRepository

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d(message = "onCreate")
        ComposeUiFlags.isInitialFocusOnFocusableAvailable = true
        audioInterface = AudioInterface(context = this)
        lifecycle.addObserver(audioInterface)
        setContent { GlimmerTheme { Content() } }
    }

    @Composable
    private fun Content() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = GlimmerTheme.colors.background)
        ) {
            val sentNotes by sentNotesRepository.sentNotes.collectAsStateWithLifecycle(
                initialValue = emptyList()
            )
            if (sentNotes.isNotEmpty()) {
                val displayedText = stringResource(id = R.string.note_displayed)
                val dismissedText = stringResource(id = R.string.note_dismissed)
                NoteList(
                    notes = sentNotes,
                    onDismissNote = { noteId ->
                        sentNotesRepository.dismissNote(noteId = noteId)
                        audioInterface.speak(textToSpeak = dismissedText)
                    },
                    onDisplayedNote = {
                        audioInterface.speak(textToSpeak = displayedText)
                    }
                )
            }
        }
    }
}

@Composable
private fun NoteList(
    notes: List<Note>,
    onDismissNote: (String) -> Unit,
    onDisplayedNote: () -> Unit
) {
    val listState = rememberGlimmerLazyListState()
    var previousSize by remember { mutableIntStateOf(value = 0) }

    LaunchedEffect(key1 = notes) {
        if (notes.isNotEmpty() && notes.size > previousSize) {
            listState.scrollToItem(index = 0)
            onDisplayedNote()
        }
        previousSize = notes.size
    }

    GlimmerLazyColumn(
        state = listState,
        title = { TitleChip { Text(text = stringResource(id = R.string.app_name)) } }
    ) {
        items(items = notes.reversed()) { note ->
            NoteActionCard(
                note = note,
                onDismiss = { onDismissNote(note.id) }
            )
        }
    }
}

@Composable
private fun NoteActionCard(
    note: Note,
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.US)

    ActionCard(
        title = { Text(text = note.topic) },
        subtitle = { Text(text = dateFormat.format(note.timestamp)) },
        action = {
            Button(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.dismiss_note_card))
            }
        }
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = note.text
        )
    }
}

@Preview(device = "id:ai_glasses_device", showBackground = true)
@Composable
private fun NoteListPreview() {
    GlimmerTheme {
        NoteList(
            notes = listOf(
                Note(
                    id = "1",
                    topic = "Meeting Notes",
                    text = "Discuss project roadmap for Q1.",
                    timestamp = System.currentTimeMillis()
                ),
                Note(
                    id = "2",
                    topic = "Shopping List",
                    text = "Milk, eggs, bread",
                    timestamp = System.currentTimeMillis() - 86400000L
                )
            ),
            onDismissNote = {},
            onDisplayedNote = {}
        )
    }
}
