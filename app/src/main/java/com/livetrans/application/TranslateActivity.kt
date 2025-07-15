package com.livetrans.application

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.livetrans.application.ui.theme.LiveTransTheme


class TranslateActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 음성인식 권한 확인
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted){
                Toast.makeText(this, "음성 인식을 위해 권한 허용이 필요합니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }.launch(Manifest.permission.RECORD_AUDIO)

        // 화면 생성
        val originLang = intent.getStringExtra("origin_language") ?: "ko-KR"
        val targetLang = intent.getStringExtra("target_language") ?: "en-US"
        setContent {
            LiveTransTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    TobAppBar(onBackClick = { finish() })

                    TranslationScreen(
                        originLang = originLang,
                        targetLang = targetLang,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

@Composable
fun TranslationScreen(
    originLang: String,
    targetLang: String,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf("trans") }
    var originText by remember { mutableStateOf("") }
    var transText by remember { mutableStateOf("") }
    var controlBtnText by remember { mutableStateOf("Start") }
    var isRecording by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val contents by remember(originText, transText, selectedTab) {
        mutableStateOf(
            if (selectedTab == "org") originText
            else transText
        )
    }
    // STT + 번역기
    var liveSpeechRecognizer: LiveSpeechRecognizer? by remember { mutableStateOf(null) }

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
                selected = selectedTab == "org",
                onClick = {
                    selectedTab = "org"
                },
                modifier = Modifier.weight(1f)
            )
            ToggleOption(
                text = "Translated Text",
                selected = selectedTab == "trans",
                onClick = {
                    selectedTab = "trans"
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

    Spacer(modifier = modifier)

    // Bottom Buttons
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = {
                if (!isRecording) {
                    liveSpeechRecognizer ?: makeLiveSpeechRecognizer(
                        context = context,
                        originLang = originLang,
                        targetLang = targetLang,
                        sttCallback = { result -> originText += result+"\n" },
                        transCallback = { result -> transText += result+"\n" }).also {
                        liveSpeechRecognizer = it
                    }
                    liveSpeechRecognizer?.startListening()
                    controlBtnText = "Stop"
                    isRecording = true
                } else {
                    liveSpeechRecognizer?.destroy()
                    liveSpeechRecognizer = null
                    controlBtnText = "Start"
                    isRecording = false
                }
            },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F2F4)),
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
        ) {
            Text(
                text = controlBtnText,
                color = Color(0xFF121416),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Button(
            onClick = {
                downloadTextFile(
                    context = context,
                    fileName = "transcription_result.txt",
                    content = contents
                )
            },
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
}

@Composable
fun TobAppBar(onBackClick: () -> Unit) {
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
}


/**
 * STT & 번역 처리
 * @param originLang
 * @param targetLang
 * @param sttCallback
 * @param transCallback
 * */
private fun makeLiveSpeechRecognizer(
    context: Context,
    originLang: String,
    targetLang: String,
    sttCallback: (String) -> Unit,
    transCallback: (String) -> Unit,
): LiveSpeechRecognizer {
    // 1. STT & 번역 처리 세팅
    // STT 처리 호출
    val liveSpeechRecognizer = LiveSpeechRecognizer(context)

    val options = TranslatorOptions.Builder()
        .setSourceLanguage(
            originLang.substring(0, 2)
        )
        .setTargetLanguage(
            targetLang.substring(0, 2)
        )
        .build()
    val translator = Translation.getClient(options)

    // 번역 성공시 callback 처리
    val transSucCallback = { result: String ->
        translator.translate(result)
            .addOnSuccessListener { translatedResult ->
                transCallback(translatedResult)
            }
            .addOnFailureListener { e ->
                Log.e("Translation", "Translation failed: ${e.message}")
            }
    }

    // 2. STT + 번역 처리 실행
    liveSpeechRecognizer.setListening(
        language = originLang,
        onResult = { result ->
            sttCallback(result)

            val conditions = DownloadConditions.Builder().build()
            translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener {
                    transSucCallback(result)
                }
                .addOnFailureListener { e ->
                    Log.e("Translation", "Translation Listener Error: ${e.message}")
                }
        })
    return liveSpeechRecognizer
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
    modifier: Modifier = Modifier,
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
            .height(40.dp),
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


