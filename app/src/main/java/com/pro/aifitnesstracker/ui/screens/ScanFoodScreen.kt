package com.pro.aifitnesstracker.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.pro.aifitnesstracker.R
import com.pro.aifitnesstracker.data.ClientIdStore
import com.pro.aifitnesstracker.data.foodscan.FoodScanAnalyzeResult
import com.pro.aifitnesstracker.data.foodscan.FoodScanApi
import com.pro.aifitnesstracker.ui.theme.AssessmentTitleColor
import com.pro.aifitnesstracker.ui.theme.SourceSansPro
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors

private val ScanPurple = Color(0xFF635BFF)

private sealed interface ScanFoodStep {
    data object Camera : ScanFoodStep
    data class Analyzing(val imageUri: Uri, val imageFile: File) : ScanFoodStep
    data class Result(val imageUri: Uri, val result: FoodScanAnalyzeResult) : ScanFoodStep
}

@Composable
fun ScanFoodScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember { FoodScanApi() }
    val clientId = remember { ClientIdStore.getOrCreate(context) }

    var step by remember { mutableStateOf<ScanFoodStep>(ScanFoodStep.Camera) }
    var isSaving by remember { mutableStateOf(false) }
    var cameraPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        cameraPermissionGranted = granted
        if (!granted) {
            Toast.makeText(
                context,
                context.getString(R.string.camera_permission_needed),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val file = copyUriToCache(context, uri) ?: run {
                Toast.makeText(
                    context,
                    context.getString(R.string.scan_food_failed),
                    Toast.LENGTH_SHORT
                ).show()
                return@rememberLauncherForActivityResult
            }
            step = ScanFoodStep.Analyzing(Uri.fromFile(file), file)
        }
    }

    LaunchedEffect(Unit) {
        if (!cameraPermissionGranted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    when (val current = step) {
        is ScanFoodStep.Result -> {
            BackHandler { step = ScanFoodStep.Camera }
            ScanFoodResultScreen(
                imageUri = current.imageUri,
                result = current.result,
                isSaving = isSaving,
                onBack = { step = ScanFoodStep.Camera },
                onAddToLog = {
                    if (isSaving) return@ScanFoodResultScreen
                    scope.launch {
                        isSaving = true
                        try {
                            api.addToLog(current.result, clientId)
                            Toast.makeText(
                                context,
                                context.getString(R.string.added_to_log),
                                Toast.LENGTH_SHORT
                            ).show()
                            onBack()
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                e.message ?: context.getString(R.string.scan_food_failed),
                                Toast.LENGTH_LONG
                            ).show()
                        } finally {
                            isSaving = false
                        }
                    }
                },
                modifier = modifier
            )
        }

        is ScanFoodStep.Analyzing -> {
            BackHandler { step = ScanFoodStep.Camera }
            LaunchedEffect(current.imageFile.absolutePath) {
                try {
                    val analyzed = api.analyze(current.imageFile, clientId)
                    step = ScanFoodStep.Result(current.imageUri, analyzed)
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        e.message ?: context.getString(R.string.scan_food_failed),
                        Toast.LENGTH_LONG
                    ).show()
                    step = ScanFoodStep.Camera
                }
            }
            ScanFoodCameraContent(
                previewImageUri = current.imageUri,
                analyzing = true,
                cameraEnabled = false,
                onBack = { step = ScanFoodStep.Camera },
                onCapture = {},
                onOpenGallery = {},
                modifier = modifier
            )
        }

        ScanFoodStep.Camera -> {
            BackHandler(onBack = onBack)
            ScanFoodCameraContent(
                previewImageUri = null,
                analyzing = false,
                cameraEnabled = cameraPermissionGranted,
                onBack = onBack,
                onCapture = { imageCapture ->
                    val photoFile = File(
                        context.cacheDir,
                        "food_scan_${System.currentTimeMillis()}.jpg"
                    )
                    val output = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                    imageCapture.takePicture(
                        output,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                step = ScanFoodStep.Analyzing(
                                    Uri.fromFile(photoFile),
                                    photoFile
                                )
                            }

                            override fun onError(exception: ImageCaptureException) {
                                Toast.makeText(
                                    context,
                                    exception.message
                                        ?: context.getString(R.string.scan_food_failed),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                },
                onOpenGallery = {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onRequestPermission = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                modifier = modifier
            )
        }
    }
}

@Composable
private fun ScanFoodCameraContent(
    previewImageUri: Uri?,
    analyzing: Boolean,
    cameraEnabled: Boolean,
    onBack: () -> Unit,
    onCapture: (ImageCapture) -> Unit,
    onOpenGallery: () -> Unit,
    onRequestPermission: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowInsetsControllerCompat(it, view) }
        val previous = controller?.isAppearanceLightStatusBars ?: true
        controller?.isAppearanceLightStatusBars = false
        onDispose { controller?.isAppearanceLightStatusBars = previous }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        if (previewImageUri != null) {
            AsyncImage(
                model = previewImageUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else if (cameraEnabled) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                onImageCaptureReady = { imageCapture = it }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1A1A1E))
                    .clickable(onClick = onRequestPermission),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.camera_permission_needed),
                    fontFamily = SourceSansPro,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.18f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
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

            Spacer(modifier = Modifier.weight(0.18f))

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp)
                    .weight(0.52f),
                contentAlignment = Alignment.Center
            ) {
                val frameSize = minOf(maxWidth, maxHeight)
                Box(modifier = Modifier.size(frameSize)) {
                    ScanCorners(modifier = Modifier.fillMaxSize())
                    if (analyzing) {
                        ScanLine(modifier = Modifier.fillMaxSize())
                    }
                }
            }

            if (analyzing) {
                Text(
                    text = stringResource(R.string.analyzing_image),
                    fontFamily = SourceSansPro,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 18.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(34.dp))
            }

            Spacer(modifier = Modifier.weight(0.18f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .padding(bottom = 28.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable(enabled = !analyzing, onClick = onOpenGallery),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_gallery),
                        contentDescription = stringResource(R.string.gallery),
                        tint = ScanPurple,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(78.dp)
                        .border(5.dp, ScanPurple, CircleShape)
                        .padding(7.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable(enabled = !analyzing) {
                            val capture = imageCapture
                            if (capture != null) {
                                onCapture(capture)
                            } else if (!cameraEnabled) {
                                onRequestPermission()
                            }
                        }
                )
            }
        }
    }
}

