package com.harini.yours.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class Memory(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val key: String,

    val value: String,

    val timestamp: Long = System.currentTimeMillis()
)