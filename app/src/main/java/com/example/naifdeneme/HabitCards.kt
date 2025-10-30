package com.example.naifdeneme.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GradientHabitCard(
    emoji: String,
    name: String,
    streak: Int,
    completionRate: Int,
    onClick: () -> Unit,
    onQuickComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var done by remember { mutableStateOf(false) }

    val gradient = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary
        )
    )

    Box(modifier = modifier.clickable { onClick() }) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gradient)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(emoji, fontSize = 28.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatIcon(Icons.Default.Favorite, "Streak $streak")
                            StatIcon(Icons.Default.KeyboardArrowUp, "%$completionRate")
                        }
                    }

                    FilledTonalButton(
                        onClick = {
                            done = true
                            onQuickComplete()
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color.White,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Tamamla")
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = done,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Harika! ✔",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0x66000000))
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
fun StatIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Surface(
        color = Color.White.copy(alpha = 0.15f),
        shape = RoundedCornerShape(50)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(icon, null, tint = Color.White)
            Spacer(Modifier.width(6.dp))
            Text(text, color = Color.White)
        }
    }
}