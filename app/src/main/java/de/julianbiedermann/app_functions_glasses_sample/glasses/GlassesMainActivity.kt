package de.julianbiedermann.app_functions_glasses_sample.glasses

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.xr.glimmer.pager.GlimmerHorizontalPager
import androidx.xr.glimmer.pager.rememberGlimmerPagerState
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
        Timber.d("onCreate")
        ComposeUiFlags.isInitialFocusOnFocusableAvailable = true
        audioInterface = AudioInterface(
            context = this,
            initializationMessage = getString(R.string.note_displayed)
        )
        lifecycle.addObserver(audioInterface)
        setContent {
            GlimmerTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(GlimmerTheme.colors.background)
                ) {
                    val sentNotes by sentNotesRepository.sentNotes.collectAsStateWithLifecycle(
                        initialValue = emptyList()
                    )
                    if (sentNotes.isNotEmpty()) {
                        val displayedText = stringResource(id = R.string.note_displayed)
                        val dismissedText = stringResource(id = R.string.note_dismissed)
                        SentNotesHorizontalPager(
                            notes = sentNotes,
                            onDismissNote = { noteId ->
                                sentNotesRepository.dismissNote(noteId = noteId)
                                audioInterface.speak(textToSpeak = dismissedText)
                                if (sentNotes.size == 1) {
                                    finish()
                                }
                            },
                            onDisplayedNote = {
                                audioInterface.speak(textToSpeak = displayedText)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SentNotesHorizontalPager(
    notes: List<Note>,
    onDismissNote: (String) -> Unit,
    onDisplayedNote: () -> Unit
) {
    var previousSize by remember { mutableIntStateOf(value = notes.size) }

    key(notes) {
        val pagerState = rememberGlimmerPagerState { notes.size }

        LaunchedEffect(notes) {
            if (notes.size > previousSize) {
                pagerState.animateScrollToPage(notes.lastIndex)
                onDisplayedNote()
            }
            previousSize = notes.size
        }

        GlimmerHorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val note = notes[page]
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = note.text)
        }
    }
}

@Preview(device = "id:ai_glasses_device", showBackground = true)
@Composable
private fun DefaultPreview() {
    GlimmerTheme {
        NoteActionCard(
            note = Note(
                id = "",
                topic = "Title",
                text = "Text",
                timestamp = System.currentTimeMillis()
            ),
            onDismiss = {}
        )
    }
}
