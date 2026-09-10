package com.teamdark.ai

data class ChatMessage(
    val isUser: Boolean,
    val text: String,
    val imageUri: String? = null
)
