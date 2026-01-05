package com.pulseup.app.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.pulseup.app.ui.components.*
import com.pulseup.app.ui.theme.*
import com.pulseup.app.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit = {},
    onNavigateToBMI: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.profileState.collectAsState()
    val user = state.user
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showChatBot by remember { mutableStateOf(false) }

    val chatListState = rememberLazyListState()

    LaunchedEffect(key1 = true) {
        viewModel.refresh()
    }

    // Auto-scroll ke pesan terbaru (index 0 dalam mode reverse)
    LaunchedEffect(state.chatHistory.size) {
        if (state.chatHistory.isNotEmpty()) {
            chatListState.animateScrollToItem(0)
        }
    }

    if (showChatBot) {
        Dialog(
            onDismissRequest = { showChatBot = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            var textInput by remember { mutableStateOf("") }
            
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = { Text("Coach AI PulseUp 🤖", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { showChatBot = false }) {
                                Icon(Icons.Default.Close, null)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryPurple, titleContentColor = Color.White, navigationIconContentColor = Color.White)
                    )
                },
                // PINDAHKAN INPUT KE BOTTOM BAR AGAR HEADER TIDAK TERDORONG
                bottomBar = {
                    Column(
                        modifier = Modifier
                            .background(Color.White)
                            .navigationBarsPadding()
                            .imePadding() // Hanya bagian bawah yang terpengaruh keyboard
                            .padding(16.dp)
                    ) {
                        // Suggestions Row
                        val suggestions = listOf("Bagaimana BMI saya?", "Cara naik level?", "Tips olahraga")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            items(suggestions) { suggestion ->
                                AssistChip(
                                    onClick = { viewModel.sendChatMessage(suggestion) },
                                    label = { Text(suggestion, fontSize = 11.sp) },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = AssistChipDefaults.assistChipColors(containerColor = PrimaryPurple.copy(alpha = 0.05f))
                                )
                            }
                        }

                        // Input Row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Tanya Coach AI...") },
                                shape = RoundedCornerShape(24.dp),
                                maxLines = 3
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (textInput.isNotBlank()) {
                                        viewModel.sendChatMessage(textInput)
                                        textInput = ""
                                    }
                                },
                                enabled = !state.isSendingChat,
                                colors = IconButtonDefaults.iconButtonColors(containerColor = PrimaryPurple, contentColor = Color.White)
                            ) {
                                if (state.isSendingChat) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.AutoMirrored.Filled.Send, null)
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                // Area Chat otomatis menyesuaikan ukuran saat keyboard muncul tanpa menggeser Header
                LazyColumn(
                    state = chatListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    reverseLayout = true
                ) {
                    items(state.chatHistory.asReversed()) { chat ->
                        val isAI = !chat.isUser
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isAI) Alignment.Start else Alignment.End
                        ) {
                            Surface(
                                color = if (isAI) Color(0xFFF5F5F5) else PrimaryPurple,
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isAI) 0.dp else 16.dp,
                                    bottomEnd = if (isAI) 16.dp else 0.dp
                                )
                            ) {
                                Text(
                                    text = chat.message,
                                    modifier = Modifier.padding(12.dp),
                                    color = if (isAI) Color.Black else Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Keluar dari Akun") },
            text = { Text("Apakah Anda yakin ingin keluar?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                        onLogout()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed)
                ) {
                    Text("Keluar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = Color.White, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, "Logout", tint = Color.White)
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryPurple)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundLight)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryPurple)
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (user?.profilePictureUrl?.isNotEmpty() == true) {
                            AsyncImage(
                                model = user.profilePictureUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(70.dp), tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = user?.username ?: "User Name",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = state.firebaseEmail ?: "user.email@example.com",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Level ${user?.level ?: 1}", fontWeight = FontWeight.Bold, color = TextPrimaryLight)
                        Text("Level ${(user?.level ?: 1) + 1}", fontWeight = FontWeight.Bold, color = TextSecondaryLight)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { user?.getProgressToNextLevel() ?: 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                        color = PrimaryPurple,
                        trackColor = Color.LightGray.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "${((user?.getProgressToNextLevel() ?: 0f) * 100).toInt()}% to next level",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )
                }
            }

            Text(
                "Statistics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCardSmall(title = "Total Points", value = "${user?.totalPoints ?: 0}", modifier = Modifier.weight(1f))
                    StatCardSmall(title = "Activities", value = "${state.totalActivities}", modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCardSmall(title = "Current Streak", value = "${user?.currentStreak ?: 0} days 🔥", modifier = Modifier.weight(1f))
                    StatCardSmall(title = "Longest Streak", value = "${user?.longestStreak ?: 0} days", modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F4F8)),
                onClick = onNavigateToBMI
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("BMI Calculator", fontWeight = FontWeight.Bold, color = TextPrimaryLight)
                        Text(
                            "Height: ${user?.height?.toInt() ?: 0} cm • Weight: ${user?.weight?.toInt() ?: 0} kg",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryLight
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val bmi = user?.calculateBMI() ?: 0f
                        val category = user?.getBMICategory() ?: "Underweight"
                        Text(
                            "BMI: ${"%.1f".format(bmi)} ($category)",
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen,
                            fontSize = 18.sp
                        )
                    }
                    Icon(
                        Icons.Default.Calculate,
                        contentDescription = null,
                        tint = InfoBlue,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "AI Coach Tips 🤖",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                onClick = { showChatBot = true }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💡", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = state.aiTip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimaryLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "My Badges",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.badges.size) { index ->
                    val badge = state.badges[index]
                    val isUnlocked = state.achievements.any { it.badgeId == badge.id }
                    BadgeCard(
                        emoji = badge.icon,
                        name = badge.name,
                        description = if (isUnlocked) "Unlocked" else "Locked",
                        isUnlocked = isUnlocked
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun StatCardSmall(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimaryLight)
        }
    }
}
