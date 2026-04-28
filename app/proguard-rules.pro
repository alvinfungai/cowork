# Fix for play-services-location R8 warning
-keep class com.google.android.gms.internal.location.** { *; }
-dontwarn com.google.android.gms.internal.location.**
