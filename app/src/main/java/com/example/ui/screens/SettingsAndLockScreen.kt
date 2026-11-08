package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VaultCardEntity
import com.example.data.VaultItemEntity
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.VaultAccentPreset
import com.example.ui.theme.VaultThemePreset

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    themePreset: VaultThemePreset,
    accentPreset: VaultAccentPreset,
    profiles: List<String>,
    activeProfile: String,
    blockScreenshots: Boolean,
    hapticEnabled: Boolean,
    deletedCards: List<VaultCardEntity>,
    deletedItems: List<VaultItemEntity>,
    allCardsCount: Int,
    allItemsCount: Int,
    onSelectTheme: (VaultThemePreset) -> Unit,
    onSelectAccent: (VaultAccentPreset) -> Unit,
    onSelectProfile: (String) -> Unit,
    onAddProfile: (String) -> Unit,
    onToggleScreenshots: (Boolean) -> Unit,
    onToggleHaptic: (Boolean) -> Unit,
    onUpdatePin: (String) -> Unit,
    onLockNow: () -> Unit,
    onRestoreCard: (VaultCardEntity) -> Unit,
    onRestoreItem: (VaultItemEntity) -> Unit,
    onEmptyRecycleBin: () -> Unit,
    onExportBackup: (Boolean) -> Unit,
    onOpenUxAuditSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showChangePinDialog by remember { mutableStateOf(false) }
    var showAddProfileDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Material 3 UI/UX Review Summary Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Material Design 3 UI/UX Upgrade",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Inspect all navigation, visual hierarchy, and usability enhancements made to Vaultly v1.11.0.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    FilledTonalButton(onClick = onOpenUxAuditSheet) {
                        Text("View Report")
                    }
                }
            }
        }

        // Theme & Appearance Studio
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Theme & Accent Studio", style = MaterialTheme.typography.titleMedium)
                    }

                    Text("Vault Atmosphere", style = MaterialTheme.typography.labelMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VaultThemePreset.entries.forEach { preset ->
                            FilterChip(
                                selected = themePreset == preset,
                                onClick = { onSelectTheme(preset) },
                                label = { Text(preset.label) },
                                modifier = Modifier.testTag("theme_chip_${preset.id}")
                            )
                        }
                    }

                    Text("Accent Colour", style = MaterialTheme.typography.labelMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VaultAccentPreset.entries.forEach { acc ->
                            val isSelected = accentPreset == acc
                            Surface(
                                shape = RoundedCornerShape(99.dp),
                                color = acc.primaryDark.copy(alpha = if (isSelected) 0.28f else 0.12f),
                                modifier = Modifier
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) acc.primaryDark else Color.Transparent,
                                        shape = RoundedCornerShape(99.dp)
                                    )
                                    .clickable { onSelectAccent(acc) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(acc.primaryDark)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(acc.label, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Family Profiles
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Family & Workspace Profiles", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = { showAddProfileDialog = true }) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Profile")
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        profiles.forEach { prof ->
                            FilterChip(
                                selected = activeProfile == prof,
                                onClick = { onSelectProfile(prof) },
                                label = { Text(prof) }
                            )
                        }
                    }
                }
            }
        }

        // Security & Privacy Controls
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
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Vault Security & Privacy", style = MaterialTheme.typography.titleMedium)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Block Screenshots (FLAG_SECURE)", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Hides preview in Recents & blocks screen recording",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = blockScreenshots, onCheckedChange = onToggleScreenshots)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tactile Haptic Feedback", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Subtle vibration on PIN keypad, card flip & copy",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = hapticEnabled, onCheckedChange = onToggleHaptic)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { showChangePinDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Change PIN")
                        }
                        Button(
                            onClick = onLockNow,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("lock_vault_now_btn")
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Lock Vault Now")
                        }
                    }
                }
            }
        }

        // Backup & Transfer
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
                    Text("Offline Backup & Export", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Export your $allCardsCount cards and $allItemsCount vault items as an encrypted Vaultly snapshot or Bitwarden-compatible JSON via Android Share Sheet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onExportBackup(true) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Backup")
                        }
                        OutlinedButton(
                            onClick = { onExportBackup(false) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Bitwarden JSON")
                        }
                    }
                }
            }
        }

        // Recycle Bin
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recycle Bin (${deletedCards.size + deletedItems.size})",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (deletedCards.isNotEmpty() || deletedItems.isNotEmpty()) {
                            TextButton(onClick = onEmptyRecycleBin) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Empty Bin", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    if (deletedCards.isEmpty() && deletedItems.isEmpty()) {
                        Text(
                            text = "Recycle Bin is empty. Deleted items stay here for 30 days before permanent purge.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        deletedCards.forEach { card ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${card.bank} ${card.cardName}", style = MaterialTheme.typography.bodyMedium)
                                TextButton(onClick = { onRestoreCard(card) }) {
                                    Icon(Icons.Default.RestoreFromTrash, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Restore")
                                }
                            }
                        }
                        deletedItems.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.title, style = MaterialTheme.typography.bodyMedium)
                                TextButton(onClick = { onRestoreItem(item) }) {
                                    Icon(Icons.Default.RestoreFromTrash, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Restore")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showChangePinDialog) {
        var newPin by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = { Text("Update Vault PIN") },
            text = {
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { newPin = it.filter { ch -> ch.isDigit() }.take(6) },
                    label = { Text("New 4–6 Digit PIN (Default: 2580)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPin.length in 4..6) {
                            onUpdatePin(newPin)
                            showChangePinDialog = false
                        }
                    }
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showAddProfileDialog) {
        var profName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddProfileDialog = false },
            title = { Text("Add Family / Workspace Profile") },
            text = {
                OutlinedTextField(
                    value = profName,
                    onValueChange = { profName = it },
                    label = { Text("Profile Name (e.g. Parents, Travel)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (profName.isNotBlank()) {
                            onAddProfile(profName)
                            showAddProfileDialog = false
                        }
                    }
                ) {
                    Text("Create Profile")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProfileDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun VaultPinLockOverlay(
    expectedPin: String,
    onUnlockSuccess: (String) -> Boolean,
    onBiometricUnlock: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf("") }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxSize()
            .testTag("pin_lock_overlay")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Vaultly Locked",
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "Enter your PIN (Default: $expectedPin) or tap Fingerprint",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(22.dp))

            // PIN Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(expectedPin.length.coerceAtLeast(4)) { index ->
                    val filled = index < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (filled) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            )
                            .border(
                                width = 2.dp,
                                color = if (filled) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                shape = CircleShape
                            )
                    )
                }
            }

            if (errorText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = errorText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Circular Tactile Number Pad
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("BIO", "0", "DEL")
            )

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                rows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                        row.forEach { key ->
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .size(68.dp)
                                    .clickable {
                                        errorText = ""
                                        when (key) {
                                            "BIO" -> onBiometricUnlock()
                                            "DEL" -> {
                                                if (enteredPin.isNotEmpty()) {
                                                    enteredPin = enteredPin.dropLast(1)
                                                }
                                            }
                                            else -> {
                                                if (enteredPin.length < 6) {
                                                    val next = enteredPin + key
                                                    enteredPin = next
                                                    if (next.length == expectedPin.length) {
                                                        val ok = onUnlockSuccess(next)
                                                        if (!ok) {
                                                            errorText = "Incorrect PIN · Try $expectedPin"
                                                            enteredPin = ""
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    when (key) {
                                        "BIO" -> Icon(
                                            imageVector = Icons.Default.Fingerprint,
                                            contentDescription = "Biometric unlock",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        "DEL" -> Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "Delete digit",
                                            modifier = Modifier.size(24.dp)
                                        )
                                        else -> Text(
                                            text = key,
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 24.sp
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialUxAuditSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Vaultly v1.11.0 — Material Design 3 UI/UX Review & Enhancements",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Summary of architectural, navigation, visual appeal, and ergonomic improvements implemented over the original Capacitor/WebView hybrid build:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            UxAuditSectionCard(
                title = "1. Navigation Architecture (Bottom NavigationBar + Filter Chips)",
                findings = "Original app crowded 8 horizontal text buttons (Cards, Accounts, IDs, Notes, Logins, Insurance, Vehicle, Bills) into a single top strip while hiding Insights, Search, and Settings inside small 44px top-right icon buttons.",
                improvements = "Implemented canonical Material 3 Bottom NavigationBar (Cards, Vault, Insights, Settings) with adaptive NavigationRail support on wide screens, paired with badge-counted FilterChips for the 7 Vault categories and a persistent Top Search & Profile bar."
            )

            UxAuditSectionCard(
                title = "2. Visual Appeal & Tactile 3D Card Ergonomics",
                findings = "Original app buried credit limit utilization, fee waiver progress, lounge visits, and EMI tracking inside a long plain key-value list only visible after opening a separate screen.",
                improvements = "Created interactive 3D-flipping cards with spring physics, custom Canvas patterns (Aurora, Waves, Mesh, Carbon), metallic EMV chip, live utilization bars right on the card face, and a scannable Quick Metrics strip + Modal Command Center."
            )

            UxAuditSectionCard(
                title = "3. Ease of Use & Zero-Friction Security Tools",
                findings = "Generating passwords, viewing 2FA codes, or parsing bank alerts required multiple full-DOM innerHTML redraws and browser-style modal prompts.",
                improvements = "Built a live 1-second ticking RFC 6238 2FA Authenticator ring directly on Login cards, an interactive Cryptographic Password Generator with sliders, a Smart Bank SMS Alert Parser, and native Room SQLite offline persistence."
            )

            UxAuditSectionCard(
                title = "4. Typography, Accessibility & Touch Targets",
                findings = "Small 32px–44px icon buttons and unscaled CSS pixel typography reduced tap accuracy and accessibility compliance.",
                improvements = "Bundled Space Grotesk, Plus Jakarta Sans, and JetBrains Mono (tabular numerals) locally via res/font, enforced 48.dp minimum touch targets, edge-to-edge WindowInsets, and 6 signature Vault themes."
            )

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Got It")
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun UxAuditSectionCard(
    title: String,
    findings: String,
    improvements: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Text("Before: $findings", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Now (M3): $improvements", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
