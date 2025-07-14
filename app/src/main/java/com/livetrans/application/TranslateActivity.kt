package com.livetrans.application

import android.os.Bundle
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
import androidx.compose.ui.tooling.preview.Preview
import com.livetrans.application.ui.theme.LiveTransTheme


class TranslateActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LiveTransTheme {
                TranslationScreen()
            }
        }
    }
}

@Composable
@Preview
fun TranslationPreView(){
    LiveTransTheme {
        TranslationScreen()
    }
}

@Composable
fun TranslationScreen(
    onBackClick: () -> Unit = {},
    onPauseClick: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf("trans") }
    var contents by remember { mutableStateOf("") }

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
                    selected = selectedTab == "org",
                    onClick = {
                        selectedTab = "org"
                        contents = "Hello!"
                              },
                    modifier = Modifier.weight(1f)
                )
                ToggleOption(
                    text = "Translated Text",
                    selected = selectedTab == "trans",
                    onClick = {
                        selectedTab = "trans"
                        contents = "안녕하세요!"
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
                Text("Pause", color = Color(0xFF121416), fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
                Text("Save", color = Color(0xFF121416), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

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



