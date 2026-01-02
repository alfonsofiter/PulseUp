package com.pulseup.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
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

    LaunchedEffect(Unit) {
        viewModel.refresh()
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
            // Header Section
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

            // Level Progress Card
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

            // Statistics Section
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

            // BMI Calculator Card
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

            // AI Coach Tips
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
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💡", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    val tips = viewModel.generateAITips()
                    Text(
                        text = if (tips.isNotEmpty()) tips.first().message else "You're just getting started! Try to log at least 3 activities per day.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimaryLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Badges Collection
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
