package com.pro.aifitnesstracker.ui.screens

import android.net.Uri
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pro.aifitnesstracker.R
import com.pro.aifitnesstracker.data.foodscan.FoodScanAnalyzeResult
import com.pro.aifitnesstracker.ui.components.PrimaryButton
import com.pro.aifitnesstracker.ui.theme.AssessmentTitleColor
import com.pro.aifitnesstracker.ui.theme.SourceSansPro
import com.pro.aifitnesstracker.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.roundToInt

private val ScanPurple = Color(0xFF635BFF)
private val TagBg = Color(0xFFEBE8FE)
private val SectionBg = Color(0xFFF3F0FF)
private val ScreenBg = Color(0xFFF7F7FB)

@Composable
fun ScanFoodResultScreen(
    imageUri: Uri,
    result: FoodScanAnalyzeResult,
    isSaving: Boolean,
    onBack: () -> Unit,
    onAddToLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
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
                        text = stringResource(R.string.scan_your_food),
                        fontFamily = SourceSansPro,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .offset(y = (-28).dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 88.dp)
            ) {
                SummaryCard(result = result)
                Spacer(modifier = Modifier.height(14.dp))
                CaloriesPerItemSection(result = result)
                Spacer(modifier = Modifier.height(14.dp))
                NutritionalBreakdownSection(result = result)
            }
        }

        PrimaryButton(
            text = stringResource(R.string.add_to_log),
            onClick = onAddToLog,
            enabled = !isSaving,
            backgroundColor = ScanPurple,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        )

        if (isSaving) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ScanPurple)
            }
        }
    }
}

@Composable
private fun SummaryCard(result: FoodScanAnalyzeResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(22.dp), ambientColor = Color(0x1A000000))
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        if (!result.tag.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(TagBg)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = result.tag,
                    fontFamily = SourceSansPro,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = ScanPurple,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = result.title,
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = AssessmentTitleColor,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(
                    R.string.scan_kcal_value,
                    result.totalKcal.roundToInt()
                ),
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = ScanPurple
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(
                R.string.scan_servings,
                formatNumber(result.servings),
                formatNumber(result.totalGrams)
            ),
            fontFamily = SourceSansPro,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = AssessmentTitleColor
        )
    }
}

@Composable
private fun CaloriesPerItemSection(result: FoodScanAnalyzeResult) {
    Column {
        Text(
            text = stringResource(R.string.calories_per_item),
            fontFamily = SourceSansPro,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = AssessmentTitleColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(SectionBg)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            result.items.forEach { item ->
                KeyValueRow(
                    label = item.name,
                    value = stringResource(R.string.scan_kcal_value, item.kcal.roundToInt()),
                    bold = false
                )
            }
            KeyValueRow(
                label = stringResource(R.string.total_calories),
                value = stringResource(R.string.scan_kcal_value, result.totalKcal.roundToInt()),
                bold = true
            )
        }
    }
}

@Composable
private fun NutritionalBreakdownSection(result: FoodScanAnalyzeResult) {
    Column {
        Text(
            text = stringResource(R.string.nutritional_breakdown),
            fontFamily = SourceSansPro,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = AssessmentTitleColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(SectionBg)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MacroRow(stringResource(R.string.protein), result.macros.proteinG)
            MacroRow(stringResource(R.string.carbs), result.macros.carbsG)
            MacroRow(stringResource(R.string.fat), result.macros.fatG)
            MacroRow(stringResource(R.string.fiber), result.macros.fiberG)
        }
    }
}

@Composable
private fun MacroRow(label: String, grams: Double?) {
    KeyValueRow(
        label = label,
        value = grams?.let { stringResource(R.string.grams_value, formatNumber(it)) } ?: "—",
        bold = false
    )
}

@Composable
private fun KeyValueRow(
    label: String,
    value: String,
    bold: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = SourceSansPro,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp,
            color = if (bold) AssessmentTitleColor else TextSecondary,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            fontFamily = SourceSansPro,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp,
            color = AssessmentTitleColor
        )
    }
}

private fun formatNumber(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
}
