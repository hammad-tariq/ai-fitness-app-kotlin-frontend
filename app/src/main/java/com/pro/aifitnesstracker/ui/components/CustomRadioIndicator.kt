package com.pro.aifitnesstracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pro.aifitnesstracker.ui.theme.RadioBorderUnselected

@Composable
fun CustomRadioIndicator(
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    selectedColor: Color = Color.White,
    unselectedBorderColor: Color = RadioBorderUnselected
) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = modifier
            .size(20.dp)
            .border(
                width = 1.5.dp,
                color = if (isSelected) selectedColor else unselectedBorderColor,
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(selectedColor, RoundedCornerShape(2.5.dp))
            )
        }
    }
}

