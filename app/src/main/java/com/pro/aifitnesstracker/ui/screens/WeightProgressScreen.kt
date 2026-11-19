package com.pro.aifitnesstracker.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import com.pro.aifitnesstracker.R
import com.pro.aifitnesstracker.ui.theme.AIFitnessTrackerTheme
import com.pro.aifitnesstracker.ui.theme.AssessmentTitleColor
import com.pro.aifitnesstracker.ui.theme.BackButtonBg
import com.pro.aifitnesstracker.ui.theme.SourceSansPro
import com.pro.aifitnesstracker.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private val WeightPurple = Color(0xFF635BFF)
private val TargetGreen = Color(0xFFB8E63A)
private val TargetGreenSoft = Color(0xFFEAF8C8)
private val GraphGreen = Color(0xFF9ED96A)
private val TrendUp = Color(0xFFE55B5B)
private val TrendDown = Color(0xFF6DBF4A)

private enum class WeightDialog {
    None,
    Current,
    Target,
    Log
}

@Composable
fun WeightProgressScreen(
    state: WeightTrackerState,
    onStateChange: (WeightTrackerState) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRecentLogs by remember { mutableStateOf(false) }
    var dialog by remember { mutableStateOf(WeightDialog.None) }
    var pendingCurrent by remember { mutableStateOf<Float?>(null) }

    if (showRecentLogs) {
        BackHandler { showRecentLogs = false }
        RecentWeightLogsScreen(
            state = state,
            onStateChange = onStateChange,
            onBack = { showRecentLogs = false },
            onAddLog = { dialog = WeightDialog.Log }
        )
        if (dialog == WeightDialog.Log) {
            WeightPickerDialog(
                mode = WeightPickerMode.Log,
                initialWeightKg = state.currentWeightKg,
                onDismiss = { dialog = WeightDialog.None },
                onConfirm = { kg ->
                    onStateChange(state.withLog(kg))
                    dialog = WeightDialog.None
                }
            )
        }
        return
    }

    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowInsetsControllerCompat(it, view) }
        val previous = controller?.isAppearanceLightStatusBars ?: false
        controller?.isAppearanceLightStatusBars = true
        onDispose { controller?.isAppearanceLightStatusBars = previous }
    }

    val recentPreview = state.logs.take(2)
    val weekLogs = remember(state.logs) {
        val weekAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
        state.logs.filter { it.loggedAtMillis >= weekAgo }.sortedBy { it.loggedAtMillis }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFFFCFCFF), Color(0xFFF3F1FB)))
                )
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BackButtonBg)
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back_chevron),
                            contentDescription = stringResource(R.string.back),
                            tint = AssessmentTitleColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.weight_progress),
                        fontFamily = SourceSansPro,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AssessmentTitleColor
                    )
                }

                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    WeightSummaryCard(
                        label = stringResource(R.string.current_weight),
                        value = formatWeightOrUnset(state.currentWeightKg),
                        buttonColor = WeightPurple,
                        imageRes = R.drawable.current_weight,
                        onEdit = { dialog = WeightDialog.Current },
                        modifier = Modifier.weight(1f)
                    )
                    WeightSummaryCard(
                        label = stringResource(R.string.target_weight),
                        value = formatWeightOrUnset(state.targetWeightKg),
                        buttonColor = TargetGreen,
                        imageRes = R.drawable.target_weight,
                        onEdit = { dialog = WeightDialog.Target },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(22.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.progress),
                        fontFamily = SourceSansPro,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = AssessmentTitleColor,
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(TargetGreen)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.week),
                            fontFamily = SourceSansPro,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = AssessmentTitleColor
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(R.drawable.ic_dropdown),
                            contentDescription = null,
                            tint = AssessmentTitleColor,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                WeightProgressGraphCard(logs = weekLogs)

                Spacer(Modifier.height(22.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.recent_weight_logs),
                        fontFamily = SourceSansPro,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = AssessmentTitleColor,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = stringResource(R.string.view_all),
                        fontFamily = SourceSansPro,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = WeightPurple,
                        modifier = Modifier.clickable { showRecentLogs = true }
                    )
                }
                Spacer(Modifier.height(12.dp))
                if (recentPreview.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_record_found),
                            fontFamily = SourceSansPro,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = AssessmentTitleColor
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        recentPreview.forEachIndexed { index, entry ->
                            val previous = state.logs.getOrNull(index + 1)
                            WeightLogRow(
                                entry = entry,
                                compareToKg = previous?.weightKg ?: state.targetWeightKg
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            AddDailyLogButton(
                onClick = { dialog = WeightDialog.Log },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp)
            )
        }

        if (dialog == WeightDialog.Current || dialog == WeightDialog.Target) {
            WeightPickerDialog(
                mode = if (dialog == WeightDialog.Current) {
                    WeightPickerMode.Current
                } else {
                    WeightPickerMode.Target
                },
                initialWeightKg = if (dialog == WeightDialog.Current) {
                    state.currentWeightKg
                } else {
                    state.targetWeightKg
                },
                onDismiss = {
                    pendingCurrent = null
                    dialog = WeightDialog.None
                },
                onConfirm = { kg ->
                    if (dialog == WeightDialog.Current) {
                        pendingCurrent = kg
                        dialog = WeightDialog.Target
                    } else {
                        val current = pendingCurrent
                        pendingCurrent = null
                        onStateChange(
                            state.copy(
                                currentWeightKg = current ?: state.currentWeightKg,
                                targetWeightKg = kg
                            )
                        )
                        dialog = WeightDialog.None
                    }
                }
            )
        } else if (dialog == WeightDialog.Log) {
            WeightPickerDialog(
                mode = WeightPickerMode.Log,
                initialWeightKg = state.currentWeightKg,
                onDismiss = { dialog = WeightDialog.None },
                onConfirm = { kg ->
                    onStateChange(state.withLog(kg))
                    dialog = WeightDialog.None
                }
            )
        }
    }
}

