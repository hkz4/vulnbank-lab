#!/usr/bin/env python3
"""
VulnBank Backend API Server
WARNING: This server contains INTENTIONAL security vulnerabilities for educational purposes.
DO NOT use in production!
"""

import sqlite3
import jwt
import datetime
import os
from flask import Flask, request, jsonify, g
from flask_cors import CORS

app = Flask(__name__)
CORS(app)  # Allow all origins - insecure

# VULNERABILITY: Hardcoded weak secret key
SECRET_KEY = "secret123"

# VULNERABILITY: Hardcoded API keys
INTERNAL_API_KEY = "sk_live_4eC39HqLyjWDarjtT1zdp7dc"
ADMIN_API_KEY = "admin_key_do_not_share_9876543210"

DATABASE = "vulnbank.db"


def get_db():
    db = getattr(g, "_database", None)
    if db is None:
        db = g._database = sqlite3.connect(DATABASE)
        db.row_factory = sqlite3.Row
    return db


@app.teardown_appcontext
def close_connection(exception):
    db = getattr(g, "_database", None)
    if db is not None:
        db.close()


# VULNERABILITY: No security headers
@app.after_request
def after_request(response):
    # Intentionally NOT adding security headers like:
    # X-Content-Type-Options, X-Frame-Options, Content-Security-Policy, etc.
    return response


# ==================== AUTH ENDPOINTS ====================

@app.route("/api/login", methods=["POST"])
def login():
    """
    VULNERABILITY: SQL Injection - raw string concatenation
    VULNERABILITY: No rate limiting - brute force possible
    VULNERABILITY: Sensitive data exposure - returns too much info
    """
    data = request.get_json()
    username = data.get("username", "")
    password = data.get("password", "")

    db = get_db()

    # VULNERABILITY: SQL Injection via string concatenation
    query = "SELECT * FROM users WHERE username = '" + username + "' AND password = '" + password + "'"

    try:
        cursor = db.execute(query)
        user = cursor.fetchone()
    except Exception as e:
        # VULNERABILITY: Exposes internal error details
        return jsonify({"error": str(e), "query": query}), 500

    if user:
        # VULNERABILITY: Broken Authentication - no expiration, weak secret
        token = jwt.encode(
            {
                "user_id": user["id"],
                "username": user["username"],
                "role": user["role"],
                # VULNERABILITY: No exp (expiration) claim
            },
            SECRET_KEY,
            algorithm="HS256",
        )

        # VULNERABILITY: Sensitive Data Exposure - returns password hash and internal fields
        return jsonify(
            {
                "success": True,
                "token": token,
                "user_id": user["id"],
                "username": user["username"],
                "email": user["email"],
                "password": user["password"],  # VULNERABILITY: Returns plaintext password
                "role": user["role"],
                "api_key": INTERNAL_API_KEY,  # VULNERABILITY: Exposes API key
                "internal_note": "User authenticated successfully - debug info",
            }
        )
    else:
        return jsonify({"success": False, "message": "Invalid credentials"}), 401


# ==================== ACCOUNT ENDPOINTS ====================

@app.route("/api/account/<int:account_id>", methods=["GET"])
def get_account(account_id):
    """
    VULNERABILITY: IDOR - no authorization check
    Any authenticated user can access any account by changing the ID
    """
    token = request.headers.get("Authorization", "").replace("Bearer ", "")

    if not token:
        return jsonify({"error": "No token provided"}), 401

    try:
        # VULNERABILITY: Weak validation - only checks token format, not ownership
        jwt.decode(token, SECRET_KEY, algorithms=["HS256"])
    except Exception:
        return jsonify({"error": "Invalid token"}), 401

    db = get_db()

    # VULNERABILITY: IDOR - returns any account regardless of who owns it
    account = db.execute(
        "SELECT * FROM accounts WHERE id = ?", (account_id,)
    ).fetchone()

    if not account:
        return jsonify({"error": "Account not found"}), 404

    # VULNERABILITY: Sensitive Data Exposure - returns all fields including internal ones
    return jsonify(
        {
            "id": account["id"],
            "user_id": account["user_id"],
            "account_number": account["account_number"],
            "balance": account["balance"],
            "account_type": account["account_type"],
            "pin_hash": account["pin_hash"],  # VULNERABILITY: Exposes PIN hash
            "created_at": account["created_at"],
            "internal_notes": account["internal_notes"],  # VULNERABILITY: Internal data
        }
    )


