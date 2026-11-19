package com.pro.aifitnesstracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pro.aifitnesstracker.R
import com.pro.aifitnesstracker.ui.theme.AssessmentTitleColor
import com.pro.aifitnesstracker.ui.theme.BackButtonBg
import com.pro.aifitnesstracker.ui.theme.BadgeBg
import com.pro.aifitnesstracker.ui.theme.BadgeText
import com.pro.aifitnesstracker.ui.theme.SourceSansPro

@Composable
fun AssessmentHeader(
    stepText: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        contentAlignment = Alignment.Center
    ) {
        // Back Button on left
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(BackButtonBg)
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back_chevron),
                contentDescription = stringResource(R.string.back),
                tint = AssessmentTitleColor,
                modifier = Modifier.size(18.dp)
            )
        }

        // Title in center
        Text(
            text = stringResource(R.string.assessment),
            fontFamily = SourceSansPro,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = AssessmentTitleColor
        )

        // Step Badge on right
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .clip(RoundedCornerShape(8.dp))
                .background(BadgeBg)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepText,
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = BadgeText
            )
        }
    }
}

