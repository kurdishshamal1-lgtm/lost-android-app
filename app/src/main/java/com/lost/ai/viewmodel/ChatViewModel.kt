package com.lost.ai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lost.ai.data.ChatRequest
import com.lost.ai.data.ChatMessagePayload
import com.lost.ai.data.GeminiApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class UiMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: String = "ئێستا",
    val isError: Boolean = false
)

class ChatViewModel(private val apiService: GeminiApiService) : ViewModel() {

    private val _messages = MutableStateFlow<List<UiMessage>>(emptyList())
    val messages: StateFlow<List<UiMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("ku") // "ku", "ar", "en"
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    fun setLanguage(lang: String) {
        _selectedLanguage.value = lang
    }

    fun sendMessage(prompt: String) {
        if (prompt.isBlank()) return

        val userMsg = UiMessage(role = "user", content = prompt)
        _messages.value = _messages.value + userMsg
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val payload = _messages.value.map { ChatMessagePayload(it.role, it.content) }
                val response = apiService.sendMessage(
                    ChatRequest(messages = payload, language = _selectedLanguage.value)
                )
                val assistantMsg = UiMessage(role = "assistant", content = response.reply)
                _messages.value = _messages.value + assistantMsg
            } catch (e: Exception) {
                val errorMsg = UiMessage(
                    role = "assistant",
                    content = "خەتایەک لە وەڵامدانەوە ڕوویدا: ${e.message}",
                    isError = true
                )
                _messages.value = _messages.value + errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun startNewChat() {
        _messages.value = emptyList()
    }
}