@app.route("/api/transfer", methods=["POST"])
def transfer():
    """Transfer money between accounts"""
    token = request.headers.get("Authorization", "").replace("Bearer ", "")

    if not token:
        return jsonify({"error": "No token provided"}), 401

    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=["HS256"])
    except Exception:
        return jsonify({"error": "Invalid token"}), 401

    data = request.get_json()
    from_account = data.get("from_account")
    to_account = data.get("to_account")
    amount = data.get("amount")

    if not all([from_account, to_account, amount]):
        return jsonify({"error": "Missing required fields"}), 400

    db = get_db()

    # VULNERABILITY: No ownership check - can transfer from any account
    source = db.execute(
        "SELECT * FROM accounts WHERE account_number = ?", (from_account,)
    ).fetchone()

    destination = db.execute(
        "SELECT * FROM accounts WHERE account_number = ?", (to_account,)
    ).fetchone()

    if not source or not destination:
        return jsonify({"error": "Account not found"}), 404

    if source["balance"] < float(amount):
        return jsonify({"error": "Insufficient funds"}), 400

    # Perform transfer
    db.execute(
        "UPDATE accounts SET balance = balance - ? WHERE account_number = ?",
        (amount, from_account),
    )
    db.execute(
        "UPDATE accounts SET balance = balance + ? WHERE account_number = ?",
        (amount, to_account),
    )

    # Log transaction
    db.execute(
        "INSERT INTO transactions (from_account, to_account, amount, description) VALUES (?, ?, ?, ?)",
        (from_account, to_account, amount, data.get("description", "Transfer")),
    )
    db.commit()

    return jsonify(
        {
            "success": True,
            "message": f"Transferred {amount} from {from_account} to {to_account}",
            "transaction_id": "TXN" + str(datetime.datetime.now().timestamp()),
        }
    )


@app.route("/api/transactions/<int:account_id>", methods=["GET"])
def get_transactions(account_id):
    """
    VULNERABILITY: IDOR - no authorization check on account_id
    """
    token = request.headers.get("Authorization", "").replace("Bearer ", "")

    if not token:
        return jsonify({"error": "No token provided"}), 401

    try:
        jwt.decode(token, SECRET_KEY, algorithms=["HS256"])
    except Exception:
        return jsonify({"error": "Invalid token"}), 401

    db = get_db()

    # Get account number for this account_id
    account = db.execute(
        "SELECT account_number FROM accounts WHERE id = ?", (account_id,)
    ).fetchone()

    if not account:
        return jsonify({"error": "Account not found"}), 404

    # VULNERABILITY: IDOR - returns transactions for any account
    transactions = db.execute(
        """SELECT * FROM transactions
           WHERE from_account = ? OR to_account = ?
           ORDER BY created_at DESC""",
        (account["account_number"], account["account_number"]),
    ).fetchall()

    return jsonify(
        {
            "account_id": account_id,
            "transactions": [dict(t) for t in transactions],
        }
    )


# ==================== DEBUG ENDPOINT ====================

@app.route("/api/debug", methods=["GET"])
def debug_info():
    """
    VULNERABILITY: Debug endpoint exposed in production
    Exposes internal configuration, secrets, and system info
    """
    db = get_db()
    users = db.execute("SELECT * FROM users").fetchall()
    accounts = db.execute("SELECT * FROM accounts").fetchall()

    return jsonify(
        {
            "debug": True,
            "secret_key": SECRET_KEY,  # VULNERABILITY: Exposes JWT secret
            "internal_api_key": INTERNAL_API_KEY,  # VULNERABILITY: Exposes API key
            "admin_api_key": ADMIN_API_KEY,
            "database": DATABASE,
            "users": [dict(u) for u in users],  # VULNERABILITY: Dumps all users
            "accounts": [dict(a) for a in accounts],  # VULNERABILITY: Dumps all accounts
            "environment": {
                "python_path": os.environ.get("PATH", ""),
                "flask_debug": True,
                "server": "VulnBank v1.0 - DEBUG BUILD",
            },
        }
    )


# ==================== ADMIN ENDPOINT ====================

@app.route("/api/admin/users", methods=["GET"])
def admin_users():
    """
    VULNERABILITY: No proper admin authentication
    VULNERABILITY: Sensitive Data Exposure
    """
    # VULNERABILITY: Trivially bypassable admin check
    admin_key = request.headers.get("X-Admin-Key", "")
    if admin_key != ADMIN_API_KEY:
        # VULNERABILITY: Reveals expected key format in error
        return jsonify({"error": "Invalid admin key", "hint": "Check /api/debug for the key"}), 403

    db = get_db()
    users = db.execute("SELECT * FROM users").fetchall()

    return jsonify({"users": [dict(u) for u in users]})


if __name__ == "__main__":
    # VULNERABILITY: Debug mode enabled in production
    app.run(host="0.0.0.0", port=5000, debug=True)
