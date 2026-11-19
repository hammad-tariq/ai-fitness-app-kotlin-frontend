package com.pro.aifitnesstracker.ui.screens

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.pro.aifitnesstracker.R
import com.pro.aifitnesstracker.steps.StepCounterTracker
import com.pro.aifitnesstracker.steps.StepCounterUiState
import com.pro.aifitnesstracker.ui.theme.AIFitnessTrackerTheme
import com.pro.aifitnesstracker.ui.theme.ActionBlue
import com.pro.aifitnesstracker.ui.theme.AssessmentBg
import com.pro.aifitnesstracker.ui.theme.AssessmentTitleColor
import com.pro.aifitnesstracker.ui.theme.BrandPrimary
import com.pro.aifitnesstracker.ui.theme.SourceSansPro
import com.pro.aifitnesstracker.ui.theme.TextSecondary

private enum class WeightSetupStep {
    None,
    Current,
    Target
}

private data class HomeTab(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int
)

private data class HomeLayout(
    val gap: Dp,
    val clusterWidth: Dp,
    val ringSize: Dp,
    val miniTitle: TextUnit,
    val miniValue: TextUnit,
    val miniCaption: TextUnit,
    val sectionTitle: TextUnit
)

private data class HomeWorkout(
    @StringRes val titleRes: Int,
    @StringRes val countRes: Int,
    @DrawableRes val imageRes: Int,
    val background: Color,
    val buttonColor: Color,
    val imageFraction: Float
)

