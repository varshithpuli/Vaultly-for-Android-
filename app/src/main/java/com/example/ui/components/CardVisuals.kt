package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VaultCardEntity
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.util.SecurityAndTotpUtils

fun parseHexColor(hex: String, fallback: Color = Color(0xFF4F46E5)): Color {
    return try {
        val clean = hex.trim().removePrefix("#")
        when (clean.length) {
            6 -> Color(("FF$clean").toLong(16))
            8 -> Color(clean.toLong(16))
            else -> fallback
        }
    } catch (_: Exception) {
        fallback
    }
}

@Composable
fun InteractiveVaultCard(
    card: VaultCardEntity,
    showUnmasked: Boolean,
    onCopyText: (String, String) -> Unit,
    onToggleFavourite: () -> Unit,
    onOpenDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFlipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "card_3d_flip"
    )

    val baseColor = parseHexColor(card.colorHex)
    val darkShade = Color(
        red = (baseColor.red * 0.45f).coerceIn(0f, 1f),
        green = (baseColor.green * 0.45f).coerceIn(0f, 1f),
        blue = (baseColor.blue * 0.55f).coerceIn(0f, 1f),
        alpha = 1f
    )
    val highlightShade = Color(
        red = (baseColor.red + 0.18f).coerceIn(0f, 1f),
        green = (baseColor.green + 0.16f).coerceIn(0f, 1f),
        blue = (baseColor.blue + 0.22f).coerceIn(0f, 1f),
        alpha = 1f
    )

    val utilization = if (card.creditLimit > 0L) {
        (card.currentOwed.toFloat() / card.creditLimit.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(224.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 16f * density
            }
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(highlightShade, baseColor, darkShade),
                    start = Offset(0f, 0f),
                    end = Offset(900f, 600f)
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.32f), Color.White.copy(alpha = 0.06f))
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { isFlipped = !isFlipped }
            .testTag("vault_card_${card.id}")
    ) {
        // Decorative pattern canvas
        CardPatternCanvas(pattern = card.pattern, modifier = Modifier.fillMaxSize())

        if (rotation <= 90f) {
            // FRONT FACE
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Bank + Variant + Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = card.bank.uppercase(),
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Color.White.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(99.dp)
                            ) {
                                Text(
                                    text = card.cardKind,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            if (card.isBlocked) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFFE11D48),
                                    shape = RoundedCornerShape(99.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Block,
                                            contentDescription = "Blocked card",
                                            tint = Color.White,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "BLOCKED",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        Text(
                            text = card.cardName,
                            color = Color.White,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onToggleFavourite,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (card.isFavourite) Icons.Default.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Toggle favourite card",
                                tint = if (card.isFavourite) Color(0xFFFBBF24) else Color.White.copy(alpha = 0.8f)
                            )
                        }
                        IconButton(
                            onClick = onOpenDetails,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("card_details_btn_${card.id}")
                        ) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.28f),
                                shape = CircleShape
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Open card tools and details",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Middle Row: EMV Chip + Contactless + Number
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        EmvSmartChip()
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = "Contactless NFC enabled",
                            tint = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(90f)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = card.variant,
                            color = Color.White.copy(alpha = 0.78f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = SecurityAndTotpUtils.formatCardNumber(
                                raw = card.cardNumber,
                                masked = !showUnmasked
                            ),
                            color = Color.White,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = 1.4.sp
                        )
                        Surface(
                            color = Color.White.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(99.dp),
                            modifier = Modifier.clickable {
                                onCopyText(card.cardNumber, "Card number")
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy card number",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Copy",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Bottom Row: Holder, Expiry, Network Emblem & Utilization
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CARDHOLDER",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 9.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = card.holderName.uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "VALID THRU",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 9.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = card.expiry,
                                color = Color.White,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        NetworkBrandBadge(network = card.network)
                    }

                    if (card.creditLimit > 0L) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { utilization },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(99.dp)),
                            color = when {
                                utilization > 0.7f -> Color(0xFFFF6B72)
                                utilization > 0.35f -> Color(0xFFFBBF24)
                                else -> Color(0xFF34D399)
                            },
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        } else {
            // BACK FACE (rotated 180 degrees so text reads right-side-up)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
                    .padding(vertical = 14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Magnetic Stripe
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .background(Color(0xFF090B10))
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    // Signature & CVV Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "AUTHORIZED SIGNATURE",
                                    color = Color(0xFF64748B),
                                    fontSize = 9.sp,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "CVV ${card.cvv}",
                                    color = Color(0xFF0F172A),
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.clickable {
                                onCopyText(card.cvv, "CVV")
                            }
                        ) {
                            Text(
                                text = "Copy CVV",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // PIN, Bill & Lounge Summary on Back
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ATM / POS PIN: ${if (card.cardPin.isNotBlank()) card.cardPin else "Not set"}",
                                color = Color.White,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Statement: Day ${card.statementDay} · Due: Day ${card.dueDay}",
                                color = Color.White.copy(alpha = 0.78f),
                                fontSize = 11.sp
                            )
                        }
                        Surface(
                            color = Color.White.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(99.dp),
                            modifier = Modifier.clickable { onOpenDetails() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlipCameraAndroid,
                                    contentDescription = "Manage card",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Manage Card",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Bottom Note
                Text(
                    text = card.notes.ifBlank { "Tap anywhere on card to flip back to front" },
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    }
}

@Composable
fun EmvSmartChip() {
    Canvas(modifier = Modifier.size(width = 38.dp, height = 28.dp)) {
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFFDE68A), Color(0xFFD97706), Color(0xFFF59E0B))
            ),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
        )
        val strokeColor = Color(0xFF78350F).copy(alpha = 0.45f)
        drawRoundRect(
            color = strokeColor,
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
            style = Stroke(width = 1.dp.toPx())
        )
        drawLine(
            color = strokeColor,
            start = Offset(0f, size.height * 0.5f),
            end = Offset(size.width, size.height * 0.5f),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = strokeColor,
            start = Offset(size.width * 0.35f, 0f),
            end = Offset(size.width * 0.35f, size.height),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = strokeColor,
            start = Offset(size.width * 0.65f, 0f),
            end = Offset(size.width * 0.65f, size.height),
            strokeWidth = 1.dp.toPx()
        )
    }
}

