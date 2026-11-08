package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class VaultCategory(val key: String, val label: String, val singular: String) {
    ACCOUNT("bank", "Accounts", "Bank Account"),
    ID_DOC("id", "IDs", "Identity Document"),
    LOGIN("login", "Logins & 2FA", "Login & 2FA"),
    NOTE("note", "Notes", "Secure Note"),
    INSURANCE("ins", "Insurance", "Insurance Policy"),
    VEHICLE("veh", "Vehicles", "Vehicle Document"),
    BILL("bill", "Bills", "Statement / Bill")
}

@Entity(tableName = "vault_cards")
data class VaultCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bank: String,
    val cardName: String,
    val network: String, // Visa, Mastercard, RuPay, Amex, Diners
    val variant: String, // e.g., Infinia Metal, Sapphiro, Atlas
    val cardKind: String, // Credit, Debit
    val holderName: String,
    val cardNumber: String,
    val expiry: String, // MM/YY
    val cvv: String,
    val cardPin: String = "",
    val creditLimit: Long = 0L,
    val currentOwed: Long = 0L,
    val annualFee: Long = 0L,
    val feeWaiverTarget: Long = 0L,
    val annualSpendSoFar: Long = 0L,
    val statementDay: Int = 15,
    val dueDay: Int = 5,
    val colorHex: String = "#4F46E5",
    val pattern: String = "Aurora", // Aurora, Mesh, Carbon, Waves, Minimal
    val domesticLoungeTotal: Int = 4,
    val domesticLoungeUsed: Int = 1,
    val intlLoungeTotal: Int = 2,
    val intlLoungeUsed: Int = 0,
    val emiTitle: String = "",
    val emiMonthlyAmount: Long = 0L,
    val emiMonthsRemaining: Int = 0,
    val isFavourite: Boolean = false,
    val isBlocked: Boolean = false,
    val profileName: String = "Personal",
    val isDeleted: Boolean = false,
    val deletedAt: Long = 0L,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "vault_items")
data class VaultItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryKey: String, // bank, id, login, note, ins, veh, bill
    val title: String, // Bank name, ID type, Website, Note title, Insurer, Vehicle model, Biller
    val subtitle: String, // Account type, Holder name, Username, Subhead, Policy type, Reg no, Invoice
    val primarySecret: String, // Account no, ID number, Password, Note body, Policy no, Reg no, Amount
    val secondarySecret: String = "", // IFSC, TOTP Base32 Secret, UPI ID, Nominee, Chassis, Paid amount
    val extraField1: String = "", // Branch / UPI / Website URL / DOB / Start Date / Ins Expiry / Bill Date
    val extraField2: String = "", // Customer ID / Expiry Date / Renewal Date / PUC Expiry / Due Date
    val badgeStatus: String = "", // Active, Paid, Unpaid, Savings, Passport, etc.
    val colorHex: String = "#1D4ED8",
    val profileName: String = "Personal",
    val isDeleted: Boolean = false,
    val deletedAt: Long = 0L,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "spend_transactions")
data class SpendTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cardId: Long,
    val cardLabel: String,
    val merchant: String,
    val category: String, // Shopping, Food & Dining, Fuel, Travel, Groceries, Bills & Utilities, Health, Entertainment
    val amount: Long,
    val dateIso: String, // YYYY-MM-DD
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
