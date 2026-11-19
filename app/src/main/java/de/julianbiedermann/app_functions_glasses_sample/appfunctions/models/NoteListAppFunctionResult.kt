package de.julianbiedermann.app_functions_glasses_sample.appfunctions.models

import androidx.appfunctions.AppFunctionSerializable
import kotlinx.serialization.Serializable

/**
 * Result returned by notes-to-glasses App Functions, containing the actual notes that were
 * processed (enqueued for glasses display and/or returned to the calling agent).
 */
@Serializable
@AppFunctionSerializable(isDescribedByKDoc = true)
data class NoteListAppFunctionResult(
    /** The list of notes that were enqueued and/or returned to the caller. */
    val notes: List<NoteAppFunctionResult>
)
