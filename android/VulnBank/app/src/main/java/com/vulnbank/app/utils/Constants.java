package com.vulnbank.app.utils;

/**
 * Constants - Application constants
 *
 * INTENTIONAL VULNERABILITIES:
 * 1. API key hardcoded in source code
 * 2. Encryption key hardcoded
 * 3. Backend URL hardcoded (not using a config file)
 * 4. Hardcoded admin credentials
 * 5. Hardcoded JWT secret (matches backend)
 */
public class Constants {

    // VULNERABILITY: Hardcoded backend URL
    public static final String BASE_URL = "http://10.0.2.2:5000";

    // VULNERABILITY: Hardcoded API key - easily found via reverse engineering
    public static final String API_KEY = "sk_live_4eC39HqLyjWDarjtT1zdp7dc";

    // VULNERABILITY: Hardcoded secondary API key
    public static final String ANALYTICS_KEY = "AIzaSyD4eC39HqLyjWDarjtT1zdp7dcXXXXXXXX";

    // VULNERABILITY: Hardcoded encryption key
    public static final String ENCRYPTION_KEY = "MyS3cr3tK3y@2024";

    // VULNERABILITY: Hardcoded JWT secret (same as backend)
    public static final String JWT_SECRET = "secret123";

    // VULNERABILITY: Hardcoded admin credentials
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin123";

    // VULNERABILITY: Hardcoded PIN
    public static final String DEFAULT_PIN = "1234";

    // App constants
    public static final String PREFS_NAME = "VulnBankPrefs";
    public static final String TOKEN_KEY = "auth_token";
    public static final String USER_ID_KEY = "user_id";
    public static final String USERNAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";

    // Database constants (for reference)
    public static final String DB_VERSION = "1.0";
    public static final String DB_SECRET = "vulnbank_db_secret_2024";
}
