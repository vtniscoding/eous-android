package com.eous.mentor.features.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eous.mentor.core.theme.EousGreen
import com.eous.mentor.core.theme.EousPurple
import com.eous.mentor.core.theme.MutedText

@Composable
fun LargeScreenIntroBanner(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(end = 40.dp)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .background(EousPurple.copy(alpha = 0.1f), CircleShape)
                .border(1.dp, EousPurple.copy(alpha = 0.2f), CircleShape)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                "🚀 Eous — The #1 AI Study Mentor",
                color = EousPurple,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(
                    "Your AI Mentor",
                    color = Color.White,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp
                )
                Text(
                    "Ready for You.",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    ),
                    modifier = Modifier.drawBehind {
                        // Custom text gradient shader placeholder using compose brushes
                    }
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            FloatingMascot(size = 80)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "A personal study assistant everyone's obsessed with — works 24/7, no setup needed. Master any subject with step-by-step explanations.",
            color = MutedText,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(EousGreen, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("No credit card needed", color = MutedText, fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(EousGreen, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cancel anytime", color = MutedText, fontSize = 12.sp)
            }
        }
    }
}
