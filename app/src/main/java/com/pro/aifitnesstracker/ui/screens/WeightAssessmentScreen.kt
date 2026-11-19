package com.pro.aifitnesstracker.ui.screens

import android.app.Activity
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import com.pro.aifitnesstracker.R
import com.pro.aifitnesstracker.ui.components.AssessmentHeader
import com.pro.aifitnesstracker.ui.components.PrimaryButton
import com.pro.aifitnesstracker.ui.theme.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

// ── Constants ───────────────────────────────────────────────────────────────
private const val KG_MIN = 30
private const val KG_MAX = 250
private const val LBS_MIN = 66
private const val LBS_MAX = 550

// 10 minor ticks per 1 kg unit → 0.1 kg visual precision
private const val SUB_TICKS_PER_UNIT = 10

private val CursorColor = BrandPrimary

// ── Main Screen ─────────────────────────────────────────────────────────────
@Composable
fun WeightAssessmentScreen(
    onBack: () -> Unit,
    onContinue: (weightKg: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isKg     by rememberSaveable { mutableStateOf(true) }
    var weightKg by rememberSaveable { mutableIntStateOf(128) }

    val displayValue = if (isKg) weightKg else (weightKg * 2.2046226218).roundToInt()
    val unitLabel    = if (isKg) "kg" else "lbs"
    val minVal       = if (isKg) KG_MIN else LBS_MIN
    val maxVal       = if (isKg) KG_MAX else LBS_MAX

    // Light status-bar icons
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window     = (view.context as? Activity)?.window
        val controller = window?.let { WindowInsetsControllerCompat(it, view) }
        val prev       = controller?.isAppearanceLightStatusBars ?: false
        controller?.isAppearanceLightStatusBars = true
        onDispose { controller?.isAppearanceLightStatusBars = prev }
    }

    // Root: NO horizontal padding – ruler must be edge-to-edge
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AssessmentBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Header ──────────────────────────────────────────────────
            AssessmentHeader(
                stepText    = stringResource(R.string.assessment_step_3),
                onBackClick = onBack,
                modifier    = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(32.dp))

            // ── Title ───────────────────────────────────────────────────
            Text(
                text       = stringResource(R.string.assessment_3_title),
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize   = 28.sp,
                color      = AssessmentTitleColor,
                textAlign  = TextAlign.Center,
                modifier   = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(32.dp))

            // ── kg / lbs toggle – full width, square card ────────────────
            UnitToggle(
                isKg     = isKg,
                onToggle = { isKg = it },
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(44.dp))

            // ── Large weight display ─────────────────────────────────────
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(
                        fontSize   = 72.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = AssessmentTitleColor,
                        fontFamily = SourceSansPro
                    )) { append(displayValue.toString()) }
                    withStyle(SpanStyle(
                        fontSize   = 28.sp,
                        fontWeight = FontWeight.Medium,
                        color      = TextSecondary,
                        fontFamily = SourceSansPro
                    )) { append(unitLabel) }
                }
            )

            Spacer(Modifier.height(32.dp))

            // ── Edge-to-edge ruler (no horizontal padding) ───────────────
            // key(isKg) resets the ruler state when units switch
            key(isKg) {
                WeightRuler(
                    initialValue  = displayValue,
                    minValue      = minVal,
                    maxValue      = maxVal,
                    onValueChange = { newDisplay ->
                        if (newDisplay != displayValue) {
                            weightKg = if (isKg) newDisplay
                            else (newDisplay / 2.2046226218).roundToInt().coerceIn(KG_MIN, KG_MAX)
                        }
                    }
                )
            }

            Spacer(Modifier.weight(1f))

            // ── Continue button ──────────────────────────────────────────
            PrimaryButton(
                text           = stringResource(R.string.continue_cta),
                onClick        = { onContinue(weightKg) },
                hasTrailingArrow = true,
                modifier       = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 20.dp)
            )
        }
    }
}

