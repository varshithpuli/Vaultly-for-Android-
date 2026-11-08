package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.VaultCardEntity
import com.example.ui.CardGrouping
import com.example.ui.CardSortOrder
import com.example.ui.components.InteractiveVaultCard
import com.example.ui.components.parseHexColor
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.VaultCardPalette
import com.example.util.SecurityAndTotpUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsScreen(
    cards: List<VaultCardEntity>,
    grouping: CardGrouping,
    sortOrder: CardSortOrder,
    showUnmasked: Boolean,
    activeProfile: String,
    onSetGrouping: (CardGrouping) -> Unit,
    onSetSortOrder: (CardSortOrder) -> Unit,
    onToggleUnmasked: () -> Unit,
    onCopyText: (String, String) -> Unit,
    onShareText: (String, String) -> Unit,
    onSaveCard: (VaultCardEntity) -> Unit,
    onToggleFavourite: (VaultCardEntity) -> Unit,
    onToggleBlocked: (VaultCardEntity) -> Unit,
    onAdjustLounge: (VaultCardEntity, Boolean, Int) -> Unit,
    onPayEmi: (VaultCardEntity) -> Unit,
    onRecordSpend: (VaultCardEntity, String, String, Long, String) -> Unit,
    onPayStatement: (VaultCardEntity, Long) -> Unit,
    onDeleteCard: (VaultCardEntity) -> Unit,
    onOpenUxAuditSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCardForDetails by remember { mutableStateOf<VaultCardEntity?>(null) }
    var cardToEdit by remember { mutableStateOf<VaultCardEntity?>(null) }
    var showAddCardDialog by remember { mutableStateOf(false) }
    var showSmartAlertDialog by remember { mutableStateOf(false) }

    val totalLimit = remember(cards) { cards.sumOf { it.creditLimit } }
    val totalOwed = remember(cards) { cards.sumOf { it.currentOwed } }
    val overallUtilization = if (totalLimit > 0L) {
        (totalOwed.toFloat() / totalLimit.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val groupedCards = remember(cards, grouping) {
        when (grouping) {
            CardGrouping.ALL -> mapOf("All Active Cards (${cards.size})" to cards)
            CardGrouping.BANK -> cards.groupBy { it.bank }
            CardGrouping.NETWORK -> cards.groupBy { "${it.network} Network" }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("cards_list_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Portfolio & Security Banner
        item {
            PortfolioHeroCard(
                cardCount = cards.size,
                totalLimit = totalLimit,
                totalOwed = totalOwed,
                utilization = overallUtilization,
                showUnmasked = showUnmasked,
                onToggleUnmasked = onToggleUnmasked,
                onOpenSmartAlert = { showSmartAlertDialog = true },
                onAddCard = {
                    cardToEdit = null
                    showAddCardDialog = true
                },
                onOpenUxAuditSheet = onOpenUxAuditSheet
            )
        }

        // Grouping & Sort Controls Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(CardGrouping.entries) { grp ->
                        FilterChip(
                            selected = grouping == grp,
                            onClick = { onSetGrouping(grp) },
                            label = { Text(grp.label) },
                            modifier = Modifier.testTag("group_chip_${grp.name.lowercase()}")
                        )
                    }
                    items(CardSortOrder.entries) { srt ->
                        FilterChip(
                            selected = sortOrder == srt,
                            onClick = { onSetSortOrder(srt) },
                            label = { Text(srt.label) }
                        )
                    }
                }
                Text(
                    text = "Tap any card to flip 3D (CVV, PIN & Statement dates on back) · Tap tune icon for Bills, Lounge & EMI tools.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (cards.isEmpty()) {
            item {
                EmptyVaultCardsState(
                    onAddCard = {
                        cardToEdit = null
                        showAddCardDialog = true
                    }
                )
            }
        } else {
            groupedCards.forEach { (groupTitle, groupItems) ->
                if (grouping != CardGrouping.ALL) {
                    item(key = "header_$groupTitle") {
                        Text(
                            text = "$groupTitle (${groupItems.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                items(groupItems, key = { it.id }) { card ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InteractiveVaultCard(
                            card = card,
                            showUnmasked = showUnmasked,
                            onCopyText = onCopyText,
                            onToggleFavourite = { onToggleFavourite(card) },
                            onOpenDetails = { selectedCardForDetails = card }
                        )

                        // Quick Companion Strip below each card for scannable metrics
                        CardQuickMetricsStrip(
                            card = card,
                            onOpenDetails = { selectedCardForDetails = card },
                            onQuickSpend = { selectedCardForDetails = card }
                        )
                    }
                }
            }
        }
    }

    // Card Command Center Bottom Sheet
    selectedCardForDetails?.let { currentCard ->
        val liveCard = cards.find { it.id == currentCard.id } ?: currentCard
        CardCommandCenterSheet(
            card = liveCard,
            onDismiss = { selectedCardForDetails = null },
            onCopyText = onCopyText,
            onShareText = onShareText,
            onEditCard = {
                selectedCardForDetails = null
                cardToEdit = liveCard
                showAddCardDialog = true
            },
            onToggleBlocked = { onToggleBlocked(liveCard) },
            onAdjustLounge = { isDomestic, delta -> onAdjustLounge(liveCard, isDomestic, delta) },
            onPayEmi = { onPayEmi(liveCard) },
            onRecordSpend = { merchant, cat, amt, note ->
                onRecordSpend(liveCard, merchant, cat, amt, note)
            },
            onPayStatement = { amt -> onPayStatement(liveCard, amt) },
            onDeleteCard = {
                selectedCardForDetails = null
                onDeleteCard(liveCard)
            }
        )
    }

    // Add / Edit Card Dialog
    if (showAddCardDialog) {
        AddOrEditCardDialog(
            initialCard = cardToEdit,
            defaultProfile = if (activeProfile == "All") "Personal" else activeProfile,
            onDismiss = {
                showAddCardDialog = false
                cardToEdit = null
            },
            onConfirm = { updated ->
                onSaveCard(updated)
                showAddCardDialog = false
                cardToEdit = null
            }
        )
    }

    // Smart SMS / Alert Parser Dialog
    if (showSmartAlertDialog) {
        SmartAlertParserDialog(
            cards = cards,
            onDismiss = { showSmartAlertDialog = false },
            onRecordParsedSpend = { card, merchant, cat, amount ->
                onRecordSpend(card, merchant, cat, amount, "Imported via Smart SMS Alert Parser")
                showSmartAlertDialog = false
            },
            onCreateCardStub = { bank, last4 ->
                val stub = VaultCardEntity(
                    bank = bank,
                    cardName = "$bank Dispatched Card",
                    network = "Visa",
                    variant = "Visa Platinum",
                    cardKind = "Credit",
                    holderName = "CARDHOLDER",
                    cardNumber = "437500000000$last4",
                    expiry = "12/29",
                    cvv = "000",
                    creditLimit = 250000L,
                    colorHex = "#4F46E5",
                    notes = "Auto-created from card dispatch SMS alert"
                )
                onSaveCard(stub)
                showSmartAlertDialog = false
            }
        )
    }
}

@Composable
private fun PortfolioHeroCard(
    cardCount: Int,
    totalLimit: Long,
    totalOwed: Long,
    utilization: Float,
    showUnmasked: Boolean,
    onToggleUnmasked: () -> Unit,
    onOpenSmartAlert: () -> Unit,
    onAddCard: () -> Unit,
    onOpenUxAuditSheet: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.img_vault_hero_1794154735346),
                contentDescription = "Vaultly encrypted security illustration",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.25f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(99.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "100% OFFLINE ENCRYPTED VAULT",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    TextButton(
                        onClick = onOpenUxAuditSheet,
                        modifier = Modifier.testTag("ux_audit_btn")
                    ) {
                        Text(
                            text = "M3 UX Review",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(34.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Total Available Credit ($cardCount Cards)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = SecurityAndTotpUtils.formatCurrencyInr(
                                (totalLimit - totalOwed).coerceAtLeast(0L)
                            ),
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Used: ${(utilization * 100).toInt()}%",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (utilization > 0.4f) Color(0xFFFBBF24) else Color(0xFF34D399)
                        )
                        Text(
                            text = "${SecurityAndTotpUtils.formatCurrencyInr(totalOwed)} / ${
                                SecurityAndTotpUtils.formatCurrencyInr(totalLimit)
                            }",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { utilization },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(99.dp)),
                    color = if (utilization > 0.5f) Color(0xFFFBBF24) else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAddCard,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hero_add_card_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Card")
                    }

                    FilledTonalButton(
                        onClick = onOpenSmartAlert,
                        modifier = Modifier.testTag("smart_alert_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Parse SMS")
                    }

                    IconButton(
                        onClick = onToggleUnmasked,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("toggle_mask_btn")
                    ) {
                        Icon(
                            imageVector = if (showUnmasked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle card numbers visibility",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardQuickMetricsStrip(
    card: VaultCardEntity,
    onOpenDetails: () -> Unit,
    onQuickSpend: () -> Unit
) {
    val utilPct = if (card.creditLimit > 0L) {
        ((card.currentOwed * 100L) / card.creditLimit).toInt()
    } else 0
    val domLeft = (card.domesticLoungeTotal - card.domesticLoungeUsed).coerceAtLeast(0)
    val intlLeft = (card.intlLoungeTotal - card.intlLoungeUsed).coerceAtLeast(0)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDetails() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Balance Owed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${SecurityAndTotpUtils.formatCurrencyInr(card.currentOwed)} ($utilPct%)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (card.domesticLoungeTotal > 0 || card.intlLoungeTotal > 0) {
                    Column {
                        Text(
                            text = "Lounge Left",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${domLeft}D · ${intlLeft}I",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                if (card.emiMonthsRemaining > 0) {
                    Column {
                        Text(
                            text = "Active EMI",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${card.emiMonthsRemaining}m left",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            FilledTonalButton(
                onClick = onQuickSpend,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Tools & Bills", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardCommandCenterSheet(
    card: VaultCardEntity,
    onDismiss: () -> Unit,
    onCopyText: (String, String) -> Unit,
    onShareText: (String, String) -> Unit,
    onEditCard: () -> Unit,
    onToggleBlocked: () -> Unit,
    onAdjustLounge: (Boolean, Int) -> Unit,
    onPayEmi: () -> Unit,
    onRecordSpend: (String, String, Long, String) -> Unit,
    onPayStatement: (Long) -> Unit,
    onDeleteCard: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSpendDialog by remember { mutableStateOf(false) }
    var showPayBillDialog by remember { mutableStateOf(false) }
    var showLostChecklist by remember { mutableStateOf(false) }

    val utilPct = if (card.creditLimit > 0L) {
        (card.currentOwed.toFloat() / card.creditLimit.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val feeWaiverPct = if (card.feeWaiverTarget > 0L) {
        (card.annualSpendSoFar.toFloat() / card.feeWaiverTarget.toFloat()).coerceIn(0f, 1f)
    } else 0f

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${card.bank} · ${card.variant}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = card.cardName,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                Row {
                    IconButton(onClick = onEditCard) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit card")
                    }
                    IconButton(
                        onClick = {
                            val shareBody = buildString {
                                appendLine("${card.bank} ${card.cardName} (${card.network})")
                                appendLine("Cardholder: ${card.holderName}")
                                appendLine("Card No: ${SecurityAndTotpUtils.formatCardNumber(card.cardNumber, masked = false)}")
                                appendLine("Expiry: ${card.expiry}")
                            }
                            onShareText(shareBody, "${card.bank} Card Details")
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share card")
                    }
                }
            }

            // Quick Copy Credentials Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CredentialCopyRow(
                        label = "Card Number",
                        value = SecurityAndTotpUtils.formatCardNumber(card.cardNumber, masked = false),
                        onCopy = { onCopyText(card.cardNumber, "Card number") }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CredentialCopyRow(
                            label = "Expiry",
                            value = card.expiry,
                            onCopy = { onCopyText(card.expiry, "Expiry date") },
                            modifier = Modifier.weight(1f)
                        )
                        CredentialCopyRow(
                            label = "CVV",
                            value = card.cvv,
                            onCopy = { onCopyText(card.cvv, "CVV") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Primary Money Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showSpendDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Record Spend")
                }
                FilledTonalButton(
                    onClick = { showPayBillDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pay Bill")
                }
            }

            // Credit Limit & Fee Waiver Trackers
            if (card.creditLimit > 0L) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Credit Limit Utilization", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${(utilPct * 100).toInt()}% used",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (utilPct > 0.5f) Color(0xFFFBBF24) else Color(0xFF34D399)
                            )
                        }
                        LinearProgressIndicator(
                            progress = { utilPct },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(99.dp))
                        )
                        Text(
                            text = "Owed: ${SecurityAndTotpUtils.formatCurrencyInr(card.currentOwed)} · Limit: ${
                                SecurityAndTotpUtils.formatCurrencyInr(card.creditLimit)
                            } · Statement Day ${card.statementDay} (Due Day ${card.dueDay})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (card.feeWaiverTarget > 0L) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Annual Fee Waiver Tracker", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = if (feeWaiverPct >= 1f) "Fee Waived ✓" else "${(feeWaiverPct * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            LinearProgressIndicator(
                                progress = { feeWaiverPct },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(99.dp)),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Spent ${SecurityAndTotpUtils.formatCurrencyInr(card.annualSpendSoFar)} of ${
                                    SecurityAndTotpUtils.formatCurrencyInr(card.feeWaiverTarget)
                                } target (Annual Fee: ${SecurityAndTotpUtils.formatCurrencyInr(card.annualFee)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Airport Lounge Access Tracker
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FlightTakeoff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Airport Lounge Access Tracker", style = MaterialTheme.typography.titleSmall)
                    }

                    LoungeCounterRow(
                        label = "Domestic Lounge Visits",
                        used = card.domesticLoungeUsed,
                        total = card.domesticLoungeTotal,
                        onDecrement = { onAdjustLounge(true, -1) },
                        onIncrement = { onAdjustLounge(true, 1) }
                    )

                    LoungeCounterRow(
                        label = "International Lounge Visits",
                        used = card.intlLoungeUsed,
                        total = card.intlLoungeTotal,
                        onDecrement = { onAdjustLounge(false, -1) },
                        onIncrement = { onAdjustLounge(false, 1) }
                    )
                }
            }

            // EMI Tracker
            if (card.emiMonthsRemaining > 0 && card.emiTitle.isNotBlank()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Active EMI: ${card.emiTitle}", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "${SecurityAndTotpUtils.formatCurrencyInr(card.emiMonthlyAmount)}/mo · ${card.emiMonthsRemaining} months left",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        FilledTonalButton(onClick = onPayEmi) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark Month Paid")
                        }
                    }
                }
            }

            // Lost or Stolen Helper & Delete Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showLostChecklist = !showLostChecklist },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (card.isBlocked) "Blocked · Checklist" else "Lost / Block Card")
                }

                OutlinedButton(
                    onClick = onDeleteCard,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Move card to Recycle Bin")
                }
            }

            if (showLostChecklist) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Lost or Stolen Emergency Checklist",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "1. Freeze international & contactless transactions in ${card.bank} mobile app.\n" +
                                "2. Call ${card.bank} 24x7 hotlisting helpline regarding card ending ${card.cardNumber.takeLast(4)}.\n" +
                                "3. Mark card as Blocked below to pause all local bill reminders.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Button(
                            onClick = onToggleBlocked,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = Color.White
                            )
                        ) {
                            Text(if (card.isBlocked) "Unblock Card & Resume Reminders" else "Mark Card as Blocked")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showSpendDialog) {
        RecordSpendModal(
            cardName = card.cardName,
            onDismiss = { showSpendDialog = false },
            onConfirm = { merchant, cat, amt, note ->
                onRecordSpend(merchant, cat, amt, note)
                showSpendDialog = false
            }
        )
    }

    if (showPayBillDialog) {
        PayStatementModal(
            cardName = card.cardName,
            currentOwed = card.currentOwed,
            onDismiss = { showPayBillDialog = false },
            onConfirm = { amt ->
                onPayStatement(amt)
                showPayBillDialog = false
            }
        )
    }
}

@Composable
private fun CredentialCopyRow(
    label: String,
    value: String,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
        IconButton(onClick = onCopy, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy $label",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun LoungeCounterRow(
    label: String,
    used: Int,
    total: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    val left = (total - used).coerceAtLeast(0)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "$used used of $total quota ($left remaining)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrement, enabled = used > 0, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease lounge count")
            }
            Text(
                text = "$used/$total",
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onIncrement, enabled = used < total, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Log lounge visit")
            }
        }
    }
}

