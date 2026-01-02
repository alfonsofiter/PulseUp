package com.pulseup.app.ui.screens.activities

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pulseup.app.data.local.entity.ActivityCategory
import com.pulseup.app.data.local.entity.HealthActivity
import com.pulseup.app.ui.theme.*
import com.pulseup.app.viewmodel.ActivitiesViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesScreen(
    onNavigate: (String) -> Unit,
    viewModel: ActivitiesViewModel = viewModel()
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val categories = listOf("All", "Exercise", "Hydration", "Nutrition", "Sleep")

    val activities by viewModel.activities.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Memastikan data dimuat saat layar dibuka
    LaunchedEffect(Unit) {
        viewModel.loadActivities()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Activities", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryPurple,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigate("add_activity") },
                containerColor = PrimaryPurple,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "Add Activity")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundLight)
        ) {
            // Filter Categories
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category || (selectedCategory == null && category == "All"),
                        onClick = {
                            selectedCategory = if (category == "All") null else category
                            val categoryEnum = if (category == "All") null else ActivityCategory.valueOf(category.uppercase())
                            viewModel.loadActivitiesByCategory(categoryEnum)
                        },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryPurple,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = TextSecondaryLight
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (selectedCategory == category || (selectedCategory == null && category == "All")) 
                                Color.Transparent else Color.LightGray,
                            enabled = true,
                            selected = selectedCategory == category || (selectedCategory == null && category == "All")
                        )
                    )
                }
            }

            // Activities List
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            } else if (activities.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.History, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No activities found",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondaryLight
                        )
                        Text(
                            "Start your journey by adding one!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryLight
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(activities) { activity ->
                        ActivityItemCard(
                            activity = activity,
                            onEdit = { onNavigate("edit_activity/${activity.id}") },
                            onDelete = {
                                viewModel.deleteActivity(activity) {}
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityItemCard(
    activity: HealthActivity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon logic
            val (emoji, categoryColor) = when (activity.category) {
                ActivityCategory.EXERCISE -> "🏃" to ExerciseColor
                ActivityCategory.HYDRATION -> "💧" to HydrationColor
                ActivityCategory.NUTRITION -> "🍎" to NutritionColor
                ActivityCategory.SLEEP -> "😴" to SleepColor
            }

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(categoryColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, style = MaterialTheme.typography.headlineSmall)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.activityName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = activity.category.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )
                    Text(
                        text = " • ${activity.duration} min • ${getTimeAgo(activity.timestamp)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "+${activity.points} points",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryPurple,
                    fontWeight = FontWeight.Bold
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, "Menu", tint = TextSecondaryLight)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            showMenu = false
                            onEdit()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, "Edit") }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            showMenu = false
                            showDeleteDialog = true
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, "Delete", tint = ErrorRed) }
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Activity") },
            text = { Text("Are you sure you want to remove this activity from your history?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun getTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> "$days d ago"
        hours > 0 -> "$hours h ago"
        minutes > 0 -> "$minutes m ago"
        else -> "Just now"
    }
}
