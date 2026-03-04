#!/bin/bash
echo "============================================"
echo "      VulnBank Lab - Setup Script"
echo "      FOR EDUCATIONAL PURPOSES ONLY"
echo "============================================"
echo ""

echo "[*] Setting up VulnBank Lab..."
echo ""

# Check Python is installed
if ! command -v python3 &> /dev/null; then
    echo "[!] Python3 not found! Please install Python 3.x"
    exit 1
fi

echo "[*] Installing Python dependencies..."
cd backend
pip3 install -r requirements.txt
if [ $? -ne 0 ]; then
    echo "[!] Failed to install dependencies"
    exit 1
fi

echo "[*] Initializing database..."
python3 init_db.py
if [ $? -ne 0 ]; then
    echo "[!] Failed to initialize database"
    exit 1
fi

echo ""
echo "[*] Starting backend server..."
python3 app.py &
BACKEND_PID=$!
cd ..

echo ""
echo "============================================"
echo "[*] Backend running on http://localhost:5000"
echo "    PID: $BACKEND_PID"
echo ""
echo "[*] Available endpoints:"
echo "    POST http://localhost:5000/api/login"
echo "    GET  http://localhost:5000/api/account/[id]"
echo "    POST http://localhost:5000/api/transfer"
echo "    GET  http://localhost:5000/api/transactions/[id]"
echo "    GET  http://localhost:5000/api/debug"
echo ""
echo "[*] Sample credentials:"
echo "    admin / admin123"
echo "    user1 / password1"
echo "    user2 / password2"
echo ""
echo "[*] Next steps:"
echo "    1. Open android/VulnBank in Android Studio"
echo "    2. Build and run the APK on an emulator"
echo "    3. Configure Burp Suite proxy on port 8080"
echo "    4. See PENTEST_GUIDE.md for detailed instructions"
echo "============================================"
echo ""
echo "Press Ctrl+C to stop the backend server"
wait $BACKEND_PID
