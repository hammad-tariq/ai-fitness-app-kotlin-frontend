package com.pro.aifitnesstracker.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.util.Locale
import kotlin.math.roundToInt

private val ScanPurple = Color(0xFF635BFF)
private val IconTileBg = Color(0xFFEBE8FE)

@Composable
fun ScanFoodHomeScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember { FoodScanApi() }
    val clientId = remember { ClientIdStore.getOrCreate(context) }

    var logs by remember { mutableStateOf<List<FoodScanLogSummary>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var showCamera by remember { mutableStateOf(false) }
    var showAllLogs by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableStateOf(0) }

    fun refreshLogs() {
        scope.launch {
            loading = true
            try {
                logs = api.listLogs(clientId = clientId, limit = 20).items
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

    LaunchedEffect(refreshKey) { refreshLogs() }

    if (showCamera) {
        BackHandler { showCamera = false }
        ScanFoodScreen(
            onBack = {
                showCamera = false
                refreshKey++
            }
        )
        return
    }

    if (showAllLogs) {
        BackHandler { showAllLogs = false }
        ScanFoodLogsScreen(
            logs = logs,
            onBack = { showAllLogs = false },
            onLogsChanged = { refreshKey++ }
        )
        return
    }

    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowInsetsControllerCompat(it, view) }
        val previous = controller?.isAppearanceLightStatusBars ?: false
        controller?.isAppearanceLightStatusBars = true
        onDispose { controller?.isAppearanceLightStatusBars = previous }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFFFCFCFF), Color(0xFFF4F2FE)))
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BackButtonBg)
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
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.scan_your_food),
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AssessmentTitleColor
            )
        }

        Spacer(Modifier.height(18.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            ScanNowHeroCard(onScanNow = { showCamera = true })

            Spacer(Modifier.height(22.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.food_scan),
                    fontFamily = SourceSansPro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AssessmentTitleColor,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(R.string.view_all),
                    fontFamily = SourceSansPro,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = ScanPurple,
                    modifier = Modifier.clickable {
                        showAllLogs = true
                    }
                )
            }

            Spacer(Modifier.height(12.dp))

            when {
                loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ScanPurple)
                    }
                }
                logs.isEmpty() -> {
                    FoodScanEmptyState(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp)
                    )
                }
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        logs.take(4).forEach { log ->
                            FoodScanLogCard(log = log)
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ScanNowHeroCard(
    onScanNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .shadow(10.dp, shape, ambientColor = Color(0x1A000000), spotColor = Color(0x14000000))
            .clip(shape)
            .background(Color.White)
    ) {
        Image(
            painter = painterResource(R.drawable.scan_food),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .fillMaxWidth(0.42f)
                .offset(x = 10.dp),
            contentScale = ContentScale.Crop,
            alignment = Alignment.CenterEnd
        )
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(0.62f)
                .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.scan_your_food),
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = AssessmentTitleColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.scan_food_sub),
                fontFamily = SourceSansPro,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(ScanPurple)
                    .clickable(onClick = onScanNow)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.scan_now),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.scan_now),
                    fontFamily = SourceSansPro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun FoodScanEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.nor_record_found),
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.no_record_found),
            fontFamily = SourceSansPro,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = AssessmentTitleColor
        )
    }
}

@Composable
fun FoodScanLogCard(
    log: FoodScanLogSummary,
    modifier: Modifier = Modifier,
    selecting: Boolean = false,
    selected: Boolean = false,
    onToggleSelect: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, shape, ambientColor = Color(0x14000000), spotColor = Color(0x10000000))
            .clip(shape)
            .background(Color.White)
            .then(
                if (onToggleSelect != null) Modifier.clickable(onClick = onToggleSelect)
                else Modifier
            )
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(IconTileBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_food_scan),
                contentDescription = null,
                tint = ScanPurple,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = log.title,
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = AssessmentTitleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(
                    R.string.food_scan_meta,
                    log.mealTypeLabel.ifBlank { log.mealType.replaceFirstChar { it.titlecase(Locale.US) } },
                    log.timeDisplay.ifBlank { "—" }
                ),
                fontFamily = SourceSansPro,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "%,d".format(Locale.US, log.totalKcal.roundToInt()),
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = AssessmentTitleColor
            )
            Text(
                text = stringResource(R.string.kcal_unit),
                fontFamily = SourceSansPro,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
        if (selecting) {
            Spacer(Modifier.width(10.dp))
            FoodScanCheckbox(
                checked = selected,
                onClick = { onToggleSelect?.invoke() }
            )
        }
    }
}

@Composable
fun FoodScanCheckbox(
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(22.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (checked) ScanPurple else Color.Transparent)
            .border(
                width = 1.5.dp,
                color = if (checked) ScanPurple else Color(0xFFC5C5C8),
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
