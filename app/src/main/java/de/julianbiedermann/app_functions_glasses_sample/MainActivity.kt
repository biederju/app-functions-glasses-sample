package de.julianbiedermann.app_functions_glasses_sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import dagger.hilt.android.AndroidEntryPoint
import de.julianbiedermann.app_functions_glasses_sample.ui.notesoverview.NotesOverviewScreen
import de.julianbiedermann.app_functions_glasses_sample.ui.theme.AppfunctionsglassessampleTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppfunctionsglassessampleTheme {
                Surface {
                    NotesOverviewScreen()
                }
            }
        }
    }
}