@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    onImageCaptureReady: (ImageCapture) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val mainExecutor = ContextCompat.getMainExecutor(context)
        val listener = Runnable {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            val imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            onImageCaptureReady(imageCapture)
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture
            )
        }
        cameraProviderFuture.addListener(listener, mainExecutor)
        onDispose {
            runCatching {
                ProcessCameraProvider.getInstance(context).get().unbindAll()
            }
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

@Composable
private fun ScanCorners(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = 4.dp.toPx()
        val length = 28.dp.toPx()
        val color = Color.White
        val style = Stroke(width = stroke, cap = StrokeCap.Round)

        fun corner(start: Offset, h: Offset, v: Offset) {
            val path = Path().apply {
                moveTo(start.x + h.x, start.y + h.y)
                lineTo(start.x, start.y)
                lineTo(start.x + v.x, start.y + v.y)
            }
            drawPath(path, color, style = style)
        }

        corner(Offset(0f, 0f), Offset(length, 0f), Offset(0f, length))
        corner(Offset(size.width, 0f), Offset(-length, 0f), Offset(0f, length))
        corner(Offset(0f, size.height), Offset(length, 0f), Offset(0f, -length))
        corner(Offset(size.width, size.height), Offset(-length, 0f), Offset(0f, -length))
    }
}

@Composable
private fun ScanLine(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "scan")
    val progress by transition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanProgress"
    )

    BoxWithConstraints(modifier = modifier) {
        val lineHeight = 56.dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(lineHeight)
                .offset(y = (maxHeight - lineHeight) * progress)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            ScanPurple.copy(alpha = 0.55f),
                            ScanPurple.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

private fun copyUriToCache(context: Context, uri: Uri): File? {
    return try {
        val extension = context.contentResolver.getType(uri)?.substringAfter('/') ?: "jpg"
        val outFile = File(context.cacheDir, "food_gallery_${System.currentTimeMillis()}.$extension")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(outFile).use { output -> input.copyTo(output) }
        } ?: return null
        outFile
    } catch (_: Exception) {
        null
    }
}
