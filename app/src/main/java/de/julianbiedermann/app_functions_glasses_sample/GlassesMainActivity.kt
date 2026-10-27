package de.julianbiedermann.app_functions_glasses_sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.ComposeUiFlags
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.xr.glimmer.ActionCard
import androidx.xr.glimmer.Button
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Text
import androidx.xr.glimmer.googlefonts.createGoogleSansFlexTypography
import androidx.xr.glimmer.pager.GlimmerHorizontalPager
import androidx.xr.glimmer.pager.rememberGlimmerPagerState
import dagger.hilt.android.AndroidEntryPoint
import de.julianbiedermann.app_functions_glasses_sample.models.Note
import de.julianbiedermann.app_functions_glasses_sample.repositories.sentnotes.SentNotesRepository
import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
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
        Log.d(TAG, "onCreate")
        ComposeUiFlags.isInitialFocusOnFocusableAvailable = true
        audioInterface = AudioInterface(
            this,
            getString(R.string.hello_ai_glasses)
        )
        lifecycle.addObserver(audioInterface)
        setContent {
            GlimmerTheme(
                typography = createGoogleSansFlexTypography()
            ) {
                Box(
                    modifier = Modifier
                        .background(GlimmerTheme.colors.background)
                        .fillMaxSize()
                ) {
                    val sentNotes by sentNotesRepository.sentNotes.collectAsStateWithLifecycle(
                        initialValue = emptyList()
                    )
                    if(sentNotes.isNotEmpty()) {
                        SentNotesPager(
                            notes = sentNotes,
                            onDismissNote = sentNotesRepository::dismissNote,
                            audioInterface = audioInterface
                        )
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "GlassesMainActivity"
    }
}

@Composable
private fun SentNotesPager(
    notes: List<Note>,
    onDismissNote: (String) -> Unit,
    audioInterface: AudioInterface
) {
    val displayedText = stringResource(id = R.string.note_displayed)
    val dismissedText = stringResource(id = R.string.note_dismissed)
    val pagerState = rememberGlimmerPagerState { notes.size }
    var previousSize by remember { mutableIntStateOf(value = 0) }

    LaunchedEffect(notes.size) {
        if (notes.size > previousSize) {
            pagerState.animateScrollToPage(0)
            audioInterface.speak(textToSpeak = displayedText)
        }
        previousSize = notes.size
    }

    GlimmerHorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        val note = notes[page]
        NoteCard(
            note = note,
            onDismiss = {
                onDismissNote(note.id)
                audioInterface.speak(textToSpeak = dismissedText)
            }
        )
    }
}

@Composable
private fun NoteCard(
    note: Note,
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.US)
    val timestamp = remember(note.timestamp) {
        dateFormat.format(note.timestamp)
    }

    ActionCard(
        title = {
            Text(
                text = note.topic,
                style = GlimmerTheme.typography.titleMedium
            )
        },
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
            Text(
                text = timestamp,
                style = GlimmerTheme.typography.caption
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = note.text,
                style = GlimmerTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(device = "id:ai_glasses_device", backgroundColor = 0x00FF00)
@Composable
private fun DefaultPreview() {
    GlimmerTheme {
        Box(
            modifier = Modifier
                .background(GlimmerTheme.colors.background)
                .fillMaxSize()
        ) {
            Text(
                text = "Preview mode - connect glasses to see full UI",
                style = GlimmerTheme.typography.bodyLarge
            )
        }
    }
}
