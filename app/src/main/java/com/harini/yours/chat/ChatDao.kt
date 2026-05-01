package com.harini.yours.chat

import androidx.room.*

@Dao
interface ChatDao {

    @Insert
    suspend fun insertSession(session: ChatSession): Long

    @Query("SELECT * FROM chat_sessions ORDER BY createdAt DESC")
    suspend fun getAllSessions(): List<ChatSession>

    @Insert
    suspend fun insertMessage(message: ChatMessage)

    @Query("""
        SELECT * FROM chat_messages 
        WHERE sessionOwnerId = :sessionId 
        ORDER BY timestamp ASC
    """)
    suspend fun getMessagesForSession(sessionId: Int): List<ChatMessage>

    @Query("DELETE FROM chat_sessions")
    suspend fun clearAllSessions()

    @Query("DELETE FROM chat_sessions WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: Int)

    @Query("UPDATE chat_sessions SET title = :title WHERE sessionId = :sessionId")
    suspend fun renameSession(sessionId: Int, title: String)
}