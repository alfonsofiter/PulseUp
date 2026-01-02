package com.pulseup.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pulseup.app.ui.theme.*
import com.pulseup.app.viewmodel.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationViewModel = viewModel()
) {
    val dailyReminder by viewModel.dailyReminder.collectAsState(initial = true)
    val goalReached by viewModel.goalReached.collectAsState(initial = true)
    val hydrationReminder by viewModel.hydrationReminder.collectAsState(initial = false)
    val sleepReminder by viewModel.sleepReminder.collectAsState(initial = false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryPurple,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundLight)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                "General Notifications",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryPurple,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            NotificationSwitchItem(
                title = "Daily Activity Reminder",
                subtitle = "Remind me to log activities every day",
                icon = Icons.Default.NotificationsActive,
                checked = dailyReminder,
                onCheckedChange = { viewModel.updateSetting("daily_reminder", it) }
            )

            NotificationSwitchItem(
                title = "Goal Reached",
                subtitle = "Notify me when I reach my daily health goals",
                icon = Icons.Default.EmojiEvents,
                checked = goalReached,
                onCheckedChange = { viewModel.updateSetting("goal_reached", it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Smart Reminders",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryPurple,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            NotificationSwitchItem(
                title = "Hydration Alert",
                subtitle = "Smart reminders based on your water goal",
                icon = Icons.Default.WaterDrop,
                checked = hydrationReminder,
                onCheckedChange = { viewModel.updateSetting("hydration_reminder", it) }
            )

            NotificationSwitchItem(
                title = "Sleep Schedule",
                subtitle = "Notify me when it's time to sleep for better recovery",
                icon = Icons.Default.Bedtime,
                checked = sleepReminder,
                onCheckedChange = { viewModel.updateSetting("sleep_reminder", it) }
            )
        }
    }
}

@Composable
fun NotificationSwitchItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp),
                color = PrimaryPurple.copy(alpha = 0.1f)
            ) {
                Icon(icon, null, modifier = Modifier.padding(8.dp), tint = PrimaryPurple)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
            }
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PrimaryPurple,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.LightGray
                )
            )
        }
    }
}