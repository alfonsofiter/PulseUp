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
import com.pulseup.app.viewmodel.GoalsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthGoalsScreen(
    onNavigateBack: () -> Unit,
    viewModel: GoalsViewModel = viewModel()
) {
    val waterGoal by viewModel.waterGoal.collectAsState(initial = 8)
    val stepsGoal by viewModel.stepsGoal.collectAsState(initial = 10000)
    val caloriesGoal by viewModel.caloriesGoal.collectAsState(initial = 2000)
    val sleepGoal by viewModel.sleepGoal.collectAsState(initial = 8)

    var tempWater by remember(waterGoal) { mutableStateOf(waterGoal.toFloat()) }
    var tempSteps by remember(stepsGoal) { mutableStateOf(stepsGoal.toFloat()) }
    var tempCalories by remember(caloriesGoal) { mutableStateOf(caloriesGoal.toFloat()) }
    var tempSleep by remember(sleepGoal) { mutableStateOf(sleepGoal.toFloat()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Goals") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.updateGoals(
                            tempWater.toInt(),
                            tempSteps.toInt(),
                            tempCalories.toInt(),
                            tempSleep.toInt()
                        )
                        onNavigateBack()
                    }) {
                        Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
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
                .padding(24.dp)
        ) {
            Text(
                "Set your daily targets to stay motivated and track your progress.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryLight,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            GoalSliderItem(
                title = "Daily Water Intake",
                value = tempWater,
                onValueChange = { tempWater = it },
                range = 4f..20f,
                unit = "glasses",
                icon = Icons.Default.WaterDrop,
                color = HydrationColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            GoalSliderItem(
                title = "Daily Steps",
                value = tempSteps,
                onValueChange = { tempSteps = it },
                range = 1000f..30000f,
                unit = "steps",
                icon = Icons.Default.DirectionsWalk,
                color = ExerciseColor,
                step = 500f
            )

            Spacer(modifier = Modifier.height(24.dp))

            GoalSliderItem(
                title = "Calories Burned",
                value = tempCalories,
                onValueChange = { tempCalories = it },
                range = 500f..5000f,
                unit = "kcal",
                icon = Icons.Default.LocalFireDepartment,
                color = StreakFire,
                step = 100f
            )

            Spacer(modifier = Modifier.height(24.dp))

            GoalSliderItem(
                title = "Sleep Duration",
                value = tempSleep,
                onValueChange = { tempSleep = it },
                range = 4f..12f,
                unit = "hours",
                icon = Icons.Default.Bedtime,
                color = SleepColor
            )
        }
    }
}

@Composable
fun GoalSliderItem(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    unit: String,
    icon: ImageVector,
    color: Color,
    step: Float = 1f
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = color.copy(alpha = 0.1f)
                ) {
                    Icon(icon, null, modifier = Modifier.padding(8.dp), tint = color)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "${value.toInt()}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPurple
                )
                Text(
                    " $unit",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondaryLight,
                    modifier = Modifier.align(Alignment.Bottom)
                )
            }
            
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = range,
                steps = if (step > 1f) ((range.endInclusive - range.start) / step).toInt() - 1 else 0,
                colors = SliderDefaults.colors(
                    thumbColor = PrimaryPurple,
                    activeTrackColor = PrimaryPurple,
                    inactiveTrackColor = PrimaryPurple.copy(alpha = 0.2f)
                )
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${range.start.toInt()}", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                Text("${range.endInclusive.toInt()}", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
            }
        }
    }
}