# Vidora - release shrinker rules

# Keep the JS bridge if a WebView interface is ever added
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes JavascriptInterface

# WebView / IFrame player relies on reflection inside the platform only,
# so nothing app-specific needs keeping beyond the bridge above.

# Keep annotations used by Compose at runtime
-keepattributes *Annotation*
-keepattributes Signature
-dontwarn org.jetbrains.annotations.**

# Coil
-dontwarn okhttp3.**
-dontwarn okio.**
