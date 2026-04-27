// MainActivity.kt
package com.example.candletime

import TimerViewModel
import TimerState
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.candletime.models.CandleSession
import com.example.candletime.ui.CandleComponent
import com.example.candletime.ui.HistoryScreen
import com.example.candletime.ui.SessionDetailScreen

private val BgDeep    = Color(0xFF070504)
private val BgCard    = Color(0xFF120F0A)
private val Amber     = Color(0xFFFFAA33)
private val AmberDim  = Color(0xFF7A4A10)
private val Gold      = Color(0xFFFFD580)
private val TextMain  = Color(0xFFF5E6CC)
private val TextMuted = Color(0xFF7A6A50)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()
        setContent { CandleApp() }
    }
}

sealed class AppScreen {
    object Home    : AppScreen()
    object History : AppScreen()
    data class Detail(val session: CandleSession) : AppScreen()
}

@Composable
fun CandleApp(viewModel: TimerViewModel = viewModel()) {
    var appScreen      by remember { mutableStateOf<AppScreen>(AppScreen.Home) }
    val sessions: List<CandleSession> by viewModel.allSessions.collectAsState(
        initial = emptyList()
    )
    val totalBurned    by viewModel.totalBurned.collectAsState(initial = 0L)
    val completedCount by viewModel.completedCount.collectAsState(initial = 0)

    LaunchedEffect(sessions) {
        val cur = appScreen
        if (cur is AppScreen.Detail && sessions.none { it.id == cur.session.id }) {
            appScreen = AppScreen.History
        }
    }

    AnimatedContent(
        targetState = appScreen,
        transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(300)) },
        label = "appNav"
    ) { screen: AppScreen ->
        when (screen) {
            is AppScreen.History -> HistoryScreen(
                sessions           = sessions,
                totalBurnedSeconds = totalBurned ?: 0L,
                completedCount     = completedCount,
                onBack             = { appScreen = AppScreen.Home },
                onDelete           = { ids -> viewModel.deleteSessions(ids) },
                onOpenDetail       = { appScreen = AppScreen.Detail(it) }
            )
            is AppScreen.Detail  -> SessionDetailScreen(
                session  = screen.session,
                onBack   = { appScreen = AppScreen.History },
                onDelete = {
                    viewModel.deleteSession(screen.session)
                    appScreen = AppScreen.History
                }
            )
            is AppScreen.Home    -> AnimatedContent(
                targetState  = viewModel.timerState,
                transitionSpec = { fadeIn(tween(600)) togetherWith fadeOut(tween(400)) },
                label = "timerNav"
            ) { state : TimerState ->
                when (state) {
                    TimerState.IDLE     -> SelectScreen(
                        onStart       = { viewModel.startTimer(it) },
                        onOpenHistory = { appScreen = AppScreen.History }
                    )
                    TimerState.RUNNING  -> TimerScreen(viewModel)
                    TimerState.FINISHED -> SuccessScreen(onBack = { viewModel.reset() })
                }
            }
        }
    }
}


