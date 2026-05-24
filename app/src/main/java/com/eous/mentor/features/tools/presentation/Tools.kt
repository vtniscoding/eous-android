package com.eous.mentor.features.tools.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eous.mentor.core.theme.*

@Composable
fun Tools(
    onMenuClick: () -> Unit,
    viewModel: ToolsViewModel = remember { ToolsViewModel() }
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Decorative radial glow
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(300.dp)
                .blur(80.dp)
                .background(Brush.radialGradient(listOf(EousIndigo.copy(alpha = 0.12f), Color.Transparent)))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open Sidebar",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Tools & Quizzes",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Brush.linearGradient(listOf(EousPurple, EousIndigo)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("S", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Study Assistance Tools",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "Unlock smart interactive study aids crafted specifically for your dynamic level.",
                    color = MutedText,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                state.tools.forEach { tool ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = tool.title,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = tool.description,
                                color = MutedText,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                            if (tool.buttonText != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {},
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EousPurple)
                                ) {
                                    Text(tool.buttonText, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
