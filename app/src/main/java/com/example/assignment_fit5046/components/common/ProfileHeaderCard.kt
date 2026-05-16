package com.example.assignment_fit5046.components.common

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.assignment_fit5046.datamodels.User
import com.example.assignment_fit5046.datamodels.UserRole
import com.example.assignment_fit5046.ui.VolunteerSecondaryContainer
import kotlinx.coroutines.delay

@Composable
fun ProfileHeaderCard(
    user: User,
    role: UserRole,
    statOneLabel: String,
    statOneValue: String,
    statTwoLabel: String,
    statTwoValue: String,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    // Single visibility trigger — flipped once on composition
    var cardVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { cardVisible = true }

    // Card fade + slide up
    val cardAlpha by animateFloatAsState(
        targetValue = if (cardVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "cardAlpha"
    )
    val cardOffset by animateDpAsState(
        targetValue = if (cardVisible) 0.dp else 16.dp,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "cardOffset"
    )

    // Stats card comes in slightly later for a staggered feel
    val statsCardAlpha by animateFloatAsState(
        targetValue = if (cardVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 120, easing = FastOutSlowInEasing),
        label = "statsCardAlpha"
    )
    val statsCardOffset by animateDpAsState(
        targetValue = if (cardVisible) 0.dp else 16.dp,
        animationSpec = tween(durationMillis = 400, delayMillis = 120, easing = FastOutSlowInEasing),
        label = "statsCardOffset"
    )

    // Avatar spring pop
    val avatarScale by animateFloatAsState(
        targetValue = if (cardVisible) 1f else 0.7f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "avatarScale"
    )

    // Staggered row visibility via produceState with kotlinx.coroutines.delay
    val nameVisible by produceState(initialValue = false, cardVisible) {
        if (cardVisible) { delay(150); value = true }
    }
    val emailVisible by produceState(initialValue = false, cardVisible) {
        if (cardVisible) { delay(200); value = true }
    }
    val bioVisible by produceState(initialValue = false, cardVisible) {
        if (cardVisible) { delay(250); value = true }
    }

    val nameAlpha by animateFloatAsState(
        targetValue = if (nameVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "nameAlpha"
    )
    val nameOffset by animateDpAsState(
        targetValue = if (nameVisible) 0.dp else 8.dp,
        animationSpec = tween(durationMillis = 350),
        label = "nameOffset"
    )
    val emailAlpha by animateFloatAsState(
        targetValue = if (emailVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "emailAlpha"
    )
    val emailOffset by animateDpAsState(
        targetValue = if (emailVisible) 0.dp else 8.dp,
        animationSpec = tween(durationMillis = 350),
        label = "emailOffset"
    )
    val bioAlpha by animateFloatAsState(
        targetValue = if (bioVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "bioAlpha"
    )
    val bioOffset by animateDpAsState(
        targetValue = if (bioVisible) 0.dp else 8.dp,
        animationSpec = tween(durationMillis = 350),
        label = "bioOffset"
    )

    // Pre-convert dp offsets to px for graphicsLayer
    val cardOffsetPx = with(density) { cardOffset.toPx() }
    val statsCardOffsetPx = with(density) { statsCardOffset.toPx() }
    val nameOffsetPx = with(density) { nameOffset.toPx() }
    val emailOffsetPx = with(density) { emailOffset.toPx() }
    val bioOffsetPx = with(density) { bioOffset.toPx() }

    // Bio content differs by role
    val bioContent = when (role) {
        UserRole.VOLUNTEER -> user.bio
        UserRole.NGO -> user.ngoDescription
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {

        // ── Upper card: profile details ──────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = cardAlpha
                    translationY = cardOffsetPx
                },
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Avatar with spring scale
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .graphicsLayer {
                                scaleX = avatarScale
                                scaleY = avatarScale
                            }
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (user.profileImageUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = user.profileImageUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        // Online presence dot
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .align(Alignment.BottomEnd)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        )
                    }

                    // User detail rows
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        // Name
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.graphicsLayer {
                                alpha = nameAlpha
                                translationY = nameOffsetPx
                            }
                        )

                        // Email
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.graphicsLayer {
                                alpha = emailAlpha
                                translationY = emailOffsetPx
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = user.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Bio / NGO description — only if not blank
                        if (bioContent.isNotBlank()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.graphicsLayer {
                                    alpha = bioAlpha
                                    translationY = bioOffsetPx
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FormatQuote,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(12.dp)
                                        .padding(top = 2.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = bioContent,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Lower card: stats ────────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = statsCardAlpha
                    translationY = statsCardOffsetPx
                },
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 11.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Stat 1
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = statOneValue,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = statOneLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }

                // Stat 2
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = statTwoValue,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = statTwoLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}