@Composable
fun NetworkBrandBadge(network: String) {
    Surface(
        color = Color.White.copy(alpha = 0.18f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = network.uppercase(),
            color = Color.White,
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun CardPatternCanvas(pattern: String, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        // Ambient top-right glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
                center = Offset(size.width * 0.88f, size.height * 0.15f),
                radius = size.width * 0.65f
            ),
            center = Offset(size.width * 0.88f, size.height * 0.15f),
            radius = size.width * 0.65f
        )

        when (pattern.lowercase()) {
            "waves" -> {
                val path = Path().apply {
                    moveTo(0f, size.height * 0.75f)
                    cubicTo(
                        size.width * 0.3f, size.height * 0.55f,
                        size.width * 0.65f, size.height * 0.95f,
                        size.width, size.height * 0.65f
                    )
                }
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.12f),
                    style = Stroke(width = 24.dp.toPx())
                )
            }
            "mesh" -> {
                val step = 36.dp.toPx()
                var x = 0f
                while (x < size.width) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(x, 0f),
                        end = Offset(x + size.height * 0.4f, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                    x += step
                }
            }
            "carbon" -> {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.05f),
                    topLeft = Offset(size.width * 0.55f, -size.height * 0.2f),
                    size = Size(size.width * 0.6f, size.height * 1.4f),
                    cornerRadius = CornerRadius(40f, 40f)
                )
            }
            else -> {
                // Aurora orbs
                drawCircle(
                    color = Color.White.copy(alpha = 0.07f),
                    radius = size.height * 0.55f,
                    center = Offset(size.width * 0.15f, size.height * 0.95f)
                )
            }
        }
    }
}
