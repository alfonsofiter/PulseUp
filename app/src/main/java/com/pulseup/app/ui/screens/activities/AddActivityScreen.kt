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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityScreen(
    activityId: Int? = null, // null = Add mode, not null = Edit mode
    onNavigateBack: () -> Unit,
    viewModel: ActivitiesViewModel = viewModel()
) {
    var selectedCategory by remember { mutableStateOf(ActivityCategory.EXERCISE) }
    var activityName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingData by remember { mutableStateOf(activityId != null) }

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
                    duration = it.duration.toString()
                }
                isLoadingData = false
            }
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
                    // Exercise
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp),
                        onClick = { selectedCategory = ActivityCategory.EXERCISE },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedCategory == ActivityCategory.EXERCISE)
                                ExerciseColor.copy(alpha = 0.2f) else CardBackground
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
                            Text("🏃", style = MaterialTheme.typography.displaySmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Exercise",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (selectedCategory == ActivityCategory.EXERCISE)
                                    FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    // Hydration
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp),
                        onClick = { selectedCategory = ActivityCategory.HYDRATION },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedCategory == ActivityCategory.HYDRATION)
                                HydrationColor.copy(alpha = 0.2f) else CardBackground
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
                            Text("💧", style = MaterialTheme.typography.displaySmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Hydration",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (selectedCategory == ActivityCategory.HYDRATION)
                                    FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    // Nutrition
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp),
                        onClick = { selectedCategory = ActivityCategory.NUTRITION },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedCategory == ActivityCategory.NUTRITION)
                                NutritionColor.copy(alpha = 0.2f) else CardBackground
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
                            Text("🍎", style = MaterialTheme.typography.displaySmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Nutrition",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (selectedCategory == ActivityCategory.NUTRITION)
                                    FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    // Sleep
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp),
                        onClick = { selectedCategory = ActivityCategory.SLEEP },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedCategory == ActivityCategory.SLEEP)
                                SleepColor.copy(alpha = 0.2f) else CardBackground
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
                            Text("😴", style = MaterialTheme.typography.displaySmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Sleep",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (selectedCategory == ActivityCategory.SLEEP)
                                    FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Activity Name
                Text(
                    "Activity Name",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = activityName,
                    onValueChange = { activityName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., Morning Jogging") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = DividerColor
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Duration
                Text(
                    "Duration (minutes)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = duration,
                    onValueChange = { newValue ->
                        duration = newValue.filter { it.isDigit() }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., 30") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = DividerColor
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    "Description (Optional)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("Add notes about your activity...") },
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = DividerColor
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Points Preview
                val durationInt = duration.toIntOrNull() ?: 0
                val activityInput = ActivityInput(
                    category = selectedCategory,
                    activityName = activityName,
                    description = description,
                    duration = durationInt
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
                        if (activityName.isNotBlank() && durationInt > 0) {
                            isLoading = true
                            if (isEditMode && activityId != null) {
                                // Update existing activity
                                viewModel.updateActivity(activityId, activityInput) {
                                    isLoading = false
                                    onNavigateBack()
                                }
                            } else {
                                // Add new activity
                                viewModel.addActivity(activityInput) {
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
                    enabled = activityName.isNotBlank() && durationInt > 0 && !isLoading
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