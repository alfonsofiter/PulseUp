package com.pulseup.app.ui.screens.bmi

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pulseup.app.ui.theme.*
import com.pulseup.app.viewmodel.ProfileViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BMICalculatorScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.profileState.collectAsState()
    val user = state.user

    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var bmiResult by remember { mutableStateOf<Double?>(null) }
    var showResult by remember { mutableStateOf(false) }

    LaunchedEffect(user) {
        if (user != null) {
            if (height.isEmpty()) height = if(user.height > 0) user.height.toInt().toString() else ""
            if (weight.isEmpty()) weight = if(user.weight > 0) user.weight.toInt().toString() else ""
            if (age.isEmpty()) age = if(user.age > 0) user.age.toString() else ""
        }
    }

    val bmiHistory = remember { listOf(24.5, 25.2, 24.8, 26.1, 25.5, 24.2) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BMI Calculator", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Gender", fontWeight = FontWeight.Bold, color = TextPrimaryLight)
                    Row(
                        modifier = Modifier.fillMaxWidth().selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GenderChip(
                            label = "Male",
                            icon = Icons.Default.Male,
                            selected = gender == "Male",
                            onClick = { gender = "Male" },
                            modifier = Modifier.weight(1f)
                        )
                        GenderChip(
                            label = "Female",
                            icon = Icons.Default.Female,
                            selected = gender == "Female",
                            onClick = { gender = "Female" },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BMITextField(
                            value = height,
                            onValueChange = { height = it },
                            label = "Height",
                            suffix = "cm",
                            icon = Icons.Default.Height,
                            modifier = Modifier.weight(1f)
                        )
                        BMITextField(
                            value = weight,
                            onValueChange = { weight = it },
                            label = "Weight",
                            suffix = "kg",
                            icon = Icons.Default.Scale,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    BMITextField(
                        value = age,
                        onValueChange = { age = it },
                        label = "Age",
                        suffix = "years",
                        icon = Icons.Default.CalendarToday,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val h = height.toFloatOrNull() ?: 0f
                            val w = weight.toFloatOrNull() ?: 0f
                            val a = age.toIntOrNull() ?: 0
                            
                            if (h > 0 && w > 0) {
                                val hMeters = h / 100
                                bmiResult = (w / (hMeters * hMeters)).toDouble()
                                showResult = true
                                
                                // PERBAIKAN: Kirim data h, w, a agar benar-benar tersimpan ke Database
                                viewModel.updateFullProfile(
                                    username = user?.username ?: "",
                                    phone = user?.phoneNumber ?: "",
                                    dob = user?.dateOfBirth ?: 0L,
                                    photoUrl = user?.profilePictureUrl ?: "",
                                    height = h,
                                    weight = w,
                                    age = a,
                                    onSuccess = {
                                        // Berhasil simpan
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        Icon(Icons.Default.Calculate, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Calculate & Save BMI", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = showResult,
                enter = fadeIn(animationSpec = tween(600)) + expandVertically(animationSpec = tween(600)),
                exit = fadeOut(animationSpec = tween(600)) + shrinkVertically(animationSpec = tween(600))
            ) {
                bmiResult?.let { bmi ->
                    Column {
                        BMIResultCard(bmi = bmi, heightCm = height.toDoubleOrNull() ?: 0.0)
                        Spacer(modifier = Modifier.height(24.dp))
                        BMIHistoryChart(history = bmiHistory)
                    }
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun GenderChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) PrimaryPurple.copy(alpha = 0.1f) else Color.Transparent,
        tonalElevation = 0.dp
    ) {
        val contentColor = if (selected) PrimaryPurple else TextSecondaryLight
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(if (selected) PrimaryPurple.copy(alpha = 0.1f) else Color.Transparent)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = contentColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(label, color = contentColor, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

@Composable
fun BMITextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suffix: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) onValueChange(it) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(icon, null, tint = PrimaryPurple, modifier = Modifier.size(20.dp)) },
            suffix = { Text(suffix, color = TextSecondaryLight) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                unfocusedBorderColor = DividerColor
            )
        )
    }
}

@Composable
fun BMIResultCard(bmi: Double, heightCm: Double) {
    val context = LocalContext.current
    val category = when {
        bmi < 18.5 -> "Underweight"
        bmi < 25.0 -> "Normal"
        bmi < 30.0 -> "Overweight"
        else -> "Obese"
    }
    
    val categoryColor = when {
        bmi < 18.5 -> InfoBlue
        bmi < 25.0 -> SuccessGreen
        bmi < 30.0 -> WarningOrange
        else -> ErrorRed
    }

    val hMeters = heightCm / 100
    val minIdealWeight = (18.5 * hMeters * hMeters).roundToInt()
    val maxIdealWeight = (24.9 * hMeters * hMeters).roundToInt()

    val bmiFormatted = "%.1f".format(bmi)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Your Result", style = MaterialTheme.typography.titleMedium, color = TextSecondaryLight)
                IconButton(onClick = {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "My BMI Result on PulseUp is $bmiFormatted ($category). Ideal weight range for me is $minIdealWeight - $maxIdealWeight kg!")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share your result"))
                }) {
                    Icon(Icons.Default.Share, "Share", tint = PrimaryPurple)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = bmiFormatted,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp),
                fontWeight = FontWeight.Black,
                color = categoryColor
            )
            
            Surface(
                color = categoryColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = category,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    color = categoryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Ideal weight range for your height:", style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
            Text("$minIdealWeight - $maxIdealWeight kg", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SuccessGreen)
            Spacer(modifier = Modifier.height(24.dp))
            BMIScale(bmi = bmi)
        }
    }
}

@Composable
fun BMIScale(bmi: Double) {
    val progress = ((bmi - 15) / (40 - 15)).coerceIn(0.0, 1.0).toFloat()
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(1000))

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("15", fontSize = 10.sp, color = TextSecondaryLight)
            Text("25", fontSize = 10.sp, color = TextSecondaryLight)
            Text("40", fontSize = 10.sp, color = TextSecondaryLight)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth().height(12.dp)) {
            Canvas(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(6.dp))) {
                val brush = Brush.horizontalGradient(listOf(InfoBlue, SuccessGreen, WarningOrange, ErrorRed))
                drawRoundRect(brush = brush)
            }
            Canvas(modifier = Modifier.fillMaxSize()) {
                val x = size.width * animatedProgress
                drawCircle(color = Color.White, radius = 8.dp.toPx(), center = Offset(x, size.height / 2))
                drawCircle(color = Color.DarkGray, radius = 8.dp.toPx(), center = Offset(x, size.height / 2), style = Stroke(width = 2.dp.toPx()))
            }
        }
    }
}

@Composable
fun BMIHistoryChart(history: List<Double>) {
    Text("Weight Progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
    Card(modifier = Modifier.fillMaxWidth().height(180.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = SurfaceLight)) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val maxVal = history.maxOrNull() ?: 1.0
                val minVal = history.minOrNull() ?: 0.0
                val range = (maxVal - minVal).coerceAtLeast(1.0)
                val points = history.mapIndexed { index, value -> Offset(x = (index * (width / (history.size - 1))), y = height - ((value - minVal) / range * height).toFloat()) }
                val path = Path().apply { moveTo(points.first().x, points.first().y); points.forEach { lineTo(it.x, it.y) } }
                drawPath(path = path, color = PrimaryPurple, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                points.forEach { drawCircle(color = PrimaryPurple, radius = 4.dp.toPx(), center = it); drawCircle(color = Color.White, radius = 2.dp.toPx(), center = it) }
            }
        }
    }
}
