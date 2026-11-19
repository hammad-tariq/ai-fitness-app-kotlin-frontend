package com.pro.aifitnesstracker.ui.screens

import android.graphics.Paint as AndroidPaint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pro.aifitnesstracker.R
import com.pro.aifitnesstracker.ui.theme.AssessmentTitleColor
import com.pro.aifitnesstracker.ui.theme.CardUnselectedBg
import com.pro.aifitnesstracker.ui.theme.SourceSansPro
import com.pro.aifitnesstracker.ui.theme.TextSecondary
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private const val KG_MIN = 30
private const val KG_MAX = 250
private const val LBS_MIN = 66
private const val LBS_MAX = 550
private const val SUB_TICKS_PER_UNIT = 10
private val WeightAccent = Color(0xFF635BFF)
private val DialogCancelBg = Color(0xFFC8C8C8)

enum class WeightPickerMode {
    Current,
    Target,
    Log
}

@Composable
fun WeightPickerDialog(
    mode: WeightPickerMode,
    initialWeightKg: Float?,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    var isKg by remember { mutableStateOf(true) }
    var weightKg by remember {
        mutableIntStateOf((initialWeightKg ?: 70f).roundToInt().coerceIn(KG_MIN, KG_MAX))
    }
    LaunchedEffect(mode, initialWeightKg) {
        isKg = true
        weightKg = (initialWeightKg ?: 70f).roundToInt().coerceIn(KG_MIN, KG_MAX)
    }
    val displayValue = if (isKg) weightKg else (weightKg * 2.2046226218).roundToInt()
    val unitLabel = if (isKg) stringResource(R.string.weight_unit_kg) else stringResource(R.string.weight_unit_lbs)
    val minVal = if (isKg) KG_MIN else LBS_MIN
    val maxVal = if (isKg) KG_MAX else LBS_MAX
    val title = when (mode) {
        WeightPickerMode.Current -> stringResource(R.string.current_weight)
        WeightPickerMode.Target -> stringResource(R.string.target_weight)
        WeightPickerMode.Log -> stringResource(R.string.log_weight)
    }
    val subtitle = when (mode) {
        WeightPickerMode.Current -> stringResource(R.string.current_weight_subtitle)
        WeightPickerMode.Target -> stringResource(R.string.target_weight_subtitle)
        WeightPickerMode.Log -> null
    }
    val confirmLabel = when (mode) {
        WeightPickerMode.Current -> stringResource(R.string.next)
        WeightPickerMode.Target, WeightPickerMode.Log -> stringResource(R.string.save)
    }
    val todayLabel = stringResource(R.string.today)
    val nowLabel = remember(todayLabel) {
        val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        "$todayLabel, $time"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            decorFitsSystemWindows = true
        )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 22.dp)
                .fillMaxWidth()
                .shadow(24.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .padding(top = 24.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = AssessmentTitleColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            // Fixed header slot so Current -> Target does not jump/jerk
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    mode == WeightPickerMode.Log -> Text(
                        text = nowLabel,
                        fontFamily = SourceSansPro,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    subtitle != null -> Text(
                        text = subtitle,
                        fontFamily = SourceSansPro,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            DialogUnitToggle(
                isKg = isKg,
                onToggle = { isKg = it },
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(22.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AssessmentTitleColor,
                            fontFamily = SourceSansPro
                        )
                    ) { append(displayValue.toString()) }
                    withStyle(
                        SpanStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            fontFamily = SourceSansPro
                        )
                    ) { append(unitLabel) }
                }
            )

            Spacer(Modifier.height(12.dp))
            key(mode, isKg) {
                DialogWeightRuler(
                    initialValue = displayValue,
                    minValue = minVal,
                    maxValue = maxVal,
                    onValueChange = { newDisplay ->
                        if (newDisplay != displayValue) {
                            weightKg = if (isKg) {
                                newDisplay
                            } else {
                                (newDisplay / 2.2046226218).roundToInt().coerceIn(KG_MIN, KG_MAX)
                            }
                        }
                    }
                )
            }

            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DialogActionButton(
                    text = stringResource(R.string.cancel),
                    background = DialogCancelBg,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
                DialogActionButton(
                    text = confirmLabel,
                    background = WeightAccent,
                    onClick = { onConfirm(weightKg.toFloat()) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DialogActionButton(
    text: String,
    background: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = SourceSansPro,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.White
        )
    }
}

@Composable
private fun DialogUnitToggle(
    isKg: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(CardUnselectedBg)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(true to stringResource(R.string.weight_unit_kg), false to stringResource(R.string.weight_unit_lbs))
            .forEach { (kg, label) ->
                val selected = kg == isKg
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(if (selected) WeightAccent else Color.Transparent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onToggle(kg) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontFamily = SourceSansPro,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (selected) Color.White else TextSecondary
                    )
                }
            }
    }
}

