package com.vulnbank.app.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * UserContentProvider - Exposes user data via Content Provider
 *
 * INTENTIONAL VULNERABILITIES:
 * 1. Exported without any permission (android:exported="true")
 * 2. Returns sensitive user data to any calling app
 * 3. Returns credentials stored in SharedPreferences
 * 4. No authentication or authorization checks
 *
 * Exploit via ADB:
 * adb shell content query --uri content://com.vulnbank.app.provider/users
 * adb shell content query --uri content://com.vulnbank.app.provider/credentials
 */
public class UserContentProvider extends ContentProvider {

    private static final String TAG = "UserContentProvider";
    public static final String AUTHORITY = "com.vulnbank.app.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    private static final int USERS = 1;
    private static final int CREDENTIALS = 2;
    private static final int TOKENS = 3;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, "users", USERS);
        uriMatcher.addURI(AUTHORITY, "credentials", CREDENTIALS);
        uriMatcher.addURI(AUTHORITY, "tokens", TOKENS);
    }

    @Override
    public boolean onCreate() {
        Log.d(TAG, "UserContentProvider initialized");
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {

        // VULNERABILITY: No permission check - any app can query
        Log.d(TAG, "Query received for URI: " + uri);

        SharedPreferences prefs = getContext().getSharedPreferences("VulnBankPrefs",
                android.content.Context.MODE_PRIVATE);

        int match = uriMatcher.match(uri);
        switch (match) {
            case USERS: {
                // VULNERABILITY: Returns user info
                MatrixCursor cursor = new MatrixCursor(
                    new String[]{"username", "email", "role", "user_id"});
                String username = prefs.getString("username", "");
                int userId = prefs.getInt("user_id", 0);
                cursor.addRow(new Object[]{username, username + "@vulnbank.com", "user", userId});
                return cursor;
            }
            case CREDENTIALS: {
                // VULNERABILITY: Returns plaintext credentials
                MatrixCursor cursor = new MatrixCursor(
                    new String[]{"username", "password", "token"});
                String username = prefs.getString("username", "");
                String password = prefs.getString("password", "");
                String token = prefs.getString("auth_token", "");
                cursor.addRow(new Object[]{username, password, token});
                Log.d(TAG, "Returning credentials: " + username + ":" + password);
                return cursor;
            }
            case TOKENS: {
                // VULNERABILITY: Returns auth token
                MatrixCursor cursor = new MatrixCursor(new String[]{"token", "user_id"});
                String token = prefs.getString("auth_token", "");
                int userId = prefs.getInt("user_id", 0);
                cursor.addRow(new Object[]{token, userId});
                return cursor;
            }
            default:
                // VULNERABILITY: Returns all data on unknown URI
                MatrixCursor cursor = new MatrixCursor(
                    new String[]{"username", "password", "token", "user_id"});
                cursor.addRow(new Object[]{
                    prefs.getString("username", ""),
                    prefs.getString("password", ""),
                    prefs.getString("auth_token", ""),
                    prefs.getInt("user_id", 0)
                });
                return cursor;
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "vnd.android.cursor.dir/vnd.vulnbank.data";
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
