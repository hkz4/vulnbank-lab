# VulnBank Lab - Vulnerable Android Banking App

> ⚠️ **DISCLAIMER**: This project contains **intentional security vulnerabilities** for educational purposes only. Do NOT deploy on production systems or real networks. Use only in isolated lab environments. The author is not responsible for any misuse.

## 📋 Description

VulnBank is a deliberately vulnerable Android banking application designed for Android penetration testing practice. It includes a vulnerable Flask backend API and an Android app packed with common real-world vulnerabilities.

**Target audience**: Legal pentesters, security researchers, students learning Android security.

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┐
│                   Windows Machine                    │
│                                                     │
│  ┌──────────────┐    ┌──────────────────────────┐   │
│  │  Burp Suite  │    │    Android Studio         │   │
│  │  (port 8080) │    │    (AVD Emulator)         │   │
│  └──────┬───────┘    └──────────┬───────────────┘   │
│         │                       │                    │
│         │    ┌──────────────────┘                    │
│         │    │  VulnBank APK                         │
│         ▼    ▼                                       │
│  ┌────────────────────┐                              │
│  │  Python Flask API  │                              │
│  │  localhost:5000    │                              │
│  │  (SQLite DB)       │                              │
│  └────────────────────┘                              │
└─────────────────────────────────────────────────────┘
```

## 🐛 Vulnerabilities Included

### Backend (Flask API)
| # | Vulnerability | Endpoint |
|---|---------------|----------|
| 1 | SQL Injection | `POST /api/login` |
| 2 | Broken Authentication (no token expiry, weak secret) | All endpoints |
| 3 | IDOR | `GET /api/account/<id>` |
| 4 | Sensitive Data Exposure | Login response, debug endpoint |
| 5 | Missing Rate Limiting (Brute Force) | `POST /api/login` |
| 6 | Hardcoded API Keys | All responses |
| 7 | Missing Security Headers | All responses |
| 8 | Debug Endpoint in Production | `GET /api/debug` |

### Android App
| # | Vulnerability | Location |
|---|---------------|----------|
| 1 | Insecure Data Storage (SharedPreferences) | `LoginActivity.java` |
| 2 | Sensitive Data in Logs | `LoginActivity.java`, `DashboardActivity.java` |
| 3 | Hardcoded Secrets & API Keys | `Constants.java`, `strings.xml` |
| 4 | Cleartext HTTP Traffic | `network_security_config.xml` |
| 5 | Trust All Certificates | `NetworkHelper.java` |
| 6 | Exported Activities (no permissions) | `AndroidManifest.xml` |
| 7 | Exported Content Provider (data leak) | `UserContentProvider.java` |
| 8 | Exported Broadcast Receiver | `TransactionReceiver.java` |
| 9 | `android:debuggable="true"` | `AndroidManifest.xml` |
| 10 | `android:allowBackup="true"` | `AndroidManifest.xml` |
| 11 | Weak Encryption (DES, ECB mode) | `CryptoHelper.java` |
| 12 | Hardcoded IV and Keys | `CryptoHelper.java` |
| 13 | Client-Side PIN Check | `PinActivity.java` |
| 14 | Bypassable Root Detection | `RootDetector.java` |
| 15 | Vulnerable WebView (JS enabled, file access) | `WebViewActivity.java` |
| 16 | JavaScript Interface Exposes Sensitive Data | `WebViewActivity.java` |

## 🛠️ Prerequisites

- **Windows** (or Linux/Mac)
- **Python 3.x** - [Download](https://www.python.org/downloads/)
- **Android Studio** - [Download](https://developer.android.com/studio)
- **Burp Suite Community/Pro** - [Download](https://portswigger.net/burp)
- **Java 8+** (included with Android Studio)

## 🚀 Quick Start

### Option 1: Windows (Recommended)
```batch
setup.bat
```

### Option 2: Manual Setup

**Start Backend:**
```bash
cd backend
pip install -r requirements.txt
python init_db.py
python app.py
```

**Build Android App:**
1. Open `android/VulnBank/` in Android Studio
2. Wait for Gradle sync to complete
3. Create/start an Android emulator (API 24+)
4. Click Run to build and install

### Option 3: Docker
```bash
cd backend
docker build -t vulnbank-backend .
docker run -p 5000:5000 vulnbank-backend
```

## 👤 Sample Credentials

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | admin |
| user1 | password1 | user |
| user2 | password2 | user |
| john | john1234 | user |

## 📖 Pentest Guide

See **[PENTEST_GUIDE.md](PENTEST_GUIDE.md)** for detailed step-by-step exercises covering all vulnerabilities (in Vietnamese).

## 📁 Project Structure

```
vulnbank-lab/
├── backend/
│   ├── app.py              # Flask API server (vulnerable)
│   ├── init_db.py          # Database initialization
│   ├── requirements.txt    # Python dependencies
│   └── Dockerfile          # Docker support
├── android/VulnBank/
│   ├── app/src/main/
│   │   ├── java/com/vulnbank/app/
│   │   │   ├── LoginActivity.java
│   │   │   ├── DashboardActivity.java
│   │   │   ├── TransferActivity.java
│   │   │   ├── WebViewActivity.java
│   │   │   ├── PinActivity.java
│   │   │   ├── providers/UserContentProvider.java
│   │   │   ├── utils/ (CryptoHelper, Constants, NetworkHelper, RootDetector)
│   │   │   └── receivers/TransactionReceiver.java
│   │   ├── res/ (layouts, values, xml configs)
│   │   └── AndroidManifest.xml
│   └── build.gradle files
├── PENTEST_GUIDE.md        # Vietnamese pentest guide
├── setup.bat               # Windows quick start
└── setup.sh                # Linux/Mac quick start
```

## ⚖️ Legal Notice

This software is provided for **educational and authorized security testing purposes only**. You must have explicit permission to test any system. Unauthorized testing is illegal. The authors assume no liability for misuse.
