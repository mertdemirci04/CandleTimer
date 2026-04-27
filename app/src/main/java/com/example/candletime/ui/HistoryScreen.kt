// HistoryScreen.kt
package com.example.candletime.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.candletime.models.CandleSession

private val BgDeep    = Color(0xFF070504)
private val BgCard    = Color(0xFF120F0A)
private val BgCard2   = Color(0xFF1A1208)
private val Amber     = Color(0xFFFFAA33)
private val AmberDim  = Color(0xFF7A4A10)
private val Gold      = Color(0xFFFFD580)
private val TextMain  = Color(0xFFF5E6CC)
private val TextMuted = Color(0xFF7A6A50)
private val Success   = Color(0xFF66CC88)
private val Warning   = Color(0xFFFF8844)


@Composable
fun HistoryScreen(
    sessions: List<CandleSession>,
    totalBurnedSeconds: Long,
    completedCount: Int,
    onBack: () -> Unit,
    onDelete: (List<Long>) -> Unit,
    onOpenDetail: (CandleSession) -> Unit
) {
    var selectionMode   by remember { mutableStateOf(false) }
    var selectedIds     by remember { mutableStateOf(setOf<Long>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showOnlyThisWeek by remember { mutableStateOf(false) }

    val now = System.currentTimeMillis()
    val oneWeekAgo = now - 7 * 86_400_000L
    val filteredSessions = if (showOnlyThisWeek) {
        sessions.filter { it.startedAt >= oneWeekAgo }
    } else {
        sessions
    }

    val completedCountFiltered = filteredSessions.count { it.completed }
    val totalBurnedSecondsFiltered = filteredSessions.sumOf { it.actualSeconds }

    LaunchedEffect(selectionMode) { if (!selectionMode) selectedIds = emptySet() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            IconButton(onClick = {
                if (selectionMode) selectionMode = false else onBack()
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = Amber)
            }
            Text(
                if (selectionMode) "${selectedIds.size} seçildi" else "Mum Geçmişi",
                color = Gold,
                fontSize = 20.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 2.sp,
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            )
            if (selectionMode) {
                TextButton(onClick = {
                    selectedIds = if (selectedIds.size == filteredSessions.size)
                        emptySet() else filteredSessions.map { it.id }.toSet()
                }) { Text("Tümü", color = Amber, fontSize = 13.sp) }

                IconButton(
                    onClick = { if (selectedIds.isNotEmpty()) showDeleteDialog = true },
                    enabled = selectedIds.isNotEmpty()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Sil",
                        tint = if (selectedIds.isNotEmpty()) Color(0xFFFF6644) else TextMuted
                    )
                }
            } else {
                if (filteredSessions.isNotEmpty()) {
                    TextButton(onClick = { selectionMode = true }) {
                        Text("Düzenle", color = Amber, fontSize = 13.sp)
                    }
                }
            }
        }

        HorizontalDivider(color = AmberDim.copy(alpha = 0.2f))

        if (filteredSessions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🕯", fontSize = 56.sp, modifier = Modifier.alpha(0.25f))
                    Spacer(Modifier.height(16.dp))
                    Text("Henüz hiç mum yakmadın",
                        color = TextMuted, fontSize = 15.sp, letterSpacing = 1.sp)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    SummaryStatsRow(
                        totalSessions     = filteredSessions.size,
                        completedCount    = completedCountFiltered,
                        totalBurnedMinutes = totalBurnedSecondsFiltered / 60
                    )
                    Spacer(Modifier.height(8.dp))
                }

                item {
                    WeeklyBarChart(sessions = filteredSessions)
                    Spacer(Modifier.height(8.dp))
                }

                item {
                    DailyDistributionChart(sessions = filteredSessions)
                    Spacer(Modifier.height(4.dp))
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "TÜM SEANS'LAR",
                            color = TextMuted,
                            fontSize = 12.sp,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.weight(1f)) // ← KRİTİK

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Sadece bu hafta",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                            Spacer(Modifier.width(8.dp))
                            Switch(
                                checked = showOnlyThisWeek,
                                onCheckedChange = { showOnlyThisWeek = it }
                            )
                        }
                    }
                }

                items(filteredSessions, key = { it.id }) { session ->
                    val isSelected = session.id in selectedIds
                    SessionCard(
                        session     = session,
                        selectionMode = selectionMode,
                        isSelected  = isSelected,
                        onLongClick = {
                            selectionMode = true
                            selectedIds   = selectedIds + session.id
                        },
                        onToggleSelect = {
                            selectedIds = if (isSelected)
                                selectedIds - session.id else selectedIds + session.id
                        },
                        onClick = {
                            if (selectionMode) {
                                selectedIds = if (isSelected)
                                    selectedIds - session.id else selectedIds + session.id
                            } else {
                                onOpenDetail(session)
                            }
                        }
                    )
                }

                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor   = BgCard2,
            titleContentColor = Gold,
            title = { Text("${selectedIds.size} seans silinsin mi?",
                fontWeight = FontWeight.Light) },
            text  = { Text("Bu işlem geri alınamaz.", color = TextMuted) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(selectedIds.toList())
                    selectedIds     = emptySet()
                    selectionMode   = false
                    showDeleteDialog = false
                }) { Text("Sil", color = Color(0xFFFF6644)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("İptal", color = Amber)
                }
            }
        )
    }
}

