# 🏦 VulnBank Lab — Android Blackbox Pentest Lab

> ⚠️ **DISCLAIMER**: Dự án này chỉ dùng cho mục đích **học tập / nghiên cứu bảo mật hợp pháp**. Không áp dụng lên ứng dụng thực tế khi chưa được cấp phép.

---

## Giới thiệu

**VulnBank** là một ứng dụng Android ngân hàng **cố tình chứa nhiều lỗ hổng bảo mật** để phục vụ thực hành **Blackbox Penetration Testing**.

Lab bao gồm:
- 📱 **Android App** — chứa 10+ lỗ hổng phổ biến (OWASP Mobile Top 10)
- 🖥️ **Backend API** (Node.js) — chứa IDOR, JWT None, No Auth, Debug endpoint...
- 📖 **Hướng dẫn pentest từng bước trên Windows** với Burp Suite

---

## 🚀 Quick Start (3 bước)

### Bước 1 — Chạy Backend
```powershell
cd backend
npm install
node server.js
# Server chạy tại http://localhost:3000
```

### Bước 2 — Build & Cài App
```powershell
# Mở android-app/ bằng Android Studio
# Build > Build Bundle(s)/APK(s) > Build APK(s)
adb install app\build\outputs\apk\debug\app-debug.apk
```

### Bước 3 — Pentest!
Xem hướng dẫn chi tiết trong thư mục `pentest-guide/`

---

## 🔑 Tài Khoản Lab

| Username | Password | Role | Balance |
|---|---|---|---|
| admin | Admin@123 | admin | 99,999 |
| alice | alice123 | user | 5,000 |
| bob | bob456 | user | 3,000 |
| superadmin | Sup3r$ecret! | backdoor | - |

---

## 🕳️ Danh Sách Lỗ Hổng

| # | Lỗ hổng | Severity | OWASP |
|---|---|---|---|
| 1 | Hardcoded Credentials trong APK | 🔴 Critical | M1 |
| 2 | Exported AdminActivity (Bypass Auth) | 🔴 Critical | M4 |
| 3 | Content Provider dump toàn bộ DB | 🔴 Critical | M4 |
| 4 | JWT Algorithm None (Token Forgery) | 🔴 Critical | M4 |
| 5 | Debug API Endpoint lộ secrets | 🔴 Critical | M2 |
| 6 | IDOR — /api/balance, /api/profile | 🟠 High | M4 |
| 7 | Sensitive Data in Logcat | 🟠 High | M2 |
| 8 | Password stored plaintext (SharedPrefs) | 🟠 High | M2 |
| 9 | WebView JS Interface Token Theft | 🟠 High | M6 |
| 10 | Password Reset không cần xác thực | 🟠 High | M4 |
| 11 | Exported BroadcastReceiver — Priv Esc | 🟡 Medium | M4 |
| 12 | Negative Transfer Amount | 🟡 Medium | M4 |
| 13 | Token trong URL Parameter | 🟡 Medium | M5 |
| 14 | No Rate Limiting (Brute Force) | 🟡 Medium | M4 |
| 15 | Debuggable APK + allowBackup | 🟢 Low | M7 |

---

## 📂 Cấu Trúc Project

```
vulnbank-lab/
├── README.md
├── backend/                    # Node.js API server
│   ├── package.json
│   └── server.js
├── android-app/                # Android Studio project
│   ├── build.gradle
│   ├── settings.gradle
│   └── app/
│       ├── build.gradle
│       └── src/main/
│           ├── AndroidManifest.xml
│           ├── java/com/vulnlab/vulnbank/
│           └── res/
└── pentest-guide/              # Hướng dẫn blackbox pentest
    ├── 00_CHECKLIST.md
    ├── 01_setup_windows.md
    ├── 02_recon.md
    ├── 03_static_analysis.md
    ├── 04_burp_setup_windows.md
    ├── 05_exported_components.md
    ├── 06_api_attacks.md
    ├── 07_data_storage.md
    ├── 08_webview_attack.md
    └── frida/
        ├── hook_login.js
        └── hook_webview.js
``` 

---

## 📖 Hướng Dẫn

| File | Nội dung |
|---|---|
| [00_CHECKLIST](pentest-guide/00_CHECKLIST.md) | Checklist toàn bộ lỗ hổng + PoC |
| [01_setup_windows](pentest-guide/01_setup_windows.md) | Setup môi trường trên Windows |
| [02_recon](pentest-guide/02_recon.md) | Recon — thu thập thông tin |
| [03_static_analysis](pentest-guide/03_static_analysis.md) | Static analysis với jadx |
| [04_burp_setup_windows](pentest-guide/04_burp_setup_windows.md) | Setup Burp Suite trên Windows |
| [05_exported_components](pentest-guide/05_exported_components.md) | Khai thác exported components |
| [06_api_attacks](pentest-guide/06_api_attacks.md) | Tấn công API với Burp Suite |
| [07_data_storage](pentest-guide/07_data_storage.md) | Phân tích data storage |
| [08_webview_attack](pentest-guide/08_webview_attack.md) | Tấn công WebView |