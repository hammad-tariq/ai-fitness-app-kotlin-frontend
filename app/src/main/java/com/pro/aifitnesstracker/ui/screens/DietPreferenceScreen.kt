package com.pro.aifitnesstracker.ui.screens

import android.app.Activity
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import com.pro.aifitnesstracker.ui.components.PrimaryButton
import com.pro.aifitnesstracker.ui.theme.AIFitnessTrackerTheme
import com.pro.aifitnesstracker.ui.theme.AssessmentBg
import com.pro.aifitnesstracker.ui.theme.AssessmentTitleColor
import com.pro.aifitnesstracker.ui.theme.BrandPrimary
import com.pro.aifitnesstracker.ui.theme.CardUnselectedBg
import com.pro.aifitnesstracker.ui.theme.SourceSansPro
import com.pro.aifitnesstracker.ui.theme.TextSecondary

enum class DietPreference(
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    @DrawableRes val iconRes: Int
) {
    PlantBased(R.string.diet_plant_based, R.string.diet_plant_based_sub, R.drawable.ic_diet_leaf),
    Traditional(R.string.diet_traditional, R.string.diet_traditional_sub, R.drawable.ic_diet_apple),
    Specialized(R.string.diet_specialized, R.string.diet_specialized_sub, R.drawable.ic_diet_fork),
    Carbo(R.string.diet_carbo, R.string.diet_carbo_sub, R.drawable.ic_diet_bread)
}

@Composable
fun DietPreferenceScreen(
    onBack: () -> Unit,
    onContinue: (DietPreference) -> Unit,
    modifier: Modifier = Modifier
) {
    var selected by remember { mutableStateOf(DietPreference.Carbo) }

    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowInsetsControllerCompat(it, view) }
        val previous = controller?.isAppearanceLightStatusBars ?: false
        controller?.isAppearanceLightStatusBars = true
        onDispose { controller?.isAppearanceLightStatusBars = previous }
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
                stepText = stringResource(R.string.assessment_step_7),
                onBackClick = onBack
            )
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = stringResource(R.string.assessment_7_title),
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = AssessmentTitleColor,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp
            )
            Spacer(modifier = Modifier.height(28.dp))

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    DietCard(
                        diet = DietPreference.PlantBased,
                        selected = selected == DietPreference.PlantBased,
                        onClick = { selected = DietPreference.PlantBased },
                        modifier = Modifier.weight(1f)
                    )
                    DietCard(
                        diet = DietPreference.Traditional,
                        selected = selected == DietPreference.Traditional,
                        onClick = { selected = DietPreference.Traditional },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    DietCard(
                        diet = DietPreference.Specialized,
                        selected = selected == DietPreference.Specialized,
                        onClick = { selected = DietPreference.Specialized },
                        modifier = Modifier.weight(1f)
                    )
                    DietCard(
                        diet = DietPreference.Carbo,
                        selected = selected == DietPreference.Carbo,
                        onClick = { selected = DietPreference.Carbo },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            PrimaryButton(
                text = stringResource(R.string.continue_cta),
                onClick = { onContinue(selected) },
                hasTrailingArrow = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            )
        }
    }
}

@Composable
private fun DietCard(
    diet: DietPreference,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(
        targetValue = if (selected) BrandPrimary else CardUnselectedBg,
        animationSpec = tween(200),
        label = "dietBg"
    )
    val titleColor = if (selected) Color.White else AssessmentTitleColor
    val subColor = if (selected) Color.White.copy(alpha = 0.85f) else TextSecondary
    val iconTint = if (selected) Color.White.copy(alpha = 0.7f) else TextSecondary.copy(alpha = 0.45f)

    Box(
        modifier = modifier
            .height(148.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(diet.titleRes),
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(diet.subtitleRes),
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = subColor
            )
        }
        Icon(
            painter = painterResource(diet.iconRes),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(28.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DietPreferenceScreenPreview() {
    AIFitnessTrackerTheme {
        DietPreferenceScreen(onBack = {}, onContinue = {})
    }
}