@Composable
fun SummaryStatsRow(
    totalSessions: Int,
    completedCount: Int,
    totalBurnedMinutes: Long
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatBox("🕯", "$totalSessions", "toplam seans", Modifier.weight(1f))
        StatBox("✅", "$completedCount", "tamamlanan",   Modifier.weight(1f))
        val h = totalBurnedMinutes / 60
        val m = totalBurnedMinutes % 60
        val label = if (h > 0) "${h}s ${m}dk" else "${m}dk"
        StatBox("⏱", label, "toplam süre",             Modifier.weight(1f))
    }
}

@Composable
fun StatBox(emoji: String, value: String, label: String, modifier: Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(BgCard)
            .border(1.dp, AmberDim.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(vertical = 14.dp)
    ) {
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = Gold,      fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Text(label, color = TextMuted, fontSize = 10.sp, letterSpacing = 1.sp)
    }
}

@Composable
fun WeeklyBarChart(sessions: List<CandleSession>) {
    val now = System.currentTimeMillis()
    val dayMs = 86_400_000L
    val days = (6 downTo 0).map { daysAgo ->
        val dayStart = now - daysAgo * dayMs - (now % dayMs)
        val dayEnd   = dayStart + dayMs
        val totalMin = sessions
            .filter { it.startedAt in dayStart until dayEnd }
            .sumOf { it.actualSeconds } / 60f
        val label = java.time.Instant.ofEpochMilli(dayStart + dayMs / 2)
            .atZone(java.time.ZoneId.systemDefault())
            .format(java.time.format.DateTimeFormatter.ofPattern("EEE", java.util.Locale("tr")))
            .take(3)
        Pair(label, totalMin)
    }
    val maxMin = days.maxOfOrNull { it.second }?.coerceAtLeast(1f) ?: 1f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .border(1.dp, AmberDim.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text("SON 7 GÜN", color = TextMuted, fontSize = 11.sp, letterSpacing = 3.sp)
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { (label, minutes) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f)
                ) {
                    val barFrac = (minutes / maxMin).coerceIn(0f, 1f)
                    val barH    = (barFrac * 72f).coerceAtLeast(if (minutes > 0) 4f else 0f)
                    if (barH > 0f) {
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(barH.dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Gold, Color(0xFFFF8C00))
                                    )
                                )
                        )
                    } else {
                        Box(
                            modifier = Modifier.width(20.dp).height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(AmberDim.copy(alpha = 0.2f))
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(label, color = TextMuted, fontSize = 10.sp)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = AmberDim.copy(alpha = 0.15f))
        Spacer(Modifier.height(8.dp))
        val weekTotal = days.sumOf { it.second.toLong() }
        val h = weekTotal / 60; val m = weekTotal % 60
        Text(
            "Bu hafta toplam: ${if (h > 0) "${h}s " else ""}${m}dk",
            color = TextMain, fontSize = 12.sp
        )
    }
}

