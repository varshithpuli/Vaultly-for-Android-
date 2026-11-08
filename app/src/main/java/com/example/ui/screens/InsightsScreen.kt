package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SpendTransactionEntity
import com.example.data.VaultCardEntity
import com.example.data.VaultCategory
import com.example.data.VaultItemEntity
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.util.SecurityAndTotpUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    cards: List<VaultCardEntity>,
    vaultItems: List<VaultItemEntity>,
    spends: List<SpendTransactionEntity>,
    modifier: Modifier = Modifier
) {
    var selectedSegment by remember { mutableIntStateOf(0) }
    val segments = listOf("Monthly Spends", "What You Owe", "Reminders")

    val totalSpent = remember(spends) { spends.sumOf { it.amount } }
    val byCategory = remember(spends) {
        spends.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .entries
            .sortedByDescending { it.value }
    }
    val byCard = remember(spends) {
        spends.groupBy { it.cardLabel }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .entries
            .sortedByDescending { it.value }
    }

    val unpaidBills = remember(vaultItems) {
        vaultItems.filter {
            it.categoryKey == VaultCategory.BILL.key && !it.badgeStatus.equals("Paid", ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("insights_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                segments.forEachIndexed { index, title ->
                    SegmentedButton(
                        selected = selectedSegment == index,
                        onClick = { selectedSegment = index },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = segments.size),
                        modifier = Modifier.testTag("insights_seg_$index")
                    ) {
                        Text(title, fontSize = 12.sp)
                    }
                }
            }
        }

        when (selectedSegment) {
            0 -> {
                // Monthly Spend Breakdown & Donut Chart
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Recorded Spend Analytics",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = SecurityAndTotpUtils.formatCurrencyInr(totalSpent),
                                        style = MaterialTheme.typography.displayMedium
                                    )
                                    Text(
                                        text = "${spends.size} transactions across ${byCard.size} cards",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                SpendDonutChart(
                                    categoryTotals = byCategory.map { it.key to it.value },
                                    total = totalSpent
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                            Text("By Category", style = MaterialTheme.typography.titleMedium)

                            byCategory.forEachIndexed { idx, (cat, amt) ->
                                val ratio = if (totalSpent > 0L) amt.toFloat() / totalSpent.toFloat() else 0f
                                val barColor = chartPalette[idx % chartPalette.size]
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(barColor)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(cat, style = MaterialTheme.typography.bodyMedium)
                                        }
                                        Text(
                                            text = "${SecurityAndTotpUtils.formatCurrencyInr(amt)} (${(ratio * 100).toInt()}%)",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = { ratio },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(7.dp)
                                            .clip(RoundedCornerShape(99.dp)),
                                        color = barColor,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Spend by Card
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Spend by Card", style = MaterialTheme.typography.titleMedium)
                            byCard.forEach { (cardName, amt) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(cardName, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = SecurityAndTotpUtils.formatCurrencyInr(amt),
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Recent Transactions Feed
                item {
                    Text("Recent Transactions", style = MaterialTheme.typography.titleMedium)
                }

                items(spends, key = { it.id }) { tx ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tx.merchant, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = "${tx.category} · ${tx.cardLabel} · ${tx.dateIso}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = SecurityAndTotpUtils.formatCurrencyInr(tx.amount),
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            1 -> {
                // Outstanding Now & Statement Bills
                val totalCardOwed = cards.sumOf { it.currentOwed }
                val totalUnpaidBillAmt = unpaidBills.sumOf { it.primarySecret.toLongOrNull() ?: 0L }
                val totalEmiMonthly = cards.filter { it.emiMonthsRemaining > 0 }.sumOf { it.emiMonthlyAmount }

                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Total Outstanding Exposure", style = MaterialTheme.typography.titleMedium)
                            }
                            Text(
                                text = SecurityAndTotpUtils.formatCurrencyInr(totalCardOwed + totalUnpaidBillAmt),
                                style = MaterialTheme.typography.displayMedium
                            )
                            Text(
                                text = "Card Balances: ${SecurityAndTotpUtils.formatCurrencyInr(totalCardOwed)} · Unpaid Bills: ${
                                    SecurityAndTotpUtils.formatCurrencyInr(totalUnpaidBillAmt)
                                } · Monthly EMIs: ${SecurityAndTotpUtils.formatCurrencyInr(totalEmiMonthly)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                items(cards.filter { it.currentOwed > 0L }, key = { it.id }) { card ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("${card.bank} ${card.cardName}", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = "Statement Day ${card.statementDay} · Due Day ${card.dueDay}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = SecurityAndTotpUtils.formatCurrencyInr(card.currentOwed),
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFBBF24)
                            )
                        }
                    }
                }
            }

            else -> {
                // Upcoming Reminders (Bills, Statement Dates, ID & Vehicle Expiries)
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Privacy-Safe Local Reminders", style = MaterialTheme.typography.titleMedium)
                            }
                            Text(
                                text = "Notifications show only the card or document name — never card numbers, CVVs, or balances.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            cards.filter { !it.isBlocked }.forEach { card ->
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("${card.bank} ${card.cardName}", style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            "Statement on Day ${card.statementDay} · Payment Due on Day ${card.dueDay}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                        shape = RoundedCornerShape(99.dp)
                                    ) {
                                        Text(
                                            text = "Day ${card.dueDay}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            vaultItems.filter { it.extraField2.isNotBlank() }.forEach { item ->
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(item.title, style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            item.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = item.extraField2,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private val chartPalette = listOf(
    Color(0xFF8C7BFF),
    Color(0xFF27E6FF),
    Color(0xFF34D399),
    Color(0xFFFBBF24),
    Color(0xFFFB7185),
    Color(0xFFA78BFA),
    Color(0xFF38BDF8)
)

@Composable
private fun SpendDonutChart(
    categoryTotals: List<Pair<String, Long>>,
    total: Long
) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(96.dp)) {
        Canvas(modifier = Modifier.size(86.dp)) {
            if (total <= 0L) {
                drawArc(
                    color = Color.Gray.copy(alpha = 0.25f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                )
            } else {
                var startAngle = -90f
                categoryTotals.forEachIndexed { idx, (_, value) ->
                    val sweep = (value.toFloat() / total.toFloat()) * 360f
                    drawArc(
                        color = chartPalette[idx % chartPalette.size],
                        startAngle = startAngle,
                        sweepAngle = (sweep - 4f).coerceAtLeast(2f),
                        useCenter = false,
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Butt)
                    )
                    startAngle += sweep
                }
            }
        }
        Icon(
            imageVector = Icons.Default.PieChart,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
    }
}
