package com.speechtotextlocal.app.speech

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

/**
 * Thin wrapper around Android's [SpeechRecognizer] that prefers on-device,
 * offline recognition when the platform supports it (API 33+).
 *
 * All callbacks are delivered on the main thread.
 */
class SpeechRecognizerManager(
    private val context: Context,
    private val onPartialResult: (String) -> Unit,
    private val onFinalResult: (String) -> Unit,
    private val onErrorMessage: (String) -> Unit,
    private val onEnd: () -> Unit,
) {
    private var recognizer: SpeechRecognizer? = null

    val isRecognitionAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    val isOnDeviceAvailable: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(context)

    fun start() {
        stop()
        val rec = if (isOnDeviceAvailable) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else {
            SpeechRecognizer.createSpeechRecognizer(context)
        }
        recognizer = rec
        rec.setRecognitionListener(listener)
        rec.startListening(recognitionIntent())
    }

    fun stop() {
        recognizer?.apply {
            stopListening()
            cancel()
            destroy()
        }
        recognizer = null
    }

    private fun recognitionIntent(): Intent =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            )
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }

    private val listener = object : RecognitionListener {
        override fun onPartialResults(partialResults: Bundle?) {
            firstResult(partialResults)?.let(onPartialResult)
        }

        override fun onResults(results: Bundle?) {
            firstResult(results)?.let(onFinalResult)
            onEnd()
        }

        override fun onError(error: Int) {
            if (error != SpeechRecognizer.ERROR_NO_MATCH &&
                error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT
            ) {
                onErrorMessage(describe(error))
            }
            onEnd()
        }

        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun firstResult(bundle: Bundle?): String? =
        bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()

    private fun describe(code: Int): String = when (code) {
        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
        SpeechRecognizer.ERROR_CLIENT -> "Client-side error."
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
        SpeechRecognizer.ERROR_NETWORK -> "Network error."
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout."
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recogniser is busy."
        SpeechRecognizer.ERROR_SERVER -> "Server error."
        else -> "Speech recognition error ($code)."
    }
}
