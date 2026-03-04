@echo off
echo ============================================
echo       VulnBank Lab - Setup Script
echo       FOR EDUCATIONAL PURPOSES ONLY
echo ============================================
echo.

echo [*] Setting up VulnBank Lab...
echo.

REM Check Python is installed
python --version >nul 2>&1
if errorlevel 1 (
    echo [!] Python not found! Please install Python 3.x from https://www.python.org
    pause
    exit /b 1
)

echo [*] Installing Python dependencies...
cd backend
pip install -r requirements.txt
if errorlevel 1 (
    echo [!] Failed to install dependencies
    pause
    exit /b 1
)

echo [*] Initializing database...
python init_db.py
if errorlevel 1 (
    echo [!] Failed to initialize database
    pause
    exit /b 1
)

echo.
echo [*] Starting backend server...
start cmd /k "python app.py"
cd ..

echo.
echo ============================================
echo [*] Backend running on http://localhost:5000
echo.
echo [*] Available endpoints:
echo     POST http://localhost:5000/api/login
echo     GET  http://localhost:5000/api/account/[id]
echo     POST http://localhost:5000/api/transfer
echo     GET  http://localhost:5000/api/transactions/[id]
echo     GET  http://localhost:5000/api/debug
echo.
echo [*] Sample credentials:
echo     admin / admin123
echo     user1 / password1
echo     user2 / password2
echo.
echo [*] Next steps:
echo     1. Open android/VulnBank in Android Studio
echo     2. Build and run the APK on an emulator
echo     3. Configure Burp Suite proxy on port 8080
echo     4. See PENTEST_GUIDE.md for detailed instructions
echo ============================================
pause
