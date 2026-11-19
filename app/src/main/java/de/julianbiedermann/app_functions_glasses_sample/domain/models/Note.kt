package de.julianbiedermann.app_functions_glasses_sample.domain.models

import kotlinx.serialization.Serializable

@Serializable
internal data class Note(
    val id: String,
    val topic: String,
    val text: String,
    val timestamp: Long
)
