package de.julianbiedermann.app_functions_glasses_sample.glasses

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import timber.log.Timber

internal class AudioInterface(context: Context) : DefaultLifecycleObserver {
    private val tts: TextToSpeech = TextToSpeech(context) { status ->
        if (status == TextToSpeech.SUCCESS) {
            Timber.d(message = "Initialization was successful")
        } else {
            Timber.e(message = "Initialization failed with status: $status")
        }
    }

    fun speak(textToSpeak: String) {
        tts.speak(
            textToSpeak,
            TextToSpeech.QUEUE_ADD,
            null,
            "id"
        )
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        tts.shutdown()
    }
}