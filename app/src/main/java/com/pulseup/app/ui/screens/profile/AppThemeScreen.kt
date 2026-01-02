package com.pulseup.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pulseup.app.ui.theme.*
import com.pulseup.app.viewmodel.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppThemeScreen(
    onNavigateBack: () -> Unit,
    viewModel: ThemeViewModel = viewModel()
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState(initial = false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Theme") },
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
                .padding(16.dp)
        ) {
            Text(
                "Choose how PulseUp looks to you.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryLight,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.selectableGroup()) {
                    ThemeOptionItem(
                        icon = Icons.Default.LightMode,
                        title = "Light Mode",
                        selected = !isDarkMode,
                        onClick = { viewModel.toggleTheme(false) }
                    )
                    
                    Divider(modifier = Modifier.padding(horizontal = 16.dp), color = DividerColor.copy(alpha = 0.5f))
                    
                    ThemeOptionItem(
                        icon = Icons.Default.DarkMode,
                        title = "Dark Mode",
                        selected = isDarkMode,
                        onClick = { viewModel.toggleTheme(true) }
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeOptionItem(
    icon: ImageVector,
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(8.dp),
            color = if (selected) PrimaryPurple.copy(alpha = 0.1f) else Color.Transparent
        ) {
            Icon(
                icon,
                null,
                modifier = Modifier.padding(8.dp),
                tint = if (selected) PrimaryPurple else Color.Gray
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
        
        RadioButton(
            selected = selected,
            onClick = null, // Handled by Row selectable
            colors = RadioButtonDefaults.colors(selectedColor = PrimaryPurple)
        )
    }
}