# Apna Khaata — Native Android App (Kotlin)

Personal expense tracker jo **bank SMS automatically capture** karta hai — bina Play Store ke, direct APK install.

## Features
- 📩 **SMS Auto-Capture** — naya bank/UPI SMS aate hi transaction automatically record (BroadcastReceiver)
- 🔎 **Inbox Scan** — purane saare SMS ek click me import (duplicates auto-skip)
- ❓ **Confirm Flow** — har auto-captured entry ke liye app poochta hai: *"Yeh payment kis liye thi?"* + category confirm (bilkul web app jaisa)
- 📊 **Dashboard** — is mahine ka kharcha/aamdani + category-wise pie chart
- 📒 **Ledger** — saari entries, search, category edit, delete
- 🏷️ **Rules** — keyword → category auto-tagging (zepto→Groceries, swiggy→Eating Out, etc. pre-loaded)
- 💾 **Backup/Restore** — JSON file me export/import; **web app (Apna Khaata) ke backup se compatible** — dono taraf data migrate ho sakta hai
- 🔔 **Notification** — naya transaction pakde jaane par alert, tap karke seedha confirm karo
- 🎨 Wahi passbook/ledger design — paper, ink, gold, red/green

## Build kaise karein (Step by Step)

### Zaruratein
- **Android Studio** (free): https://developer.android.com/studio — download & install (Windows/Mac/Linux)
- Internet (pehli baar dependencies download hongi, ~5 min)

### Steps
1. Is folder ko unzip karke kahin rakho (e.g. `Documents/ApnaKhaataAndroid`)
2. Android Studio kholo → **Open** → yeh folder select karo
3. Pehli baar **Gradle Sync** apne aap chalega (bottom me progress dikhega) — 3-10 min lag sakte hain
4. Sync complete hone ke baad: menu me **Build → Build Bundle(s) / APK(s) → Build APK(s)**
5. Complete hone par bottom-right me notification aayegi → **"locate"** pe click karo
6. APK milegi yahan: `app/build/outputs/apk/debug/app-debug.apk`

### Phone me Install
1. `app-debug.apk` ko WhatsApp/USB/Drive se phone me bhejo
2. Phone me file kholo → "Install unknown apps" ki permission maangega → allow karo
3. Install → Open
4. App khulte hi **SMS permission maangega → Allow karo** (yeh critical hai)
5. Settings tab → **"Scan SMS Inbox"** dabao — saare purane transactions import ho jayenge
6. Pending tab me jaake har entry ka purpose likho + category confirm karo

### Xiaomi / Oppo / Vivo / Realme users (IMPORTANT)
In phones me background SMS capture ke liye extra setting chahiye:
- **Settings → Apps → Apna Khaata → Autostart → ON**
- **Battery → No restrictions / Don't optimize**

## Project Structure
```
app/src/main/java/com/apnakhaata/app/
├── MainActivity.kt          # UI root — tabs, permissions, dialogs
├── ApnaKhaataApp.kt         # App init — notification channel, default rules
├── data/                    # Room database (transactions + rules)
├── sms/
│   ├── SmsParser.kt         # Regex engine — 14 real bank formats tested
│   ├── SmsReceiver.kt       # Live SMS capture (BroadcastReceiver)
│   ├── InboxScanner.kt      # Historical SMS import
│   └── NotificationHelper.kt
├── backup/BackupManager.kt  # JSON export/import (web-app compatible)
└── ui/                      # Jetpack Compose screens + theme
```

## Tech Stack
- Kotlin 1.9.22 · Jetpack Compose (Material 3) · Room 2.6.1 · minSdk 26 (Android 8+)
- Zero external/network dependencies at runtime — 100% offline, data sirf phone me

## Parser kaise kaam karta hai
1. SMS me `debited/paid/sent` (debit) ya `credited/received` (credit) keyword dhoondta hai
2. Amount nikaalta hai: `Rs.500`, `INR 1,234.56`, `₹500`, `500.00 debited` — sab formats
3. Merchant: pehle "to/from NAME" pattern, fir UPI VPA (`zepto@ybl` → zepto), fir sender ID fallback
4. **Skip karta hai:** OTP, promo, "will be debited" (future reminders), failed/reversed transactions
5. Duplicate SMS kabhi do baar record nahi hoga (unique hash index)
