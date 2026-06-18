package com.speechtotextlocal.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.speechtotextlocal.app.ui.DictationScreen
import com.speechtotextlocal.app.ui.theme.SpeechToTextLocalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpeechToTextLocalTheme {
                DictationScreen()
            }
        }
    }
}
