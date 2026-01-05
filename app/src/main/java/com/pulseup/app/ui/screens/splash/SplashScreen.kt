package com.pulseup.app.ui.screens.splash

import android.app.Activity
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.pulseup.app.ui.theme.PrimaryPurple
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onNext: () -> Unit) {
    val view = LocalView.current
    val splashColor = Color(0xFF0F011A) // Warna background splash

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = splashColor.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    val translateY = remember { Animatable(50f) }

    val pulseScale = rememberInfiniteTransition(label = "pulse")
    val pulseAnim by pulseScale.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow"
    )

    LaunchedEffect(key1 = true) {
        launch {
            scale.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            delay(300)
            alpha.animateTo(1f, tween(1000))
        }
        launch {
            delay(300)
            translateY.animateTo(0f, tween(1000, easing = FastOutSlowInEasing))
        }

        delay(3000)
        onNext()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(splashColor) 
    ) {
        Canvas(modifier = Modifier.fillMaxSize().blur(80.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(PrimaryPurple.copy(alpha = glowAlpha), Color.Transparent),
                    center = center,
                    radius = size.minDimension / 1.5f
                ),
                center = center,
                radius = size.minDimension / 1.2f
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale.value * if (scale.value >= 1f) pulseAnim else 1f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            Brush.linearGradient(listOf(PrimaryPurple, Color(0xFF9C27B0))),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(y = translateY.value.dp)
                    .alpha(alpha.value)
            ) {
                Text(
                    text = "PulseUp",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 8.sp,
                    fontSize = 36.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                // PERBAIKAN: Menebalkan tulisan dan membuat warnanya lebih terang
                Text(
                    text = "Level Up Your Health",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White, // Warna putih solid tanpa transparansi
                    fontWeight = FontWeight.Bold, // Diubah menjadi Bold agar lebih terlihat
                    letterSpacing = 2.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp)
                .alpha(alpha.value)
        ) {
            Text(
                text = "Initializing...",
                color = Color.White.copy(alpha = 0.4f),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.sp
            )
        }
    }
}
