package com.pro.aifitnesstracker.ui.screens

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowInsetsControllerCompat
import com.pro.aifitnesstracker.R
import com.pro.aifitnesstracker.ui.components.PrimaryButton
import com.pro.aifitnesstracker.ui.theme.AIFitnessTrackerTheme
import com.pro.aifitnesstracker.ui.theme.AssessmentTitleColor
import com.pro.aifitnesstracker.ui.theme.BackButtonBg
import com.pro.aifitnesstracker.ui.theme.SourceSansPro
import com.pro.aifitnesstracker.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.roundToInt

private const val GoalMin = 1_000
private const val GoalDefaultMax = 30_000
private const val GoalCeiling = 100_000
private val StepsPurple = Color(0xFF644CFA)
private val StepsTrack = Color(0xFFE6E2FB)
private val SliderTrack = Color(0xFFD9D9D9)
private val ActivePill = Color(0xFFFFE1DC)
private val ActiveText = Color(0xFFE57764)
private val CaloriesCard = Color(0xFFF3DEFB)
private val DistanceCard = Color(0xFFFAEAE7)
private val DialogInputBg = Color(0xFFF4F4F4)
private val DialogCancelBg = Color(0xFFC8C8C8)

@Composable
fun StepsDailyScreen(
    initialGoal: Int = 15_000,
    onBack: () -> Unit,
    onSetGoal: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var maxGoal by remember(initialGoal) {
        mutableIntStateOf(maxOf(GoalDefaultMax, initialGoal).coerceAtMost(GoalCeiling))
    }
    var goal by remember(initialGoal) {
        mutableIntStateOf(initialGoal.coerceIn(GoalMin, GoalCeiling))
    }
    var showManualGoalDialog by remember { mutableStateOf(false) }
    val calories = (goal * 760f / 15_000f).roundToInt()
    val distanceKm = goal * 14.5f / 15_000f
    val onGoalChange: (Int, Int) -> Unit = { nextGoal, nextMax ->
        val resolvedMax = nextMax.coerceIn(GoalDefaultMax, GoalCeiling)
        maxGoal = resolvedMax
        goal = nextGoal.coerceIn(GoalMin, resolvedMax)
    }

    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowInsetsControllerCompat(it, view) }
        val previous = controller?.isAppearanceLightStatusBars ?: false
        controller?.isAppearanceLightStatusBars = true
        onDispose { controller?.isAppearanceLightStatusBars = previous }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
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
                    text = stringResource(R.string.steps_daily),
                    fontFamily = SourceSansPro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = AssessmentTitleColor
                )
            }

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val ring = minOf(maxWidth, maxHeight).coerceAtMost(280.dp)
                StepsGoalRing(
                    goal = goal,
                    maxGoal = maxGoal,
                    onValueChange = onGoalChange,
                    modifier = Modifier.size(ring)
                )
            }

            Text(
                text = stringResource(R.string.current_goal),
                fontFamily = SourceSansPro,
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = formatSteps(goal),
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = AssessmentTitleColor,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            GoalSlider(
                value = goal,
                maxGoal = maxGoal,
                onValueChange = onGoalChange,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatSteps(GoalMin),
                    fontFamily = SourceSansPro,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = formatSteps(maxGoal),
                    fontFamily = SourceSansPro,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StepStatCard(
                    iconRes = R.drawable.fire,
                    label = stringResource(R.string.est_calories),
                    value = stringResource(R.string.kcal_value, calories),
                    background = CaloriesCard,
                    modifier = Modifier.weight(1f)
                )
                StepStatCard(
                    iconRes = R.drawable.pin,
                    label = stringResource(R.string.est_distance),
                    value = stringResource(R.string.km_value, distanceKm),
                    background = DistanceCard,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
            Spacer(modifier = Modifier.weight(0.35f))
            PrimaryButton(
                text = stringResource(R.string.set_daily_goal),
                onClick = { showManualGoalDialog = true },
                backgroundColor = StepsPurple,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }

        if (showManualGoalDialog) {
            SetDailyStepGoalDialog(
                initialGoal = goal,
                onDismiss = { showManualGoalDialog = false },
                onSave = { manualGoal ->
                    val resolvedMax = maxOf(maxGoal, manualGoal)
                        .coerceIn(GoalDefaultMax, GoalCeiling)
                    maxGoal = resolvedMax
                    goal = manualGoal.coerceIn(GoalMin, resolvedMax)
                    showManualGoalDialog = false
                    onSetGoal(goal)
                }
            )
        }
    }
}

@Composable
private fun SetDailyStepGoalDialog(
    initialGoal: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var textValue by remember {
        val raw = initialGoal.coerceIn(GoalMin, GoalCeiling).toString()
        mutableStateOf(TextFieldValue(raw, TextRange(raw.length)))
    }
    val focusRequester = remember { FocusRequester() }
    val parsedGoal = textValue.text.toIntOrNull()
    val canSave = parsedGoal != null && parsedGoal in GoalMin..GoalCeiling

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .fillMaxWidth()
                .shadow(24.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .padding(horizontal = 22.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.steps),
                contentDescription = null,
                tint = StepsPurple,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.set_daily_step_goal_title),
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = AssessmentTitleColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.set_daily_step_goal_subtitle),
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(22.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DialogInputBg)
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                BasicTextField(
                    value = textValue,
                    onValueChange = { next ->
                        val digits = next.text.filter { it.isDigit() }.take(6)
                        textValue = if (digits == next.text) {
                            next
                        } else {
                            TextFieldValue(digits, TextRange(digits.length))
                        }
                    },
                    modifier = Modifier.focusRequester(focusRequester),
                    textStyle = TextStyle(
                        fontFamily = SourceSansPro,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = AssessmentTitleColor,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    cursorBrush = SolidColor(StepsPurple)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.steps_label),
                    fontFamily = SourceSansPro,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DialogCancelBg)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        fontFamily = SourceSansPro,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (canSave) StepsPurple else StepsPurple.copy(alpha = 0.45f)
                        )
                        .clickable(enabled = canSave) {
                            parsedGoal?.let(onSave)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.save),
                        fontFamily = SourceSansPro,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun StepsGoalRing(
    goal: Int,
    maxGoal: Int,
    onValueChange: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = goalFraction(goal, maxGoal)
    val valueState = rememberUpdatedState(goal)
    val maxState = rememberUpdatedState(maxGoal)
    val changeState = rememberUpdatedState(onValueChange)
    Box(
        modifier = modifier.pointerInput(Unit) {
            scrubGoal(
                value = { valueState.value },
                maxGoal = { maxState.value },
                onValueChange = { next, nextMax -> changeState.value(next, nextMax) }
            )
        },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 20.dp.toPx()
            val inset = strokePx / 2f
            val arcSize = Size(size.width - strokePx, size.height - strokePx)
            val topLeft = Offset(inset, inset)
            val stroke = Stroke(width = strokePx, cap = StrokeCap.Round)
            drawArc(
                color = StepsTrack,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke
            )
            drawArc(
                color = StepsPurple,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(R.drawable.steps),
                contentDescription = null,
                tint = StepsPurple,
                modifier = Modifier.size(42.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = formatSteps(goal),
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Black,
                fontSize = 36.sp,
                color = AssessmentTitleColor,
                lineHeight = 38.sp
            )
            Text(
                text = stringResource(R.string.steps_unit),
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                letterSpacing = 1.2.sp,
                color = AssessmentTitleColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(ActivePill)
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.steps_active),
                    fontFamily = SourceSansPro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = ActiveText
                )
            }
        }
    }
}

