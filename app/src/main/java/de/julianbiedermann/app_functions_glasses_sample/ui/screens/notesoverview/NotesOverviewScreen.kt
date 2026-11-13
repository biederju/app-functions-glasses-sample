package de.julianbiedermann.app_functions_glasses_sample.ui.screens.notesoverview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.xr.projected.ProjectedContext
import androidx.xr.projected.experimental.ExperimentalProjectedApi
import de.julianbiedermann.app_functions_glasses_sample.R
import de.julianbiedermann.app_functions_glasses_sample.glasses.GlassesMainActivity
import de.julianbiedermann.app_functions_glasses_sample.domain.models.Note
import timber.log.Timber
import java.text.SimpleDateFormat

@OptIn(ExperimentalProjectedApi::class)
@Composable
internal fun NotesOverviewScreen(
    viewModel: NotesOverviewViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isConnected by ProjectedContext.isProjectedDeviceConnected(
        context,
        scope.coroutineContext
    ).collectAsStateWithLifecycle(initialValue = false)

    NotesOverviewContent(
        uiState = uiState,
        onAddNote = viewModel::addNote,
        onDeleteNote = viewModel::deleteNote,
        onSendNote = { note ->
            viewModel.sendNoteToGlasses(note)
            if (isConnected) {
                try {
                    val options =
                        ProjectedContext.createProjectedActivityOptions(context).toBundle()
                    val intent = android.content.Intent(context, GlassesMainActivity::class.java)
                    context.startActivity(intent, options)
                } catch (e: Exception) {
                    Timber.e(e, "Error launching glasses")
                }
            }
        },
        snackbarHostState = snackbarHostState,
        isConnected = isConnected
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotesOverviewContent(
    uiState: NotesOverviewUiState,
    onAddNote: (String, String) -> Unit,
    onDeleteNote: (String) -> Unit,
    onSendNote: (Note) -> Unit,
    snackbarHostState: SnackbarHostState,
    isConnected: Boolean
) {
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.app_name))
                },
                actions = {
                    Icon(
                        modifier = Modifier.padding(all = 16.dp),
                        imageVector = if (isConnected) Icons.Filled.Wifi else Icons.Filled.WifiOff,
                        contentDescription = if (isConnected) {
                            stringResource(id = R.string.status_connected)
                        } else {
                            stringResource(id = R.string.status_disconnected)
                        }
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(id = R.string.fab_add_note)
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (uiState.notes.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.empty_notes),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } else {
                NotesList(
                    notes = uiState.notes,
                    onDeleteNote = onDeleteNote,
                    onSendNote = onSendNote
                )
            }
        }
    }

    if (showDialog) {
        AddNoteDialog(
            onDismiss = { showDialog = false },
            onSave = { topic, text ->
                onAddNote(topic, text)
                showDialog = false
            }
        )
    }
}

@Composable
private fun NotesList(
    notes: List<Note>,
    onDeleteNote: (String) -> Unit,
    onSendNote: (Note) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        items(notes, key = { it.id }) { note ->
            NoteItem(
                note = note,
                onDeleteNote = onDeleteNote,
                onSendNote = onSendNote
            )
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun NoteItem(
    note: Note,
    onDeleteNote: (String) -> Unit,
    onSendNote: (Note) -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState()

    SwipeToDismissBox(
        state = dismissState,
        modifier = Modifier.fillMaxWidth(),
        backgroundContent = {
            SwipeDismissBackground(
                state = dismissState,
                onDeleteNote = onDeleteNote,
                noteId = note.id
            )
        }
    ) {
        NoteItemForeground(note = note, onSendNote = onSendNote)
    }
}

@Composable
private fun NoteItemForeground(note: Note, onSendNote: (Note) -> Unit) {
    val locale = Locale.current
    val dateFormat = SimpleDateFormat("MMM d, yyyy HH:mm", locale.platformLocale)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(
                    id = R.string.note_timestamp,
                    dateFormat.format(note.timestamp)
                ),
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = note.topic,
                style = MaterialTheme.typography.titleMedium
            )
            if (note.text.isNotEmpty()) {
                Text(
                    text = note.text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        IconButton(onClick = { onSendNote(note) }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = stringResource(id = R.string.send_note)
            )
        }
    }
}

@Composable
private fun SwipeDismissBackground(
    state: androidx.compose.material3.SwipeToDismissBoxState,
    onDeleteNote: (String) -> Unit,
    noteId: String
) {
    val color = when (state.currentValue) {
        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.error.copy(alpha = 0f)
    }
    val alignment = when (state.currentValue) {
        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
        else -> Alignment.Center
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .background(color),
        contentAlignment = alignment
    ) {
        if (state.currentValue == SwipeToDismissBoxValue.EndToStart) {
            IconButton(
                onClick = { onDeleteNote(noteId) }
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(id = R.string.delete_note),
                    tint = MaterialTheme.colorScheme.onError
                )
            }
        }
    }
}

@Composable
private fun AddNoteDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var topic by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.dialog_add_note_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text(text = stringResource(id = R.string.dialog_add_note_topic_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(text = stringResource(id = R.string.dialog_add_note_text_hint)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (topic.isNotBlank()) {
                        onSave(topic, text)
                    }
                }
            ) {
                Text(text = stringResource(id = R.string.dialog_add_note_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.dialog_add_note_cancel))
            }
        }
    )
}

@PreviewLightDark
@Composable
private fun NotesOverviewContentPreview() {
    MaterialTheme {
        NotesOverviewContent(
            uiState = NotesOverviewUiState(
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
                )
            ),
            onAddNote = { _, _ -> },
            onDeleteNote = { },
            onSendNote = { },
            snackbarHostState = remember { SnackbarHostState() },
            isConnected = true
        )
    }
}

@PreviewLightDark
@Composable
private fun NotesOverviewContentEmptyPreview() {
    MaterialTheme {
        NotesOverviewContent(
            uiState = NotesOverviewUiState(notes = emptyList()),
            onAddNote = { _, _ -> },
            onDeleteNote = { },
            onSendNote = { },
            snackbarHostState = remember { SnackbarHostState() },
            isConnected = false
        )
    }
}