@Composable
fun SelectScreen(onStart: (Long) -> Unit, onOpenHistory: () -> Unit) {
    val presets = listOf(
        Triple("Focus", "25 dk", 25 * 60L),
        Triple("Short Break", "5 dk", 5 * 60L),
        Triple("Long Break", "15 dk", 15 * 60L),
        Triple("Deep Work", "50 dk", 50 * 60L),
    )

    var selectedPreset by remember { mutableStateOf<Long?>(null) }
    var customMinutes  by remember { mutableStateOf("") }
    var customError    by remember { mutableStateOf(false) }

    val resolvedSeconds: Long? = remember(customMinutes, selectedPreset) {
        val cm = customMinutes.toLongOrNull()
        when {
            cm != null && cm > 0 -> cm * 60L
            selectedPreset != null -> selectedPreset
            else -> null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep),
        contentAlignment = Alignment.Center
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            ) {

            Spacer(Modifier.height(52.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(BgCard)
                    .border(1.dp, AmberDim.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onOpenHistory
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.TopStart
            ) {
                Text("📜 Geçmiş", fontSize = 16.sp, color = Gold)
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgDeep),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp)
                ) {
                    Spacer(Modifier.height(64.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Candle Timer",
                                color = Gold,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Light,
                                letterSpacing = 4.sp
                            )
                            Text(
                                text = "odaklan · dinlen · yak",
                                color = TextMuted,
                                fontSize = 13.sp,
                                letterSpacing = 3.sp,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }

                    }

                    Spacer(Modifier.height(48.dp))
                    Text(
                        "POMODORO",
                        color = TextMuted,
                        fontSize = 11.sp,
                        letterSpacing = 3.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        presets.forEach { (label, time, secs) ->
                            val selected = selectedPreset == secs && customMinutes.isEmpty()
                            PresetChip(
                                label = label,
                                time  = time,
                                selected = selected,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedPreset = secs
                                    customMinutes  = ""
                                    customError    = false
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    Text(
                        "VEYA ÖZEL SÜRE (dakika)",
                        color = TextMuted,
                        fontSize = 11.sp,
                        letterSpacing = 3.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    )
                    OutlinedTextField(
                        value = customMinutes,
                        onValueChange = {
                            customMinutes  = it.filter { c -> c.isDigit() }.take(3)
                            customError    = false
                            if (customMinutes.isNotEmpty()) selectedPreset = null
                        },
                        placeholder = { Text("ör. 45", color = TextMuted) },
                        singleLine = true,
                        isError = customError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Amber,
                            unfocusedBorderColor = AmberDim,
                            focusedTextColor     = TextMain,
                            unfocusedTextColor   = TextMain,
                            cursorColor          = Amber,
                            errorBorderColor     = Color(0xFFFF5555)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (customError) {
                        Text(
                            "Geçerli bir dakika girin (1–999)",
                            color = Color(0xFFFF6666),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(40.dp))

                    val canStart = resolvedSeconds != null
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (canStart)
                                    Brush.horizontalGradient(listOf(Color(0xFFFF8C00), Color(0xFFFFBF00)))
                                else
                                    Brush.horizontalGradient(listOf(AmberDim, AmberDim))
                            )
                            .clickable(
                                enabled = true,
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                if (resolvedSeconds != null) {
                                    onStart(resolvedSeconds)
                                } else {
                                    customError = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Mumu Yak  🕯",
                            color     = if (canStart) Color(0xFF1A0A00) else TextMuted,
                            fontSize  = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(Modifier.height(64.dp))
                }
            }
        }
    }
}

@Composable
fun PresetChip(
    label: String,
    time: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(if (selected) 1.04f else 1f, label = "chipScale")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color(0xFF2A1800) else BgCard)
            .border(
                width = 1.2.dp,
                color = if (selected) Amber else AmberDim.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(vertical = 12.dp, horizontal = 4.dp)
    ) {
        Text(time,  color = if (selected) Gold  else TextMain,  fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Text(label, color = if (selected) Amber else TextMuted, fontSize = 10.sp, letterSpacing = 1.sp)
    }
}



@Composable
fun TimerScreen(viewModel: TimerViewModel) {
    var showQuitDialog by remember { mutableStateOf(false) }

    val motivationalQuotes = listOf(
        "Derin bir nefes al ve anın tadını çıkar.",
        "Şu an tam olman gereken yerdesin.",
        "Kendine ayırdığın bu zaman en değerli yatırımın.",
        "Zihnini serbest bırak, sadece odaklan.",
        "Sessizliğin içindeki gücü hisset.",
        "Sabır, en güzel meyveleri veren ağaçtır.",
        "Zihnin ne kadar sakinse, yolun o kadar aydınlıktır."
    )


    val randomQuote = remember { motivationalQuotes.random() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            CandleComponent(progress = viewModel.candleProgress)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xFF0D0905), BgDeep)
                    )
                )
                .padding(top = 12.dp, bottom = 44.dp, start = 28.dp, end = 28.dp)
        ) {
            Text(
                text = formatTime(viewModel.remainingTimeSeconds),
                color = Gold,
                fontSize = 52.sp,
                fontWeight = FontWeight.Thin,
                letterSpacing = 4.sp
            )

            Text(
                text = "kalan süre",
                color = TextMuted,
                fontSize = 12.sp,
                letterSpacing = 3.sp
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = randomQuote,
                color = TextMuted.copy(alpha = 0.6f),
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Text(
                    text = "Arka plan sesi",
                    color = TextMuted,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )

                Switch(
                    checked = viewModel.isSoundEnabled,
                    onCheckedChange = { viewModel.toggleSound(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Amber,
                        checkedTrackColor = Amber.copy(alpha = 0.5f),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.DarkGray
                    )
                )
            }

            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1A0F08))
                    .border(
                        1.dp,
                        AmberDim.copy(alpha = 0.5f),
                        RoundedCornerShape(14.dp)
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        showQuitDialog = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Mumu söndür",
                    color = TextMuted,
                    fontSize = 14.sp,
                    letterSpacing = 2.sp
                )
            }
        }
    }

    if (showQuitDialog) {
        AlertDialog(
            onDismissRequest = { showQuitDialog = false },
            containerColor = Color(0xFF1A1208),
            titleContentColor = Gold,
            textContentColor = TextMain,
            title = {
                Text(
                    "Seans yarıda kesilsin mi?",
                    fontWeight = FontWeight.Light
                )
            },
            text = {
                Text(
                    "Mum sönecek ve ilerleme kaybolacak.",
                    color = TextMuted
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.reset(savePartial = true)
                        showQuitDialog = false
                    }
                ) {
                    Text("Söndür", color = Color(0xFFFF6644))
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuitDialog = false }) {
                    Text("Devam et", color = Amber)
                }
            }
        )
    }
}

@Composable
fun SuccessScreen(onBack: () -> Unit) {
    val infiniteAlpha = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteAlpha.animateFloat(
        0.5f, 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                "🕯",
                fontSize = 64.sp,
                modifier = Modifier.alpha(0.35f)
            )

            Spacer(Modifier.height(28.dp))

            Text(
                "Tamamlandı",
                color = Gold.copy(alpha = glowAlpha),
                fontSize = 40.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 4.sp
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "Odak seansın başarıyla bitti.",
                color = TextMuted,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(52.dp))

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(Color(0xFF3A2000), BgCard)))
                    .border(1.5.dp, AmberDim, CircleShape)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onBack
                    )
                    .padding(horizontal = 40.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Ana Ekrana Dön",
                    color = Amber,
                    fontSize = 15.sp,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

fun formatTime(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s)
    else "%02d:%02d".format(m, s)
}
