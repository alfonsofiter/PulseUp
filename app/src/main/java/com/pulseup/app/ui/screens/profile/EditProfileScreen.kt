package com.pulseup.app.ui.screens.profile

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pulseup.app.ui.theme.*
import com.pulseup.app.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.profileState.collectAsState()
    val user = state.user
    val context = LocalContext.current

    var fullName by remember { mutableStateOf(user?.username ?: "") }
    var phoneNumber by remember { mutableStateOf(user?.phoneNumber ?: "") }
    var dobLong by remember { mutableStateOf(user?.dateOfBirth ?: 0L) }
    
    val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    val dobString = if (dobLong > 0) sdf.format(Date(dobLong)) else "Select Date"

    // Sync values when user state is loaded
    LaunchedEffect(user) {
        user?.let {
            fullName = it.username
            phoneNumber = it.phoneNumber
            dobLong = it.dateOfBirth
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.updateFullProfile(
                            username = fullName,
                            phone = phoneNumber,
                            dob = dobLong
                        ) {
                            onNavigateBack()
                        }
                    }) {
                        Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryPurple, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundLight)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture Section
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(PrimaryPurple.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(80.dp), tint = PrimaryPurple)
                }
                Surface(
                    modifier = Modifier.size(36.dp).offset(x = (-4).dp, y = (-4).dp),
                    shape = CircleShape,
                    color = PrimaryPurple,
                    onClick = { /* Handle Photo Upload */ }
                ) {
                    Icon(Icons.Default.CameraAlt, null, modifier = Modifier.padding(8.dp), tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Full Name
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Badge, null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email (ReadOnly with Verified Badge)
            OutlinedTextField(
                value = state.firebaseEmail ?: "",
                onValueChange = {},
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Email, null) },
                trailingIcon = {
                    Surface(
                        color = SuccessGreen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Verified", color = SuccessGreen, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phone Number
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Phone, null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Date of Birth
            val calendar = Calendar.getInstance()
            if (dobLong > 0) calendar.timeInMillis = dobLong
            
            val datePickerDialog = DatePickerDialog(
                context,
                { _, y, m, d ->
                    calendar.set(y, m, d)
                    dobLong = calendar.timeInMillis
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            OutlinedTextField(
                value = dobString,
                onValueChange = {},
                label = { Text("Date of Birth") },
                modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() },
                readOnly = true,
                enabled = false,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}