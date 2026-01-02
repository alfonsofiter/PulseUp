package com.pulseup.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pulseup.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToHelpSupport: () -> Unit,
    onNavigateToAppTheme: () -> Unit,
    onNavigateToHealthGoals: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                "Account",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryPurple,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            SettingsItem(
                icon = Icons.Default.Person,
                title = "Update Profile",
                subtitle = "Name, phone, and birth date",
                onClick = onNavigateToEditProfile
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Preferences",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryPurple,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                subtitle = "Manage your alerts",
                enabled = true,
                onClick = onNavigateToNotifications
            )
            
            SettingsItem(
                icon = Icons.Default.TrackChanges,
                title = "Health Goals",
                subtitle = "Set your daily targets",
                enabled = true,
                onClick = onNavigateToHealthGoals
            )

            SettingsItem(
                icon = Icons.Default.Palette,
                title = "App Theme",
                subtitle = "Light or Dark mode",
                enabled = true,
                onClick = onNavigateToAppTheme
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Support",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryPurple,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            SettingsItem(
                icon = Icons.Default.Help,
                title = "Help & Support",
                subtitle = "FAQ and contact us",
                enabled = true,
                onClick = onNavigateToHelpSupport
            )
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) Color.White else Color.White.copy(alpha = 0.5f)
        )
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
                color = if (enabled) PrimaryPurple.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.2f)
            ) {
                Icon(
                    icon, 
                    null, 
                    modifier = Modifier.padding(8.dp), 
                    tint = if (enabled) PrimaryPurple else Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title, 
                    style = MaterialTheme.typography.bodyLarge, 
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) TextPrimaryLight else Color.Gray
                )
                Text(
                    subtitle, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = TextSecondaryLight
                )
            }
            
            if (enabled) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForwardIos, 
                    null, 
                    modifier = Modifier.size(16.dp),
                    tint = DividerColor
                )
            }
        }
    }
}