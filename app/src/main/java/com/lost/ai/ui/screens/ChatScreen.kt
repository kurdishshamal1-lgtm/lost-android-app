package com.lost.ai.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lost.ai.ui.theme.Slate800
import com.lost.ai.ui.theme.Slate900
import kotlinx.coroutines.launch

data class ChatMessageItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "ai"
    val content: String,
    val time: String = "ئێستا"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var textInput by remember { mutableStateOf("") }
    val messages = remember {
        mutableStateListOf(
            ChatMessageItem(
                sender = "ai",
                content = "سڵاو شەماڵ! من Lust AI م 👋\nیاریدەدەری کەسی و هاوڕێی ڕاستەقینەت. چۆن دەتوانم لەم داواکارییەتدا یارمەتیت بدەم؟"
            )
        )
    }

    // Speech Recognizer Launcher for Voice-to-Text
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                val recognizedText = matches[0]
                textInput = if (textInput.isBlank()) recognizedText else "$textInput $recognizedText"
            }
        }
    }

    fun startVoiceToText() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ckb-IQ")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "بدوێ... (نامەکەت بڵێ)")
            }
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "فەرمانی دەنگی لەسەر ئامێرەکەت بەردەست نییە", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendMessage() {
        val trimmed = textInput.trim()
        if (trimmed.isBlank()) return

        messages.add(ChatMessageItem(sender = "user", content = trimmed))
        textInput = ""

        // Auto Scroll to newest message
        coroutineScope.launch {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }

        // Simulate AI Response
        coroutineScope.launch {
            kotlinx.coroutines.delay(800)
            messages.add(
                ChatMessageItem(
                    sender = "ai",
                    content = "پەیامەکەت بەسەرکەوتوویی گەیشت: \"$trimmed\"\nمن وەک Lust AI بەردەوامم لە وەڵامدانەوەت!"
                )
            )
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("L", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Lust AI", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("یاریدەدەری دەنگی و دەقی - شەماڵ ئاری", fontSize = 11.sp, color = Color.LightGray)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        messages.clear()
                        messages.add(
                            ChatMessageItem(
                                sender = "ai",
                                content = "چاتێکی نوێ دەستی پێکرد. نامەکەت بنووسە..."
                            )
                        )
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "چاتی نوێ", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate900)
            )
        },
        containerColor = Slate900
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        ) {
            // Conversation Message List
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        val isUser = msg.sender == "user"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 18.dp,
                                    topEnd = 18.dp,
                                    bottomStart = if (isUser) 18.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 18.dp
                                ),
                                color = if (isUser) Color(0xFF7C3AED) else Slate800,
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = msg.content,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = msg.time,
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 10.sp,
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Fixed Bottom Input Dock Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(28.dp),
                color = Slate800,
                shadowElevation = 10.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Microphone button for speech-to-text input (Always available)
                    IconButton(
                        onClick = { startVoiceToText() },
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "تۆماری دەنگی",
                            tint = Color(0xFFA855F7)
                        )
                    }

                    // Text Input Field with requested placeholder: "نامەکەت بنووسە..."
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("نامەکەت بنووسە...", color = Color.Gray, fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        singleLine = false,
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { sendMessage() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    // Send Button
                    IconButton(
                        onClick = { sendMessage() },
                        enabled = textInput.isNotBlank(),
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                color = if (textInput.isNotBlank()) Color(0xFF7C3AED) else Color.Transparent,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "ناردنی پەیام",
                            tint = if (textInput.isNotBlank()) Color.White else Color.Gray
                        )
                    }
                }
            }
        }
    }
}
