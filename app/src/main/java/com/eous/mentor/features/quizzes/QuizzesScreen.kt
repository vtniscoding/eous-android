package com.eous.mentor.features.quizzes

import android.media.MediaPlayer
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eous.mentor.R
import com.eous.mentor.core.ui.theme.*

@Composable
fun Tools(onMenuClick: () -> Unit, viewModel: QuizzesViewModel = remember { QuizzesViewModel() }) {
        val state by viewModel.state.collectAsState()

        // Tab Navigation state
        val tabs = listOf("Timer", "Quizzes", "Flashcards")
        var selectedTab by remember { mutableStateOf("Timer") }

        // Pomodoro Timer & Music state
        var timerMode by remember { mutableStateOf("focus") } // "focus", "short_break", "long_break"
        var timeRemaining by remember { mutableStateOf(1500) } // default 25 min (1500 sec)
        var isTimerRunning by remember { mutableStateOf(false) }
        var isMusicPlaying by remember { mutableStateOf(false) }
        var volume by remember { mutableStateOf(0.6f) }

        val activeColor = when (timerMode) {
                "focus" -> EousPurple
                "short_break" -> EousGreen
                "long_break" -> EousBlue
                else -> EousPurple
        }

        // Spin animation for music icon circle when playing
        val spinTransition = rememberInfiniteTransition(label = "music_spin")
        val rotationAngle by spinTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                        animation = tween(4000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
        )
        val rotation = if (isMusicPlaying) rotationAngle else 0f

        // Local music player implementation
        val context = LocalContext.current
        var currentTrack by remember { mutableStateOf("sunset") } // "sunset" or "bikes"
        var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

        LaunchedEffect(isMusicPlaying, currentTrack) {
                if (isMusicPlaying) {
                        try {
                                mediaPlayer?.let {
                                        try {
                                                if (it.isPlaying) {
                                                        it.stop()
                                                }
                                                it.release()
                                        } catch (e: Exception) {
                                                e.printStackTrace()
                                        }
                                }
                                val rawId = if (currentTrack == "sunset") R.raw.sunset else R.raw.bikes
                                val mp = MediaPlayer.create(context, rawId).apply {
                                        isLooping = true
                                        setVolume(volume, volume)
                                        start()
                                }
                                mediaPlayer = mp
                        } catch (e: Exception) {
                                e.printStackTrace()
                        }
                } else {
                        mediaPlayer?.let {
                                try {
                                        if (it.isPlaying) {
                                                it.stop()
                                        }
                                        it.release()
                                } catch (e: Exception) {
                                        e.printStackTrace()
                                }
                        }
                        mediaPlayer = null
                }
        }

        // Adjust volume in real-time when the volume slider changes
        LaunchedEffect(volume) {
                mediaPlayer?.setVolume(volume, volume)
        }

        // Release MediaPlayer on dispose
        DisposableEffect(Unit) {
                onDispose {
                        mediaPlayer?.let {
                                try {
                                        if (it.isPlaying) {
                                                it.stop()
                                        }
                                        it.release()
                                } catch (e: Exception) {
                                        e.printStackTrace()
                                }
                        }
                }
        }

        LaunchedEffect(isTimerRunning, timerMode) {
                if (isTimerRunning) {
                        isMusicPlaying = true
                        while (timeRemaining > 0) {
                                kotlinx.coroutines.delay(1000)
                                timeRemaining -= 1
                        }
                        isTimerRunning = false
                        isMusicPlaying = false
                        // Automatically cycle mode: focus -> short_break -> focus
                        timerMode = if (timerMode == "focus") "short_break" else "focus"
                        timeRemaining = if (timerMode == "focus") 1500 else 300
                } else {
                        isMusicPlaying = false
                }
        }

        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                // Decorative background gradient glow
                Box(
                        modifier =
                                Modifier.align(Alignment.Center)
                                        .size(350.dp)
                                        .blur(100.dp)
                                        .background(
                                                Brush.radialGradient(
                                                        listOf(
                                                                EousPurple.copy(alpha = 0.15f),
                                                                Color.Transparent
                                                        )
                                                )
                                        )
                )

                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .statusBarsPadding()
                                        .navigationBarsPadding()
                ) {
                        // Header Top Bar
                        Row(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .padding(horizontal = 20.dp, vertical = 15.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Text(
                                        text = "Study Assistance Tools",
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                )
                        }

                        // Sliding Top Navigation Tab Bar Container
                        Column(
                                modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 28.dp)
                        ) {
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        tabs.forEachIndexed { index, tabTitle ->
                                                val isSelected = selectedTab == tabTitle
                                                val textColor by animateColorAsState(
                                                        targetValue = if (isSelected) EousPurple else Color.White.copy(alpha = 0.5f),
                                                        label = "tab_text"
                                                )
                                                Box(
                                                        modifier = Modifier
                                                                .weight(1f)
                                                                .clickable(
                                                                        interactionSource = remember { MutableInteractionSource() },
                                                                        indication = null
                                                                ) {
                                                                        selectedTab = tabTitle
                                                                }
                                                                .padding(vertical = 14.dp),
                                                        contentAlignment = Alignment.Center
                                                ) {
                                                        Text(
                                                                text = tabTitle,
                                                                color = textColor,
                                                                fontSize = 16.sp,
                                                                fontWeight = FontWeight.Bold
                                                        )
                                                }
                                        }
                                }

                                // Bottom border line with slide animation using EousPurple (app theme color)
                                val tabIndex = tabs.indexOf(selectedTab)
                                val indicatorOffsetFraction by animateFloatAsState(
                                        targetValue = tabIndex.toFloat() / tabs.size,
                                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                                        label = "tab_indicator"
                                )
                                BoxWithConstraints(
                                        modifier = Modifier
                                                .fillMaxWidth()
                                                .height(2.dp)
                                                .background(Color.White.copy(alpha = 0.05f))
                                ) {
                                        val indicatorWidth = maxWidth / tabs.size
                                        Box(
                                                modifier = Modifier
                                                        .offset(x = maxWidth * indicatorOffsetFraction)
                                                        .width(indicatorWidth)
                                                        .fillMaxHeight()
                                                        .background(EousPurple, shape = RoundedCornerShape(1.dp))
                                        )
                                }
                        }

                        // Scrollable content area
                        Column(
                                modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(horizontal = 20.dp, vertical = 20.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                                when (selectedTab) {
                                        "Timer" -> {
                                                // --- Premium Minimalist Pomodoro Timer Card (with integrated Lofi Music) ---
                                                Card(
                                                        shape = RoundedCornerShape(28.dp),
                                                        colors =
                                                                CardDefaults.cardColors(
                                                                        containerColor =
                                                                                Color(0xFF0F0F16).copy(alpha = 0.8f)
                                                                ),
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .border(
                                                                                width = 1.dp,
                                                                                brush =
                                                                                        Brush.verticalGradient(
                                                                                                listOf(
                                                                                                        Color.White.copy(alpha = 0.08f),
                                                                                                        Color.White.copy(alpha = 0.02f)
                                                                                                )
                                                                                        ),
                                                                                shape = RoundedCornerShape(28.dp)
                                                                        )
                                                ) {
                                                        Column(
                                                                modifier = Modifier.padding(24.dp),
                                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                                verticalArrangement = Arrangement.spacedBy(24.dp)
                                                        ) {
                                                                // 1. Top Segmented Control (Focus / Short Break / Long Break)
                                                                Row(
                                                                        modifier = Modifier
                                                                                .fillMaxWidth()
                                                                                .background(Color(0xFF07070A), RoundedCornerShape(16.dp))
                                                                                .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                                                                                .padding(4.dp),
                                                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                        val modes = listOf(
                                                                                Triple("focus", "Focus", EousPurple),
                                                                                Triple("short_break", "Short Break", EousGreen),
                                                                                Triple("long_break", "Long Break", EousBlue)
                                                                        )
                                                                        modes.forEach { (modeKey, modeLabel, modeColor) ->
                                                                                val isSelected = timerMode == modeKey
                                                                                Box(
                                                                                        modifier = Modifier
                                                                                                .weight(1f)
                                                                                                .height(38.dp)
                                                                                                .clip(RoundedCornerShape(12.dp))
                                                                                                .then(
                                                                                                        if (isSelected) {
                                                                                                                Modifier
                                                                                                                        .background(modeColor.copy(alpha = 0.12f))
                                                                                                                        .border(1.dp, modeColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                                                                                        } else Modifier
                                                                                                )
                                                                                                .clickable {
                                                                                                        timerMode = modeKey
                                                                                                        isTimerRunning = false
                                                                                                        timeRemaining = when (modeKey) {
                                                                                                                "focus" -> 1500
                                                                                                                "short_break" -> 300
                                                                                                                "long_break" -> 900
                                                                                                                else -> 1500
                                                                                                        }
                                                                                                },
                                                                                        contentAlignment = Alignment.Center
                                                                                ) {
                                                                                        Text(
                                                                                                text = modeLabel,
                                                                                                color = if (isSelected) Color.White else MutedText,
                                                                                                fontSize = 13.sp,
                                                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                                                        )
                                                                                }
                                                                        }
                                                                }

                                                                // 2. Circular countdown timer (Modern minimalistic style)
                                                                val minutes = timeRemaining / 60
                                                                val seconds = timeRemaining % 60
                                                                val timeText = String.format("%02d:%02d", minutes, seconds)

                                                                // Pulsing progress glow transition
                                                                val infiniteTransition = rememberInfiniteTransition(label = "pulse_glow")
                                                                val glowAlpha by infiniteTransition.animateFloat(
                                                                        initialValue = 0.02f,
                                                                        targetValue = if (isTimerRunning) 0.08f else 0.02f,
                                                                        animationSpec = infiniteRepeatable(
                                                                                animation = tween(1500, easing = EaseInOutSine),
                                                                                repeatMode = RepeatMode.Reverse
                                                                        ),
                                                                        label = "glow_alpha"
                                                                )

                                                                Box(
                                                                        modifier = Modifier.size(200.dp),
                                                                        contentAlignment = Alignment.Center
                                                                ) {
                                                                        // Outer pulsing radial aura matching the active color
                                                                        Box(
                                                                                modifier = Modifier
                                                                                        .size(200.dp)
                                                                                        .background(
                                                                                                Brush.radialGradient(
                                                                                                        colors = listOf(activeColor.copy(alpha = glowAlpha), Color.Transparent)
                                                                                                )
                                                                                        )
                                                                        )

                                                                        Canvas(modifier = Modifier.size(170.dp)) {
                                                                                val strokeWidth = 6.dp.toPx()
                                                                                // Background Circle
                                                                                drawCircle(
                                                                                        color = Color.White.copy(alpha = 0.04f),
                                                                                        style = Stroke(width = strokeWidth)
                                                                                )
                                                                                // Sweep progress matching active mode color
                                                                                val totalSeconds = when (timerMode) {
                                                                                        "focus" -> 1500f
                                                                                        "short_break" -> 300f
                                                                                        "long_break" -> 900f
                                                                                        else -> 1500f
                                                                                }
                                                                                val sweepAngle = (timeRemaining.toFloat() / totalSeconds) * 360f
                                                                                
                                                                                drawArc(
                                                                                        color = activeColor,
                                                                                        startAngle = -90f,
                                                                                        sweepAngle = sweepAngle,
                                                                                        useCenter = false,
                                                                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                                                                )
                                                                        }

                                                                        Column(
                                                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                                                verticalArrangement = Arrangement.Center
                                                                        ) {
                                                                                Text(
                                                                                        text = timeText,
                                                                                        color = Color.White,
                                                                                        fontSize = 38.sp,
                                                                                        fontWeight = FontWeight.Bold,
                                                                                        letterSpacing = 1.sp
                                                                                )
                                                                                Spacer(modifier = Modifier.height(4.dp))
                                                                                Text(
                                                                                        text = if (!isTimerRunning) "PAUSED" else if (timerMode == "focus") "FOCUSING" else "BREAKING",
                                                                                        color = MutedText,
                                                                                        fontSize = 11.sp,
                                                                                        fontWeight = FontWeight.ExtraBold,
                                                                                        letterSpacing = 2.sp
                                                                                )
                                                                        }
                                                                }

                                                                // 3. Control Action Buttons (Reset, Play/Pause, Skip)
                                                                Row(
                                                                        modifier = Modifier.fillMaxWidth(),
                                                                        horizontalArrangement = Arrangement.Center,
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                        // Reset button
                                                                        Box(
                                                                                modifier = Modifier
                                                                                        .size(46.dp)
                                                                                        .clip(RoundedCornerShape(12.dp))
                                                                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                                                                        .background(Color.White.copy(alpha = 0.02f))
                                                                                        .clickable {
                                                                                                isTimerRunning = false
                                                                                                timeRemaining = when (timerMode) {
                                                                                                        "focus" -> 1500
                                                                                                        "short_break" -> 300
                                                                                                        "long_break" -> 900
                                                                                                        else -> 1500
                                                                                                }
                                                                                        },
                                                                                contentAlignment = Alignment.Center
                                                                        ) {
                                                                                Icon(
                                                                                        imageVector = Icons.Default.Refresh,
                                                                                        contentDescription = "Reset",
                                                                                        tint = Color.White.copy(alpha = 0.7f),
                                                                                        modifier = Modifier.size(20.dp)
                                                                                )
                                                                        }

                                                                        Spacer(modifier = Modifier.width(20.dp))

                                                                        // Play / Pause central button
                                                                        Box(
                                                                                modifier = Modifier
                                                                                        .size(62.dp)
                                                                                        .clip(CircleShape)
                                                                                        .background(activeColor)
                                                                                        .clickable {
                                                                                                isTimerRunning = !isTimerRunning
                                                                                        },
                                                                                contentAlignment = Alignment.Center
                                                                        ) {
                                                                                Icon(
                                                                                        imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                                                        contentDescription = "Play/Pause",
                                                                                        tint = Color.Black,
                                                                                        modifier = Modifier.size(28.dp)
                                                                                )
                                                                        }

                                                                        Spacer(modifier = Modifier.width(20.dp))

                                                                        // Skip button
                                                                        Box(
                                                                                modifier = Modifier
                                                                                        .size(46.dp)
                                                                                        .clip(RoundedCornerShape(12.dp))
                                                                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                                                                        .background(Color.White.copy(alpha = 0.02f))
                                                                                        .clickable {
                                                                                                isTimerRunning = false
                                                                                                timerMode = when (timerMode) {
                                                                                                        "focus" -> "short_break"
                                                                                                        "short_break" -> "long_break"
                                                                                                        "long_break" -> "focus"
                                                                                                        else -> "focus"
                                                                                                }
                                                                                                timeRemaining = when (timerMode) {
                                                                                                        "focus" -> 1500
                                                                                                        "short_break" -> 300
                                                                                                        "long_break" -> 900
                                                                                                        else -> 1500
                                                                                                }
                                                                                        },
                                                                                contentAlignment = Alignment.Center
                                                                        ) {
                                                                                Icon(
                                                                                        imageVector = Icons.Default.SkipNext,
                                                                                        contentDescription = "Skip",
                                                                                        tint = Color.White.copy(alpha = 0.7f),
                                                                                        modifier = Modifier.size(20.dp)
                                                                                )
                                                                        }
                                                                }

                                                                // 4. Lofi music sub-card with custom volume bar (inside the same 1 grid/card)
                                                                Card(
                                                                        shape = RoundedCornerShape(22.dp),
                                                                        colors = CardDefaults.cardColors(
                                                                                containerColor = Color(0xFF0F0F16).copy(alpha = 0.6f)
                                                                        ),
                                                                        modifier = Modifier
                                                                                .fillMaxWidth()
                                                                                .border(
                                                                                        1.dp,
                                                                                        Color.White.copy(alpha = 0.04f),
                                                                                        RoundedCornerShape(22.dp)
                                                                                )
                                                                ) {
                                                                        Column(
                                                                                modifier = Modifier.padding(18.dp),
                                                                                verticalArrangement = Arrangement.spacedBy(14.dp)
                                                                        ) {
                                                                                Row(
                                                                                        verticalAlignment = Alignment.CenterVertically,
                                                                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                                                                        modifier = Modifier
                                                                                                .fillMaxWidth()
                                                                                                .clickable {
                                                                                                        currentTrack = if (currentTrack == "sunset") "bikes" else "sunset"
                                                                                                }
                                                                                ) {
                                                                                        Box(
                                                                                                modifier = Modifier
                                                                                                        .size(42.dp)
                                                                                                        .rotate(rotation)
                                                                                                        .clip(CircleShape)
                                                                                                        .background(Color.White.copy(alpha = 0.04f)),
                                                                                                contentAlignment = Alignment.Center
                                                                                        ) {
                                                                                                Icon(
                                                                                                        imageVector = Icons.Default.MusicNote,
                                                                                                        contentDescription = "Music",
                                                                                                        tint = if (isMusicPlaying) activeColor else Color.White.copy(alpha = 0.4f),
                                                                                                        modifier = Modifier.size(20.dp)
                                                                                                )
                                                                                        }
                                                                                        Column {
                                                                                                Text(
                                                                                                        text = if (currentTrack == "sunset") "Sunset Lofi Beats 🌅" else "Bikes Lofi Beats 🚲",
                                                                                                        color = Color.White,
                                                                                                        fontSize = 15.sp,
                                                                                                        fontWeight = FontWeight.Bold
                                                                                                )
                                                                                                Spacer(modifier = Modifier.height(2.dp))
                                                                                                Text(
                                                                                                        text = if (isMusicPlaying) {
                                                                                                                if (currentTrack == "sunset") "Playing sunset chords..." else "Playing summer beats..."
                                                                                                        } else {
                                                                                                                "Tap row to change track • Auto-plays"
                                                                                                        },
                                                                                                        color = MutedText,
                                                                                                        fontSize = 12.sp
                                                                                                )
                                                                                        }
                                                                                }

                                                                                HorizontalDivider(
                                                                                        color = Color.White.copy(alpha = 0.05f),
                                                                                        thickness = 1.dp
                                                                                )

                                                                                Row(
                                                                                        verticalAlignment = Alignment.CenterVertically,
                                                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                                                        modifier = Modifier.fillMaxWidth()
                                                                                ) {
                                                                                        Icon(
                                                                                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                                                                contentDescription = "Volume",
                                                                                                tint = MutedText,
                                                                                                modifier = Modifier.size(16.dp)
                                                                                        )
                                                                                        BoxWithConstraints(
                                                                                                modifier = Modifier
                                                                                                        .weight(1f)
                                                                                                        .height(20.dp)
                                                                                        ) {
                                                                                                val maxPx = constraints.maxWidth.toFloat()
                                                                                                Box(
                                                                                                        modifier = Modifier
                                                                                                                .fillMaxSize()
                                                                                                                .pointerInput(Unit) {
                                                                                                                        detectTapGestures { offset ->
                                                                                                                                volume = (offset.x / maxPx).coerceIn(0f, 1f)
                                                                                                                        }
                                                                                                                }
                                                                                                                .pointerInput(Unit) {
                                                                                                                        detectDragGestures { change, dragAmount ->
                                                                                                                                change.consume()
                                                                                                                                volume = (volume + dragAmount.x / maxPx).coerceIn(0f, 1f)
                                                                                                                        }
                                                                                                                }
                                                                                                ) {
                                                                                                        val width = this@BoxWithConstraints.maxWidth
                                                                                                        val activeWidth = width * volume

                                                                                                        // Track container (capsule shape)
                                                                                                        Box(
                                                                                                                modifier = Modifier
                                                                                                                        .fillMaxWidth()
                                                                                                                        .height(14.dp)
                                                                                                                        .align(Alignment.Center)
                                                                                                                        .clip(RoundedCornerShape(7.dp))
                                                                                                                        .background(Color.White.copy(alpha = 0.08f))
                                                                                                        ) {
                                                                                                                // Active track inside the track container
                                                                                                                Box(
                                                                                                                        modifier = Modifier
                                                                                                                                .width(activeWidth)
                                                                                                                                .fillMaxHeight()
                                                                                                                                .background(activeColor)
                                                                                                                )
                                                                                                        }

                                                                                                        // Thumb pill indicator
                                                                                                        Box(
                                                                                                                modifier = Modifier
                                                                                                                        .offset(x = activeWidth - 3.dp)
                                                                                                                        .width(6.dp)
                                                                                                                        .height(18.dp)
                                                                                                                        .align(Alignment.CenterStart)
                                                                                                                        .clip(RoundedCornerShape(3.dp))
                                                                                                                        .background(activeColor)
                                                                                                        )
                                                                                                }
                                                                                        }
                                                                                        Text(
                                                                                                text = "${(volume * 100).toInt()}%",
                                                                                                color = MutedText,
                                                                                                fontSize = 11.sp,
                                                                                                fontWeight = FontWeight.Bold,
                                                                                                modifier = Modifier.width(32.dp)
                                                                                        )
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                        "Quizzes" -> {
                                                val quiz = state.quizzes.firstOrNull { it.title.contains("Quizzes") || it.title.contains("Smart") }
                                                if (quiz != null) {
                                                        StudyAidCard(quiz)
                                                }
                                        }
                                        "Flashcards" -> {
                                                val flashcard = state.quizzes.firstOrNull { it.title.contains("Flashcards") || it.title.contains("Active") }
                                                if (flashcard != null) {
                                                        StudyAidCard(flashcard)
                                                }
                                        }
                                }
                        }
                }
        }
}

@Composable
private fun StudyAidCard(quiz: com.eous.mentor.features.quizzes.QuizzesItem) {
        Card(
                shape = RoundedCornerShape(20.dp),
                colors =
                        CardDefaults.cardColors(
                                containerColor = CardBackground.copy(alpha = 0.5f)
                        ),
                modifier =
                        Modifier.fillMaxWidth()
                                .border(
                                        1.dp,
                                        BorderColor.copy(alpha = 0.5f),
                                        RoundedCornerShape(20.dp)
                                )
        ) {
                Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                        Text(
                                text = quiz.title,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                        )
                        Text(
                                text = quiz.description,
                                color = MutedText,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                        )
                        if (quiz.buttonText != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                        onClick = {},
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                                containerColor = EousPurple
                                        )
                                ) {
                                        Text(
                                                quiz.buttonText,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                        )
                                }
                        }
                }
        }
}
