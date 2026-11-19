package com.pro.aifitnesstracker.ui.screens

import android.app.Activity
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import com.pro.aifitnesstracker.R
import com.pro.aifitnesstracker.ui.components.PrimaryButton
import com.pro.aifitnesstracker.ui.theme.AIFitnessTrackerTheme
import com.pro.aifitnesstracker.ui.theme.AssessmentTitleColor
import com.pro.aifitnesstracker.ui.theme.BackButtonBg
import com.pro.aifitnesstracker.ui.theme.SourceSansPro
import com.pro.aifitnesstracker.ui.theme.TextSecondary
private val ActivityPurple = Color(0xFF635BFF)
private val ChartCardBg = Color(0xFFEAF8FD)
private val ChartCardBorder = Color(0xFFC8E8F5)
private val ChartBar = Color(0xFF6EC4E0)
private val WeekPill = Color(0xFF6EC4E0)
private val CaloriesTile = Color(0xFFF7E5FF)
private val DistanceTile = Color(0xFFFFF0E5)
private val StepsTile = Color(0xFFEBE5FF)
private val EmptyCardBorder = Color(0xFFD6EAF8)

private data class ActivityDay(
    val label: String,
    val distanceKm: Float,
    val kcal: Int,
    val barFraction: Float
)

private val sampleWeek = listOf(
    ActivityDay("M", 0.7f, 32, 0.42f),
    ActivityDay("T", 0.9f, 42, 0.58f),
    ActivityDay("W", 0.6f, 28, 0.36f),
    ActivityDay("T", 1.1f, 50, 0.78f),
    ActivityDay("F", 0.8f, 38, 0.50f),
    ActivityDay("S", 1.0f, 46, 0.68f),
    ActivityDay("S", 0.7f, 32, 0.44f)
)

@Composable
fun ActivityScreen(
    onBack: () -> Unit,
    onStartWorkout: () -> Unit = {},
    hasRecords: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showRecords by remember(hasRecords) { mutableStateOf(hasRecords) }
    val weekDays = if (showRecords) sampleWeek else emptyList()

    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowInsetsControllerCompat(it, view) }
        val previous = controller?.isAppearanceLightStatusBars ?: false
        controller?.isAppearanceLightStatusBars = true
        onDispose { controller?.isAppearanceLightStatusBars = previous }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFCFCFF), Color(0xFFF4F2FE))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
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
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.activity),
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AssessmentTitleColor
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (weekDays.isEmpty()) {
            ActivityEmptyState(
                onStartWorkout = {
                    showRecords = true
                    onStartWorkout()
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            )
        } else {
            ActivityRecordsContent(
                days = weekDays,
                onStartWorkout = onStartWorkout,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ActivityEmptyState(
    onStartWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(28.dp)
    Box(
        modifier = modifier
            .border(1.5.dp, EmptyCardBorder, cardShape)
            .clip(cardShape)
            .background(Color.White)
    ) {
        ActivityEmptyBackdrop(
            modifier = Modifier
                .fillMaxSize()
                .blur(14.dp)
                .padding(horizontal = 18.dp, vertical = 22.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.42f))
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.no_record_found),
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = AssessmentTitleColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.activity_empty_subtitle),
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(22.dp))
            PrimaryButton(
                text = stringResource(R.string.start_workout),
                onClick = onStartWorkout,
                backgroundColor = ActivityPurple,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(0.86f)
            )
        }
    }
}

