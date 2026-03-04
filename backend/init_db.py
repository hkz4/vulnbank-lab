#!/usr/bin/env python3
"""
Initialize VulnBank SQLite database with sample data.
WARNING: Contains intentional vulnerabilities for educational purposes.
"""

import sqlite3
import os

DATABASE = "vulnbank.db"


def init_db():
    if os.path.exists(DATABASE):
        os.remove(DATABASE)
        print(f"[*] Removed existing database: {DATABASE}")

    conn = sqlite3.connect(DATABASE)
    cursor = conn.cursor()

    # Create users table
    # VULNERABILITY: Passwords stored in plaintext
    cursor.execute("""
        CREATE TABLE users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT NOT NULL UNIQUE,
            password TEXT NOT NULL,
            email TEXT,
            role TEXT DEFAULT 'user',
            full_name TEXT,
            phone TEXT,
            ssn TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    """)

    # Create accounts table
    cursor.execute("""
        CREATE TABLE accounts (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER,
            account_number TEXT UNIQUE,
            balance REAL DEFAULT 0.0,
            account_type TEXT DEFAULT 'checking',
            pin_hash TEXT,
            internal_notes TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (user_id) REFERENCES users(id)
        )
    """)

    # Create transactions table
    cursor.execute("""
        CREATE TABLE transactions (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            from_account TEXT,
            to_account TEXT,
            amount REAL,
            description TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    """)

    # Insert sample users
    # VULNERABILITY: Plaintext passwords, weak passwords, PII exposed
    users = [
        ("admin", "admin123", "admin@vulnbank.com", "admin", "Administrator", "+1-555-0001", "123-45-6789"),
        ("user1", "password1", "alice@example.com", "user", "Alice Johnson", "+1-555-0002", "987-65-4321"),
        ("user2", "password2", "bob@example.com", "user", "Bob Smith", "+1-555-0003", "456-78-9012"),
        ("john", "john1234", "john@example.com", "user", "John Doe", "+1-555-0004", "321-54-9876"),
    ]

    cursor.executemany(
        "INSERT INTO users (username, password, email, role, full_name, phone, ssn) VALUES (?, ?, ?, ?, ?, ?, ?)",
        users,
    )

    # Insert sample accounts
    accounts = [
        (1, "ACC-001-ADMIN", 999999.99, "checking", "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", "Admin master account - DO NOT TOUCH"),
        (2, "ACC-002-USER1", 5000.00, "checking", "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", "Alice personal account"),
        (2, "ACC-003-USER1", 12500.50, "savings", "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", "Alice savings account"),
        (3, "ACC-004-USER2", 3200.75, "checking", "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", "Bob personal account"),
        (4, "ACC-005-JOHN", 1500.00, "checking", "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", "John personal account"),
    ]

    cursor.executemany(
        "INSERT INTO accounts (user_id, account_number, balance, account_type, pin_hash, internal_notes) VALUES (?, ?, ?, ?, ?, ?)",
        accounts,
    )

    # Insert sample transactions
    transactions = [
        ("ACC-001-ADMIN", "ACC-002-USER1", 500.00, "Welcome bonus"),
        ("ACC-002-USER1", "ACC-004-USER2", 100.00, "Payment for lunch"),
        ("ACC-004-USER2", "ACC-002-USER1", 50.00, "Refund"),
        ("ACC-001-ADMIN", "ACC-005-JOHN", 200.00, "Referral bonus"),
        ("ACC-002-USER1", "ACC-003-USER1", 1000.00, "Savings transfer"),
        ("ACC-003-USER1", "ACC-002-USER1", 250.00, "Withdrawal"),
    ]

    cursor.executemany(
        "INSERT INTO transactions (from_account, to_account, amount, description) VALUES (?, ?, ?, ?)",
        transactions,
    )

    conn.commit()
    conn.close()

    print("[*] Database initialized successfully!")
    print("[*] Sample users created:")
    print("    - admin / admin123 (role: admin)")
    print("    - user1 / password1 (role: user)")
    print("    - user2 / password2 (role: user)")
    print("    - john / john1234 (role: user)")
    print("[*] Sample accounts and transactions created.")


if __name__ == "__main__":
    init_db()
