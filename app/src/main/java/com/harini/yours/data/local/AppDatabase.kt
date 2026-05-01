package com.harini.yours.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.harini.yours.chat.ChatDao
import com.harini.yours.chat.ChatMessage
import com.harini.yours.chat.ChatSession

@Database(
    entities = [
        Memory::class,
        ChatSession::class,
        ChatMessage::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun memoryDao(): MemoryDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yours_database"
                )
                    .fallbackToDestructiveMigration() // ⚠ During development only
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}