@Composable
fun DailyDistributionChart(sessions: List<CandleSession>) {
    val slots = listOf("Sabah\n06–12", "Öğleden\n12–18", "Akşam\n18–24", "Gece\n00–06")
    val counts = IntArray(4)
    sessions.forEach { s ->
        val hour = java.time.Instant.ofEpochMilli(s.startedAt)
            .atZone(java.time.ZoneId.systemDefault()).hour
        val idx = when (hour) {
            in 6..11  -> 0
            in 12..17 -> 1
            in 18..23 -> 2
            else      -> 3
        }
        counts[idx]++
    }
    val total = counts.sum().coerceAtLeast(1)
    val slotColors = listOf(
        Color(0xFFFFCC44), Color(0xFFFF8C00), Color(0xFFFF4400), Color(0xFF6644AA)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .border(1.dp, AmberDim.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text("GÜNÜN SAATİ DAĞILIMI", color = TextMuted, fontSize = 11.sp, letterSpacing = 3.sp)
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(100.dp)) {
                    val stroke = 22f
                    val radius = (size.minDimension - stroke) / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)
                    var startAngle = -90f
                    counts.forEachIndexed { i, c ->
                        val sweep = (c.toFloat() / total) * 360f
                        if (sweep > 0f) {
                            drawArc(
                                color      = slotColors[i],
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter  = false,
                                topLeft    = Offset(center.x - radius, center.y - radius),
                                size       = Size(radius * 2f, radius * 2f),
                                style      = Stroke(width = stroke, cap = StrokeCap.Butt)
                            )
                            startAngle += sweep
                        }
                    }
                    if (total == 0) {
                        drawArc(
                            color = AmberDim.copy(alpha = 0.2f), startAngle = 0f,
                            sweepAngle = 360f, useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size  = Size(radius * 2f, radius * 2f),
                            style = Stroke(width = stroke)
                        )
                    }
                }
                Text(
                    "$total\nseans",
                    color     = TextMain,
                    fontSize  = 11.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )
            }

            Spacer(Modifier.width(20.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                slots.forEachIndexed { i, label ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(slotColors[i])
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            label.replace("\n", " "),
                            color    = TextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${counts[i]}",
                            color      = if (counts[i] > 0) Gold else TextMuted,
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SessionCard(
    session: CandleSession,
    selectionMode: Boolean,
    isSelected: Boolean,
    onLongClick: () -> Unit,
    onToggleSelect: () -> Unit,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        if (isSelected) Amber else AmberDim.copy(alpha = 0.25f), label = "border"
    )
    val bgColor by animateColorAsState(
        if (isSelected) Color(0xFF2A1800) else BgCard, label = "bg"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick    = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        if (selectionMode) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Amber else BgCard2)
                    .border(1.5.dp, if (isSelected) Amber else AmberDim, CircleShape)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onToggleSelect
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected)
                    Icon(Icons.Default.Check, contentDescription = null,
                        tint = Color(0xFF1A0A00), modifier = Modifier.size(14.dp))
            }
            Spacer(Modifier.width(12.dp))
        }

        Text(
            if (session.completed) "🕯" else "💨",
            fontSize = 26.sp,
            modifier = Modifier.padding(end = 12.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${session.formattedDayOfWeek()}, ${session.formattedDate()}",
                color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("⏰ ${session.formattedTime()}", color = TextMuted, fontSize = 12.sp)
                Text("•", color = TextMuted, fontSize = 12.sp)
                Text("🕐 ${session.durationLabel()}", color = TextMuted, fontSize = 12.sp)
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (session.completed) Success.copy(alpha = 0.12f)
                        else Warning.copy(alpha = 0.12f)
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    if (session.completed) "Tamam" else "Yarım",
                    color    = if (session.completed) Success else Warning,
                    fontSize = 11.sp, fontWeight = FontWeight.Medium
                )
            }
            if (!session.completed) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "${session.completionPercent}%",
                    color = TextMuted, fontSize = 11.sp
                )
            }
        }
    }
}