@Composable
private fun ActivityRecordsContent(
    days: List<ActivityDay>,
    onStartWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalKm = days.sumOf { it.distanceKm.toDouble() }.toFloat()
    val totalKcal = days.sumOf { it.kcal }
    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            ActivityWeekChartCard(
                totalKm = totalKm,
                totalKcal = totalKcal,
                days = days,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActivityStatCard(
                    iconRes = R.drawable.fire,
                    label = stringResource(R.string.est_calories),
                    value = stringResource(R.string.kcal_value, 760),
                    background = CaloriesTile,
                    modifier = Modifier.weight(1f)
                )
                ActivityStatCard(
                    iconRes = R.drawable.pin,
                    label = stringResource(R.string.est_distance),
                    value = stringResource(R.string.km_value, 14.5f),
                    background = DistanceTile,
                    modifier = Modifier.weight(1f)
                )
                ActivityStatCard(
                    iconRes = R.drawable.steps,
                    label = stringResource(R.string.step_count),
                    value = stringResource(R.string.step_count_value, 15_000),
                    background = StepsTile,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        PrimaryButton(
            text = stringResource(R.string.start_workout),
            onClick = onStartWorkout,
            backgroundColor = ActivityPurple,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )
    }
}

@Composable
private fun ActivityWeekChartCard(
    totalKm: Float,
    totalKcal: Int,
    days: List<ActivityDay>,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = modifier
            .border(1.5.dp, ChartCardBorder, shape)
            .clip(shape)
            .background(ChartCardBg)
            .padding(horizontal = 14.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.activity_total_km, totalKm),
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = AssessmentTitleColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.activity_total_kcal, totalKcal),
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(WeekPill)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.week),
                    fontFamily = SourceSansPro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_dropdown),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            days.forEach { day ->
                ActivityDayColumn(
                    day = day,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun ActivityDayColumn(
    day: ActivityDay,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.activity_bar_kcal, day.kcal),
            fontFamily = SourceSansPro,
            fontWeight = FontWeight.Medium,
            fontSize = 9.sp,
            color = AssessmentTitleColor,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .width(18.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barWidth = size.width
                val maxHeight = size.height * 0.92f
                val barHeight = maxHeight * day.barFraction.coerceIn(0.18f, 1f)
                val left = (size.width - barWidth) / 2f
                val top = size.height - barHeight
                drawRoundRect(
                    color = ChartBar,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.activity_bar_km, day.distanceKm),
            fontFamily = SourceSansPro,
            fontWeight = FontWeight.Medium,
            fontSize = 9.sp,
            color = AssessmentTitleColor,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = day.label,
            fontFamily = SourceSansPro,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = AssessmentTitleColor
        )
    }
}

@Composable
private fun ActivityStatCard(
    @DrawableRes iconRes: Int,
    label: String,
    value: String,
    background: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .border(1.5.dp, Color.White.copy(alpha = 0.9f), CircleShape)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = label,
            fontFamily = SourceSansPro,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = AssessmentTitleColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontFamily = SourceSansPro,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = AssessmentTitleColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ActivityEmptyBackdrop(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (size.width <= 0f || size.height <= 0f) return@Canvas
            val bars = floatArrayOf(0.35f, 0.62f, 0.48f, 0.88f, 0.55f, 0.72f, 0.42f)
            val gap = size.width / bars.size
            val stroke = 14.dp.toPx()
            bars.forEachIndexed { index, value ->
                val x = gap * index + gap / 2f
                drawLine(
                    color = ChartBar.copy(alpha = 0.85f),
                    start = Offset(x, size.height * 0.92f),
                    end = Offset(x, size.height * (1f - value.coerceIn(0.15f, 0.95f))),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActivityStatCard(
                iconRes = R.drawable.fire,
                label = stringResource(R.string.est_calories),
                value = "264 kcal",
                background = CaloriesTile,
                modifier = Modifier.weight(1f)
            )
            ActivityStatCard(
                iconRes = R.drawable.pin,
                label = stringResource(R.string.est_distance),
                value = "5.8 km",
                background = DistanceTile,
                modifier = Modifier.weight(1f)
            )
            ActivityStatCard(
                iconRes = R.drawable.steps,
                label = stringResource(R.string.step_count),
                value = "8,420",
                background = StepsTile,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun ActivityScreenEmptyPreview() {
    AIFitnessTrackerTheme {
        ActivityScreen(onBack = {}, hasRecords = false)
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun ActivityScreenRecordsPreview() {
    AIFitnessTrackerTheme {
        ActivityScreen(onBack = {}, hasRecords = true)
    }
}
