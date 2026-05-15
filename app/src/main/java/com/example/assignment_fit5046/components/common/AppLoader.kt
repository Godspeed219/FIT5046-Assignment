package com.example.assignment_fit5046.components.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.assignment_fit5046.R
import com.example.assignment_fit5046.datamodels.UserRole

@Composable
fun AppLoader(
    isLoading: Boolean,
    role: UserRole,
    modifier: Modifier = Modifier
) {
    val rawRes = if (role == UserRole.NGO) R.raw.loading_ngo else R.raw.loading_volunteer
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(rawRes))

    AnimatedVisibility(
        visible = isLoading,
        modifier = modifier,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(150.dp)
            )
        }
    }
}
