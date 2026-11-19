package com.pro.aifitnesstracker.ui.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pro.aifitnesstracker.R
import com.pro.aifitnesstracker.ui.components.PrimaryButton
import com.pro.aifitnesstracker.ui.theme.AIFitnessTrackerTheme
import com.pro.aifitnesstracker.ui.theme.SourceSansPro
import com.pro.aifitnesstracker.ui.theme.SplashSubtitle
import kotlinx.coroutines.launch

private data class IntroPage(
    @DrawableRes val imageRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val imageAlignment: Alignment
)

private val introPages = listOf(
    IntroPage(
        imageRes = R.drawable.intro_1,
        titleRes = R.string.intro_1_title,
        subtitleRes = R.string.intro_1_subtitle,
        imageAlignment = Alignment.TopCenter
    ),
    IntroPage(
        imageRes = R.drawable.intro_2,
        titleRes = R.string.intro_2_title,
        subtitleRes = R.string.intro_2_subtitle,
        imageAlignment = Alignment.Center
    ),
    IntroPage(
        imageRes = R.drawable.intro_3,
        titleRes = R.string.intro_3_title,
        subtitleRes = R.string.intro_3_subtitle,
        imageAlignment = Alignment.Center
    ),
    IntroPage(
        imageRes = R.drawable.intro_4,
        titleRes = R.string.intro_4_title,
        subtitleRes = R.string.intro_4_subtitle,
        imageAlignment = Alignment.Center
    ),
    IntroPage(
        imageRes = R.drawable.intro_5,
        titleRes = R.string.intro_5_title,
        subtitleRes = R.string.intro_5_subtitle,
        imageAlignment = Alignment.Center
    )
)

@Composable
fun IntroScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { introPages.size })
    val scope = rememberCoroutineScope()
    val currentPage = pagerState.currentPage
    val isLastPage = currentPage == introPages.lastIndex

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            Image(
                painter = painterResource(introPages[page].imageRes),
                contentDescription = stringResource(introPages[page].titleRes),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = introPages[page].imageAlignment
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.35f to Color.Black.copy(alpha = 0.55f),
                        1f to Color.Black
                    )
                )
        )

        Text(
            text = stringResource(R.string.skip),
            color = Color.White,
            fontFamily = SourceSansPro,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 8.dp, end = 12.dp)
                .clickable(onClick = onFinished)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 40.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(introPages[currentPage].titleRes),
                color = Color.White,
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(introPages[currentPage].subtitleRes),
                color = SplashSubtitle,
                fontFamily = SourceSansPro,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            IntroProgressBar(
                progress = (currentPage + 1) / introPages.size.toFloat()
            )

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = stringResource(
                    if (isLastPage) R.string.continue_cta else R.string.next
                ),
                onClick = {
                    if (isLastPage) {
                        onFinished()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(currentPage + 1)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
private fun IntroProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "introProgress"
    )
    Box(
        modifier = modifier
            .width(148.dp)
            .height(5.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.28f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .background(Color.White, RoundedCornerShape(50))
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun IntroScreenPreview() {
    AIFitnessTrackerTheme(darkTheme = true, dynamicColor = false) {
        IntroScreen(onFinished = {})
    }
}
