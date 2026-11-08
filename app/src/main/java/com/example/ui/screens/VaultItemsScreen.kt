package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VaultCategory
import com.example.data.VaultItemEntity
import com.example.ui.components.parseHexColor
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.util.SecurityAndTotpUtils
import kotlinx.coroutines.delay

@Composable
fun VaultItemsScreen(
    items: List<VaultItemEntity>,
    allUnfilteredItems: List<VaultItemEntity>,
    selectedCategory: VaultCategory?,
    activeProfile: String,
    showUnmaskedGlobal: Boolean,
    onSelectCategory: (VaultCategory?) -> Unit,
    onSaveVaultItem: (VaultItemEntity) -> Unit,
    onToggleBillPaid: (VaultItemEntity) -> Unit,
    onDeleteVaultItem: (VaultItemEntity) -> Unit,
    onCopyText: (String, String) -> Unit,
    onShareText: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showPasswordGenDialog by remember { mutableStateOf(false) }
    var revealedItemIds by remember { mutableStateOf(setOf<Long>()) }

    // Live 1-second ticker for RFC 6238 2FA TOTP Authenticator codes
    var currentMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val categoryCounts = remember(allUnfilteredItems, activeProfile) {
        val profileFiltered = allUnfilteredItems.filter {
            activeProfile == "All" || it.profileName.equals(activeProfile, ignoreCase = true)
        }
        VaultCategory.entries.associateWith { cat ->
            profileFiltered.count { it.categoryKey == cat.key }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("vault_items_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Action & Category Filter Strip
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_vault_item_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedCategory != null) "Add ${selectedCategory.singular}"
                            else "Add Vault Item"
                        )
                    }

                    FilledTonalButton(
                        onClick = { showPasswordGenDialog = true },
                        modifier = Modifier.testTag("password_gen_btn")
                    ) {
                        Icon(Icons.Default.Password, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Password Gen")
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { onSelectCategory(null) },
                            label = {
                                val total = categoryCounts.values.sum()
                                Text("All ($total)")
                            },
                            modifier = Modifier.testTag("cat_chip_all")
                        )
                    }
                    items(VaultCategory.entries) { cat ->
                        val count = categoryCounts[cat] ?: 0
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { onSelectCategory(cat) },
                            leadingIcon = {
                                Icon(
                                    imageVector = iconForCategory(cat.key),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text("${cat.label} ($count)") },
                            modifier = Modifier.testTag("cat_chip_${cat.key}")
                        )
                    }
                }
            }
        }

        if (items.isEmpty()) {
            item {
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(38.dp)
                        )
                        Text("No items in this category yet", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Store bank accounts, IDs, 2FA logins, insurance policies, vehicles, and bills offline.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(items, key = { it.id }) { item ->
                val isRevealed = showUnmaskedGlobal || revealedItemIds.contains(item.id)
                VaultItemCard(
                    item = item,
                    isRevealed = isRevealed,
                    currentMillis = currentMillis,
                    onToggleReveal = {
                        revealedItemIds = if (revealedItemIds.contains(item.id)) {
                            revealedItemIds - item.id
                        } else {
                            revealedItemIds + item.id
                        }
                    },
                    onCopyText = onCopyText,
                    onShareText = onShareText,
                    onToggleBillPaid = { onToggleBillPaid(item) },
                    onDelete = { onDeleteVaultItem(item) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddVaultItemDialog(
            initialCategory = selectedCategory ?: VaultCategory.ACCOUNT,
            defaultProfile = if (activeProfile == "All") "Personal" else activeProfile,
            onDismiss = { showAddDialog = false },
            onConfirm = { newItem ->
                onSaveVaultItem(newItem)
                showAddDialog = false
            }
        )
    }

    if (showPasswordGenDialog) {
        PasswordGeneratorModal(
            onDismiss = { showPasswordGenDialog = false },
            onCopyPassword = { pw ->
                onCopyText(pw, "Generated password")
            }
        )
    }
}

@Composable
private fun VaultItemCard(
    item: VaultItemEntity,
    isRevealed: Boolean,
    currentMillis: Long,
    onToggleReveal: () -> Unit,
    onCopyText: (String, String) -> Unit,
    onShareText: (String, String) -> Unit,
    onToggleBillPaid: () -> Unit,
    onDelete: () -> Unit
) {
    val accentColor = parseHexColor(item.colorHex)
    val expiryBadge = remember(item.extraField2) {
        if (item.categoryKey in listOf("id", "ins", "veh") && item.extraField2.isNotBlank()) {
            SecurityAndTotpUtils.computeExpiryBadge(item.extraField2)
        } else null
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vault_item_${item.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Accent Header Bar for Accounts & IDs
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.85f),
                                accentColor.copy(alpha = 0.45f)
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = iconForCategory(item.categoryKey),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    val pillText = expiryBadge?.first ?: item.badgeStatus
                    val isWarning = expiryBadge?.second == true || item.badgeStatus.equals("Unpaid", ignoreCase = true)
                    if (pillText.isNotBlank()) {
                        Surface(
                            color = if (isWarning) Color(0xFFFFE4E6) else Color.White.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(99.dp)
                        ) {
                            Text(
                                text = pillText,
                                color = if (isWarning) Color(0xFF9F1239) else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = item.profileName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Primary Secret Row (Account No / ID No / Password / Note / Policy No / Reg No / Bill Amount)
                val formattedSecret = when {
                    item.categoryKey == VaultCategory.BILL.key -> {
                        val amt = item.primarySecret.toLongOrNull() ?: 0L
                        "Bill Amount: ${SecurityAndTotpUtils.formatCurrencyInr(amt)} (Due: ${item.extraField2.ifBlank { "On receipt" }})"
                    }
                    isRevealed || item.categoryKey in listOf("note", "veh") -> item.primarySecret
                    else -> {
                        val tail = item.primarySecret.takeLast(4)
                        "•••• •••• $tail"
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = labelForPrimarySecret(item.categoryKey),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formattedSecret,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (item.categoryKey !in listOf("note", "veh", "bill")) {
                                IconButton(
                                    onClick = onToggleReveal,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Reveal or hide secret",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    onCopyText(item.primarySecret, labelForPrimarySecret(item.categoryKey))
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy value",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Live 2FA TOTP Authenticator Block for Logins
                if (item.categoryKey == VaultCategory.LOGIN.key && item.secondarySecret.isNotBlank()) {
                    val totpCode = SecurityAndTotpUtils.generateTotpCode(item.secondarySecret, currentMillis)
                    val secsLeft = SecurityAndTotpUtils.getTotpSecondsRemaining(currentMillis)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onCopyText(totpCode.replace(" ", ""), "2FA Authenticator Code")
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "LIVE 2FA AUTHENTICATOR CODE (TAP TO COPY)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = totpCode,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    letterSpacing = 2.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { secsLeft / 30f },
                                    modifier = Modifier.size(38.dp),
                                    strokeWidth = 3.5.dp,
                                    color = if (secsLeft <= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${secsLeft}s",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Secondary Metadata Row (IFSC, UPI, Branch, Policy Renewal, or Bill Action)
                if (item.categoryKey == VaultCategory.ACCOUNT.key && item.secondarySecret.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "IFSC: ${item.secondarySecret}  ·  UPI: ${item.extraField1}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { onCopyText(item.secondarySecret, "IFSC Code") }) {
                            Text("Copy IFSC")
                        }
                    }
                }

                if (item.notes.isNotBlank()) {
                    Text(
                        text = item.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Bottom Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.categoryKey == VaultCategory.BILL.key) {
                        FilledTonalButton(onClick = onToggleBillPaid) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (item.badgeStatus.equals("Paid", ignoreCase = true)) "Mark Unpaid"
                                else "Mark Paid in Full"
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Row {
                        IconButton(
                            onClick = {
                                val text = "${item.title}\n${item.subtitle}\n${labelForPrimarySecret(item.categoryKey)}: ${item.primarySecret}"
                                onShareText(text, item.title)
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share item",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Move to Recycle Bin",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordGeneratorModal(
    onDismiss: () -> Unit,
    onCopyPassword: (String) -> Unit
) {
    var lengthSlider by remember { mutableFloatStateOf(18f) }
    var includeSymbols by remember { mutableStateOf(true) }
    var easyToRead by remember { mutableStateOf(false) }
    var generated by remember {
        mutableStateOf(SecurityAndTotpUtils.generatePassword(18, true, false))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cryptographic Password Generator") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = generated,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                generated = SecurityAndTotpUtils.generatePassword(
                                    lengthSlider.toInt(),
                                    includeSymbols,
                                    easyToRead
                                )
                            }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Regenerate password")
                        }
                    }
                }

                Text("Length: ${lengthSlider.toInt()} characters", style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = lengthSlider,
                    onValueChange = {
                        lengthSlider = it
                        generated = SecurityAndTotpUtils.generatePassword(
                            it.toInt(),
                            includeSymbols,
                            easyToRead
                        )
                    },
                    valueRange = 8f..32f
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Include Symbols (!@#\$%)", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = includeSymbols,
                        onCheckedChange = {
                            includeSymbols = it
                            generated = SecurityAndTotpUtils.generatePassword(
                                lengthSlider.toInt(),
                                it,
                                easyToRead
                            )
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Easy to Read (No 0/O, 1/l)", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = easyToRead,
                        onCheckedChange = {
                            easyToRead = it
                            generated = SecurityAndTotpUtils.generatePassword(
                                lengthSlider.toInt(),
                                includeSymbols,
                                it
                            )
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCopyPassword(generated)
                    onDismiss()
                }
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Password")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun AddVaultItemDialog(
    initialCategory: VaultCategory,
    defaultProfile: String,
    onDismiss: () -> Unit,
    onConfirm: (VaultItemEntity) -> Unit
) {
    var category by remember { mutableStateOf(initialCategory) }
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var primarySecret by remember { mutableStateOf("") }
    var secondarySecret by remember { mutableStateOf("") }
    var extraField1 by remember { mutableStateOf("") }
    var extraField2 by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add ${category.singular}") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(VaultCategory.entries) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.label) }
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = {
                        Text(
                            when (category) {
                                VaultCategory.ACCOUNT -> "Bank Name (e.g. HDFC Bank)"
                                VaultCategory.ID_DOC -> "ID Type (e.g. Aadhaar, Passport, PAN)"
                                VaultCategory.LOGIN -> "Website or App (e.g. GitHub)"
                                VaultCategory.NOTE -> "Note Title"
                                VaultCategory.INSURANCE -> "Insurer & Plan Name"
                                VaultCategory.VEHICLE -> "Vehicle Make & Model"
                                VaultCategory.BILL -> "Vendor / Biller Name"
                            }
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = {
                        Text(
                            when (category) {
                                VaultCategory.ACCOUNT -> "Account Holder & Type (e.g. Savings)"
                                VaultCategory.ID_DOC -> "Document Holder Name"
                                VaultCategory.LOGIN -> "Username or Email"
                                VaultCategory.NOTE -> "Subtitle / Summary"
                                VaultCategory.INSURANCE -> "Policy Type (Health, Motor, Life)"
                                VaultCategory.VEHICLE -> "Vehicle Type (Car, Bike)"
                                VaultCategory.BILL -> "Invoice / Bill Reference"
                            }
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = primarySecret,
                    onValueChange = { primarySecret = it },
                    label = { Text(labelForPrimarySecret(category.key)) },
                    modifier = Modifier.fillMaxWidth()
                )

                if (category == VaultCategory.LOGIN) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                primarySecret = SecurityAndTotpUtils.generatePassword(18, true, false)
                            }
                        ) {
                            Text("Generate Strong Password")
                        }
                    }
                }

                OutlinedTextField(
                    value = secondarySecret,
                    onValueChange = { secondarySecret = it },
                    label = {
                        Text(
                            when (category) {
                                VaultCategory.ACCOUNT -> "IFSC Code"
                                VaultCategory.LOGIN -> "2FA Authenticator Key (Base32 / otpauth)"
                                VaultCategory.INSURANCE -> "Annual Premium (₹)"
                                VaultCategory.VEHICLE -> "Chassis / VIN Number"
                                else -> "Secondary Reference (optional)"
                            }
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = extraField1,
                        onValueChange = { extraField1 = it },
                        label = {
                            Text(
                                when (category) {
                                    VaultCategory.ACCOUNT -> "UPI ID"
                                    VaultCategory.LOGIN -> "Sign-in URL"
                                    else -> "Issued / Start"
                                }
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = extraField2,
                        onValueChange = { extraField2 = it },
                        label = { Text("Expiry / Due (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Encrypted Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val item = VaultItemEntity(
                        categoryKey = category.key,
                        title = title.ifBlank { category.singular },
                        subtitle = subtitle.ifBlank { defaultProfile },
                        primarySecret = primarySecret.ifBlank { "0000" },
                        secondarySecret = secondarySecret,
                        extraField1 = extraField1,
                        extraField2 = extraField2,
                        badgeStatus = when (category) {
                            VaultCategory.BILL -> "Unpaid"
                            VaultCategory.LOGIN -> if (secondarySecret.isNotBlank()) "2FA Active" else "Login"
                            else -> category.label
                        },
                        colorHex = defaultColorForCategory(category.key),
                        profileName = defaultProfile,
                        notes = notes
                    )
                    onConfirm(item)
                }
            ) {
                Text("Save to Vault")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

fun iconForCategory(key: String): ImageVector {
    return when (key) {
        VaultCategory.ACCOUNT.key -> Icons.Default.AccountBalance
        VaultCategory.ID_DOC.key -> Icons.Default.Badge
        VaultCategory.LOGIN.key -> Icons.Default.Key
        VaultCategory.NOTE.key -> Icons.Default.NoteAlt
        VaultCategory.INSURANCE.key -> Icons.Default.HealthAndSafety
        VaultCategory.VEHICLE.key -> Icons.Default.DirectionsCar
        VaultCategory.BILL.key -> Icons.Default.Receipt
        else -> Icons.Default.Lock
    }
}

private fun labelForPrimarySecret(key: String): String {
    return when (key) {
        VaultCategory.ACCOUNT.key -> "Account Number"
        VaultCategory.ID_DOC.key -> "ID / Document Number"
        VaultCategory.LOGIN.key -> "Password"
        VaultCategory.NOTE.key -> "Secure Note Body"
        VaultCategory.INSURANCE.key -> "Policy Number"
        VaultCategory.VEHICLE.key -> "Registration Number"
        VaultCategory.BILL.key -> "Bill Amount (₹)"
        else -> "Secret Value"
    }
}

private fun defaultColorForCategory(key: String): String {
    return when (key) {
        VaultCategory.ACCOUNT.key -> "#1D4ED8"
        VaultCategory.ID_DOC.key -> "#047857"
        VaultCategory.LOGIN.key -> "#4F46E5"
        VaultCategory.NOTE.key -> "#7E22CE"
        VaultCategory.INSURANCE.key -> "#0F766E"
        VaultCategory.VEHICLE.key -> "#B45309"
        VaultCategory.BILL.key -> "#BE123C"
        else -> "#4F46E5"
    }
}
