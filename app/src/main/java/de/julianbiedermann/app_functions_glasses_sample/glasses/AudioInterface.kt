package de.julianbiedermann.app_functions_glasses_sample.glasses

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import timber.log.Timber

internal class AudioInterface(
    private val context: Context,
    private val initializationMessage: String
) : DefaultLifecycleObserver {
    private lateinit var tts: TextToSpeech
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                speak(initializationMessage)
            } else {
                Timber.e("Initialization failed with status: $status")
            }
        }
    }

    fun speak(textToSpeak: String) {
        tts.speak(
            textToSpeak,
            TextToSpeech.QUEUE_ADD,
            null,
            initializationMessage.lowercase().replace(" ", "_")
        )
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        tts.shutdown()
    }
}