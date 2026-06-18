package com.speechtotextlocal.app

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.speechtotextlocal.app.enhance.RuleBasedTextEnhancer
import com.speechtotextlocal.app.enhance.TextEnhancer
import com.speechtotextlocal.app.speech.SpeechRecognizerManager

/**
 * Owns the speech recogniser and exposes Compose-observable dictation state.
 */
class DictationViewModel(application: Application) : AndroidViewModel(application) {

    var rawTranscript by mutableStateOf("")
        private set
    var isListening by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
    var cleanUpEnabled by mutableStateOf(true)

    private val enhancer: TextEnhancer = RuleBasedTextEnhancer()

    private val manager = SpeechRecognizerManager(
        context = application,
        onPartialResult = { rawTranscript = it },
        onFinalResult = { rawTranscript = it },
        onErrorMessage = { message ->
            errorMessage = message
            isListening = false
        },
        onEnd = { isListening = false },
    )

    val isOnDeviceAvailable: Boolean get() = manager.isOnDeviceAvailable
    val isRecognitionAvailable: Boolean get() = manager.isRecognitionAvailable

    val displayedText: String
        get() = if (cleanUpEnabled) enhancer.enhance(rawTranscript) else rawTranscript

    val wordCount: Int
        get() = displayedText.split(Regex("\\s+")).count { it.isNotBlank() }

    fun toggle() {
        if (isListening) stop() else start()
    }

    fun start() {
        errorMessage = null
        if (!manager.isRecognitionAvailable) {
            errorMessage = "Speech recognition is not available on this device."
            return
        }
        rawTranscript = ""
        manager.start()
        isListening = true
    }

    fun stop() {
        manager.stop()
        isListening = false
    }

    fun clear() {
        rawTranscript = ""
    }

    fun reportPermissionDenied() {
        errorMessage = "Microphone permission is required to dictate."
    }

    override fun onCleared() {
        manager.stop()
        super.onCleared()
    }
}
