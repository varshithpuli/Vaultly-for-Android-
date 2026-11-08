# Vaultly (v1.11.0 — Material Design 3 Edition)

**A private, offline-first Android vault for credit & debit cards, bank accounts, identity documents, 2FA TOTP logins, insurance policies, vehicle documents, bills, and spend insights.**

- **Package ID:** `com.aistudio.vaultly.vltkpq`
- **Version:** `1.11.0` (`versionCode = 11`)
- **UI Framework:** Jetpack Compose + Material Design 3 (`NavigationBar` / adaptive `NavigationRail`)
- **Database:** Room SQLite (`vaultly_offline_vault.db`) with KSP
- **Privacy Architecture:** Zero `INTERNET` permission declared — 100% offline on-device storage

## Key Features
- **Interactive 3D Card Deck:** Tap any card to flip 180° in 3D with spring physics, revealing magnetic stripe, signature strip, CVV, PIN, and statement/due dates.
- **Card Command Center:** Credit limit utilization bar, annual fee waiver progress, domestic & international airport lounge visit counters, EMI tracker, and Lost/Stolen checklist.
- **Unified 7-Category Vault:** Bank Accounts (with IFSC & UPI copy), IDs & Passports (with live expiry countdown badges), Logins with live 30-second **RFC 6238 2FA TOTP Authenticator** codes, Secure Notes, Insurance Policies, Vehicles (RC/PUC), and Bills.
- **Built-in Cryptographic Tools:** Password Generator (`8–32` chars, symbols, easy-to-read toggle) and Smart Bank SMS / Alert Parser.
- **Theme & Accent Studio:** 6 themes (*Aurora, Midnight, Obsidian, Ocean, Sunset, Daylight*) and 5 accent palettes.

## Building the APK Locally
```bash
gradle assembleDebug
```
The compiled APK is generated at:
- `app/build/outputs/apk/debug/app-debug.apk`
- `.build-outputs/app-debug.apk`