@Composable
private fun GoalSlider(
    value: Int,
    maxGoal: Int,
    onValueChange: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val fraction = goalFraction(value, maxGoal)
    val valueState = rememberUpdatedState(value)
    val maxState = rememberUpdatedState(maxGoal)
    val changeState = rememberUpdatedState(onValueChange)
    Box(
        modifier = modifier
            .height(40.dp)
            .pointerInput(Unit) {
                scrubGoal(
                    value = { valueState.value },
                    maxGoal = { maxState.value },
                    onValueChange = { next, nextMax -> changeState.value(next, nextMax) }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val trackY = size.height / 2f
            val stroke = 4.dp.toPx()
            val thumbR = 9.dp.toPx()
            val start = thumbR
            val end = size.width - thumbR
            val x = start + (end - start) * fraction
            drawLine(
                color = SliderTrack,
                start = Offset(start, trackY),
                end = Offset(end, trackY),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
            drawLine(
                color = StepsPurple,
                start = Offset(start, trackY),
                end = Offset(x, trackY),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
            drawCircle(color = StepsPurple, radius = thumbR, center = Offset(x, trackY))
        }
    }
}

@Composable
private fun StepStatCard(
    iconRes: Int,
    label: String,
    value: String,
    background: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                fontFamily = SourceSansPro,
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 1
            )
            Text(
                text = value,
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = AssessmentTitleColor,
                maxLines = 1
            )
        }
    }
}

private fun goalFraction(goal: Int, maxGoal: Int): Float {
    val span = (maxGoal - GoalMin).coerceAtLeast(1)
    return ((goal - GoalMin).toFloat() / span).coerceIn(0f, 1f)
}

private suspend fun PointerInputScope.scrubGoal(
    value: () -> Int,
    maxGoal: () -> Int,
    onValueChange: (Int, Int) -> Unit
) {
    awaitEachGesture {
        val down = awaitFirstDown()
        val thumbInset = 9.dp.toPx()
        fun span() = (size.width - thumbInset * 2f).coerceAtLeast(1f)
        var dragging = value().toFloat()

        fun jumpTo(x: Float) {
            val currentMax = maxGoal()
            val fraction = ((x - thumbInset) / span()).coerceIn(0f, 1f)
            val next = (GoalMin + fraction * (currentMax - GoalMin)).roundToInt()
                .coerceIn(GoalMin, currentMax)
            dragging = next.toFloat()
            onValueChange(next, currentMax)
        }

        jumpTo(down.position.x)
        drag(down.id) { change ->
            val stepsPerPx = (GoalDefaultMax - GoalMin).toFloat() / span()
            val delta = change.position.x - change.previousPosition.x
            dragging = (dragging + delta * stepsPerPx)
                .coerceIn(GoalMin.toFloat(), GoalCeiling.toFloat())
            val next = dragging.roundToInt().coerceIn(GoalMin, GoalCeiling)
            val newMax = maxOf(maxGoal(), next).coerceIn(GoalDefaultMax, GoalCeiling)
            val resolved = next.coerceAtMost(newMax)
            dragging = resolved.toFloat()
            onValueChange(resolved, newMax)
            change.consume()
        }
    }
}

private fun formatSteps(value: Int): String = "%,d".format(Locale.US, value)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StepsDailyScreenPreview() {
    AIFitnessTrackerTheme {
        StepsDailyScreen(onBack = {}, onSetGoal = {})
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun SetDailyStepGoalDialogPreview() {
    AIFitnessTrackerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x66000000)),
            contentAlignment = Alignment.Center
        ) {
            SetDailyStepGoalDialog(
                initialGoal = 12_008,
                onDismiss = {},
                onSave = {}
            )
        }
    }
}
