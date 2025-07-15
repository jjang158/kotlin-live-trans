package com.livetrans.application

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.livetrans.application.ui.theme.LiveTransTheme


class TranslateActivity : ComponentActivity() {
    private lateinit var speechRecognizer: SpeechRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LiveTransTheme {
                val originLang = intent.getStringExtra("origin_language") ?: "ko-KR"
                val targetLang = intent.getStringExtra("target_language") ?: "en-US"
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
                TranslationScreen(
                    originLang = originLang,
                    targetLang = targetLang,
                    speechRecognizer = speechRecognizer,
                    onBackClick = { finish() }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.let {
            Log.d("STT", "SpeechRecognizer destroyed")
            it.destroy()
        }
    }
}

@Composable
@Preview
fun TranslationPreView() {
    LiveTransTheme {
//        TranslationScreen()
    }
}

@Composable
fun TranslationScreen(
    originLang: String,
    targetLang: String,
    speechRecognizer: SpeechRecognizer,
    onBackClick: () -> Unit = {},
    onPauseClick: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var selectedTab = remember { mutableStateOf("trans") }
    var originText by remember { mutableStateOf("") }
    var transText by remember { mutableStateOf("") }
    val contents by remember(originText, transText, selectedTab) {
        mutableStateOf(
            if (selectedTab.value == "org") originText
            else transText
        )
    }

    val translator = remember {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.fromLanguageTag(originLang) ?: TranslateLanguage.KOREAN)
            .setTargetLanguage(TranslateLanguage.fromLanguageTag(targetLang) ?: TranslateLanguage.ENGLISH)
            .build()
        Translation.getClient(options)
    }

    // STT 자동 실행을 위한 권한 확인
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                1
            )
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                startListening(
                    context = context,
                    speechRecognizer = speechRecognizer,
                    language = originLang,
                    onResult = { result ->
                        originText += result

                        val conditions = DownloadConditions.Builder().build()
                        translator.downloadModelIfNeeded(conditions)
                            .addOnSuccessListener {
                                translator.translate(result)
                                    .addOnSuccessListener { translatedResult ->
                                        transText += translatedResult
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Translation", "Translation failed: ${e.message}")
                                    }
                            }
                            .addOnFailureListener { e ->
                                Log.e("Translation", "Translation Listener Error: ${e.message}")
                            }
                    })
            }, 500)
        }
    }

    DrawTransScreen(selectedTab, contents, onBackClick, onPauseClick, onSaveClick)
}

/**
* 실시간번역 화면
* @param selectedTab
* @param contents
* @param onBackClick
* @param onPauseClick
* @param onSaveClick
* */
@Composable
fun DrawTransScreen(
    selectedTab: MutableState<String>,
    contents: String,
    onBackClick: () -> Unit = {},
    onPauseClick: () -> Unit = {},
    onSaveClick: () -> Unit = {}
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 12.dp, 16.dp, 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onBackClick() }
                    .padding(end = 8.dp),
                tint = Color(0xFF121416)
            )
            Text(
                text = "Live Translation",
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 24.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF121416),
                textAlign = TextAlign.Center
            )
        }

        // Toggle Buttons (Original / Translated)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .height(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F2F4)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToggleOption(
                    text = "Original Text",
                    selected = selectedTab.value == "org",
                    onClick = {
                        selectedTab.value = "org"
                    },
                    modifier = Modifier.weight(1f)
                )
                ToggleOption(
                    text = "Translated Text",
                    selected = selectedTab.value == "trans",
                    onClick = {
                        selectedTab.value = "trans"
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Transcription Result
        Text(
            text = contents,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 16.sp,
            color = Color(0xFF121416)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Bottom Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { onPauseClick() },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F2F4)),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
            ) {
                Text(
                    "Pause",
                    color = Color(0xFF121416),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = { onSaveClick() },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDCE8F3)),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
            ) {
                Text(
                    "Save",
                    color = Color(0xFF121416),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

/**
* 언어 선택 토글 (원어/번역)
* @param text
* @param selected
* @param onClick
* @param modifier
* @return Unit
* */
@Composable
fun ToggleOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) Color.White else Color.Transparent
    val textColor = if (selected) Color(0xFF121416) else Color(0xFF6A7681)
    val shadow = if (selected) 4.dp else 0.dp

    Box(
        modifier = modifier
            .padding(3.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .shadow(elevation = shadow, shape = CircleShape)
            .clickable { onClick() }
            .height(40.dp), // 명시적으로 높이 지정
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

/**
* STT 구현체
* @param context
* @param speechRecognizer
* @param onResult
* @param language
* @return Unit
* */
fun startListening(
    context: Context,
    speechRecognizer: SpeechRecognizer,
    onResult: (String) -> Unit,
    language: String
) {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
        // 완전히 조용한 상태로 인식 종료까지 기다리는 시간
        putExtra("android.speech.extra.SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS", 100000)
        // 어느 정도 조용할 때 종료까지 기다리는 시간
        putExtra("android.speech.extra.SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS", 100000)
        // 최소 음성 인식 유지 시간
        putExtra("android.speech.extra.SPEECH_INPUT_MINIMUM_LENGTH_MILLIS", 500000)
    }

    Log.d("STT", "startListening start!! $language")

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
            Handler(Looper.getMainLooper()).postDelayed({
                speechRecognizer.startListening(intent)
            }, 1000)
        }

        override fun onError(error: Int) {
            Log.e("STT", "SpeechRecognizer error: $error")
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

    speechRecognizer.startListening(intent)
}