@Composable
private fun WeightSummaryCard(
    label: String,
    value: String,
    buttonColor: Color,
    @DrawableRes imageRes: Int,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .height(96.dp)
            .shadow(6.dp, cardShape, ambientColor = Color(0x14000000))
            .clip(cardShape)
            .background(Color.White)
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 10.dp, y = 14.dp)
                .size(78.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = label,
                    fontFamily = SourceSansPro,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = value,
                    fontFamily = SourceSansPro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = AssessmentTitleColor
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(buttonColor)
                    .clickable(onClick = onEdit)
                    .padding(horizontal = 9.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_edit_pencil),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.edit),
                    fontFamily = SourceSansPro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun WeightProgressGraphCard(logs: List<WeightLogEntry>) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .border(1.5.dp, WeightPurple.copy(alpha = 0.35f), shape)
            .clip(shape)
            .background(
                Brush.verticalGradient(listOf(Color(0xFFEAF8C8), Color(0xFFF7FBEA)))
            )
            .padding(16.dp)
    ) {
        if (logs.isEmpty()) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(6.dp)
            ) {
                val path = Path().apply {
                    moveTo(0f, size.height * 0.55f)
                    cubicTo(
                        size.width * 0.2f, size.height * 0.2f,
                        size.width * 0.45f, size.height * 0.85f,
                        size.width * 0.7f, size.height * 0.4f
                    )
                    cubicTo(
                        size.width * 0.85f, size.height * 0.2f,
                        size.width * 0.95f, size.height * 0.55f,
                        size.width, size.height * 0.35f
                    )
                }
                drawPath(path, GraphGreen.copy(alpha = 0.35f), style = Stroke(6.dp.toPx(), cap = StrokeCap.Round))
            }
            Text(
                text = stringResource(R.string.no_record_found),
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AssessmentTitleColor,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            val latest = logs.last().weightKg
            val first = logs.first().weightKg
            val delta = latest - first
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (logs.size == 1) {
                    val y = size.height * 0.45f
                    drawLine(
                        color = GraphGreen,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                } else {
                    val minW = logs.minOf { it.weightKg }
                    val maxW = logs.maxOf { it.weightKg }
                    val span = (maxW - minW).coerceAtLeast(1f)
                    val path = Path()
                    logs.forEachIndexed { index, entry ->
                        val x = size.width * (index / (logs.size - 1).toFloat())
                        val y = size.height * (0.7f - ((entry.weightKg - minW) / span) * 0.5f)
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, GraphGreen, style = Stroke(5.dp.toPx(), cap = StrokeCap.Round))
                }
            }
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Text(
                    text = stringResource(R.string.weight_value_kg, latest),
                    fontFamily = SourceSansPro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = AssessmentTitleColor
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(
                            if (delta > 0f) R.drawable.ic_trend_up else R.drawable.ic_trend_down
                        ),
                        contentDescription = null,
                        tint = if (delta > 0f) TrendUp else TrendDown,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(
                            R.string.weight_change_this_month,
                            formatSignedKg(delta)
                        ),
                        fontFamily = SourceSansPro,
                        fontSize = 13.sp,
                        color = AssessmentTitleColor
                    )
                }
            }
        }
    }
}

@Composable
fun WeightLogRow(
    entry: WeightLogEntry,
    compareToKg: Float?,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    val trendColor = when {
        compareToKg == null -> TextSecondary
        entry.weightKg > compareToKg -> TrendUp
        else -> TrendDown
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x10000000))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(TargetGreenSoft),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.footsteps_weight_progress),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = formatLogDate(entry.loggedAtMillis),
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = AssessmentTitleColor
            )
            Text(
                text = formatLogTime(entry.loggedAtMillis),
                fontFamily = SourceSansPro,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = buildString {
                    append(entry.weightKg.toInt())
                    append(stringResource(R.string.weight_unit_kg))
                },
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = AssessmentTitleColor
            )
            if (compareToKg != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(
                            if (entry.weightKg > compareToKg) R.drawable.ic_trend_up
                            else R.drawable.ic_trend_down
                        ),
                        contentDescription = null,
                        tint = trendColor,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = "${compareToKg.toInt()}${stringResource(R.string.weight_unit_kg)}",
                        fontFamily = SourceSansPro,
                        fontSize = 11.sp,
                        color = trendColor
                    )
                }
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(10.dp))
            trailing()
        }
    }
}

@Composable
fun AddDailyLogButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(WeightPurple)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .border(1.5.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_plus),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = stringResource(R.string.add_daily_log),
            fontFamily = SourceSansPro,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.White
        )
    }
}

@Composable
private fun formatWeightOrUnset(weightKg: Float?): String {
    return if (weightKg == null) {
        stringResource(R.string.weight_unset)
    } else {
        stringResource(R.string.weight_value_kg, weightKg)
    }
}

private fun formatLogDate(millis: Long): String {
    val cal = Calendar.getInstance()
    val today = Calendar.getInstance()
    cal.timeInMillis = millis
    val sameDay = cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
        cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    return if (sameDay) "Today"
    else SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date(millis))
}

private fun formatLogTime(millis: Long): String =
    SimpleDateFormat("h:mma", Locale.getDefault()).format(Date(millis)).lowercase(Locale.getDefault())

private fun formatSignedKg(delta: Float): String =
    String.format(Locale.US, "%+.1fkg", delta)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun WeightProgressEmptyPreview() {
    AIFitnessTrackerTheme {
        WeightProgressScreen(
            state = WeightTrackerState(),
            onStateChange = {},
            onBack = {}
        )
    }
}
