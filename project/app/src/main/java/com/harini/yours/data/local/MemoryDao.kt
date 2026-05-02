package com.harini.yours.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MemoryDao {

    @Insert
    suspend fun insertMemory(memory: Memory)

    @Query("SELECT * FROM memories WHERE `key` = :key LIMIT 1")
    suspend fun getMemoryByKey(key: String): Memory?

    @Query("SELECT * FROM memories")
    suspend fun getAllMemories(): List<Memory>

    @Query("UPDATE memories SET value = :newValue WHERE `key` = :key")
    suspend fun updateMemory(key: String, newValue: String)

    @Query("DELETE FROM memories WHERE `key` = :key")
    suspend fun deleteMemory(key: String)
}