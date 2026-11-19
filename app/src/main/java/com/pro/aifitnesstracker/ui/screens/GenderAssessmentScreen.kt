package com.pro.aifitnesstracker.ui.screens

import android.app.Activity
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.pro.aifitnesstracker.ui.components.CustomRadioIndicator
import com.pro.aifitnesstracker.ui.components.PrimaryButton
import com.pro.aifitnesstracker.ui.theme.AIFitnessTrackerTheme
import com.pro.aifitnesstracker.ui.theme.AssessmentBg
import com.pro.aifitnesstracker.ui.theme.AssessmentTitleColor
import com.pro.aifitnesstracker.ui.theme.BrandPrimary
import com.pro.aifitnesstracker.ui.theme.CardUnselectedBg
import com.pro.aifitnesstracker.ui.theme.RadioBorderUnselected
import com.pro.aifitnesstracker.ui.theme.SourceSansPro

enum class Gender(
    @StringRes val titleRes: Int,
    @DrawableRes val iconRes: Int,
    @DrawableRes val imageRes: Int
) {
    Male(R.string.gender_male, R.drawable.ic_male, R.drawable.male),
    Female(R.string.gender_female, R.drawable.ic_female, R.drawable.female)
}

@Composable
fun GenderAssessmentScreen(
    onBack: () -> Unit,
    onContinue: (Gender) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedGender by remember { mutableStateOf(Gender.Female) }

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
                stepText = stringResource(R.string.assessment_step_2),
                onBackClick = onBack
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.assessment_2_title),
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = AssessmentTitleColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Male and Female cards
            Gender.values().forEach { gender ->
                val isSelected = selectedGender == gender
                val cardShape = RoundedCornerShape(28.dp)

                val animatedBg by animateColorAsState(
                    targetValue = if (isSelected) Color.White else CardUnselectedBg,
                    animationSpec = tween(durationMillis = 200),
                    label = "genderCardBg"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(148.dp)
                        .clip(cardShape)
                        .background(animatedBg)
                        .then(
                            if (isSelected) {
                                Modifier.border(1.5.dp, BrandPrimary, cardShape)
                            } else {
                                Modifier
                            }
                        )
                        .clickable { selectedGender = gender }
                ) {
                    // Person image on right
                    Image(
                        painter = painterResource(gender.imageRes),
                        contentDescription = stringResource(gender.titleRes),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .fillMaxHeight(),
                        contentScale = ContentScale.FillHeight
                    )

                    // Text & icon on left
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(start = 22.dp, top = 20.dp, bottom = 20.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(gender.iconRes),
                                contentDescription = null,
                                tint = AssessmentTitleColor,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = stringResource(gender.titleRes),
                                fontFamily = SourceSansPro,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = AssessmentTitleColor
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        CustomRadioIndicator(
                            isSelected = isSelected,
                            selectedColor = BrandPrimary,
                            unselectedBorderColor = RadioBorderUnselected
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = stringResource(R.string.continue_cta),
                onClick = { onContinue(selectedGender) },
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
private fun GenderAssessmentScreenPreview() {
    AIFitnessTrackerTheme {
        GenderAssessmentScreen(
            onBack = {},
            onContinue = {}
        )
    }
}

