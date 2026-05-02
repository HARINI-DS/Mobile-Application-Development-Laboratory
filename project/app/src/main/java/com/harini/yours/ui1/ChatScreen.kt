package com.harini.yours.ui1

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.*
import com.harini.yours.chat.ChatSession
import com.harini.yours.ui.theme.*
import com.harini.yours.ui1.components.MessageBubble
import com.harini.yours.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
//  ROOT SCREEN
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel) {

    val drawerState  = rememberDrawerState(DrawerValue.Closed)
    val scope        = rememberCoroutineScope()

    val messages  by viewModel.messages.collectAsState()
    val sessions  by viewModel.sessions.collectAsState()

    var input     by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to latest message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    ModalNavigationDrawer(
        drawerState   = drawerState,
        scrimColor    = Color(0xCC000000),
        drawerContent = {
            YoursDrawer(
                sessions     = sessions,
                onNewChat    = {
                    viewModel.createNewSession()
                    scope.launch { drawerState.close() }
                },
                onSelectSession = { sessionId ->
                    viewModel.selectSession(sessionId)
                    scope.launch { drawerState.close() }
                },
                onDeleteSession = { sessionId ->
                    viewModel.deleteSession(sessionId)
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            containerColor = Black800,
            topBar = {
                YoursTopBar(onMenuClick = { scope.launch { drawerState.open() } })
            }
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Black800)
                    .imePadding()
            ) {

                // ── Message list ──────────────────────────────────────────
                LazyColumn(
                    state    = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    if (messages.isEmpty()) {
                        item { EmptyState() }
                    } else {
                        items(messages) { message ->
                            MessageBubble(text = message.text, isUser = message.isUser)
                        }
                    }
                }

                // ── Divider ───────────────────────────────────────────────
                HorizontalDivider(
                    color     = Black500,
                    thickness = 0.5.dp
                )

                // ── Input bar ─────────────────────────────────────────────
                YoursInputBar(
                    input    = input,
                    onChange = { input = it },
                    onSend   = {
                        val msg = input.trim()
                        if (msg.isNotEmpty()) {
                            viewModel.sendMessage(msg)
                            input = ""
                        }
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  DRAWER
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun YoursDrawer(
    sessions: List<ChatSession>,
    onNewChat: () -> Unit,
    onSelectSession: (Int) -> Unit,
    onDeleteSession: (Int) -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = Black700,
        drawerContentColor   = TextPrimary
    ) {
        // ── Header ───────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(colors = listOf(Black800, Black700))
                )
                .padding(24.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Crimson),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = "Y",
                            color      = Color.White,
                            fontSize   = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text          = "YOURS",
                            color         = TextPrimary,
                            fontSize      = 18.sp,
                            fontWeight    = FontWeight.Black,
                            letterSpacing = 3.sp
                        )
                        Text(
                            text          = "Private AI Memory",
                            color         = TextSecondary,
                            fontSize      = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Crimson, Color.Transparent)
                            )
                        )
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── New Chat Button ───────────────────────────────────────────────
        Button(
            onClick  = onNewChat,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = Crimson,
                contentColor   = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector        = Icons.Default.Add,
                contentDescription = "New Chat",
                modifier           = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text       = "+ New Chat",
                fontWeight = FontWeight.SemiBold,
                fontSize   = 14.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Sessions Label ────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text          = "CHAT HISTORY",
                color         = Crimson,
                fontSize      = 10.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(CrimsonDark, Color.Transparent)
                        )
                    )
            )
        }

        Spacer(Modifier.height(8.dp))

        // ── Session List ──────────────────────────────────────────────────
        LazyColumn(
            modifier       = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) {
            items(sessions) { session ->
                SessionItem(
                    session   = session,
                    onClick   = { onSelectSession(session.sessionId) },
                    onDelete  = { onDeleteSession(session.sessionId) }
                )
            }
        }

        // ── Footer ────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text     = "v2.0  •  Yours AI",
                color    = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun SessionItem(
    session: ChatSession,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(CrimsonDark)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text       = session.title,
            color      = TextSecondary,
            fontSize   = 14.sp,
            fontWeight = FontWeight.Normal,
            maxLines   = 1,
            modifier   = Modifier.weight(1f)
        )
        IconButton(
            onClick  = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector        = Icons.Default.Delete,
                contentDescription = "Delete session",
                tint               = TextSecondary,
                modifier           = Modifier.size(16.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TOP BAR
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YoursTopBar(onMenuClick: () -> Unit) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(22.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Crimson)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text          = "YOURS",
                    color         = TextPrimary,
                    fontSize      = 20.sp,
                    fontWeight    = FontWeight.Black,
                    letterSpacing = 3.sp
                )
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CrimsonDark)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text          = "AI",
                        color         = Crimson,
                        fontSize      = 9.sp,
                        fontWeight    = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector        = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint               = TextPrimary
                )
            }
        },
        actions = {
            PulsingDot()
            Spacer(Modifier.width(16.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor    = Black700,
            titleContentColor = TextPrimary
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  PULSING DOT
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PulsingDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue  = 0.8f,
        targetValue   = 1.3f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue  = 0.5f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )
    Box(
        modifier = Modifier
            .scale(scale)
            .alpha(alpha)
            .size(8.dp)
            .clip(CircleShape)
            .background(Crimson)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  INPUT BAR
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun YoursInputBar(
    input:    String,
    onChange: (String) -> Unit,
    onSend:   () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Black700)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value         = input,
            onValueChange = onChange,
            modifier      = Modifier.weight(1f),
            placeholder   = {
                Text(
                    text  = "Message Yours…",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            singleLine    = false,
            maxLines      = 4,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
            shape  = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor   = Black500,
                unfocusedContainerColor = Black500,
                focusedTextColor        = TextPrimary,
                unfocusedTextColor      = TextPrimary,
                cursorColor             = Crimson,
                focusedIndicatorColor   = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(Modifier.width(8.dp))

        IconButton(
            onClick  = onSend,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (input.isNotBlank()) Crimson else Black500
                )
        ) {
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint               = if (input.isNotBlank()) Color.White else TextSecondary,
                modifier           = Modifier.size(20.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  EMPTY STATE
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyState() {
    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(CrimsonBright, CrimsonDark)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = "Y",
                    color      = Color.White,
                    fontSize   = 42.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text       = "Hello, I'm Yours.",
                color      = TextPrimary,
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text     = "Tell me something to remember.",
                color    = TextSecondary,
                fontSize = 14.sp
            )
        }
    }
}