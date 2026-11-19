// Final assessment screen – shown after gender selection
package com.pro.aifitnesstracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pro.aifitnesstracker.R
import com.pro.aifitnesstracker.ui.components.AssessmentHeader
import com.pro.aifitnesstracker.ui.components.PrimaryButton
import com.pro.aifitnesstracker.ui.theme.AIFitnessTrackerTheme
import com.pro.aifitnesstracker.ui.theme.AssessmentBg
import com.pro.aifitnesstracker.ui.theme.AssessmentTitleColor
import com.pro.aifitnesstracker.ui.theme.SourceSansPro
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun FinalAssessmentScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AssessmentBg)
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AssessmentHeader(
                stepText = "3 of 10",
                onBackClick = onBack
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "All set!\nWe’re ready to start your journey.",
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = AssessmentTitleColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 36.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            PrimaryButton(
                text = stringResource(R.string.continue_cta),
                onClick = onContinue,
                hasTrailingArrow = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun FinalAssessmentScreenPreview() {
    AIFitnessTrackerTheme {
        FinalAssessmentScreen(onBack = {}, onContinue = {})
    }
}
