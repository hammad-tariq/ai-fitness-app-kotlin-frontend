package com.pro.aifitnesstracker.ui.screens

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import com.pro.aifitnesstracker.ui.theme.BadgeBg
import com.pro.aifitnesstracker.ui.theme.BrandPrimary
import com.pro.aifitnesstracker.ui.theme.SourceSansPro
import com.pro.aifitnesstracker.ui.theme.TextSecondary

private val SuggestionChips = listOf("Arthritis", "Back Pain", "Asthma", "Obesity")
private const val MAX_LIMITATIONS = 10

@Composable
fun PhysicalLimitationsScreen(
    onBack: () -> Unit,
    onContinue: (limitations: List<String>) -> Unit,
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

    val selected = remember { mutableStateListOf("Arthritis", "Back Pain", "Asthma", "Obesity") }
    var input by remember { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current

    fun addCustom() {
        val value = input.trim()
        if (value.isNotEmpty() && selected.size < MAX_LIMITATIONS && selected.none { it.equals(value, true) }) {
            selected.add(value)
        }
        input = ""
        keyboard?.hide()
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
                stepText = stringResource(R.string.assessment_step_6),
                onBackClick = onBack
            )
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = stringResource(R.string.assessment_6_title),
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = AssessmentTitleColor,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Image(
                painter = painterResource(R.drawable.phy_limitiation),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.5.dp, BrandPrimary, RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SuggestionChips.take(2).forEach { chip ->
                            LimitationChip(
                                text = chip,
                                selected = selected.any { it.equals(chip, true) },
                                onClick = { toggleChip(chip, selected) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SuggestionChips.drop(2).forEach { chip ->
                            LimitationChip(
                                text = chip,
                                selected = selected.any { it.equals(chip, true) },
                                onClick = { toggleChip(chip, selected) }
                            )
                        }
                    }
                    val extras = selected.filter { custom ->
                        SuggestionChips.none { it.equals(custom, true) }
                    }
                    if (extras.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            extras.forEach { chip ->
                                LimitationChip(
                                    text = chip,
                                    selected = true,
                                    onClick = { selected.remove(chip) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                BasicTextField(
                    value = input,
                    onValueChange = { if (it.length <= 40) input = it },
                    textStyle = TextStyle(
                        fontFamily = SourceSansPro,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = AssessmentTitleColor
                    ),
                    cursorBrush = SolidColor(BrandPrimary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { addCustom() }),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        Box {
                            if (input.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.limitations_hint),
                                    fontFamily = SourceSansPro,
                                    fontSize = 16.sp,
                                    color = TextSecondary.copy(alpha = 0.6f)
                                )
                            }
                            inner()
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_document),
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = " ${selected.size}/$MAX_LIMITATIONS",
                        fontFamily = SourceSansPro,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            PrimaryButton(
                text = stringResource(R.string.continue_cta),
                onClick = { onContinue(selected.toList()) },
                hasTrailingArrow = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            )
        }
    }
}

private fun toggleChip(chip: String, selected: MutableList<String>) {
    val isOn = selected.any { it.equals(chip, true) }
    if (isOn) {
        selected.removeAll { it.equals(chip, true) }
    } else if (selected.size < MAX_LIMITATIONS) {
        selected.add(chip)
    }
}

@Composable
private fun LimitationChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = text,
        fontFamily = SourceSansPro,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        color = if (selected) BrandPrimary else TextSecondary,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) BadgeBg else Color(0xFFF4F4F6))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PhysicalLimitationsScreenPreview() {
    AIFitnessTrackerTheme {
        PhysicalLimitationsScreen(onBack = {}, onContinue = {})
    }
}
