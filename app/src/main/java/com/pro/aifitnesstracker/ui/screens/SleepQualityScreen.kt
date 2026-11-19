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
import com.pro.aifitnesstracker.ui.components.PrimaryButton
import com.pro.aifitnesstracker.ui.theme.AIFitnessTrackerTheme
import com.pro.aifitnesstracker.ui.theme.AssessmentBg
import com.pro.aifitnesstracker.ui.theme.AssessmentTitleColor
import com.pro.aifitnesstracker.ui.theme.BrandPrimary
import com.pro.aifitnesstracker.ui.theme.CardUnselectedBg
import com.pro.aifitnesstracker.ui.theme.SourceSansPro
import com.pro.aifitnesstracker.ui.theme.TextSecondary

enum class SleepQuality(
    @StringRes val titleRes: Int,
    @StringRes val hoursRes: Int,
    @DrawableRes val iconRes: Int
) {
    Excellent(R.string.sleep_excellent, R.string.sleep_excellent_hours, R.drawable.excelent),
    Great(R.string.sleep_great, R.string.sleep_great_hours, R.drawable.great),
    Normal(R.string.sleep_normal, R.string.sleep_normal_hours, R.drawable.normal),
    Bad(R.string.sleep_bad, R.string.sleep_bad_hours, R.drawable.bad),
    Insomniac(R.string.sleep_insomniac, R.string.sleep_insomniac_hours, R.drawable.insomniac)
}

@Composable
fun SleepQualityScreen(
    onBack: () -> Unit,
    onContinue: (SleepQuality) -> Unit,
    modifier: Modifier = Modifier
) {
    var selected by remember { mutableStateOf(SleepQuality.Great) }

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
                stepText = stringResource(R.string.assessment_step_9),
                onBackClick = onBack
            )
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = stringResource(R.string.assessment_9_title),
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = AssessmentTitleColor,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )
            Spacer(modifier = Modifier.height(28.dp))

            val options = listOf(
                SleepQuality.Excellent,
                SleepQuality.Great,
                SleepQuality.Normal,
                SleepQuality.Bad,
                SleepQuality.Insomniac
            )
            options.forEach { option ->
                val isSelected = option == selected
                val bg by animateColorAsState(
                    targetValue = if (isSelected) BrandPrimary else CardUnselectedBg,
                    animationSpec = tween(200),
                    label = "sleepBg"
                )
                val content = if (isSelected) Color.White else AssessmentTitleColor
                val muted = if (isSelected) Color.White.copy(alpha = 0.85f) else TextSecondary

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .height(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(bg)
                        .clickable { selected = option }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(option.iconRes),
                        contentDescription = null,
                        tint = content,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(option.titleRes),
                        fontFamily = SourceSansPro,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = content,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_clock),
                        contentDescription = null,
                        tint = muted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(option.hoursRes),
                        fontFamily = SourceSansPro,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = muted
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SleepQualityScreenPreview() {
    AIFitnessTrackerTheme {
        SleepQualityScreen(onBack = {}, onContinue = {})
    }
}
