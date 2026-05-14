package com.example.assignment_fit5046.components.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.assignment_fit5046.datamodels.UserRole
import com.example.assignment_fit5046.ui.NgoPrimary
import com.example.assignment_fit5046.ui.VolunteerPrimary

@Composable
fun AppLoader(
    isLoading: Boolean,
    role: UserRole,
    modifier: Modifier = Modifier
) {
    val indicatorColor = if (role == UserRole.NGO) NgoPrimary else VolunteerPrimary

    AnimatedVisibility(
        visible = isLoading,
        modifier = modifier,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = indicatorColor,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
