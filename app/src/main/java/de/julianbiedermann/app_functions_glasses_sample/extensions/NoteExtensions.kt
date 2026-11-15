package de.julianbiedermann.app_functions_glasses_sample.extensions

import de.julianbiedermann.app_functions_glasses_sample.appfunctions.models.NoteAppFunctionResult
import de.julianbiedermann.app_functions_glasses_sample.domain.models.Note

/** Converts a full [Note] (including its internal [id]) to a [de.julianbiedermann.app_functions_glasses_sample.appfunctions.models.NoteAppFunctionResult]. */
internal fun Note.toNoteAppFunctionResult(): NoteAppFunctionResult = NoteAppFunctionResult(
    topic = topic,
    text = text,
    timestamp = timestamp
)