@Composable
private fun DialogWeightRuler(
    initialValue: Int,
    minValue: Int,
    maxValue: Int,
    onValueChange: (Int) -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val originValue = remember { initialValue }
    val minorSpacingPx = with(density) { 8.dp.toPx() }
    val unitSpacingPx = minorSpacingPx * SUB_TICKS_PER_UNIT
    val cursorWidthPx = with(density) { 12.dp.toPx() }
    val cursorHeightPx = with(density) { 88.dp.toPx() }
    val cursorRadiusPx = with(density) { 6.dp.toPx() }
    val cursorClearancePx = with(density) { 20.dp.toPx() }
    val minorTickHeightPx = with(density) { 18.dp.toPx() }
    val midTickHeightPx = with(density) { 34.dp.toPx() }
    val majorTickHeightPx = with(density) { 44.dp.toPx() }
    val minorStrokePx = with(density) { 1.6.dp.toPx() }
    val majorStrokePx = with(density) { 2.6.dp.toPx() }
    val minorColor = Color(0xFFD5D5D8)
    val majorColor = Color(0xFFB8B8BC)
    val labelPaint = remember {
        AndroidPaint().apply {
            textAlign = AndroidPaint.Align.CENTER
            color = android.graphics.Color.parseColor("#71717A")
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
        }
    }
    val labelTextSizePx = with(density) { 11.sp.toPx() }
    val minScroll = -(maxValue - originValue).toFloat() * unitSpacingPx
    val maxScroll = (originValue - minValue).toFloat() * unitSpacingPx
    var scroll by remember { mutableFloatStateOf(0f) }
    val scrollAnim = remember { Animatable(0f) }
    var flingJob by remember { mutableStateOf<Job?>(null) }
    val currentValue by remember {
        derivedStateOf {
            (originValue - (scroll / unitSpacingPx).roundToInt())
                .coerceIn(minValue, maxValue)
        }
    }
    LaunchedEffect(currentValue) { onValueChange(currentValue) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .pointerInput(minScroll, maxScroll, unitSpacingPx) {
                val decay = splineBasedDecay<Float>(this)
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    flingJob?.cancel()
                    val velocityTracker = VelocityTracker()
                    velocityTracker.addPosition(down.uptimeMillis, down.position)
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break
                        if (!change.pressed) {
                            change.consume()
                            break
                        }
                        val dx = change.position.x - change.previousPosition.x
                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                        change.consume()
                        scroll = (scroll + dx).coerceIn(minScroll, maxScroll)
                    }
                    val velocity = velocityTracker.calculateVelocity().x
                    flingJob = scope.launch {
                        scrollAnim.snapTo(scroll)
                        if (abs(velocity) > 250f) {
                            scrollAnim.updateBounds(minScroll, maxScroll)
                            scrollAnim.animateDecay(velocity, decay) { scroll = value }
                            scrollAnim.updateBounds(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY)
                        }
                        val snapped = (scrollAnim.value / unitSpacingPx).roundToInt() * unitSpacingPx
                        scrollAnim.animateTo(
                            targetValue = snapped.coerceIn(minScroll, maxScroll),
                            animationSpec = spring(dampingRatio = 0.86f, stiffness = Spring.StiffnessLow)
                        ) { scroll = value }
                    }
                }
            }
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val centerX = widthPx / 2f
        val rulerCenterY = cursorHeightPx / 2f
        val labelY = rulerCenterY + majorTickHeightPx / 2f + with(density) { 18.dp.toPx() }
        val cursorLeft = centerX - cursorWidthPx / 2f
        val offset = scroll
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    labelPaint.textSize = labelTextSizePx
                    val firstSi = kotlin.math.floor((-centerX - offset) / minorSpacingPx).toInt() - 4
                    val lastSi = kotlin.math.ceil((widthPx - centerX - offset) / minorSpacingPx).toInt() + 4
                    for (si in firstSi..lastSi) {
                        val x = centerX + si * minorSpacingPx + offset
                        if (x < -minorSpacingPx || x > widthPx + minorSpacingPx) continue
                        val isMajor = si % SUB_TICKS_PER_UNIT == 0
                        val isMid = !isMajor && si % (SUB_TICKS_PER_UNIT / 2) == 0
                        val tickH = when {
                            isMajor -> majorTickHeightPx
                            isMid -> midTickHeightPx
                            else -> minorTickHeightPx
                        }
                        drawLine(
                            color = if (isMajor || isMid) majorColor else minorColor,
                            start = Offset(x, rulerCenterY - tickH / 2f),
                            end = Offset(x, rulerCenterY + tickH / 2f),
                            strokeWidth = if (isMajor || isMid) majorStrokePx else minorStrokePx,
                            cap = StrokeCap.Round
                        )
                        if (isMajor && abs(x - centerX) > cursorClearancePx) {
                            val tickLabel = originValue + si.floorDiv(SUB_TICKS_PER_UNIT)
                            if (tickLabel in minValue..maxValue) {
                                drawContext.canvas.nativeCanvas.drawText(
                                    tickLabel.toString(),
                                    x,
                                    labelY,
                                    labelPaint
                                )
                            }
                        }
                    }
                    drawRoundRect(
                        color = WeightAccent,
                        topLeft = Offset(cursorLeft, 0f),
                        size = Size(cursorWidthPx, cursorHeightPx),
                        cornerRadius = CornerRadius(cursorRadiusPx, cursorRadiusPx)
                    )
                }
        )
    }
}

@Composable
fun DeleteWeightLogsDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 36.dp)
                .fillMaxWidth()
                .shadow(24.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .padding(horizontal = 22.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFFFE8E8))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    painter = androidx.compose.ui.res.painterResource(R.drawable.ic_trash),
                    contentDescription = null,
                    tint = Color(0xFFE55B5B),
                    modifier = Modifier.height(28.dp).width(28.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.delete_recent_logs_title),
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AssessmentTitleColor,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.delete_recent_logs_message),
                fontFamily = SourceSansPro,
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(22.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DialogActionButton(
                    text = stringResource(R.string.cancel),
                    background = DialogCancelBg,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
                DialogActionButton(
                    text = stringResource(R.string.delete),
                    background = Color(0xFFE55B5B),
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
