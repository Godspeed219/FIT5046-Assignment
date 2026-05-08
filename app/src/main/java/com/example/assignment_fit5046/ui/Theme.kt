package com.example.assignment_fit5046.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.example.assignment_fit5046.datamodels.UserRole

private val VolunteerColorScheme = lightColorScheme(
    primary = VolunteerPrimary,
    onPrimary = VolunteerOnPrimary,
    primaryContainer = VolunteerPrimaryContainer,
    onPrimaryContainer = VolunteerOnPrimaryContainer,
    secondary = VolunteerSecondary,
    onSecondary = VolunteerOnSecondary,
    secondaryContainer = VolunteerSecondaryContainer,
    onSecondaryContainer = VolunteerOnSecondaryContainer,
    background = VolunteerBackground,
    onBackground = VolunteerOnBackground,
    surface = VolunteerSurface,
    onSurface = VolunteerOnSurface,
    onSurfaceVariant = VolunteerOnSurfaceVariant,
    outline = VolunteerOutline,
    error = VolunteerError,
    onError = VolunteerOnError,
    errorContainer = VolunteerErrorContainer,
    onErrorContainer = VolunteerOnErrorContainer
)

private val NgoColorScheme = lightColorScheme(
    primary = NgoPrimary,
    onPrimary = NgoOnPrimary,
    primaryContainer = NgoPrimaryContainer,
    onPrimaryContainer = NgoOnPrimaryContainer,
    secondary = NgoSecondary,
    onSecondary = NgoOnSecondary,
    secondaryContainer = NgoSecondaryContainer,
    onSecondaryContainer = NgoOnSecondaryContainer,
    background = NgoBackground,
    onBackground = NgoOnBackground,
    surface = NgoSurface,
    onSurface = NgoOnSurface,
    onSurfaceVariant = NgoOnSurfaceVariant,
    outline = NgoOutline,
    error = NgoError,
    onError = NgoOnError,
    errorContainer = NgoErrorContainer,
    onErrorContainer = NgoOnErrorContainer
)

@Composable
fun AppTheme(
    role: UserRole = UserRole.VOLUNTEER,
    content: @Composable () -> Unit
) {
    val colorScheme = when (role) {
        UserRole.VOLUNTEER -> VolunteerColorScheme
        UserRole.NGO -> NgoColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
