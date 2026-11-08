package de.julianbiedermann.app_functions_glasses_sample.ai

import androidx.appfunctions.AppFunctionSerializable
import kotlinx.serialization.Serializable

/** Result returned by notes-to-glasses App Functions, describing how many notes were shown. */
@Serializable
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class ShowNotesResult(
    /** The number of notes that were enqueued for display on the glasses. */
    val count: Int
)
