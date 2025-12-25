package com.pulseup.app.ui.screens.activities

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pulseup.app.data.local.entity.ActivityCategory
import com.pulseup.app.data.local.entity.ActivityInput
import com.pulseup.app.ui.theme.*
import com.pulseup.app.viewmodel.ActivitiesViewModel
import kotlinx.coroutines.launch

/**
 * Pilihan aktivitas untuk kategori EXERCISE
 */
val EXERCISE_OPTIONS = listOf("Berjalan", "Berlari", "Bersepeda", "Mendaki")

/**
 * Pilihan aktivitas untuk kategori HYDRATION
 */
val HYDRATION_OPTIONS = listOf("Minum Air")

/**
 * Pilihan aktivitas untuk kategori NUTRITION
 */
val NUTRITION_OPTIONS = listOf(
    "Sarapan (Breakfast)",
    "Makan Siang (Lunch)",
    "Makan Malam (Dinner)",
    "Minuman Berkalori (Beverage)",
    "Suplemen (Supplement)"
)

/**
 * Pilihan aktivitas untuk kategori SLEEP
 */
val SLEEP_OPTIONS = listOf("Tidur Malam (Night Sleep)", "Tidur Siang (Nap)")

/**
 * Pilihan kualitas tidur
 */
val SLEEP_QUALITY_OPTIONS = listOf(
    "Sangat Baik (Excellent)",
    "Baik (Good)",
    "Cukup (Fair)",
    "Kurang (Poor)",
    "Buruk (Very Poor)"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityScreen(
    activityId: Int? = null,
    onNavigateBack: () -> Unit,
    viewModel: ActivitiesViewModel = viewModel()
) {
    var selectedCategory by remember { mutableStateOf(ActivityCategory.EXERCISE) }
    var activityName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var durationOrVolume by remember { mutableStateOf("") }
    var sleepQuality by remember { mutableStateOf(SLEEP_QUALITY_OPTIONS[1]) } // Default: Baik
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingData by remember { mutableStateOf(activityId != null) }

    // Dropdown state
    var expandedName by remember { mutableStateOf(false) }
    var expandedQuality by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val isEditMode = activityId != null

    // Load existing activity data if edit mode
    LaunchedEffect(activityId) {
        if (activityId != null) {
            scope.launch {
                val activity = viewModel.getActivityById(activityId)
                activity?.let {
                    selectedCategory = it.category
                    activityName = it.activityName
                    description = it.description
                    durationOrVolume = it.duration.toString()
                }
                isLoadingData = false
            }
        }
    }

    // Update activity name when category changes
    LaunchedEffect(selectedCategory) {
        activityName = when(selectedCategory) {
            ActivityCategory.EXERCISE -> EXERCISE_OPTIONS.first()
            ActivityCategory.HYDRATION -> HYDRATION_OPTIONS.first()
            ActivityCategory.NUTRITION -> NUTRITION_OPTIONS.first()
            ActivityCategory.SLEEP -> SLEEP_OPTIONS.first()
            else -> ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Activity" else "Add Activity") },
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
        if (isLoadingData) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryPurple)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(BackgroundLight)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Category Selection
                Text(
                    "Select Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CategoryItem(
                        icon = "🏃",
                        label = "Exercise",
                        category = ActivityCategory.EXERCISE,
                        selectedCategory = selectedCategory,
                        color = ExerciseColor,
                        onClick = { selectedCategory = ActivityCategory.EXERCISE }
                    )
                    CategoryItem(
                        icon = "💧",
                        label = "Hydration",
                        category = ActivityCategory.HYDRATION,
                        selectedCategory = selectedCategory,
                        color = HydrationColor,
                        onClick = { selectedCategory = ActivityCategory.HYDRATION }
                    )
                    CategoryItem(
                        icon = "🍎",
                        label = "Nutrition",
                        category = ActivityCategory.NUTRITION,
                        selectedCategory = selectedCategory,
                        color = NutritionColor,
                        onClick = { selectedCategory = ActivityCategory.NUTRITION }
                    )
                    CategoryItem(
                        icon = "😴",
                        label = "Sleep",
                        category = ActivityCategory.SLEEP,
                        selectedCategory = selectedCategory,
                        color = SleepColor,
                        onClick = { selectedCategory = ActivityCategory.SLEEP }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Activity Name Dropdown
                Text(
                    "Activity Name",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                val nameOptions = when(selectedCategory) {
                    ActivityCategory.EXERCISE -> EXERCISE_OPTIONS
                    ActivityCategory.HYDRATION -> HYDRATION_OPTIONS
                    ActivityCategory.NUTRITION -> NUTRITION_OPTIONS
                    ActivityCategory.SLEEP -> SLEEP_OPTIONS
                    else -> emptyList()
                }

                if (nameOptions.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedName,
                        onExpandedChange = { expandedName = !expandedName },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = activityName,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedName) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPurple,
                                unfocusedBorderColor = DividerColor
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedName,
                            onDismissRequest = { expandedName = false }
                        ) {
                            nameOptions.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        activityName = selectionOption
                                        expandedName = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Duration/Volume Field
                val durationLabel = when(selectedCategory) {
                    ActivityCategory.HYDRATION -> "Volume (liter)"
                    ActivityCategory.SLEEP -> "Duration (hours)"
                    ActivityCategory.NUTRITION -> "Amount / Calorie (approx)"
                    else -> "Duration (minutes)"
                }
                val durationPlaceholder = when(selectedCategory) {
                    ActivityCategory.HYDRATION -> "e.g., 0.5"
                    ActivityCategory.SLEEP -> "e.g., 8"
                    else -> "e.g., 30"
                }
                
                Text(
                    durationLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = durationOrVolume,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("""^\d*\.?\d*$"""))) {
                            durationOrVolume = newValue
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(durationPlaceholder) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = DividerColor
                    )
                )

                if (selectedCategory == ActivityCategory.SLEEP) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Sleep Quality",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = expandedQuality,
                        onExpandedChange = { expandedQuality = !expandedQuality },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = sleepQuality,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedQuality) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPurple,
                                unfocusedBorderColor = DividerColor
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedQuality,
                            onDismissRequest = { expandedQuality = false }
                        ) {
                            SLEEP_QUALITY_OPTIONS.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        sleepQuality = selectionOption
                                        expandedQuality = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Recommendations & Description
                Text(
                    "Description / Notes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Recommendation helper text
                val recommendation = when(selectedCategory) {
                    ActivityCategory.NUTRITION -> "Recommendation: Fill in what you ate (e.g., Brown Rice, Chicken Breast, Broccoli) for better tracking."
                    ActivityCategory.SLEEP -> {
                        when(sleepQuality) {
                            "Kurang (Poor)", "Buruk (Very Poor)" -> "Tip: Try avoiding screens 1 hour before bed and keep your room cool for better quality."
                            "Sangat Baik (Excellent)" -> "Awesome! Maintain your routine for consistent energy."
                            else -> "Tip: Consistency is key. Try to sleep and wake up at the same time every day."
                        }
                    }
                    else -> "Add notes about your activity..."
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text(recommendation) },
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = DividerColor
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Points Preview
                val durationValue = durationOrVolume.toDoubleOrNull() ?: 0.0
                val activityInput = ActivityInput(
                    category = selectedCategory,
                    activityName = activityName,
                    description = description,
                    duration = if (selectedCategory == ActivityCategory.SLEEP) (durationValue * 60).toInt() else durationValue.toInt()
                )
                val estimatedPoints = activityInput.calculatePoints()

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryPurple.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (isEditMode) "New points:" else "You will earn:",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "+$estimatedPoints points",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Save Button
                Button(
                    onClick = {
                        if (activityName.isNotBlank() && durationValue > 0) {
                            isLoading = true
                            // Append sleep quality to description if it's a sleep activity
                            val finalDescription = if (selectedCategory == ActivityCategory.SLEEP) {
                                "Quality: $sleepQuality. $description"
                            } else description
                            
                            val finalInput = activityInput.copy(description = finalDescription)

                            if (isEditMode && activityId != null) {
                                viewModel.updateActivity(activityId, finalInput) {
                                    isLoading = false
                                    onNavigateBack()
                                }
                            } else {
                                viewModel.addActivity(finalInput) {
                                    isLoading = false
                                    onNavigateBack()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPurple
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = activityName.isNotBlank() && durationValue > 0 && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Icon(Icons.Default.Check, "Save")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isEditMode) "Update Activity" else "Save Activity",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RowScope.CategoryItem(
    icon: String,
    label: String,
    category: ActivityCategory,
    selectedCategory: ActivityCategory,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .height(100.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (selectedCategory == category)
                color.copy(alpha = 0.2f) else CardBackground
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, style = MaterialTheme.typography.displaySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (selectedCategory == category)
                    FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
