package com.vulnbank.app.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.File;

/**
 * RootDetector - Checks if device is rooted
 *
 * INTENTIONAL VULNERABILITIES:
 * 1. Easily bypassed root detection
 * 2. Simple file existence checks that can be hooked with Frida
 * 3. Only checks common paths (not comprehensive)
 * 4. Can be bypassed by renaming files or using Magisk Hide
 * 5. Result can be intercepted and modified at runtime
 */
public class RootDetector {

    private static final String TAG = "RootDetector";

    // VULNERABILITY: Simple list of paths - easily bypassed
    private static final String[] ROOT_PATHS = {
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su"
    };

    /**
     * VULNERABILITY: Simple root check that is easily bypassed via Frida
     * Frida hook example:
     *   Java.use("com.vulnbank.app.utils.RootDetector").isRooted.implementation = function() { return false; }
     */
    public static boolean isRooted() {
        boolean rooted = checkRootFiles() || checkBuildTags();
        Log.d(TAG, "Root check result: " + rooted);
        return rooted;
    }

    // VULNERABILITY: Only checks file existence - can be bypassed by Magisk Hide
    private static boolean checkRootFiles() {
        for (String path : ROOT_PATHS) {
            File f = new File(path);
            if (f.exists()) {
                Log.d(TAG, "Root indicator found: " + path);
                return true;
            }
        }
        return false;
    }

    // VULNERABILITY: Build tag check - easily bypassed on custom ROMs
    private static boolean checkBuildTags() {
        String buildTags = Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    /**
     * VULNERABILITY: Emulator detection that can be bypassed
     */
    public static boolean isEmulator() {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
            || Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.HARDWARE.contains("goldfish")
            || Build.HARDWARE.contains("ranchu")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.MANUFACTURER.contains("Genymotion")
            || Build.PRODUCT.contains("sdk_gphone")
            || Build.PRODUCT.contains("google_sdk");
    }
}
