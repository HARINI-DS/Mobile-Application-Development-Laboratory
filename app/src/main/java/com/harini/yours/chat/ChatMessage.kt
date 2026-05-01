package com.harini.yours.chat

import androidx.room.*

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatSession::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionOwnerId")]
)
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val messageId: Int = 0,
    val sessionOwnerId: Int,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)