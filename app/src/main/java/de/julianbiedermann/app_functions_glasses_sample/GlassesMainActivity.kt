package de.julianbiedermann.app_functions_glasses_sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ComposeUiFlags
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.xr.glimmer.ActionCard
import androidx.xr.glimmer.Button
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Text
import androidx.xr.glimmer.googlefonts.createGoogleSansFlexTypography
import dagger.hilt.android.AndroidEntryPoint
import android.util.Log

@AndroidEntryPoint
internal class GlassesMainActivity : ComponentActivity() {
    private lateinit var audioInterface: AudioInterface

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
                HomeScreen(onClose = {
                    audioInterface.speak("Goodbye!")
                    finish()
                })
            }
        }
    }

    companion object {
        private const val TAG = "GlassesMainActivity"
    }
}

@Composable
private fun HomeScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit
) {
    Box(
        modifier = modifier
            .background(GlimmerTheme.colors.background)
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        ActionCard(
            title = {
                Text(text = stringResource(id = R.string.app_name))
            },
            action = {
                Button(onClick = {
                    onClose()
                }) {
                    Text(text = stringResource(id = R.string.close))
                }
            }
        ) {
            Text(text = stringResource(id = R.string.hello_ai_glasses))
        }
    }
}

@Preview(device = "id:ai_glasses_device", backgroundColor = 0x00FF00)
@Composable
private fun DefaultPreview() {
    GlimmerTheme {
        HomeScreen(onClose = {})
    }
}