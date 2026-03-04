# Add project specific ProGuard rules here.
# VULNERABILITY: ProGuard is disabled in release builds (minifyEnabled false)
# This makes reverse engineering easier
-keep class com.vulnbank.app.** { *; }
