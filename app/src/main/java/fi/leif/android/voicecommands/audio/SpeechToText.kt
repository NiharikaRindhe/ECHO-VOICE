package fi.leif.android.voicecommands.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

interface SpeechToTextListener {
    fun onResult(text: String)
    fun onPartialResult(text: String)
    fun onRmsChanged(rmsDb: Float)
    fun onError()
}

class SpeechToText(val listener: SpeechToTextListener) {

    var sp: SpeechRecognizer? = null

    fun stop() {
        sp?.cancel()
    }

    fun start(context: Context, extraLang: String) {
        sp = SpeechRecognizer.createSpeechRecognizer(context)

        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

        speechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            extraLang
        )

        sp?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(bundle: Bundle?) {
                val data = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if(!data.isNullOrEmpty()) {
                    listener.onResult(data[0])
                }
            }

            override fun onPartialResults(bundle: Bundle?) {
                val data = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if(!data.isNullOrEmpty()) {
                    listener.onPartialResult(data[0])
                }
            }

            override fun onRmsChanged(rmsDb: Float) {
                // Filter out negative values (noise?)
                listener.onRmsChanged(if(rmsDb < 0) 0f else rmsDb)
            }

            override fun onReadyForSpeech(p0: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onBufferReceived(p0: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(p0: Int) {
                listener.onError()
            }
            override fun onEvent(p0: Int, p1: Bundle?) {}
        })

        sp?.startListening(speechRecognizerIntent);
    }
}