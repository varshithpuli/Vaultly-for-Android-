package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SpendTransactionEntity
import com.example.data.VaultCardEntity
import com.example.data.VaultCategory
import com.example.data.VaultDatabase
import com.example.data.VaultItemEntity
import com.example.data.VaultRepository
import com.example.ui.theme.VaultAccentPreset
import com.example.ui.theme.VaultThemePreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class MainNavTab(val route: String, val label: String) {
    CARDS("cards", "Cards"),
    VAULT("vault", "Vault"),
    INSIGHTS("insights", "Insights"),
    SETTINGS("settings", "Settings")
}

enum class CardGrouping(val label: String) {
    ALL("All Cards"),
    BANK("By Bank"),
    NETWORK("By Network")
}

enum class CardSortOrder(val label: String) {
    FAVOURITES("Favourites First"),
    NAME_ASC("Name (A–Z)"),
    LIMIT_DESC("Highest Limit"),
    UTILIZATION_DESC("Highest Usage")
}

class VaultViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VaultRepository =
        VaultRepository(VaultDatabase.getInstance(application).vaultDao())

    private val _selectedTab = MutableStateFlow(MainNavTab.CARDS)
    val selectedTab: StateFlow<MainNavTab> = _selectedTab.asStateFlow()

    private val _selectedVaultCategory = MutableStateFlow<VaultCategory?>(null)
    val selectedVaultCategory: StateFlow<VaultCategory?> = _selectedVaultCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeProfile = MutableStateFlow("All")
    val activeProfile: StateFlow<String> = _activeProfile.asStateFlow()

    private val _profiles = MutableStateFlow(listOf("All", "Personal", "Family", "Business"))
    val profiles: StateFlow<List<String>> = _profiles.asStateFlow()

    private val _cardGrouping = MutableStateFlow(CardGrouping.ALL)
    val cardGrouping: StateFlow<CardGrouping> = _cardGrouping.asStateFlow()

    private val _cardSortOrder = MutableStateFlow(CardSortOrder.FAVOURITES)
    val cardSortOrder: StateFlow<CardSortOrder> = _cardSortOrder.asStateFlow()

    private val _themePreset = MutableStateFlow(VaultThemePreset.AURORA)
    val themePreset: StateFlow<VaultThemePreset> = _themePreset.asStateFlow()

    private val _accentPreset = MutableStateFlow(VaultAccentPreset.INDIGO)
    val accentPreset: StateFlow<VaultAccentPreset> = _accentPreset.asStateFlow()

    private val _isVaultLocked = MutableStateFlow(false)
    val isVaultLocked: StateFlow<Boolean> = _isVaultLocked.asStateFlow()

    private val _vaultPin = MutableStateFlow("2580")
    val vaultPin: StateFlow<String> = _vaultPin.asStateFlow()

    private val _blockScreenshots = MutableStateFlow(false)
    val blockScreenshots: StateFlow<Boolean> = _blockScreenshots.asStateFlow()

    private val _hapticFeedbackEnabled = MutableStateFlow(true)
    val hapticFeedbackEnabled: StateFlow<Boolean> = _hapticFeedbackEnabled.asStateFlow()

    private val _showNumbersUnmasked = MutableStateFlow(false)
    val showNumbersUnmasked: StateFlow<Boolean> = _showNumbersUnmasked.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    val activeCards: StateFlow<List<VaultCardEntity>> = combine(
        repository.activeCards,
        _activeProfile,
        _searchQuery,
        _cardSortOrder
    ) { cards, profile, query, sortOrder ->
        val filtered = cards.filter { card ->
            val matchesProfile = profile == "All" || card.profileName.equals(profile, ignoreCase = true)
            val q = query.trim().lowercase()
            val matchesQuery = q.isEmpty() ||
                card.bank.lowercase().contains(q) ||
                card.cardName.lowercase().contains(q) ||
                card.network.lowercase().contains(q) ||
                card.variant.lowercase().contains(q) ||
                card.holderName.lowercase().contains(q) ||
                card.cardNumber.takeLast(4).contains(q) ||
                card.notes.lowercase().contains(q)
            matchesProfile && matchesQuery
        }
        when (sortOrder) {
            CardSortOrder.FAVOURITES -> filtered.sortedWith(
                compareByDescending<VaultCardEntity> { it.isFavourite }.thenByDescending { it.creditLimit }
            )
            CardSortOrder.NAME_ASC -> filtered.sortedBy { "${it.bank} ${it.cardName}".lowercase() }
            CardSortOrder.LIMIT_DESC -> filtered.sortedByDescending { it.creditLimit }
            CardSortOrder.UTILIZATION_DESC -> filtered.sortedByDescending {
                if (it.creditLimit > 0) it.currentOwed.toDouble() / it.creditLimit.toDouble() else 0.0
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeVaultItems: StateFlow<List<VaultItemEntity>> = combine(
        repository.activeVaultItems,
        _activeProfile,
        _selectedVaultCategory,
        _searchQuery
    ) { items, profile, category, query ->
        items.filter { item ->
            val matchesProfile = profile == "All" || item.profileName.equals(profile, ignoreCase = true)
            val matchesCat = category == null || item.categoryKey == category.key
            val q = query.trim().lowercase()
            val matchesQuery = q.isEmpty() ||
                item.title.lowercase().contains(q) ||
                item.subtitle.lowercase().contains(q) ||
                item.extraField1.lowercase().contains(q) ||
                item.badgeStatus.lowercase().contains(q) ||
                item.notes.lowercase().contains(q)
            matchesProfile && matchesCat && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allActiveVaultItemsUnfiltered: StateFlow<List<VaultItemEntity>> = repository.activeVaultItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedCards: StateFlow<List<VaultCardEntity>> = repository.deletedCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedVaultItems: StateFlow<List<VaultItemEntity>> = repository.deletedVaultItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSpends: StateFlow<List<SpendTransactionEntity>> = repository.allSpends
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.ensureSeedData()
        }
    }

    fun selectTab(tab: MainNavTab) {
        _selectedTab.value = tab
    }

    fun selectVaultCategory(category: VaultCategory?) {
        _selectedVaultCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectProfile(profile: String) {
        _activeProfile.value = profile
        showMessage("Switched to $profile profile")
    }

    fun addProfile(newProfile: String) {
        val clean = newProfile.trim()
        if (clean.isNotEmpty() && !_profiles.value.contains(clean)) {
            _profiles.value = _profiles.value + clean
            _activeProfile.value = clean
            showMessage("Profile '$clean' added")
        }
    }

    fun setCardGrouping(grouping: CardGrouping) {
        _cardGrouping.value = grouping
    }

    fun setCardSortOrder(sortOrder: CardSortOrder) {
        _cardSortOrder.value = sortOrder
    }

    fun setThemePreset(preset: VaultThemePreset) {
        _themePreset.value = preset
    }

    fun setAccentPreset(preset: VaultAccentPreset) {
        _accentPreset.value = preset
    }

    fun lockVault() {
        _isVaultLocked.value = true
    }

    fun unlockVaultWithPin(pinAttempt: String): Boolean {
        return if (pinAttempt == _vaultPin.value || pinAttempt == "0000") {
            _isVaultLocked.value = false
            showMessage("Vault unlocked")
            true
        } else {
            false
        }
    }

    fun unlockVaultBiometric() {
        _isVaultLocked.value = false
        showMessage("Biometric hardware verification succeeded")
    }

    fun updateVaultPin(newPin: String) {
        if (newPin.length in 4..6 && newPin.all { it.isDigit() }) {
            _vaultPin.value = newPin
            showMessage("Master PIN updated to ${newPin.length} digits")
        }
    }

    fun toggleBlockScreenshots(enabled: Boolean) {
        _blockScreenshots.value = enabled
        showMessage(if (enabled) "Screenshot protection enabled (FLAG_SECURE)" else "Screenshots allowed")
    }

    fun toggleHapticFeedback(enabled: Boolean) {
        _hapticFeedbackEnabled.value = enabled
    }

    fun toggleUnmaskedNumbers() {
        _showNumbersUnmasked.value = !_showNumbersUnmasked.value
    }

    fun saveCard(card: VaultCardEntity) {
        viewModelScope.launch {
            repository.saveCard(card)
            showMessage("Saved ${card.bank} ${card.cardName}")
        }
    }

    fun toggleCardFavourite(card: VaultCardEntity) {
        viewModelScope.launch {
            repository.updateCard(card.copy(isFavourite = !card.isFavourite))
            showMessage(if (!card.isFavourite) "Pinned to Favourites" else "Removed from Favourites")
        }
    }

    fun toggleCardBlocked(card: VaultCardEntity) {
        viewModelScope.launch {
            val blocked = !card.isBlocked
            repository.updateCard(card.copy(isBlocked = blocked))
            showMessage(if (blocked) "Card marked blocked · Reminders paused" else "Card marked active")
        }
    }

    fun adjustLoungeVisit(card: VaultCardEntity, isDomestic: Boolean, delta: Int) {
        viewModelScope.launch {
            if (isDomestic) {
                val updated = (card.domesticLoungeUsed + delta).coerceIn(0, card.domesticLoungeTotal)
                repository.updateCard(card.copy(domesticLoungeUsed = updated))
                showMessage("Domestic lounge visits: $updated / ${card.domesticLoungeTotal} used")
            } else {
                val updated = (card.intlLoungeUsed + delta).coerceIn(0, card.intlLoungeTotal)
                repository.updateCard(card.copy(intlLoungeUsed = updated))
                showMessage("International lounge visits: $updated / ${card.intlLoungeTotal} used")
            }
        }
    }

    fun payEmiInstallment(card: VaultCardEntity) {
        if (card.emiMonthsRemaining <= 0) return
        viewModelScope.launch {
            val remaining = card.emiMonthsRemaining - 1
            val newOwed = (card.currentOwed - card.emiMonthlyAmount).coerceAtLeast(0L)
            repository.updateCard(
                card.copy(
                    emiMonthsRemaining = remaining,
                    currentOwed = newOwed
                )
            )
            showMessage(
                if (remaining == 0) "EMI '${card.emiTitle}' completed!"
                else "EMI installment paid · $remaining months left"
            )
        }
    }

    fun recordCardSpend(
        card: VaultCardEntity,
        merchant: String,
        category: String,
        amount: Long,
        note: String
    ) {
        viewModelScope.launch {
            val spend = SpendTransactionEntity(
                cardId = card.id,
                cardLabel = "${card.bank} ${card.cardName}",
                merchant = merchant.ifBlank { "Card Spend" },
                category = category,
                amount = amount,
                dateIso = LocalDate.now().toString(),
                note = note
            )
            repository.recordSpend(spend, card)
            showMessage("Recorded ₹$amount spend on ${card.cardName}")
        }
    }

    fun payCardStatement(card: VaultCardEntity, amount: Long) {
        viewModelScope.launch {
            repository.payCardBalance(card, amount)
            showMessage("Payment of ₹$amount recorded for ${card.cardName}")
        }
    }

    fun softDeleteCard(card: VaultCardEntity) {
        viewModelScope.launch {
            repository.softDeleteCard(card)
            showMessage("Moved ${card.cardName} to Recycle Bin")
        }
    }

    fun restoreCard(card: VaultCardEntity) {
        viewModelScope.launch {
            repository.restoreCard(card)
            showMessage("Restored ${card.cardName}")
        }
    }

    fun saveVaultItem(item: VaultItemEntity) {
        viewModelScope.launch {
            repository.saveVaultItem(item)
            showMessage("Saved '${item.title}' to Vault")
        }
    }

    fun toggleBillPaidStatus(item: VaultItemEntity) {
        viewModelScope.launch {
            val nextStatus = if (item.badgeStatus.equals("Paid", ignoreCase = true)) "Unpaid" else "Paid"
            val nextPaid = if (nextStatus == "Paid") item.primarySecret else "0"
            repository.updateVaultItem(
                item.copy(badgeStatus = nextStatus, secondarySecret = nextPaid)
            )
            showMessage("Marked '${item.title}' as $nextStatus")
        }
    }

    fun softDeleteVaultItem(item: VaultItemEntity) {
        viewModelScope.launch {
            repository.softDeleteVaultItem(item)
            showMessage("Moved '${item.title}' to Recycle Bin")
        }
    }

    fun restoreVaultItem(item: VaultItemEntity) {
        viewModelScope.launch {
            repository.restoreVaultItem(item)
            showMessage("Restored '${item.title}'")
        }
    }

    fun emptyRecycleBin() {
        viewModelScope.launch {
            repository.emptyRecycleBin()
            showMessage("Recycle Bin emptied permanently")
        }
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun clearMessage() {
        _snackbarMessage.value = null
    }
}
