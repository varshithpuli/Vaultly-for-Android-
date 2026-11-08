package com.example.util

import java.nio.ByteBuffer
import java.security.SecureRandom
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow

object SecurityAndTotpUtils {

    private const val BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    /**
     * Generates a real RFC 6238 6-digit TOTP code from a Base32 secret or otpauth:// URI.
     */
    fun generateTotpCode(rawSecret: String, timeMillis: Long = System.currentTimeMillis()): String {
        val cleaned = extractBase32Secret(rawSecret)
        if (cleaned.isEmpty()) return "------"
        val keyBytes = decodeBase32(cleaned)
        if (keyBytes.isEmpty()) return "------"

        return try {
            val timeWindow = timeMillis / 1000L / 30L
            val data = ByteBuffer.allocate(8).putLong(timeWindow).array()
            val mac = Mac.getInstance("HmacSHA1")
            mac.init(SecretKeySpec(keyBytes, "HmacSHA1"))
            val hash = mac.doFinal(data)
            val offset = hash[hash.size - 1].toInt() and 0x0F
            val binary = ((hash[offset].toInt() and 0x7F) shl 24) or
                ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                (hash[offset + 3].toInt() and 0xFF)
            val otp = binary % (10.0.pow(6).toInt())
            otp.toString().padStart(6, '0').chunked(3).joinToString(" ")
        } catch (e: Exception) {
            "------"
        }
    }

    fun getTotpSecondsRemaining(timeMillis: Long = System.currentTimeMillis()): Int {
        return (30 - ((timeMillis / 1000L) % 30L)).toInt()
    }

    private fun extractBase32Secret(raw: String): String {
        val trimmed = raw.trim()
        val secretMatch = Regex("[?&]secret=([A-Za-z2-7=]+)", RegexOption.IGNORE_CASE).find(trimmed)
        val candidate = secretMatch?.groupValues?.get(1) ?: trimmed
        return candidate.uppercase().filter { it in BASE32_CHARS }
    }

    private fun decodeBase32(secret: String): ByteArray {
        var buffer = 0
        var bitsLeft = 0
        val out = ArrayList<Byte>()
        for (ch in secret) {
            val val5 = BASE32_CHARS.indexOf(ch)
            if (val5 < 0) continue
            buffer = (buffer shl 5) or val5
            bitsLeft += 5
            if (bitsLeft >= 8) {
                bitsLeft -= 8
                out.add(((buffer shr bitsLeft) and 0xFF).toByte())
            }
        }
        return out.toByteArray()
    }

    /**
     * Generates a cryptographically strong random password with customizable rules.
     */
    fun generatePassword(
        length: Int = 18,
        includeSymbols: Boolean = true,
        easyToRead: Boolean = false
    ): String {
        val lower = if (easyToRead) "abcdefghjkmnpqrstuvwxyz" else "abcdefghijklmnopqrstuvwxyz"
        val upper = if (easyToRead) "ABCDEFGHJKLMNPQRSTUVWXYZ" else "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val digits = if (easyToRead) "23456789" else "0123456789"
        val symbols = "!@#\$%^&*_-+=?"
        val pool = buildString {
            append(lower)
            append(upper)
            append(digits)
            if (includeSymbols) append(symbols)
        }
        val random = SecureRandom()
        return (1..length.coerceIn(8, 40))
            .map { pool[random.nextInt(pool.length)] }
            .joinToString("")
    }

    /**
     * Validates a credit/debit card number using the Luhn checksum algorithm.
     */
    fun isValidLuhn(cardNumber: String): Boolean {
        val digits = cardNumber.filter { it.isDigit() }
        if (digits.length !in 13..19) return false
        var sum = 0
        var alternate = false
        for (i in digits.length - 1 downTo 0) {
            var n = digits[i].digitToInt()
            if (alternate) {
                n *= 2
                if (n > 9) n -= 9
            }
            sum += n
            alternate = !alternate
        }
        return sum % 10 == 0
    }

    fun detectNetwork(cardNumber: String): String {
        val d = cardNumber.filter { it.isDigit() }
        return when {
            d.startsWith("4") -> "Visa"
            d.take(2).toIntOrNull() in 51..55 || d.take(4).toIntOrNull() in 2221..2720 -> "Mastercard"
            d.startsWith("34") || d.startsWith("37") -> "Amex"
            d.startsWith("36") || d.startsWith("38") || d.startsWith("30") -> "Diners"
            d.startsWith("60") || d.startsWith("65") || d.startsWith("81") || d.startsWith("82") || d.startsWith("508") -> "RuPay"
            else -> "Visa"
        }
    }

