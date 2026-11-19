package com.pro.aifitnesstracker.ui.screens

import android.app.Activity
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import com.pro.aifitnesstracker.R
import com.pro.aifitnesstracker.ui.components.AssessmentHeader
import com.pro.aifitnesstracker.ui.components.CustomRadioIndicator
import com.pro.aifitnesstracker.ui.components.PrimaryButton
import com.pro.aifitnesstracker.ui.theme.AIFitnessTrackerTheme
import com.pro.aifitnesstracker.ui.theme.AssessmentBg
import com.pro.aifitnesstracker.ui.theme.AssessmentTitleColor
import com.pro.aifitnesstracker.ui.theme.BrandPrimary
import com.pro.aifitnesstracker.ui.theme.CardUnselectedBg
import com.pro.aifitnesstracker.ui.theme.RadioBorderUnselected
import com.pro.aifitnesstracker.ui.theme.SourceSansPro
import com.pro.aifitnesstracker.ui.theme.TextSecondary

enum class FitnessGoal(
    @StringRes val titleRes: Int,
    @DrawableRes val iconRes: Int
) {
    LoseWeight(R.string.goal_lose_weight, R.drawable.ic_scale),
    AiCoach(R.string.goal_ai_coach, R.drawable.ic_robot),
    Bulks(R.string.goal_bulks, R.drawable.ic_dumbbell),
    Endurance(R.string.goal_endurance, R.drawable.ic_pulse),
    JustTrying(R.string.goal_just_trying, R.drawable.ic_phone)
}

@Composable
fun GoalAssessmentScreen(
    onBack: () -> Unit,
    onContinue: (FitnessGoal) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedGoal by remember { mutableStateOf(FitnessGoal.AiCoach) }

    // Ensure dark status bar icons on light background
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowInsetsControllerCompat(it, view) }
        val previous = controller?.isAppearanceLightStatusBars ?: false
        controller?.isAppearanceLightStatusBars = true
        onDispose {
            controller?.isAppearanceLightStatusBars = previous
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AssessmentBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            AssessmentHeader(
                stepText = stringResource(R.string.assessment_step_1),
                onBackClick = onBack
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.assessment_1_title),
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = AssessmentTitleColor,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                FitnessGoal.values().forEach { goal ->
                    val isSelected = selectedGoal == goal
                    val animatedBg by animateColorAsState(
                        targetValue = if (isSelected) BrandPrimary else CardUnselectedBg,
                        animationSpec = tween(durationMillis = 200),
                        label = "goalCardBg"
                    )
                    val iconTint = if (isSelected) Color.White else TextSecondary
                    val textColor = if (isSelected) Color.White else AssessmentTitleColor

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(animatedBg)
                            .clickable { selectedGoal = goal }
                            .padding(horizontal = 18.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                painter = painterResource(goal.iconRes),
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(22.dp)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = stringResource(goal.titleRes),
                                fontFamily = SourceSansPro,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = textColor,
                                modifier = Modifier.weight(1f)
                            )

                            CustomRadioIndicator(
                                isSelected = isSelected,
                                selectedColor = Color.White,
                                unselectedBorderColor = RadioBorderUnselected
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = stringResource(R.string.continue_cta),
                onClick = { onContinue(selectedGoal) },
                hasTrailingArrow = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun GoalAssessmentScreenPreview() {
    AIFitnessTrackerTheme {
        GoalAssessmentScreen(
            onBack = {},
            onContinue = {}
        )
    }
}

