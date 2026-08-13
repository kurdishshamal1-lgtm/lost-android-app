package com.lost.ai.data

import retrofit2.http.Body
import retrofit2.http.POST

data class ChatMessagePayload(
    val role: String,
    val content: String
)

data class ImageAttachmentPayload(
    val base64: String,
    val mimeType: String
)

data class ChatRequest(
    val messages: List<ChatMessagePayload>,
    val language: String = "ku",
    val image: ImageAttachmentPayload? = null
)

data class ChatResponse(
    val reply: String
)

interface GeminiApiService {
    @POST("api/chat")
    suspend fun sendMessage(@Body request: ChatRequest): ChatResponse
}