    fun formatCardNumber(raw: String, masked: Boolean): String {
        val digits = raw.filter { it.isDigit() }
        if (digits.isEmpty()) return "•••• •••• •••• ••••"
        if (masked) {
            val last4 = digits.takeLast(4).padStart(4, '•')
            return "••••  ••••  ••••  $last4"
        }
        return digits.chunked(4).joinToString("  ")
    }

    fun formatCurrencyInr(amount: Long): String {
        if (amount == 0L) return "₹0"
        val str = amount.toString()
        if (str.length <= 3) return "₹$str"
        val lastThree = str.takeLast(3)
        val rest = str.dropLast(3)
        val grouped = rest.reversed().chunked(2).joinToString(",").reversed()
        return "₹$grouped,$lastThree"
    }

    fun computeExpiryBadge(expiryIso: String): Pair<String, Boolean>? {
        if (expiryIso.isBlank()) return null
        return try {
            val expDate = LocalDate.parse(expiryIso.trim())
            val days = ChronoUnit.DAYS.between(LocalDate.now(), expDate)
            when {
                days < 0 -> "Expired ${-days}d ago" to true
                days <= 45 -> "Expires in ${days}d" to true
                days <= 180 -> "Renews in ${days}d" to false
                else -> "Valid till ${expiryIso.take(7)}" to false
            }
        } catch (_: Exception) {
            "Exp: $expiryIso" to false
        }
    }

    data class ParsedAlertResult(
        val isNewCardStub: Boolean,
        val bank: String,
        val last4: String,
        val amount: Long,
        val merchant: String,
        val category: String
    )

    /**
     * Parses pasted bank SMS / notification alerts into either a Spend Transaction or a New Card stub.
     */
    fun parseBankAlertText(text: String): ParsedAlertResult {
        val lower = text.lowercase()
        val isDispatch = lower.contains("dispatched") || lower.contains("approved") || lower.contains("delivered")
        val bank = listOf("HDFC", "ICICI", "Axis", "SBI", "Amex", "Kotak", "IDFC", "IndusInd", "HSBC", "Citibank")
            .firstOrNull { lower.contains(it.lowercase()) } ?: "HDFC Bank"

        val last4Match = Regex("(?:ending|xx|x{2,4}|card\\s*no\\.?)\\s*(\\d{4})", RegexOption.IGNORE_CASE).find(text)
            ?: Regex("\\b(\\d{4})\\b").findAll(text).lastOrNull()
        val last4 = last4Match?.groupValues?.get(1) ?: "4829"

        val amountMatch = Regex("(?:rs\\.?|inr|₹)\\s*([\\d,]+(?:\\.\\d{1,2})?)", RegexOption.IGNORE_CASE).find(text)
        val amount = amountMatch?.groupValues?.get(1)?.replace(",", "")?.substringBefore(".")?.toLongOrNull() ?: 1850L

        val merchantMatch = Regex("(?:at|to|on|towards)\\s+([A-Za-z0-9 &._-]{3,24})", RegexOption.IGNORE_CASE).find(text)
        val merchant = merchantMatch?.groupValues?.get(1)?.trim() ?: "Smart Alert Merchant"

        val category = when {
            lower.contains("swiggy") || lower.contains("zomato") || lower.contains("cafe") || lower.contains("dining") -> "Food & Dining"
            lower.contains("uber") || lower.contains("indigo") || lower.contains("flight") || lower.contains("hotel") || lower.contains("irctc") -> "Travel"
            lower.contains("shell") || lower.contains("hpcl") || lower.contains("fuel") || lower.contains("petrol") -> "Fuel"
            lower.contains("blinkit") || lower.contains("zepto") || lower.contains("mart") -> "Groceries"
            lower.contains("airtel") || lower.contains("jio") || lower.contains("electricity") || lower.contains("bill") -> "Bills & Utilities"
            lower.contains("apollo") || lower.contains("pharma") || lower.contains("hospital") -> "Health"
            lower.contains("netflix") || lower.contains("spotify") || lower.contains("bookmyshow") -> "Entertainment"
            else -> "Shopping"
        }

        return ParsedAlertResult(
            isNewCardStub = isDispatch,
            bank = bank,
            last4 = last4,
            amount = amount,
            merchant = merchant,
            category = category
        )
    }
}