private val homeWorkouts = listOf(
    HomeWorkout(R.string.full_body_workout, R.string.exercises_5, R.drawable.full_body_workout, Color(0xFFA29BF0), Color(0xFF5F59A1), 0.52f),
    HomeWorkout(R.string.upper_body_workout, R.string.exercises_5, R.drawable.upper_body_workout, Color(0xFFF59494), Color(0xFFBA6767), 0.54f),
    HomeWorkout(R.string.lower_body_workout, R.string.exercises_5, R.drawable.lower_body_workout, Color(0xFF82C9B5), Color(0xFF63B49D), 0.46f),
    HomeWorkout(R.string.abs_core_workout, R.string.exercises_5, R.drawable.abs_work_out, Color(0xFF48A7D6), Color(0xFF1D79A7), 0.48f),
    HomeWorkout(R.string.fat_loss_hiit, R.string.exercises_5, R.drawable.fat_loss, Color(0xFFF3B477), Color(0xFFE39345), 0.58f),
    HomeWorkout(R.string.muscle_building, R.string.exercises_5, R.drawable.muscle_building, Color(0xFFB69AE8), Color(0xFF9271CE), 0.50f),
    HomeWorkout(R.string.recovery_stretching, R.string.exercises_4, R.drawable.recovery_exe, Color(0xFF85C7E8), Color(0xFF68B5DB), 0.66f),
    HomeWorkout(R.string.yoga_flexibility, R.string.exercises_3, R.drawable.yoga, Color(0xFFA4C6A0), Color(0xFF8AB585), 0.50f)
)

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showStepsDaily by remember { mutableStateOf(false) }
    var showWeightProgress by remember { mutableStateOf(false) }
    var showActivity by remember { mutableStateOf(false) }
    var showScanFood by remember { mutableStateOf(false) }
    var weightSetupStep by remember { mutableStateOf(WeightSetupStep.None) }
    var pendingCurrentWeight by remember { mutableStateOf<Float?>(null) }
    var weightState by remember { mutableStateOf(WeightTrackerState()) }
    val stepState by StepCounterTracker.state.collectAsState()

    fun startStepTracking() {
        StepCounterTracker.start(context)
    }

    fun hasStepPermissions(): Boolean {
        val recognitionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        val notificationsGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        return recognitionGranted && notificationsGranted
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val denied = result.filterValues { granted -> !granted }.keys
        if (denied.isEmpty()) {
            startStepTracking()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.step_permission_needed),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    LaunchedEffect(Unit) {
        StepCounterTracker.init(context)
        if (hasStepPermissions()) {
            startStepTracking()
        } else {
            val needed = buildList {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    add(Manifest.permission.ACTIVITY_RECOGNITION)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            if (needed.isNotEmpty()) {
                permissionLauncher.launch(needed.toTypedArray())
            } else {
                startStepTracking()
            }
        }
    }

    LaunchedEffect(stepState.sensorAvailable) {
        if (!stepState.sensorAvailable && stepState.isTracking) {
            Toast.makeText(
                context,
                context.getString(R.string.step_sensor_unavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun openWeightActivity() {
        val needsSetup = weightState.currentWeightKg == null || weightState.targetWeightKg == null
        if (needsSetup) {
            pendingCurrentWeight = null
            weightSetupStep = WeightSetupStep.Current
        } else {
            showWeightProgress = true
        }
    }

    if (showStepsDaily) {
        BackHandler { showStepsDaily = false }
        StepsDailyScreen(
            initialGoal = stepState.goal,
            onBack = { showStepsDaily = false },
            onSetGoal = { goal ->
                StepCounterTracker.updateGoal(context, goal)
                showStepsDaily = false
            }
        )
        return
    }
    if (showWeightProgress) {
        BackHandler { showWeightProgress = false }
        WeightProgressScreen(
            state = weightState,
            onStateChange = { weightState = it },
            onBack = { showWeightProgress = false }
        )
        return
    }
    if (showActivity) {
        BackHandler { showActivity = false }
        ActivityScreen(
            onBack = { showActivity = false }
        )
        return
    }
    if (showScanFood) {
        BackHandler { showScanFood = false }
        ScanFoodHomeScreen(onBack = { showScanFood = false })
        return
    }
    val tabs = listOf(
        HomeTab(R.string.nav_home, R.drawable.home),
        HomeTab(R.string.nav_workout, R.drawable.workout),
        HomeTab(R.string.nav_nutrition, R.drawable.nutrition),
        HomeTab(R.string.nav_progress, R.drawable.progress),
        HomeTab(R.string.nav_ai_coach, R.drawable.ai_coach)
    )

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
                .background(AssessmentBg)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            HomeDashboard(
                onSetDailyGoal = { showStepsDaily = true },
                onOpenWeightProgress = { openWeightActivity() },
                onOpenActivity = { showActivity = true },
                onOpenScanFood = { showScanFood = true },
                weightState = weightState,
                stepState = stepState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(top = 6.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, tab ->
                    val selected = index == selectedTab
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { selectedTab = index }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            painter = painterResource(tab.iconRes),
                            contentDescription = stringResource(tab.labelRes),
                            tint = if (selected) BrandPrimary else Color(0xFFB0B0B5),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(tab.labelRes),
                            fontFamily = SourceSansPro,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp,
                            color = if (selected) BrandPrimary else Color(0xFFB0B0B5)
                        )
                    }
                }
            }
        }

        if (weightSetupStep != WeightSetupStep.None) {
            WeightPickerDialog(
                mode = if (weightSetupStep == WeightSetupStep.Current) {
                    WeightPickerMode.Current
                } else {
                    WeightPickerMode.Target
                },
                initialWeightKg = if (weightSetupStep == WeightSetupStep.Current) {
                    weightState.currentWeightKg
                } else {
                    weightState.targetWeightKg
                },
                onDismiss = {
                    pendingCurrentWeight = null
                    weightSetupStep = WeightSetupStep.None
                },
                onConfirm = { kg ->
                    if (weightSetupStep == WeightSetupStep.Current) {
                        pendingCurrentWeight = kg
                        weightSetupStep = WeightSetupStep.Target
                    } else {
                        weightState = weightState.copy(
                            currentWeightKg = pendingCurrentWeight ?: weightState.currentWeightKg,
                            targetWeightKg = kg
                        )
                        pendingCurrentWeight = null
                        weightSetupStep = WeightSetupStep.None
                        showWeightProgress = true
                    }
                }
            )
        }
    }
}

@Composable
private fun HomeDashboard(
    onSetDailyGoal: () -> Unit,
    onOpenWeightProgress: () -> Unit,
    onOpenActivity: () -> Unit,
    onOpenScanFood: () -> Unit,
    weightState: WeightTrackerState,
    stepState: StepCounterUiState,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val contentWidth = maxWidth
        val layout = remember(maxWidth, maxHeight) {
            val h = maxHeight.value
            val w = maxWidth.value
            val scale = ((h / 720f) * (w / 392f)).coerceIn(0.88f, 1.2f)
            HomeLayout(
                gap = (6f * scale).dp,
                clusterWidth = (w * 0.38f).coerceIn(118f, 156f).dp,
                ringSize = (34f * scale).coerceIn(30f, 40f).dp,
                miniTitle = (10f * scale).sp,
                miniValue = (13f * scale).sp,
                miniCaption = (10f * scale).sp,
                sectionTitle = (14f * scale).sp
            )
        }
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(layout.gap))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.ai_fitness_icon),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    fontFamily = SourceSansPro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = AssessmentTitleColor,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BrandPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_settings),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(layout.gap))
            HeroWorkoutCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.15f)
            )

            Spacer(modifier = Modifier.height(layout.gap))
            Text(
                text = stringResource(R.string.step_counter),
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = layout.sectionTitle,
                color = AssessmentTitleColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            StepCounterCard(
                clusterWidth = layout.clusterWidth,
                ringSize = layout.ringSize,
                stepState = stepState,
                onSetDailyGoal = onSetDailyGoal,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.95f)
            )

            Spacer(modifier = Modifier.height(layout.gap))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.05f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WeightProgressMiniCard(
                    layout = layout,
                    weightState = weightState,
                    onClick = onOpenWeightProgress,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                ActivityMiniCard(
                    layout = layout,
                    onClick = onOpenActivity,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                SleepMiniCard(layout = layout, modifier = Modifier.weight(1f).fillMaxHeight())
            }

            Spacer(modifier = Modifier.height(layout.gap))
            Text(
                text = stringResource(R.string.work_out),
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = layout.sectionTitle,
                color = AssessmentTitleColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            val workoutGap = 8.dp
            val workoutCardWidth = (contentWidth - workoutGap) / 2
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f),
                horizontalArrangement = Arrangement.spacedBy(workoutGap)
            ) {
                items(homeWorkouts, key = { it.titleRes }) { workout ->
                    WorkoutSeriesCard(
                        title = stringResource(workout.titleRes),
                        subtitle = stringResource(workout.countRes),
                        background = workout.background,
                        buttonColor = workout.buttonColor,
                        imageRes = workout.imageRes,
                        imageFraction = workout.imageFraction,
                        modifier = Modifier
                            .fillParentMaxHeight()
                            .width(workoutCardWidth)
                    )
                }
            }

            Spacer(modifier = Modifier.height(layout.gap))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.28f)
                    .padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FoodActionCard(
                    title = stringResource(R.string.scan_your_food),
                    subtitle = stringResource(R.string.scan_food_sub),
                    button = stringResource(R.string.scan_now),
                    buttonIconRes = R.drawable.scan_now,
                    imageRes = R.drawable.scan_food,
                    onClick = onOpenScanFood,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                FoodActionCard(
                    title = stringResource(R.string.meal_planer),
                    subtitle = stringResource(R.string.meal_planer_sub),
                    button = stringResource(R.string.plan_now),
                    buttonIconRes = R.drawable.plan_now,
                    imageRes = R.drawable.meal_planner,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun HeroWorkoutCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(listOf(Color(0xFFD4C8FF), Color(0xFFB7A4FF)))
            )
    ) {
        Image(
            painter = painterResource(R.drawable.main_screen_work),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxHeight()
                .width(130.dp)
                .offset(x = 8.dp),
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 10.dp)
        ) {
            Column(modifier = Modifier.padding(end = 108.dp)) {
                Text(
                    text = stringResource(R.string.series_workout),
                    fontFamily = SourceSansPro,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.95f)
                )
                Text(
                    text = stringResource(R.string.upper_strength),
                    fontFamily = SourceSansPro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.35f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.125f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(Color.White)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text("25min", fontFamily = SourceSansPro, fontSize = 10.sp, color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("412kcal", fontFamily = SourceSansPro, fontSize = 10.sp, color = Color.White)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("1/8", fontFamily = SourceSansPro, fontSize = 10.sp, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .padding(bottom = 2.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ActionBlue)
                    .clickable { }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = stringResource(R.string.continue_workout),
                    fontFamily = SourceSansPro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    color = Color.White,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun StepCounterCard(
    clusterWidth: Dp,
    ringSize: Dp,
    stepState: StepCounterUiState,
    onSetDailyGoal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(18.dp)
    Row(
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = cardShape,
                ambientColor = Color(0x26000000),
                spotColor = Color(0x1A000000)
            )
            .clip(cardShape)
            .background(Color.White)
            .padding(start = 12.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RingIcon(
                    iconRes = R.drawable.steps,
                    progress = stepState.progress,
                    ringColor = ActionBlue,
                    trackColor = Color(0xFFD6E8FF),
                    size = 42.dp,
                    iconSize = 16.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = stringResource(
                            R.string.steps_progress_value,
                            stepState.steps,
                            stepState.goal
                        ),
                        fontFamily = SourceSansPro,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = AssessmentTitleColor
                    )
                    Text(
                        text = stringResource(R.string.steps_daily),
                        fontFamily = SourceSansPro,
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(ActionBlue)
                    .clickable(onClick = onSetDailyGoal)
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = stringResource(R.string.set_daily_goal),
                    fontFamily = SourceSansPro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color.White
                )
            }
        }
        StepStatsCluster(
            ringSize = ringSize,
            stepState = stepState,
            modifier = Modifier
                .width(clusterWidth)
                .fillMaxHeight()
                .heightIn(min = 96.dp)
        )
    }
}

@Composable
private fun StepStatsCluster(
    ringSize: Dp,
    stepState: StepCounterUiState,
    modifier: Modifier = Modifier
) {
    val minutesProgress = (stepState.activeMinutes / 60f).coerceIn(0f, 1f)
    val calorieProgress = (stepState.calories / 400f).coerceIn(0f, 1f)
    val distanceProgress = (stepState.distanceKm / 5f).coerceIn(0f, 1f)
    Box(modifier = modifier) {
        Column(
            modifier = Modifier.align(Alignment.BottomStart),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RingIcon(
                iconRes = R.drawable.loading,
                progress = minutesProgress,
                ringColor = Color(0xFF3DDC84),
                trackColor = Color(0xFFD9F6E6),
                size = ringSize,
                iconSize = 14.dp
            )
            Text(
                text = stringResource(R.string.step_active_minutes, stepState.activeMinutes),
                fontFamily = SourceSansPro,
                fontSize = 9.sp,
                color = TextSecondary
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(x = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RingIcon(
                iconRes = R.drawable.fire,
                progress = calorieProgress,
                ringColor = Color(0xFF3DDC84),
                trackColor = Color(0xFFD9F6E6),
                size = ringSize + 2.dp,
                iconSize = 14.dp
            )
            Text(
                text = stringResource(R.string.step_kcal_short, stepState.calories),
                fontFamily = SourceSansPro,
                fontSize = 9.sp,
                color = TextSecondary
            )
        }
        Column(
            modifier = Modifier.align(Alignment.BottomEnd),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RingIcon(
                iconRes = R.drawable.pin,
                progress = distanceProgress,
                ringColor = Color(0xFFFF8A4C),
                trackColor = Color(0xFFFFE4D4),
                size = ringSize,
                iconSize = 14.dp
            )
            Text(
                text = stringResource(R.string.step_km_short, stepState.distanceKm),
                fontFamily = SourceSansPro,
                fontSize = 9.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun RingIcon(
    @DrawableRes iconRes: Int,
    progress: Float,
    ringColor: Color,
    trackColor: Color,
    size: Dp,
    iconSize: Dp
) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke
            )
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                style = stroke
            )
        }
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
private fun MiniStatCard(
    title: String,
    @DrawableRes iconRes: Int,
    panelColor: Color,
    layout: HomeLayout,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardShape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = cardShape,
                ambientColor = Color(0x1A000000),
                spotColor = Color(0x14000000)
            )
            .clip(cardShape)
            .background(Color.White)
            .padding(horizontal = 6.dp, vertical = 5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.SemiBold,
                fontSize = layout.miniTitle,
                lineHeight = layout.miniTitle,
                color = AssessmentTitleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(panelColor)
                .padding(horizontal = 5.dp, vertical = 4.dp),
            content = content
        )
    }
}

@Composable
private fun WeightProgressMiniCard(
    layout: HomeLayout,
    weightState: WeightTrackerState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val latest = weightState.logs.firstOrNull()?.weightKg ?: weightState.currentWeightKg
    val oldest = weightState.logs.lastOrNull()?.weightKg
    val delta = if (latest != null && oldest != null) latest - oldest else null
    MiniStatCard(
        title = stringResource(R.string.weight_progress),
        iconRes = R.drawable.footsteps_weight_progress,
        panelColor = Color(0xFFE7F8C9),
        layout = layout,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val w = size.width
            val h = size.height
            if (w <= 0f || h <= 0f) return@Canvas
            if (weightState.logs.isEmpty()) return@Canvas
            val values = weightState.logs.asReversed().map { it.weightKg }
            val minW = values.minOrNull() ?: return@Canvas
            val maxW = values.maxOrNull() ?: return@Canvas
            val span = (maxW - minW).coerceAtLeast(1f)
            val line = Path()
            values.forEachIndexed { index, value ->
                val x = if (values.size == 1) w / 2f else w * (index / (values.size - 1).toFloat())
                val y = h * (0.75f - ((value - minW) / span) * 0.55f)
                if (index == 0) line.moveTo(x, y) else line.lineTo(x, y)
            }
            val fill = Path().apply {
                addPath(line)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(fill, Color(0xFFD4F08A).copy(alpha = 0.55f))
            drawPath(line, Color(0xFF7BC67A), style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))
        }
        Text(
            text = if (latest != null) {
                stringResource(R.string.weight_value_kg, latest)
            } else {
                stringResource(R.string.no_record_found)
            },
            fontFamily = SourceSansPro,
            fontWeight = FontWeight.Bold,
            fontSize = layout.miniValue,
            lineHeight = layout.miniValue,
            color = AssessmentTitleColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = when {
                delta == null -> stringResource(R.string.weight_unset)
                else -> stringResource(
                    R.string.weight_change_this_month,
                    String.format(java.util.Locale.US, "%+.1fkg", delta)
                )
            },
            fontFamily = SourceSansPro,
            fontSize = layout.miniCaption,
            lineHeight = layout.miniCaption,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ActivityMiniCard(
    layout: HomeLayout,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    MiniStatCard(
        title = stringResource(R.string.activity),
        iconRes = R.drawable.activity,
        panelColor = Color(0xFFD8F4FB),
        layout = layout,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (size.width <= 0f || size.height <= 0f) return@Canvas
            val bars = floatArrayOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.8f, 0.45f)
            val gap = size.width / bars.size
            bars.forEachIndexed { i, v ->
                val x = gap * i + gap / 2f
                drawLine(
                    color = Color(0xFF5EC8E0),
                    start = Offset(x, size.height),
                    end = Offset(x, size.height * (1f - v)),
                    strokeWidth = 5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
        Text(
            "5.8km",
            fontFamily = SourceSansPro,
            fontWeight = FontWeight.Bold,
            fontSize = layout.miniValue,
            lineHeight = layout.miniValue,
            color = AssessmentTitleColor
        )
        Text(
            "264kcal",
            fontFamily = SourceSansPro,
            fontSize = layout.miniCaption,
            lineHeight = layout.miniCaption,
            color = TextSecondary
        )
    }
}

@Composable
private fun SleepMiniCard(
    layout: HomeLayout,
    modifier: Modifier = Modifier
) {
    MiniStatCard(
        title = stringResource(R.string.sleep_tracker),
        iconRes = R.drawable.sleep_tracker,
        panelColor = Color(0xFFDDF6F0),
        layout = layout,
        modifier = modifier
    ) {
        Text(
            text = "Sleep & Recovery",
            fontFamily = SourceSansPro,
            fontWeight = FontWeight.Bold,
            fontSize = layout.miniValue,
            lineHeight = layout.miniValue,
            color = AssessmentTitleColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            "7h 20m",
            fontFamily = SourceSansPro,
            fontSize = layout.miniCaption,
            lineHeight = layout.miniCaption,
            color = TextSecondary
        )
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (size.width <= 0f || size.height <= 0f) return@Canvas
            val bars = floatArrayOf(0.4f, 0.7f, 0.55f, 0.9f, 0.65f, 0.8f, 0.5f)
            val gap = size.width / bars.size
            bars.forEachIndexed { i, v ->
                val x = gap * i + gap / 2f
                drawLine(
                    color = BrandPrimary,
                    start = Offset(x, size.height),
                    end = Offset(x, size.height * (1f - v)),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun WorkoutSeriesCard(
    title: String,
    subtitle: String,
    background: Color,
    buttonColor: Color,
    @DrawableRes imageRes: Int,
    imageFraction: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(background)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val spacing = 9.dp.toPx()
            val radius = 1.15.dp.toPx()
            val dot = Color.White.copy(alpha = 0.28f)
            val dotLimit = size.height - 34.dp.toPx()
            var y = spacing * 0.5f
            while (y < dotLimit) {
                var x = spacing * 0.5f
                while (x < size.width) {
                    drawCircle(color = dot, radius = radius, center = Offset(x, y))
                    x += spacing
                }
                y += spacing
            }
        }
        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxHeight()
                .fillMaxWidth(imageFraction),
            contentScale = ContentScale.Fit,
            alignment = Alignment.BottomEnd
        )
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.64f)
                .padding(start = 10.dp, top = 8.dp, bottom = 12.dp, end = 2.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    fontFamily = SourceSansPro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontFamily = SourceSansPro,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.95f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box(
                modifier = Modifier
                    .height(18.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(buttonColor)
                    .clickable { }
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.start),
                    fontFamily = SourceSansPro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    lineHeight = 10.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun FoodActionCard(
    title: String,
    subtitle: String,
    button: String,
    @DrawableRes buttonIconRes: Int,
    @DrawableRes imageRes: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val cardShape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = cardShape,
                ambientColor = Color(0x26000000),
                spotColor = Color(0x1A000000)
            )
            .clip(cardShape)
            .background(Color.White)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .fillMaxWidth(0.48f)
                .fillMaxHeight(0.42f)
                .clip(RoundedCornerShape(topEnd = 16.dp, bottomStart = 18.dp)),
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopEnd
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, bottom = 12.dp, top = 6.dp)
        ) {
            Text(
                title,
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = AssessmentTitleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                subtitle,
                fontFamily = SourceSansPro,
                fontSize = 9.sp,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 11.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(ActionBlue)
                    .clickable(enabled = onClick != null) { onClick?.invoke() }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(buttonIconRes),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = button,
                    fontFamily = SourceSansPro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    AIFitnessTrackerTheme {
        HomeScreen()
    }
}