// ── Unit Toggle ─────────────────────────────────────────────────────────────
@Composable
private fun UnitToggle(
    isKg: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardUnselectedBg)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(true to "kg", false to "lbs").forEach { (kg, label) ->
            val selected = kg == isKg
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) BrandPrimary else Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onToggle(kg) }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = label,
                    fontFamily = SourceSansPro,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 15.sp,
                    color      = if (selected) Color.White else TextSecondary
                )
            }
        }
    }
}

// ── Weight Ruler ─────────────────────────────────────────────────────────────
//
// Layout (all heights are physical):
//
//   y=0 ─────────────── TOP of composable
//       │  ┌───┐        ← cursor top (CursorAboveDp above ticks)
//       │  │   │
//       │  │ P │  ← purple rounded-rect cursor, fixed at centerX
//       │  │ U │
//       │  │ R │  ─── tick zone top  (ticks hang from tickBottomY)
//       │  │ P │  ─── ticks
//       │  │ L │  ─── tick zone bottom  (= cursorBottomY)
//       │  │ E │
//       │  └───┘
//       │  127   128   129   ← labels below cursor
//   y=total ─────────────── BOTTOM of composable
//
// scrollPx > 0 → ruler scrolled RIGHT → lower value comes to center
//
@Composable
private fun WeightRuler(
    initialValue: Int,
    minValue: Int,
    maxValue: Int,
    onValueChange: (Int) -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val originValue = remember { initialValue }

    val minorSpacingPx = with(density) { 10.dp.toPx() }
    val unitSpacingPx = minorSpacingPx * SUB_TICKS_PER_UNIT

    val cursorWidthPx = with(density) { 14.dp.toPx() }
    val cursorHeightPx = with(density) { 114.dp.toPx() }
    val cursorRadiusPx = with(density) { 7.dp.toPx() }
    val cursorClearancePx = with(density) { 26.dp.toPx() }

    val minorTickHeightPx = with(density) { 24.dp.toPx() }
    val midTickHeightPx = with(density) { 46.dp.toPx() }
    val majorTickHeightPx = with(density) { 58.dp.toPx() }

    val minorStrokePx = with(density) { 1.8.dp.toPx() }
    val majorStrokePx = with(density) { 3.dp.toPx() }

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
    val labelTextSizePx = with(density) { 12.sp.toPx() }

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
            .height(148.dp)
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
                            scrollAnim.animateDecay(velocity, decay) {
                                scroll = value
                            }
                            scrollAnim.updateBounds(
                                Float.NEGATIVE_INFINITY,
                                Float.POSITIVE_INFINITY
                            )
                        }
                        val snapped =
                            (scrollAnim.value / unitSpacingPx).roundToInt() * unitSpacingPx
                        scrollAnim.animateTo(
                            targetValue = snapped.coerceIn(minScroll, maxScroll),
                            animationSpec = spring(
                                dampingRatio = 0.86f,
                                stiffness = Spring.StiffnessLow
                            )
                        ) {
                            scroll = value
                        }
                    }
                }
            }
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val centerX = widthPx / 2f
        val rulerCenterY = cursorHeightPx / 2f
        val labelY = rulerCenterY + majorTickHeightPx / 2f + with(density) { 24.dp.toPx() }
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
                        val halfH = tickH / 2f
                        val strokeW = if (isMajor || isMid) majorStrokePx else minorStrokePx
                        val color = if (isMajor || isMid) majorColor else minorColor

                        drawLine(
                            color = color,
                            start = Offset(x, rulerCenterY - halfH),
                            end = Offset(x, rulerCenterY + halfH),
                            strokeWidth = strokeW,
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
                        color = CursorColor,
                        topLeft = Offset(cursorLeft, 0f),
                        size = Size(cursorWidthPx, cursorHeightPx),
                        cornerRadius = CornerRadius(cursorRadiusPx, cursorRadiusPx)
                    )
                }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun WeightAssessmentScreenPreview() {
    AIFitnessTrackerTheme {
        WeightAssessmentScreen(onBack = {}, onContinue = {})
    }
}
