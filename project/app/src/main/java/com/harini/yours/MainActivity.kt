package com.harini.yours

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.harini.yours.data.local.AppDatabase
import com.harini.yours.repository.MemoryRepository
import com.harini.yours.ui.theme.YoursTheme
import com.harini.yours.ui1.ChatScreen
import com.harini.yours.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Let the UI draw behind system bars (edge-to-edge)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val database   = AppDatabase.getDatabase(this)
        val repository = MemoryRepository(
            database.memoryDao(),
            database.chatDao()
        )
        val viewModel  = ChatViewModel(repository)

        setContent {
            YoursTheme {
                ChatScreen(viewModel)
            }
        }
    }
}