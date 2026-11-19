package com.pro.aifitnesstracker.ui.screens

import android.app.Activity
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import com.pro.aifitnesstracker.R
import com.pro.aifitnesstracker.ui.theme.AssessmentTitleColor
import com.pro.aifitnesstracker.ui.theme.BackButtonBg
import com.pro.aifitnesstracker.ui.theme.SourceSansPro

private val WeightPurple = Color(0xFF635BFF)
private val DeleteRed = Color(0xFFE55B5B)
private val CancelGray = Color(0xFFC8C8C8)

@Composable
fun RecentWeightLogsScreen(
    state: WeightTrackerState,
    onStateChange: (WeightTrackerState) -> Unit,
    onBack: () -> Unit,
    onAddLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selecting by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowInsetsControllerCompat(it, view) }
        val previous = controller?.isAppearanceLightStatusBars ?: false
        controller?.isAppearanceLightStatusBars = true
        onDispose { controller?.isAppearanceLightStatusBars = previous }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFFFCFCFF), Color(0xFFF3F1FB)))
                )
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BackButtonBg)
                        .clickable {
                            if (selecting) {
                                selecting = false
                                selectedIds = emptySet()
                            } else {
                                onBack()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back_chevron),
                        contentDescription = stringResource(R.string.back),
                        tint = AssessmentTitleColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.recent_weight_logs),
                    fontFamily = SourceSansPro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = AssessmentTitleColor,
                    modifier = Modifier.weight(1f)
                )
                if (selecting) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            selectedIds = if (selectedIds.size == state.logs.size) {
                                emptySet()
                            } else {
                                state.logs.map { it.id }.toSet()
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.select_all),
                            fontFamily = SourceSansPro,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = WeightPurple
                        )
                        Spacer(Modifier.width(6.dp))
                        WeightCheckbox(
                            checked = state.logs.isNotEmpty() && selectedIds.size == state.logs.size,
                            onClick = {
                                selectedIds = if (selectedIds.size == state.logs.size) {
                                    emptySet()
                                } else {
                                    state.logs.map { it.id }.toSet()
                                }
                            }
                        )
                    }
                } else if (state.logs.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selecting = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_trash),
                            contentDescription = stringResource(R.string.delete_logs),
                            tint = DeleteRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            if (state.logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_record_found),
                        fontFamily = SourceSansPro,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AssessmentTitleColor,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(state.logs, key = { _, item -> item.id }) { index, entry ->
                        val compareTo = state.logs.getOrNull(index + 1)?.weightKg
                            ?: state.targetWeightKg
                        WeightLogRow(
                            entry = entry,
                            compareToKg = compareTo,
                            trailing = if (selecting) {
                                {
                                    WeightCheckbox(
                                        checked = entry.id in selectedIds,
                                        onClick = {
                                            selectedIds = if (entry.id in selectedIds) {
                                                selectedIds - entry.id
                                            } else {
                                                selectedIds + entry.id
                                            }
                                        }
                                    )
                                }
                            } else null,
                            modifier = if (selecting) {
                                Modifier.clickable {
                                    selectedIds = if (entry.id in selectedIds) {
                                        selectedIds - entry.id
                                    } else {
                                        selectedIds + entry.id
                                    }
                                }
                            } else Modifier
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }

            if (selecting) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(CancelGray)
                            .clickable {
                                selecting = false
                                selectedIds = emptySet()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.cancel),
                            fontFamily = SourceSansPro,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (selectedIds.isEmpty()) DeleteRed.copy(alpha = 0.45f) else DeleteRed
                            )
                            .clickable(enabled = selectedIds.isNotEmpty()) {
                                showDeleteConfirm = true
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_trash),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.delete_logs),
                            fontFamily = SourceSansPro,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            } else {
                AddDailyLogButton(
                    onClick = onAddLog,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }
        }

        if (showDeleteConfirm) {
            DeleteWeightLogsDialog(
                onDismiss = { showDeleteConfirm = false },
                onConfirm = {
                    onStateChange(state.withoutLogs(selectedIds))
                    selectedIds = emptySet()
                    selecting = false
                    showDeleteConfirm = false
                }
            )
        }
    }
}

@Composable
private fun WeightCheckbox(
    checked: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (checked) WeightPurple else Color.Transparent)
            .border(
                width = 1.5.dp,
                color = if (checked) WeightPurple else Color(0xFFC5C5C8),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
