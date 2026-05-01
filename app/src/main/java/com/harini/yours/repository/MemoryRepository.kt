package com.harini.yours.repository

import com.harini.yours.chat.ChatDao
import com.harini.yours.chat.ChatMessage
import com.harini.yours.chat.ChatSession
import com.harini.yours.data.local.Memory
import com.harini.yours.data.local.MemoryDao

class MemoryRepository(
    private val memoryDao: MemoryDao,
    private val chatDao: ChatDao
) {

    // ----------------------------
    // MEMORY FUNCTIONS
    // ----------------------------

    suspend fun insertMemory(key: String, value: String) {
        val memory = Memory(key = key.lowercase(), value = value)
        memoryDao.insertMemory(memory)
    }

    suspend fun updateMemory(key: String, newValue: String) {
        memoryDao.updateMemory(key.lowercase(), newValue)
    }

    suspend fun deleteMemory(key: String) {
        memoryDao.deleteMemory(key.lowercase())
    }

    suspend fun getMemory(key: String): Memory? {
        return memoryDao.getMemoryByKey(key.lowercase())
    }

    suspend fun getAllMemories(): List<Memory> {
        return memoryDao.getAllMemories()
    }

    // ----------------------------
    // CHAT SESSION FUNCTIONS
    // ----------------------------

    suspend fun createSession(title: String): Int {
        return chatDao.insertSession(ChatSession(title = title)).toInt()
    }

    suspend fun getAllSessions(): List<ChatSession> {
        return chatDao.getAllSessions()
    }

    suspend fun deleteSession(sessionId: Int) {
        chatDao.deleteSession(sessionId)
    }

    suspend fun renameSession(sessionId: Int, title: String) {
        chatDao.renameSession(sessionId, title)
    }

    suspend fun insertMessage(message: ChatMessage) {
        chatDao.insertMessage(message)
    }

    suspend fun getMessagesForSession(sessionId: Int): List<ChatMessage> {
        return chatDao.getMessagesForSession(sessionId)
    }

    suspend fun clearAllSessions() {
        chatDao.clearAllSessions()
    }
}