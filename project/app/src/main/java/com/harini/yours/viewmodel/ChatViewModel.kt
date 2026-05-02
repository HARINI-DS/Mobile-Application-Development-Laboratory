package com.harini.yours.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harini.yours.chat.ChatMessage
import com.harini.yours.chat.ChatSession
import com.harini.yours.repository.MemoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: MemoryRepository
) : ViewModel() {

    private var currentSessionId: Int? = null

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _sessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val sessions: StateFlow<List<ChatSession>> = _sessions

    init {
        loadSessions()
        createNewSession()
    }

    // -------------------------------------------------------------------------
    // SESSION MANAGEMENT
    // -------------------------------------------------------------------------

    fun createNewSession() {
        viewModelScope.launch {
            val newSessionId = repository.createSession("New Chat")
            currentSessionId = newSessionId
            _messages.value = emptyList()
            loadSessions()
        }
    }

    fun loadSessions() {
        viewModelScope.launch {
            _sessions.value = repository.getAllSessions()
        }
    }

    fun selectSession(sessionId: Int) {
        viewModelScope.launch {
            currentSessionId = sessionId
            _messages.value = repository.getMessagesForSession(sessionId)
        }
    }

    fun deleteSession(sessionId: Int) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            // If deleted session was active, open a fresh one
            if (currentSessionId == sessionId) {
                createNewSession()
            } else {
                loadSessions()
            }
        }
    }

    // -------------------------------------------------------------------------
    // MESSAGE HANDLING
    // -------------------------------------------------------------------------

    fun sendMessage(userText: String) {
        val sessionId = currentSessionId ?: return

        viewModelScope.launch {
            // Save user message
            repository.insertMessage(
                ChatMessage(sessionOwnerId = sessionId, text = userText, isUser = true)
            )

            // Generate response
            val response = generateResponse(userText.trim())

            // Save bot response
            repository.insertMessage(
                ChatMessage(sessionOwnerId = sessionId, text = response, isUser = false)
            )

            // Auto-name session from the very first user message
            val allMessages = repository.getMessagesForSession(sessionId)
            if (allMessages.count { it.isUser } == 1) {
                val autoTitle = deriveSessionTitle(userText.trim())
                repository.renameSession(sessionId, autoTitle)
            }

            // Reload UI
            _messages.value = repository.getMessagesForSession(sessionId)
            loadSessions()
        }
    }

    // -------------------------------------------------------------------------
    // AUTO SESSION TITLE  —  derives a short meaningful title from first message
    // -------------------------------------------------------------------------

    private fun deriveSessionTitle(firstMessage: String): String {
        val lower = firstMessage.lowercase()

        if (isGreeting(lower)) return "👋 New Conversation"

        // "remember that / my X is Y"  →  "📌 X"
        val rememberRegex = Regex("""remember\s+(?:that\s+)?(.+?)\s+(?:is|are|was)\s+.+""", RegexOption.IGNORE_CASE)
        val myIsRegex     = Regex("""(?:my\s+)?(.+?)\s+(?:is|are|was|=)\s+.+""",           RegexOption.IGNORE_CASE)

        val keyMatch = rememberRegex.find(lower) ?: myIsRegex.find(lower)
        if (keyMatch != null) {
            val key = keyMatch.groupValues[1]
                .removePrefix("my ").trim()
                .replaceFirstChar { it.uppercase() }
            if (key.length in 2..30) return "📌 $key"
        }

        // Question → "❓ question text"
        if (lower.contains("?") || lower.startsWith("what") || lower.startsWith("tell me")) {
            val trimmed = firstMessage.trimEnd('?').trim()
            return "❓ ${trimmed.take(28)}${if (trimmed.length > 28) "…" else ""}"
        }

        // Generic fallback — first 30 chars
        val trimmed = firstMessage.trim()
        return trimmed.take(30) + if (trimmed.length > 30) "…" else ""
    }

    // -------------------------------------------------------------------------
    // CORE RESPONSE LOGIC
    // -------------------------------------------------------------------------

    private suspend fun generateResponse(input: String): String {
        val lower = input.lowercase()

        // Greeting
        if (isGreeting(lower)) {
            return "Hey! 👋 I'm Yours — your private memory assistant.\n\n" +
                    "You can tell me things like:\n" +
                    "• \"My Aadhaar number is 1234 5678 9012\"\n" +
                    "• \"Rahul's birthday is March 15\"\n" +
                    "• \"My blood group is O+\"\n\n" +
                    "And later, just ask me back! 😊\n\n" +
                    "You can also:\n" +
                    "• \"Update my email to new@gmail.com\"  →  edit saved info\n" +
                    "• \"Delete my email\"  →  remove saved info\n" +
                    "• \"Show my memories\"  →  see everything I know"
        }

        // UPDATE: "update/change/correct my X to Y"
        val updateResult = tryUpdateMemory(input, lower)
        if (updateResult != null) return updateResult

        // DELETE: "delete/forget/remove my X"
        val deleteResult = tryDeleteMemory(lower)
        if (deleteResult != null) return deleteResult

        // SAVE: "my X is Y"
        val saveResult = trySaveMemory(input, lower)
        if (saveResult != null) return saveResult

        // RETRIEVE: "what is my X?"
        val retrieveResult = tryRetrieveMemory(lower)
        if (retrieveResult != null) return retrieveResult

        // LIST ALL
        if (lower.contains("what do you know") || lower.contains("what have i told") ||
            lower.contains("show my memories") || lower.contains("list everything") ||
            lower.contains("show everything")
        ) return listAllMemories()

        // Block general knowledge
        if (isGeneralKnowledgeQuestion(lower)) {
            return "I'm only here to remember *your* personal information — not to answer general knowledge questions. 🙂\n\n" +
                    "Try telling me something about yourself, like:\n" +
                    "\"My passport number is XXXXXXX\""
        }

        // Fallback
        return "I didn't quite understand that. You can:\n" +
                "• Tell me something: \"My email is x@y.com\"\n" +
                "• Ask me back: \"What is my email?\"\n" +
                "• Edit info: \"Update my email to new@y.com\"\n" +
                "• Delete info: \"Delete my email\"\n" +
                "• Say \"show my memories\" to see everything"
    }

    // -------------------------------------------------------------------------
    // UPDATE  —  "update/change/correct/edit/fix my X to Y"
    // -------------------------------------------------------------------------

    private suspend fun tryUpdateMemory(raw: String, lower: String): String? {
        val pattern = Regex(
            """(?:update|change|correct|edit|fix)\s+(?:my\s+)?(.+?)\s+to\s+(.+)""",
            RegexOption.IGNORE_CASE
        )
        val m = pattern.find(lower) ?: return null
        val key = m.groupValues[1].trim()
        val newVal = m.groupValues[2].trim()
        if (key.isBlank() || newVal.isBlank()) return null

        // Preserve original casing from raw input
        val rawPattern = Regex(
            """(?:update|change|correct|edit|fix)\s+(?:my\s+)?${Regex.escape(key)}\s+to\s+(.+)""",
            RegexOption.IGNORE_CASE
        )
        val finalValue = rawPattern.find(raw)?.groupValues?.getOrNull(1)?.trim() ?: newVal

        val existing = repository.getMemory(key)
        return if (existing != null) {
            repository.updateMemory(key, finalValue)
            "✅ Updated!\n\n🔑 **${capitalise(key)}** → $finalValue"
        } else {
            // Doesn't exist yet — save it fresh
            repository.insertMemory(key, finalValue)
            "✅ Saved!\n\n🔑 **${capitalise(key)}** → $finalValue\n\n_(I didn't have this before, so I saved it fresh.)_"
        }
    }

    // -------------------------------------------------------------------------
    // DELETE  —  "delete/forget/remove/clear my X"
    // -------------------------------------------------------------------------

    private suspend fun tryDeleteMemory(lower: String): String? {
        val pattern = Regex(
            """(?:delete|forget|remove|clear)\s+(?:my\s+)?(.+)""",
            RegexOption.IGNORE_CASE
        )
        val m = pattern.find(lower) ?: return null
        val key = m.groupValues[1].trim().trimEnd('.')
        if (key.isBlank()) return null

        // Don't intercept UI-level delete commands
        if (key.contains("chat") || key.contains("session") || key.contains("history")) return null

        val existing = repository.getMemory(key)
        if (existing != null) {
            repository.deleteMemory(key)
            return "🗑️ Done! I've forgotten your **${capitalise(key)}**."
        }

        // Fuzzy match
        val all = repository.getAllMemories()
        val fuzzy = all.firstOrNull { it.key.contains(key) || key.contains(it.key) }
        return if (fuzzy != null) {
            repository.deleteMemory(fuzzy.key)
            "🗑️ Done! I've forgotten your **${capitalise(fuzzy.key)}**."
        } else {
            "Hmm, I don't have anything saved for \"$key\" to delete. 🤔"
        }
    }

    // -------------------------------------------------------------------------
    // SAVE  —  "my X is Y"
    // -------------------------------------------------------------------------

    private suspend fun trySaveMemory(raw: String, lower: String): String? {
        val myIsRegex     = Regex("""(?:my\s+)?(.+?)\s+(?:is|are|was|=)\s+(.+)""", RegexOption.IGNORE_CASE)
        val rememberRegex = Regex("""remember\s+(?:that\s+)?(.+?)\s+(?:is|are|was)\s+(.+)""", RegexOption.IGNORE_CASE)
        val saveAsRegex   = Regex("""(?:save|store)\s+(.+?)\s+as\s+(.+)""",       RegexOption.IGNORE_CASE)

        val (key, value) = when {
            lower.startsWith("remember") -> {
                val m = rememberRegex.find(lower) ?: return null
                Pair(m.groupValues[1].trim(), m.groupValues[2].trim())
            }
            lower.startsWith("save") || lower.startsWith("store") -> {
                val m = saveAsRegex.find(lower) ?: return null
                Pair(m.groupValues[1].trim(), m.groupValues[2].trim())
            }
            lower.startsWith("my ") || lower.contains(" is ") || lower.contains(" are ") -> {
                val m = myIsRegex.find(lower) ?: return null
                val k = m.groupValues[1].trim().removePrefix("my ").trim()
                val v = m.groupValues[2].trim()
                if (k.length < 2 || v.length < 1) return null
                Pair(k, v)
            }
            else -> return null
        }

        if (lower.contains("?") && !lower.startsWith("my")) return null
        if (key.isBlank() || value.isBlank()) return null

        // Preserve original casing of value from raw input
        val rawMatch = Regex(
            """(?:my\s+)?${Regex.escape(key)}\s+(?:is|are|was|=)\s+(.+)""",
            RegexOption.IGNORE_CASE
        ).find(raw)
        val finalValue = rawMatch?.groupValues?.getOrNull(1)?.trim() ?: value

        // Auto-update if key already saved
        val existing = repository.getMemory(key)
        return if (existing != null) {
            repository.updateMemory(key, finalValue)
            "✅ I already had your **${capitalise(key)}** saved. I've updated it:\n\n" +
                    "🔑 **${capitalise(key)}** → $finalValue"
        } else {
            repository.insertMemory(key, finalValue)
            "Got it! ✅ I've saved:\n\n🔑 **${capitalise(key)}** → $finalValue\n\n" +
                    "Just ask me anytime and I'll remember this for you."
        }
    }

    // -------------------------------------------------------------------------
    // RETRIEVE  —  "what is my X?"
    // -------------------------------------------------------------------------

    private suspend fun tryRetrieveMemory(lower: String): String? {
        val patterns = listOf(
            Regex("""what(?:'s| is| was) (?:my |the )?(.+?)\??$""",               RegexOption.IGNORE_CASE),
            Regex("""tell me (?:my |the )?(.+)""",                                 RegexOption.IGNORE_CASE),
            Regex("""(?:remind me of |remind me |recall |what about )(?:my )?(.+?)\??$""", RegexOption.IGNORE_CASE),
            Regex("""do you (?:know|remember) (?:my )?(.+?)\??$""",               RegexOption.IGNORE_CASE),
            Regex("""(?:my) (.+)\?$""",                                            RegexOption.IGNORE_CASE)
        )

        var queryKey: String? = null
        for (p in patterns) {
            val m = p.find(lower)
            if (m != null) {
                queryKey = m.groupValues[1].trim().removeSuffix("?").trim().removePrefix("my ").trim()
                break
            }
        }
        if (queryKey == null || queryKey.isBlank()) return null

        val exact = repository.getMemory(queryKey)
        if (exact != null) return "Here you go! 📋\n\n🔑 **${capitalise(exact.key)}** → ${exact.value}"

        val all = repository.getAllMemories()
        val fuzzy = all.firstOrNull { it.key.contains(queryKey) || queryKey.contains(it.key) }
        if (fuzzy != null) return "Here you go! 📋\n\n🔑 **${capitalise(fuzzy.key)}** → ${fuzzy.value}"

        return "Hmm, I don't have anything saved for \"$queryKey\" yet. 🤔\n\n" +
                "You can tell me: \"My $queryKey is ...\""
    }

    // -------------------------------------------------------------------------
    // LIST ALL
    // -------------------------------------------------------------------------

    private suspend fun listAllMemories(): String {
        val all = repository.getAllMemories()
        if (all.isEmpty()) return "I don't have anything saved yet!\n\nStart by saying:\n\"My blood group is O+\""
        val lines = all.joinToString("\n") { "🔑 **${capitalise(it.key)}** → ${it.value}" }
        return "Here's everything I know about you:\n\n$lines"
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private fun isGreeting(lower: String): Boolean {
        val greetings = listOf("hi", "hello", "hey", "hii", "helo", "sup", "yo",
            "good morning", "good evening", "good afternoon", "what's up", "howdy")
        return greetings.any { lower == it || lower.startsWith("$it ") || lower.startsWith("$it,") }
    }

    private fun isGeneralKnowledgeQuestion(lower: String): Boolean {
        val signals = listOf(
            "who is", "who was", "who are", "what is the", "what are the",
            "when did", "when was", "where is", "where was", "capital of",
            "president of", "prime minister", "chief minister", "cm of",
            "population of", "history of", "define ", "meaning of",
            "how does", "explain ", "tell me about ", "what happened"
        )
        if (lower.contains(" my ") || lower.startsWith("my ")) return false
        return signals.any { lower.contains(it) }
    }

    private fun capitalise(s: String) = s.replaceFirstChar { it.uppercase() }
}