@Composable
private fun RecordSpendModal(
    cardName: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long, String) -> Unit
) {
    val categories = listOf(
        "Shopping", "Food & Dining", "Travel", "Groceries",
        "Fuel", "Bills & Utilities", "Health", "Entertainment"
    )
    var merchant by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Spend · $cardName") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    label = { Text("Merchant / Store") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Category", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toLongOrNull() ?: 0L
                    if (amt > 0L) {
                        onConfirm(merchant, selectedCategory, amt, note)
                    }
                }
            ) {
                Text("Save Spend")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun PayStatementModal(
    cardName: String,
    currentOwed: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var paymentText by remember { mutableStateOf(currentOwed.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pay Statement · $cardName") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Current balance owed: ${SecurityAndTotpUtils.formatCurrencyInr(currentOwed)}. Paying in full or part immediately updates your utilization meter.",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = paymentText,
                    onValueChange = { paymentText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Payment Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = paymentText.toLongOrNull() ?: 0L
                    if (amt > 0L) onConfirm(amt)
                }
            ) {
                Text("Record Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun SmartAlertParserDialog(
    cards: List<VaultCardEntity>,
    onDismiss: () -> Unit,
    onRecordParsedSpend: (VaultCardEntity, String, String, Long) -> Unit,
    onCreateCardStub: (String, String) -> Unit
) {
    var smsText by remember {
        mutableStateOf("Alert: INR 4,650 spent on HDFC Bank Card ending 4829 at Swiggy Instamart on 2026-11-08.")
    }
    val parsed = remember(smsText) { SecurityAndTotpUtils.parseBankAlertText(smsText) }
    val matchedCard = remember(parsed, cards) {
        cards.find { it.cardNumber.endsWith(parsed.last4) }
            ?: cards.find { it.bank.contains(parsed.bank, ignoreCase = true) }
            ?: cards.firstOrNull()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Smart Bank SMS / Alert Parser") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Paste a bank transaction alert or 'card dispatched' SMS. Vaultly extracts the card last-4 digits, merchant, category, or creates a new card stub offline.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = smsText,
                    onValueChange = { smsText = it },
                    label = { Text("Bank SMS / Notification Text") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (parsed.isNewCardStub) "Detected: New Card Dispatch Alert"
                            else "Detected: Spend Alert (${parsed.category})",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Bank: ${parsed.bank} · Ending: •••• ${parsed.last4} · Amount: ₹${parsed.amount}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (!parsed.isNewCardStub) {
                            Text(
                                text = "Merchant: ${parsed.merchant}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (parsed.isNewCardStub) {
                        onCreateCardStub(parsed.bank, parsed.last4)
                    } else if (matchedCard != null) {
                        onRecordParsedSpend(
                            matchedCard,
                            parsed.merchant,
                            parsed.category,
                            parsed.amount
                        )
                    }
                }
            ) {
                Text(if (parsed.isNewCardStub) "Create Card Stub" else "Save Spend")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddOrEditCardDialog(
    initialCard: VaultCardEntity?,
    defaultProfile: String,
    onDismiss: () -> Unit,
    onConfirm: (VaultCardEntity) -> Unit
) {
    var bank by remember { mutableStateOf(initialCard?.bank ?: "HDFC Bank") }
    var cardName by remember { mutableStateOf(initialCard?.cardName ?: "") }
    var holderName by remember { mutableStateOf(initialCard?.holderName ?: "AARAV SHARMA") }
    var cardNumber by remember { mutableStateOf(initialCard?.cardNumber ?: "") }
    var expiry by remember { mutableStateOf(initialCard?.expiry ?: "08/29") }
    var cvv by remember { mutableStateOf(initialCard?.cvv ?: "") }
    var cardPin by remember { mutableStateOf(initialCard?.cardPin ?: "") }
    var cardKind by remember { mutableStateOf(initialCard?.cardKind ?: "Credit") }
    var creditLimit by remember { mutableStateOf((initialCard?.creditLimit ?: 500000L).toString()) }
    var currentOwed by remember { mutableStateOf((initialCard?.currentOwed ?: 0L).toString()) }
    var colorHex by remember { mutableStateOf(initialCard?.colorHex ?: VaultCardPalette.first().first) }
    var pattern by remember { mutableStateOf(initialCard?.pattern ?: "Aurora") }

    val detectedNetwork = remember(cardNumber) {
        if (cardNumber.isBlank()) (initialCard?.network ?: "Visa")
        else SecurityAndTotpUtils.detectNetwork(cardNumber)
    }
    val luhnValid = remember(cardNumber) {
        cardNumber.length >= 13 && SecurityAndTotpUtils.isValidLuhn(cardNumber)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialCard == null) "Add New Card" else "Edit Card") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = bank,
                    onValueChange = { bank = it },
                    label = { Text("Bank Name (e.g. HDFC Bank, Axis, Amex)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = cardName,
                    onValueChange = { cardName = it },
                    label = { Text("Card Name / Variant (e.g. Infinia, Atlas)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { cardNumber = it.filter { ch -> ch.isDigit() }.take(19) },
                    label = { Text("Card Number ($detectedNetwork)") },
                    supportingText = {
                        if (cardNumber.length >= 13) {
                            Text(
                                text = if (luhnValid) "Luhn checksum valid ✓ ($detectedNetwork)"
                                else "Custom / test number ($detectedNetwork)",
                                color = if (luhnValid) Color(0xFF34D399) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = expiry,
                        onValueChange = { expiry = it.take(5) },
                        label = { Text("Expiry (MM/YY)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = cvv,
                        onValueChange = { cvv = it.filter { ch -> ch.isDigit() }.take(4) },
                        label = { Text("CVV") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = holderName,
                    onValueChange = { holderName = it },
                    label = { Text("Cardholder Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = creditLimit,
                        onValueChange = { creditLimit = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Limit (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = currentOwed,
                        onValueChange = { currentOwed = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Balance Owed (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Card Finish & Colour", style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VaultCardPalette.forEach { (hex, _) ->
                        val isSelected = colorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(hex))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                                .clickable { colorHex = hex }
                        )
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(listOf("Aurora", "Waves", "Mesh", "Carbon")) { pat ->
                        FilterChip(
                            selected = pattern == pat,
                            onClick = { pattern = pat },
                            label = { Text(pat) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalNumber = cardNumber.ifBlank { "4375889012349921" }
                    val entity = (initialCard ?: VaultCardEntity(
                        bank = bank.ifBlank { "HDFC Bank" },
                        cardName = cardName.ifBlank { "Privilege Card" },
                        network = detectedNetwork,
                        variant = "$detectedNetwork Signature",
                        cardKind = cardKind,
                        holderName = holderName.ifBlank { "CARDHOLDER" },
                        cardNumber = finalNumber,
                        expiry = expiry.ifBlank { "12/29" },
                        cvv = cvv.ifBlank { "999" },
                        profileName = defaultProfile
                    )).copy(
                        bank = bank.ifBlank { "HDFC Bank" },
                        cardName = cardName.ifBlank { "Privilege Card" },
                        network = detectedNetwork,
                        holderName = holderName.ifBlank { "CARDHOLDER" },
                        cardNumber = finalNumber,
                        expiry = expiry.ifBlank { "12/29" },
                        cvv = cvv.ifBlank { "999" },
                        cardPin = cardPin,
                        creditLimit = creditLimit.toLongOrNull() ?: 500000L,
                        currentOwed = currentOwed.toLongOrNull() ?: 0L,
                        colorHex = colorHex,
                        pattern = pattern
                    )
                    onConfirm(entity)
                }
            ) {
                Text("Save Card")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun EmptyVaultCardsState(onAddCard: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CreditCard,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(42.dp)
            )
            Text("No cards match your filter", style = MaterialTheme.typography.titleMedium)
            Text(
                "Add a credit or debit card to track utilization, lounge access, EMIs, and bills offline.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onAddCard) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add First Card")
            }
        }
    }
}
