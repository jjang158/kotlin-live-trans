package com.livetrans.application

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log


/**
 * STT 구현체
 * @param context
 * @return Unit
 * */
class LiveSpeechRecognizer(
    val context: Context
) {
    private lateinit var speechRecognizer: SpeechRecognizer
    private var intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

    init {
        createSpeechRecognizer(context)
    }

    fun createSpeechRecognizer(context: Context){
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    }

    /**
     * STT 서비스 시작
     * @param onResult
     * @param language
     * @return Unit
     * */
    fun setListening(
        onResult: (String) -> Unit,
        language: String
    ) {
        intent.apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            // 완전히 조용한 상태로 인식 종료까지 기다리는 시간
            putExtra("android.speech.extra.SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS", 100000)
            // 어느 정도 조용할 때 종료까지 기다리는 시간
            putExtra("android.speech.extra.SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS", 100000)
            // 최소 음성 인식 유지 시간
            putExtra("android.speech.extra.SPEECH_INPUT_MINIMUM_LENGTH_MILLIS", 500000)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("STT", "onReadyForSpeech")
            }

            override fun onBeginningOfSpeech() {
                Log.d("STT", "onBeginningOfSpeech")
            }

            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                Log.d("STT", "onEndOfSpeech")
                startListening()
            }

            override fun onError(error: Int) {
                Log.e("STT", "SpeechRecognizer error: $error")
                startListening()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                Log.d("STT", "onResults start!!")
                matches?.let {
                    onResult(it[0])
                    Log.d("STT", "Result: ${it.firstOrNull()}")
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

    }

    /**
     * STT 서비스 시작
     * */
    fun startListening(){
        Log.d("STT", "startListening start!!")
        speechRecognizer.startListening(intent)
    }

    /**
     * STT 서비스 종료
     * */
    fun destroy(){
        speechRecognizer.let {
            Log.d("STT", "SpeechRecognizer destroyed")
            it.destroy()
        }
    }
}
