package com.example

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainNavTab
import com.example.ui.VaultViewModel
import com.example.ui.screens.CardsScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.MaterialUxAuditSheet
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VaultItemsScreen
import com.example.ui.screens.VaultPinLockOverlay
import com.example.ui.theme.VaultlyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: VaultViewModel = viewModel()
            val themePreset by viewModel.themePreset.collectAsStateWithLifecycle()
            val accentPreset by viewModel.accentPreset.collectAsStateWithLifecycle()

            // Note: WindowManager.LayoutParams.FLAG_SECURE is intentionally not applied to the
            // host window so that the browser-based Streaming Android Emulator can render frames.
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)

            VaultlyTheme(
                themePreset = themePreset,
                accentPreset = accentPreset
            ) {
                VaultlyAppRoot(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultlyAppRoot(viewModel: VaultViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedVaultCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    val grouping by viewModel.cardGrouping.collectAsStateWithLifecycle()
    val sortOrder by viewModel.cardSortOrder.collectAsStateWithLifecycle()
    val themePreset by viewModel.themePreset.collectAsStateWithLifecycle()
    val accentPreset by viewModel.accentPreset.collectAsStateWithLifecycle()
    val isVaultLocked by viewModel.isVaultLocked.collectAsStateWithLifecycle()
    val vaultPin by viewModel.vaultPin.collectAsStateWithLifecycle()
    val blockScreenshots by viewModel.blockScreenshots.collectAsStateWithLifecycle()
    val hapticEnabled by viewModel.hapticFeedbackEnabled.collectAsStateWithLifecycle()
    val showUnmasked by viewModel.showNumbersUnmasked.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val activeCards by viewModel.activeCards.collectAsStateWithLifecycle()
    val activeVaultItems by viewModel.activeVaultItems.collectAsStateWithLifecycle()
    val allVaultItemsUnfiltered by viewModel.allActiveVaultItemsUnfiltered.collectAsStateWithLifecycle()
    val deletedCards by viewModel.deletedCards.collectAsStateWithLifecycle()
    val deletedVaultItems by viewModel.deletedVaultItems.collectAsStateWithLifecycle()
    val allSpends by viewModel.allSpends.collectAsStateWithLifecycle()

    var showSearchBar by remember { mutableStateOf(false) }
    var showProfileMenu by remember { mutableStateOf(false) }
    var showUxAuditSheet by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    // Back navigation handler for secondary tabs & search
    BackHandler(enabled = showSearchBar || selectedTab != MainNavTab.CARDS) {
        if (showSearchBar) {
            showSearchBar = false
            viewModel.updateSearchQuery("")
        } else {
            viewModel.selectTab(MainNavTab.CARDS)
        }
    }

    val copyWithFeedback: (String, String) -> Unit = { value, label ->
        clipboardManager.setText(AnnotatedString(value))
        if (hapticEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        viewModel.showMessage("$label copied to clipboard")
    }

    val shareTextIntent: (String, String) -> Unit = { text, subject ->
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share via Vaultly"))
    }

    if (isVaultLocked) {
        VaultPinLockOverlay(
            expectedPin = vaultPin,
            onUnlockSuccess = { attempt ->
                if (hapticEnabled) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                viewModel.unlockVaultWithPin(attempt)
            },
            onBiometricUnlock = {
                if (hapticEnabled) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                viewModel.unlockVaultBiometric()
            }
        )
        return
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        ),
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "Vaultly",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = when (selectedTab) {
                                            MainNavTab.CARDS -> "${activeCards.size} Cards · Offline Encrypted"
                                            MainNavTab.VAULT -> "${activeVaultItems.size} Vault Records"
                                            MainNavTab.INSIGHTS -> "Spends, Bills & Reminders"
                                            MainNavTab.SETTINGS -> "Themes, Security & Backup"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        actions = {
                            // Profile Switcher Pill
                            Box {
                                Surface(
                                    shape = RoundedCornerShape(99.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(99.dp))
                                        .clickable { showProfileMenu = true }
                                        .testTag("profile_switcher_pill")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Active profile",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = activeProfile,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                                DropdownMenu(
                                    expanded = showProfileMenu,
                                    onDismissRequest = { showProfileMenu = false }
                                ) {
                                    profiles.forEach { prof ->
                                        DropdownMenuItem(
                                            text = { Text(prof) },
                                            onClick = {
                                                viewModel.selectProfile(prof)
                                                showProfileMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = {
                                    showSearchBar = !showSearchBar
                                    if (!showSearchBar) viewModel.updateSearchQuery("")
                                },
                                modifier = Modifier.testTag("top_search_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search across Vaultly"
                                )
                            }

                            IconButton(
                                onClick = { viewModel.lockVault() },
                                modifier = Modifier.testTag("top_lock_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Lock Vaultly"
                                )
                            }
                        }
                    )

                    AnimatedVisibility(visible = showSearchBar) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("Search cards, banks, last-4 digits, IDs, 2FA, bills…") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .testTag("global_search_input")
                        )
                    }
                }
            },
            bottomBar = {
                if (!isExpandedScreen) {
                    NavigationBar(modifier = Modifier.testTag("bottom_nav_bar")) {
                        MainNavTab.entries.forEach { tab ->
                            val selected = selectedTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = navIconForTab(tab, selected),
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag("nav_tab_${tab.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isExpandedScreen) {
                    NavigationRail(modifier = Modifier.fillMaxHeight()) {
                        MainNavTab.entries.forEach { tab ->
                            val selected = selectedTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = navIconForTab(tab, selected),
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) }
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        MainNavTab.CARDS -> CardsScreen(
                            cards = activeCards,
                            grouping = grouping,
                            sortOrder = sortOrder,
                            showUnmasked = showUnmasked,
                            activeProfile = activeProfile,
                            onSetGrouping = viewModel::setCardGrouping,
                            onSetSortOrder = viewModel::setCardSortOrder,
                            onToggleUnmasked = viewModel::toggleUnmaskedNumbers,
                            onCopyText = copyWithFeedback,
                            onShareText = shareTextIntent,
                            onSaveCard = viewModel::saveCard,
                            onToggleFavourite = viewModel::toggleCardFavourite,
                            onToggleBlocked = viewModel::toggleCardBlocked,
                            onAdjustLounge = viewModel::adjustLoungeVisit,
                            onPayEmi = viewModel::payEmiInstallment,
                            onRecordSpend = viewModel::recordCardSpend,
                            onPayStatement = viewModel::payCardStatement,
                            onDeleteCard = viewModel::softDeleteCard,
                            onOpenUxAuditSheet = { showUxAuditSheet = true }
                        )

                        MainNavTab.VAULT -> VaultItemsScreen(
                            items = activeVaultItems,
                            allUnfilteredItems = allVaultItemsUnfiltered,
                            selectedCategory = selectedCategory,
                            activeProfile = activeProfile,
                            showUnmaskedGlobal = showUnmasked,
                            onSelectCategory = viewModel::selectVaultCategory,
                            onSaveVaultItem = viewModel::saveVaultItem,
                            onToggleBillPaid = viewModel::toggleBillPaidStatus,
                            onDeleteVaultItem = viewModel::softDeleteVaultItem,
                            onCopyText = copyWithFeedback,
                            onShareText = shareTextIntent
                        )

                        MainNavTab.INSIGHTS -> InsightsScreen(
                            cards = activeCards,
                            vaultItems = allVaultItemsUnfiltered,
                            spends = allSpends
                        )

                        MainNavTab.SETTINGS -> SettingsScreen(
                            themePreset = themePreset,
                            accentPreset = accentPreset,
                            profiles = profiles,
                            activeProfile = activeProfile,
                            blockScreenshots = blockScreenshots,
                            hapticEnabled = hapticEnabled,
                            deletedCards = deletedCards,
                            deletedItems = deletedVaultItems,
                            allCardsCount = activeCards.size,
                            allItemsCount = allVaultItemsUnfiltered.size,
                            onSelectTheme = viewModel::setThemePreset,
                            onSelectAccent = viewModel::setAccentPreset,
                            onSelectProfile = viewModel::selectProfile,
                            onAddProfile = viewModel::addProfile,
                            onToggleScreenshots = viewModel::toggleBlockScreenshots,
                            onToggleHaptic = viewModel::toggleHapticFeedback,
                            onUpdatePin = viewModel::updateVaultPin,
                            onLockNow = viewModel::lockVault,
                            onRestoreCard = viewModel::restoreCard,
                            onRestoreItem = viewModel::restoreVaultItem,
                            onEmptyRecycleBin = viewModel::emptyRecycleBin,
                            onExportBackup = { shareViaIntent ->
                                val backupJson = buildString {
                                    appendLine("{")
                                    appendLine("  \"app\": \"Vaultly\",")
                                    appendLine("  \"version\": \"1.11.0\",")
                                    appendLine("  \"cardsCount\": ${activeCards.size},")
                                    appendLine("  \"vaultItemsCount\": ${allVaultItemsUnfiltered.size}")
                                    appendLine("}")
                                }
                                if (shareViaIntent) {
                                    shareTextIntent(backupJson, "Vaultly Encrypted Backup")
                                } else {
                                    copyWithFeedback(backupJson, "Bitwarden / Vaultly JSON export")
                                }
                            },
                            onOpenUxAuditSheet = { showUxAuditSheet = true }
                        )
                    }
                }
            }
        }
    }

    if (showUxAuditSheet) {
        MaterialUxAuditSheet(onDismiss = { showUxAuditSheet = false })
    }
}

private fun navIconForTab(tab: MainNavTab, selected: Boolean): ImageVector {
    return when (tab) {
        MainNavTab.CARDS -> if (selected) Icons.Filled.CreditCard else Icons.Outlined.CreditCard
        MainNavTab.VAULT -> if (selected) Icons.Filled.Security else Icons.Outlined.Security
        MainNavTab.INSIGHTS -> if (selected) Icons.Filled.Insights else Icons.Outlined.Insights
        MainNavTab.SETTINGS -> if (selected) Icons.Filled.Settings else Icons.Outlined.Settings
    }
}
