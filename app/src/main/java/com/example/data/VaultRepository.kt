package com.example.data

import kotlinx.coroutines.flow.Flow

class VaultRepository(private val dao: VaultDao) {

    val activeCards: Flow<List<VaultCardEntity>> = dao.getActiveCards()
    val deletedCards: Flow<List<VaultCardEntity>> = dao.getDeletedCards()
    val activeVaultItems: Flow<List<VaultItemEntity>> = dao.getActiveVaultItems()
    val deletedVaultItems: Flow<List<VaultItemEntity>> = dao.getDeletedVaultItems()
    val allSpends: Flow<List<SpendTransactionEntity>> = dao.getAllSpends()

    suspend fun saveCard(card: VaultCardEntity): Long {
        return dao.insertCard(card)
    }

    suspend fun updateCard(card: VaultCardEntity) {
        dao.updateCard(card)
    }

    suspend fun softDeleteCard(card: VaultCardEntity) {
        dao.updateCard(card.copy(isDeleted = true, deletedAt = System.currentTimeMillis()))
    }

    suspend fun restoreCard(card: VaultCardEntity) {
        dao.updateCard(card.copy(isDeleted = false, deletedAt = 0L))
    }

    suspend fun deleteCardPermanently(id: Long) {
        dao.deleteCardPermanently(id)
    }

    suspend fun saveVaultItem(item: VaultItemEntity): Long {
        return dao.insertVaultItem(item)
    }

    suspend fun updateVaultItem(item: VaultItemEntity) {
        dao.updateVaultItem(item)
    }

    suspend fun softDeleteVaultItem(item: VaultItemEntity) {
        dao.updateVaultItem(item.copy(isDeleted = true, deletedAt = System.currentTimeMillis()))
    }

    suspend fun restoreVaultItem(item: VaultItemEntity) {
        dao.updateVaultItem(item.copy(isDeleted = false, deletedAt = 0L))
    }

    suspend fun deleteVaultItemPermanently(id: Long) {
        dao.deleteVaultItemPermanently(id)
    }

    suspend fun recordSpend(spend: SpendTransactionEntity, card: VaultCardEntity?) {
        dao.insertSpend(spend)
        if (card != null && card.cardKind.equals("Credit", ignoreCase = true)) {
            dao.updateCard(
                card.copy(
                    currentOwed = card.currentOwed + spend.amount,
                    annualSpendSoFar = card.annualSpendSoFar + spend.amount
                )
            )
        }
    }

    suspend fun payCardBalance(card: VaultCardEntity, paymentAmount: Long) {
        val newOwed = (card.currentOwed - paymentAmount).coerceAtLeast(0L)
        dao.updateCard(card.copy(currentOwed = newOwed))
    }

    suspend fun emptyRecycleBin() {
        dao.emptyDeletedCards()
        dao.emptyDeletedVaultItems()
    }

