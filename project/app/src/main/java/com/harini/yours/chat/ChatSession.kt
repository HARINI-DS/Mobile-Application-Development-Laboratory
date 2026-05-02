package com.harini.yours.chat

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSession(
    @PrimaryKey(autoGenerate = true)
    val sessionId: Int = 0,
    val title: String,
    val createdAt: Long = System.currentTimeMillis()
)