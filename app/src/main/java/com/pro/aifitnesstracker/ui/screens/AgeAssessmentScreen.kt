package com.pro.aifitnesstracker.ui.screens

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.pro.aifitnesstracker.ui.components.AssessmentHeader
import com.pro.aifitnesstracker.ui.components.PrimaryButton
import com.pro.aifitnesstracker.ui.theme.AssessmentBg
import com.pro.aifitnesstracker.ui.theme.BrandPrimary
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private val CursorColor = BrandPrimary
private const val MIN_AGE = 10
private const val MAX_AGE = 100
private const val DEFAULT_AGE = 18
private const val VISIBLE_RANGE = 4

@Composable
fun AgeAssessmentScreen(
    onBack: () -> Unit,
    onContinue: (age: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val window = (context as? Activity)?.window ?: return@LaunchedEffect
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AssessmentBg, Color(0xFFEEEAFF)),
                    startY = 0f,
                    endY = 1200f
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AssessmentHeader(
                stepText = "4 of 10",
                onBackClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "What is your age?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF141416)
            )

            Spacer(modifier = Modifier.height(32.dp))

            var selectedAge by remember { mutableIntStateOf(DEFAULT_AGE) }
            AgePicker(
                onAgeChange = { selectedAge = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = "Continue",
                onClick = { onContinue(selectedAge) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun AgePicker(
    onAgeChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemHeightPx = with(LocalDensity.current) { 96.dp.toPx() }
    val minScroll = (MIN_AGE - DEFAULT_AGE) * itemHeightPx
    val maxScroll = (MAX_AGE - DEFAULT_AGE) * itemHeightPx
    var scrollPx by remember { mutableFloatStateOf(0f) }
    val scrollAnim = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var flingJob by remember { mutableStateOf<Job?>(null) }

    val fractionalAge = DEFAULT_AGE + scrollPx / itemHeightPx
    val selectedAge by remember {
        derivedStateOf {
            (DEFAULT_AGE + scrollPx / itemHeightPx).roundToInt()
                .coerceIn(MIN_AGE, MAX_AGE)
        }
    }

    LaunchedEffect(selectedAge) { onAgeChange(selectedAge) }

    Box(
        modifier = modifier.clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(288.dp)
                .height(130.dp)
                .clip(RoundedCornerShape(42.dp))
                .background(CursorColor)
        )

        AgePickerWheel(
            fractionalAge = fractionalAge,
            itemHeightPx = itemHeightPx,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(itemHeightPx, minScroll, maxScroll) {
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
                            val dy = change.position.y - change.previousPosition.y
                            velocityTracker.addPosition(change.uptimeMillis, change.position)
                            change.consume()
                            scrollPx = (scrollPx - dy).coerceIn(minScroll, maxScroll)
                        }

                        val flingVelocity = -velocityTracker.calculateVelocity().y
                        flingJob = scope.launch {
                            scrollAnim.snapTo(scrollPx)
                            if (abs(flingVelocity) > 400f) {
                                scrollAnim.updateBounds(minScroll, maxScroll)
                                scrollAnim.animateDecay(flingVelocity, decay) {
                                    scrollPx = value
                                }
                                scrollAnim.updateBounds(
                                    Float.NEGATIVE_INFINITY,
                                    Float.POSITIVE_INFINITY
                                )
                            }
                            val snapped =
                                (scrollAnim.value / itemHeightPx).roundToInt() * itemHeightPx
                            scrollAnim.animateTo(
                                targetValue = snapped.coerceIn(minScroll, maxScroll),
                                animationSpec = spring(
                                    dampingRatio = 0.78f,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ) {
                                scrollPx = value
                            }
                        }
                    }
                }
        )
    }
}

@Composable
private fun AgePickerWheel(
    fractionalAge: Float,
    itemHeightPx: Float,
    modifier: Modifier = Modifier
) {
    val centerAge = fractionalAge.roundToInt()
    val firstAge = (centerAge - VISIBLE_RANGE).coerceAtLeast(MIN_AGE)
    val lastAge = (centerAge + VISIBLE_RANGE).coerceAtMost(MAX_AGE)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        for (age in firstAge..lastAge) {
            val distance = age - fractionalAge
            val absDist = abs(distance)
            val y = distance * itemHeightPx
            val tNear = absDist.coerceIn(0f, 1f)
            val tFar = (absDist - 1f).coerceIn(0f, 1f)
            val fontSize = if (absDist <= 1f) {
                lerp(112f, 54f, tNear)
            } else {
                lerp(54f, 26f, tFar)
            }
            val textColor = lerp(
                Color.White,
                Color(0xFFD6D6D8),
                (absDist / 0.62f).coerceIn(0f, 1f)
            )
            val weight = when {
                absDist < 0.45f -> FontWeight.ExtraBold
                absDist < 1.2f -> FontWeight.Bold
                else -> FontWeight.SemiBold
            }
            val alpha = (1f - ((absDist - 2.2f) / 1.6f).coerceIn(0f, 1f))

            Text(
                text = age.toString(),
                fontSize = fontSize.sp,
                fontWeight = weight,
                color = textColor.copy(alpha = alpha),
                lineHeight = fontSize.sp,
                modifier = Modifier.graphicsLayer {
                    translationY = y
                }
            )
        }
    }
}
