package com.pro.aifitnesstracker.ui.screens

import android.app.Activity
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowInsetsControllerCompat
import com.pro.aifitnesstracker.R
import com.pro.aifitnesstracker.data.ClientIdStore
import com.pro.aifitnesstracker.data.foodscan.FoodScanApi
import com.pro.aifitnesstracker.data.foodscan.FoodScanLogSummary
import com.pro.aifitnesstracker.ui.theme.AssessmentTitleColor
import com.pro.aifitnesstracker.ui.theme.BackButtonBg
import com.pro.aifitnesstracker.ui.theme.SourceSansPro
import com.pro.aifitnesstracker.ui.theme.TextSecondary
import kotlinx.coroutines.launch

private val ScanPurple = Color(0xFF635BFF)
private val DeleteRed = Color(0xFFE55B5B)
private val CancelGray = Color(0xFFC8C8C8)

@Composable
fun ScanFoodLogsScreen(
    logs: List<FoodScanLogSummary>,
    onBack: () -> Unit,
    onLogsChanged: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember { FoodScanApi() }
    val clientId = remember { ClientIdStore.getOrCreate(context) }

    var items by remember { mutableStateOf(logs) }
    var selecting by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Int>()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(logs) {
        items = logs
    }

    fun reload() {
        scope.launch {
            loading = true
            try {
                items = api.listLogs(clientId = clientId).items
                onLogsChanged()
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    e.message ?: context.getString(R.string.scan_food_failed),
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                loading = false
            }
        }
    }

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
                    text = stringResource(R.string.scan_your_food),
                    fontFamily = SourceSansPro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = AssessmentTitleColor,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start
                )

                if (selecting) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            selectedIds = if (selectedIds.size == items.size && items.isNotEmpty()) {
                                emptySet()
                            } else {
                                items.map { it.id }.toSet()
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.select_all),
                            fontFamily = SourceSansPro,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = ScanPurple
                        )
                        Spacer(Modifier.width(6.dp))
                        FoodScanCheckbox(
                            checked = items.isNotEmpty() && selectedIds.size == items.size,
                            onClick = {
                                selectedIds = if (selectedIds.size == items.size) {
                                    emptySet()
                                } else {
                                    items.map { it.id }.toSet()
                                }
                            }
                        )
                    }
                } else if (items.isNotEmpty()) {
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

            when {
                loading -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ScanPurple)
                    }
                }
                items.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        FoodScanEmptyState()
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(items, key = { it.id }) { log ->
                            FoodScanLogCard(
                                log = log,
                                selecting = selecting,
                                selected = log.id in selectedIds,
                                onToggleSelect = if (selecting) {
                                    {
                                        selectedIds = if (log.id in selectedIds) {
                                            selectedIds - log.id
                                        } else {
                                            selectedIds + log.id
                                        }
                                    }
                                } else null
                            )
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                    }
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
            }
        }

        if (showDeleteConfirm) {
            DeleteFoodScanLogsDialog(
                onDismiss = { showDeleteConfirm = false },
                onConfirm = {
                    if (deleting) return@DeleteFoodScanLogsDialog
                    scope.launch {
                        deleting = true
                        try {
                            api.deleteLogs(clientId, selectedIds)
                            selectedIds = emptySet()
                            selecting = false
                            showDeleteConfirm = false
                            reload()
                            Toast.makeText(
                                context,
                                context.getString(R.string.logs_deleted),
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                e.message ?: context.getString(R.string.scan_food_failed),
                                Toast.LENGTH_LONG
                            ).show()
                        } finally {
                            deleting = false
                        }
                    }
                }
            )
        }

        if (deleting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ScanPurple)
            }
        }
    }
}

@Composable
private fun DeleteFoodScanLogsDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 36.dp)
                .fillMaxWidth()
                .shadow(24.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .padding(horizontal = 22.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFFFE8E8))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_trash),
                    contentDescription = null,
                    tint = DeleteRed,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.delete_recent_logs_title),
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AssessmentTitleColor,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.delete_recent_logs_message),
                fontFamily = SourceSansPro,
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(22.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CancelGray)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        fontFamily = SourceSansPro,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DeleteRed)
                        .clickable(onClick = onConfirm),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.delete),
                        fontFamily = SourceSansPro,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