@Composable
fun SessionDetailScreen(
    session: CandleSession,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = Amber)
            }
            Text(
                "Seans Detayı",
                color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Light,
                letterSpacing = 2.sp, modifier = Modifier.weight(1f).padding(start = 4.dp)
            )
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Sil",
                    tint = Color(0xFFFF6644))
            }
        }

        HorizontalDivider(color = AmberDim.copy(alpha = 0.2f))
        Spacer(Modifier.height(24.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (session.completed) "🕯" else "💨", fontSize = 72.sp)
            Spacer(Modifier.height(10.dp))
            Text(
                if (session.completed) "Tamamlandı" else "Yarıda Kesildi",
                color      = if (session.completed) Success else Warning,
                fontSize   = 22.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 2.sp
            )
        }

        Spacer(Modifier.height(28.dp))

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DetailRow("📅 Tarih",     "${session.formattedDayOfWeek()}, ${session.formattedDate()}")
            DetailRow("🕐 Başlangıç", session.formattedTime())
            DetailRow("⏱ Planlanan",  session.durationLabel())
            if (!session.completed) {
                DetailRow("✅ Gerçekleşen", session.actualDurationLabel())
            }

            Spacer(Modifier.height(4.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(BgCard)
                    .border(1.dp, AmberDim.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tamamlanma", color = TextMuted, fontSize = 12.sp)
                    Text(
                        "${session.completionPercent}%",
                        color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(10.dp))
                LinearProgressBar(fraction = session.completionPercent / 100f)
            }

            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BgCard)
                    .border(1.dp, AmberDim.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                RadialProgressArc(fraction = session.completionPercent / 100f)
            }
        }

        Spacer(Modifier.height(48.dp))
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor   = BgCard2,
            titleContentColor = Gold,
            title = { Text("Bu seans silinsin mi?", fontWeight = FontWeight.Light) },
            text  = { Text("Bu işlem geri alınamaz.", color = TextMuted) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) { Text("Sil", color = Color(0xFFFF6644)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("İptal", color = Amber)
                }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BgCard)
            .border(1.dp, AmberDim.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextMuted, fontSize = 13.sp)
        Text(value, color = TextMain,  fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun LinearProgressBar(fraction: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(AmberDim.copy(alpha = 0.25f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(4.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFFFF8C00), Gold)))
        )
    }
}

@Composable
fun RadialProgressArc(fraction: Float) {
    Box(
        modifier = Modifier.size(140.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(140.dp)) {
            val stroke = 16f
            val radius = (size.minDimension - stroke) / 2f
            val cx = size.width / 2f; val cy = size.height / 2f

            drawArc(
                color = AmberDim.copy(alpha = 0.25f),
                startAngle = -90f, sweepAngle = 360f,
                useCenter = false,
                topLeft   = Offset(cx - radius, cy - radius),
                size      = Size(radius * 2f, radius * 2f),
                style     = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            if (fraction > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(Color(0xFFFF8C00), Gold, Color(0xFFFF8C00)),
                        center = Offset(cx, cy)
                    ),
                    startAngle = -90f, sweepAngle = fraction * 360f,
                    useCenter = false,
                    topLeft   = Offset(cx - radius, cy - radius),
                    size      = Size(radius * 2f, radius * 2f),
                    style     = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${(fraction * 100).toInt()}%",
                color = Gold, fontSize = 28.sp, fontWeight = FontWeight.Light
            )
            Text("tamamlandı", color = TextMuted, fontSize = 11.sp)
        }
    }
}