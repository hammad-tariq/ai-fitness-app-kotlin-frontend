package com.pro.aifitnesstracker.ui.screens

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.pro.aifitnesstracker.ui.theme.SourceSansPro

@Composable
fun FitnessExperienceScreen(
    onBack: () -> Unit,
    onContinue: (hasExperience: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
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
                stepText = stringResource(R.string.assessment_step_5),
                onBackClick = onBack
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.assessment_5_title),
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = AssessmentTitleColor,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(R.drawable.fitness_exp),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                PrimaryButton(
                    text = stringResource(R.string.no),
                    onClick = { onContinue(false) },
                    backgroundColor = Color(0xFFD4D4D8),
                    contentColor = AssessmentTitleColor,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                PrimaryButton(
                    text = stringResource(R.string.yes),
                    onClick = { onContinue(true) },
                    backgroundColor = BrandPrimary,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun FitnessExperienceScreenPreview() {
    AIFitnessTrackerTheme {
        FitnessExperienceScreen(onBack = {}, onContinue = {})
    }
}
