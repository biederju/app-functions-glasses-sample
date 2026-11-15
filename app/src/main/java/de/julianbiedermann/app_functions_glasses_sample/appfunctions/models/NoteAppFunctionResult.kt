package de.julianbiedermann.app_functions_glasses_sample.appfunctions.models

import androidx.appfunctions.AppFunctionSerializable
import kotlinx.serialization.Serializable

/**
 * A minimal, serializable representation of a note returned by App Functions.
 */
@Serializable
@AppFunctionSerializable(isDescribedByKDoc = true)
data class NoteAppFunctionResult(
    /** The note's title / topic. */
    val topic: String,
    /** The note's body text. */
    val text: String,
    /** The note's creation timestamp in epoch milliseconds. */
    val timestamp: Long
)
