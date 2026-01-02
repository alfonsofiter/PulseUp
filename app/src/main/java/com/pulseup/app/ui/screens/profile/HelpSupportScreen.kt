package com.pulseup.app.ui.screens.profile

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.pulseup.app.ui.theme.*

data class FAQItem(val question: String, val answer: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(
    onNavigateBack: () -> Unit
) {
    val faqs = listOf(
        FAQItem("How do I earn points?", "You can earn points by logging activities like exercise, drinking water, eating healthy meals, and getting enough sleep."),
        FAQItem("What is BMI?", "Body Mass Index (BMI) is a measure of body fat based on height and weight that applies to adult men and women."),
        FAQItem("How do streaks work?", "Streaks increase when you log at least one activity every day. If you miss a day, your streak resets to zero."),
        FAQItem("How do I update my profile?", "Go to Profile > Settings > Update Profile to change your name, phone number, and birth date."),
        FAQItem("Is my data safe?", "Yes, your data is stored securely using Firebase and local encryption.")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help & Support") },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundLight)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Frequently Asked Questions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(faqs) { faq ->
                FAQCard(faq)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Contact Us",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                ContactMethodItem(
                    icon = Icons.Default.Email,
                    title = "Email Support",
                    content = "support@pulseup.app"
                )
                ContactMethodItem(
                    icon = Icons.Default.Language,
                    title = "Website",
                    content = "www.pulseup.app"
                )
            }
        }
    }
}

@Composable
fun FAQCard(faq: FAQItem) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    faq.question,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = PrimaryPurple
                )
            }
            
            AnimatedVisibility(visible = expanded) {
                Column {
                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = DividerColor.copy(alpha = 0.5f))
                    Text(
                        faq.answer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryLight
                    )
                }
            }
        }
    }
}

@Composable
fun ContactMethodItem(icon: ImageVector, title: String, content: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
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
            Column {
                Text(title, style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                Text(content, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}