    suspend fun ensureSeedData() {
        if (dao.getCardCount() > 0) return

        val sampleCards = listOf(
            VaultCardEntity(
                bank = "HDFC Bank",
                cardName = "Infinia Metal Edition",
                network = "Visa",
                variant = "Visa Infinite",
                cardKind = "Credit",
                holderName = "AARAV SHARMA",
                cardNumber = "4375518890124829",
                expiry = "09/29",
                cvv = "842",
                cardPin = "4910",
                creditLimit = 1200000L,
                currentOwed = 148500L,
                annualFee = 12500L,
                feeWaiverTarget = 1000000L,
                annualSpendSoFar = 785000L,
                statementDay = 18,
                dueDay = 8,
                colorHex = "#090D16",
                pattern = "Aurora",
                domesticLoungeTotal = 12,
                domesticLoungeUsed = 3,
                intlLoungeTotal = 12,
                intlLoungeUsed = 2,
                emiTitle = "MacBook Pro M4 16\"",
                emiMonthlyAmount = 24500L,
                emiMonthsRemaining = 4,
                isFavourite = true,
                profileName = "Personal",
                notes = "Unlimited Priority Pass lounge access · 5X Reward Points on SmartBuy"
            ),
            VaultCardEntity(
                bank = "Axis Bank",
                cardName = "Atlas Travel Miles",
                network = "Visa",
                variant = "Visa Signature",
                cardKind = "Credit",
                holderName = "AARAV SHARMA",
                cardNumber = "4514072134907731",
                expiry = "11/28",
                cvv = "319",
                cardPin = "7284",
                creditLimit = 650000L,
                currentOwed = 64200L,
                annualFee = 5000L,
                feeWaiverTarget = 750000L,
                annualSpendSoFar = 510000L,
                statementDay = 12,
                dueDay = 2,
                colorHex = "#4F46E5",
                pattern = "Waves",
                domesticLoungeTotal = 8,
                domesticLoungeUsed = 2,
                intlLoungeTotal = 4,
                intlLoungeUsed = 1,
                emiTitle = "",
                emiMonthlyAmount = 0L,
                emiMonthsRemaining = 0,
                isFavourite = true,
                profileName = "Personal",
                notes = "5 EDGE Miles per ₹100 on flights & hotels"
            ),
            VaultCardEntity(
                bank = "ICICI Bank",
                cardName = "Sapphiro Dual",
                network = "Amex",
                variant = "Amex Network",
                cardKind = "Credit",
                holderName = "AARAV SHARMA",
                cardNumber = "376942189503110",
                expiry = "04/28",
                cvv = "5901",
                cardPin = "1029",
                creditLimit = 500000L,
                currentOwed = 19800L,
                annualFee = 3500L,
                feeWaiverTarget = 600000L,
                annualSpendSoFar = 420000L,
                statementDay = 22,
                dueDay = 10,
                colorHex = "#B45309",
                pattern = "Mesh",
                domesticLoungeTotal = 4,
                domesticLoungeUsed = 1,
                intlLoungeTotal = 2,
                intlLoungeUsed = 0,
                emiTitle = "Sony Bravia OLED 65\"",
                emiMonthlyAmount = 11200L,
                emiMonthsRemaining = 6,
                isFavourite = false,
                profileName = "Personal",
                notes = "BookMyShow Buy-1-Get-1 up to ₹500 twice a month"
            ),
            VaultCardEntity(
                bank = "SBI Card",
                cardName = "Cashback Online",
                network = "Visa",
                variant = "Visa Platinum",
                cardKind = "Credit",
                holderName = "PRIYA SHARMA",
                cardNumber = "4216839011229044",
                expiry = "07/27",
                cvv = "612",
                cardPin = "8831",
                creditLimit = 300000L,
                currentOwed = 14900L,
                annualFee = 999L,
                feeWaiverTarget = 200000L,
                annualSpendSoFar = 192000L,
                statementDay = 5,
                dueDay = 25,
                colorHex = "#0F766E",
                pattern = "Carbon",
                domesticLoungeTotal = 4,
                domesticLoungeUsed = 0,
                intlLoungeTotal = 0,
                intlLoungeUsed = 0,
                isFavourite = false,
                profileName = "Family",
                notes = "Flat 5% cashback on all online merchants without merchant restrictions"
            )
        )
        dao.insertCards(sampleCards)

        val sampleItems = listOf(
            // Bank Accounts
            VaultItemEntity(
                categoryKey = VaultCategory.ACCOUNT.key,
                title = "HDFC Bank Imperia",
                subtitle = "Aarav Sharma · Savings Account",
                primarySecret = "50100482910482",
                secondarySecret = "HDFC0000240",
                extraField1 = "aarav@hdfcbank",
                extraField2 = "Cust ID: 88412099",
                badgeStatus = "Savings",
                colorHex = "#1D4ED8",
                profileName = "Personal",
                notes = "Indiranagar Branch · RM: Vikram (+91 98201 11223)"
            ),
            VaultItemEntity(
                categoryKey = VaultCategory.ACCOUNT.key,
                title = "ICICI Wealth Management",
                subtitle = "Aarav Sharma · Salary Account",
                primarySecret = "000401589320",
                secondarySecret = "ICIC0000004",
                extraField1 = "aarav.sharma@icici",
                extraField2 = "Cust ID: 55190341",
                badgeStatus = "Salary",
                colorHex = "#B45309",
                profileName = "Personal",
                notes = "Zero balance corporate wealth privilege account"
            ),
            // IDs
            VaultItemEntity(
                categoryKey = VaultCategory.ID_DOC.key,
                title = "Passport (Republic of India)",
                subtitle = "Aarav Sharma",
                primarySecret = "Z4829104",
                secondarySecret = "1994-05-14",
                extraField1 = "Bengaluru RPO",
                extraField2 = "2031-08-22",
                badgeStatus = "Passport",
                colorHex = "#1E3A8A",
                profileName = "Personal",
                notes = "ECNR · US B1/B2 Visa valid until 2033"
            ),
            VaultItemEntity(
                categoryKey = VaultCategory.ID_DOC.key,
                title = "PAN Card (Income Tax)",
                subtitle = "Aarav Sharma",
                primarySecret = "ABCPS4921K",
                secondarySecret = "1994-05-14",
                extraField1 = "Govt of India",
                extraField2 = "",
                badgeStatus = "Permanent",
                colorHex = "#1D4ED8",
                profileName = "Personal",
                notes = "Aadhaar linked & verified"
            ),
            VaultItemEntity(
                categoryKey = VaultCategory.ID_DOC.key,
                title = "Driving Licence",
                subtitle = "Aarav Sharma · MCWG & LMV",
                primarySecret = "KA0120180049281",
                secondarySecret = "1994-05-14",
                extraField1 = "Koramangala RTO",
                extraField2 = "2026-12-10",
                badgeStatus = "Driving Licence",
                colorHex = "#047857",
                profileName = "Personal",
                notes = "DigiLocker verified original"
            ),
            // Logins with live RFC 6238 TOTP
            VaultItemEntity(
                categoryKey = VaultCategory.LOGIN.key,
                title = "GitHub Enterprise",
                subtitle = "aarav@vaultly.dev",
                primarySecret = "Vh9!kP2#mQ8_xL4@zR9",
                secondarySecret = "JBSWY3DPEHPK3PXP",
                extraField1 = "https://github.com/login",
                extraField2 = "SSH + Hardware Key Backup",
                badgeStatus = "2FA Active",
                colorHex = "#4F46E5",
                profileName = "Personal",
                notes = "Recovery codes stored in offline hardware safe"
            ),
            VaultItemEntity(
                categoryKey = VaultCategory.LOGIN.key,
                title = "AWS Root Console",
                subtitle = "ops-admin@vaultly.dev",
                primarySecret = "AwS#9928!CloudVault_26",
                secondarySecret = "GEZDGNBVGY3TQOJQ",
                extraField1 = "https://console.aws.amazon.com",
                extraField2 = "Account #8492-0192-4410",
                badgeStatus = "2FA Active",
                colorHex = "#B45309",
                profileName = "Personal",
                notes = "Production billing & KMS master account"
            ),
            // Secure Notes
            VaultItemEntity(
                categoryKey = VaultCategory.NOTE.key,
                title = "Home Safe & Locker Combo",
                subtitle = "Godrej Rhino Electronic Safe + Bank Locker #412",
                primarySecret = "Safe PIN: 74-19-88 ·HDFC Indiranagar Locker Key #412B",
                secondarySecret = "security, home, locker",
                extraField1 = "Updated Nov 2026",
                extraField2 = "",
                badgeStatus = "Encrypted",
                colorHex = "#7E22CE",
                profileName = "Family",
                notes = "Property deeds and gold sovereign bonds inside"
            ),
            // Insurance
            VaultItemEntity(
                categoryKey = VaultCategory.INSURANCE.key,
                title = "HDFC ERGO Optima Secure",
                subtitle = "Health Floater · ₹50 Lakh Cover",
                primarySecret = "2819-4002-9910-8821",
                secondarySecret = "₹28,400 / yr",
                extraField1 = "Cashless Network",
                extraField2 = "2026-12-02",
                badgeStatus = "Health",
                colorHex = "#0F766E",
                profileName = "Family",
                notes = "Covers Aarav & Priya · Zero co-pay + Restore benefit"
            ),
            // Vehicle
            VaultItemEntity(
                categoryKey = VaultCategory.VEHICLE.key,
                title = "Hyundai IONIQ 5 EV",
                subtitle = "Car · Gravity Gold Matte",
                primarySecret = "KA 01 MV 4829",
                secondarySecret = "MALC38194829104",
                extraField1 = "Ins Exp: 2027-04-15",
                extraField2 = "2026-11-28",
                badgeStatus = "Car",
                colorHex = "#334155",
                profileName = "Personal",
                notes = "Zero Dep ICICI Lombard Policy #3001/948291"
            ),
            // Bills
            VaultItemEntity(
                categoryKey = VaultCategory.BILL.key,
                title = "HDFC Infinia Oct Statement",
                subtitle = "Statement Bill · Min Due ₹7,400",
                primarySecret = "68400",
                secondarySecret = "0",
                extraField1 = "2026-10-18",
                extraField2 = "2026-11-12",
                badgeStatus = "Unpaid",
                colorHex = "#BE123C",
                profileName = "Personal",
                notes = "Auto-debit off; pay via NEFT/BillPay for 1% reward"
            ),
            VaultItemEntity(
                categoryKey = VaultCategory.BILL.key,
                title = "BESCOM Electricity & Airtel Fiber",
                subtitle = "Utility Bundle · Indiranagar",
                primarySecret = "4850",
                secondarySecret = "4850",
                extraField1 = "2026-11-01",
                extraField2 = "2026-11-15",
                badgeStatus = "Paid",
                colorHex = "#15803D",
                profileName = "Family",
                notes = "Paid via SBI Cashback Visa"
            )
        )
        dao.insertVaultItems(sampleItems)

        val sampleSpends = listOf(
            SpendTransactionEntity(
                cardId = 1L,
                cardLabel = "HDFC Infinia Metal",
                merchant = "IndiGo Airlines Return",
                category = "Travel",
                amount = 34800L,
                dateIso = "2026-11-06",
                note = "Bengaluru to Delhi business trip via SmartBuy"
            ),
            SpendTransactionEntity(
                cardId = 1L,
                cardLabel = "HDFC Infinia Metal",
                merchant = "Apple Store Online",
                category = "Shopping",
                amount = 28900L,
                dateIso = "2026-11-04",
                note = "AirPods Pro 2 + MagSafe"
            ),
            SpendTransactionEntity(
                cardId = 2L,
                cardLabel = "Axis Atlas Travel",
                merchant = "Taj West End Bengaluru",
                category = "Food & Dining",
                amount = 8450L,
                dateIso = "2026-11-03",
                note = "Anniversary dinner · EazyDiner 25% off"
            ),
            SpendTransactionEntity(
                cardId = 4L,
                cardLabel = "SBI Cashback Online",
                merchant = "Blinkit & BigBasket",
                category = "Groceries",
                amount = 6200L,
                dateIso = "2026-11-02",
                note = "Monthly pantry essentials"
            ),
            SpendTransactionEntity(
                cardId = 3L,
                cardLabel = "ICICI Sapphiro Dual",
                merchant = "Shell V-Power Station",
                category = "Fuel",
                amount = 4100L,
                dateIso = "2026-11-01",
                note = "1% fuel surcharge waiver applied"
            ),
            SpendTransactionEntity(
                cardId = 4L,
                cardLabel = "SBI Cashback Online",
                merchant = "Apollo Pharmacy & Lab",
                category = "Health",
                amount = 3250L,
                dateIso = "2026-11-05",
                note = "Annual preventive checkup"
            )
        )
        dao.insertSpends(sampleSpends)
    }
}
