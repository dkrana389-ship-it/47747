# Hilt
-keep class dagger.hilt.** { *; }
-keep class com.keepfocus.app.** { *; }
-dontwarn dagger.hilt.**

# Room
-keepclassmembers class * extends androidx.room.RoomDatabase {
    *;
}
-keep class * extends androidx.room.RoomDatabase

# AccessibilityService
-keep public class com.keepfocus.app.service.FocusAccessibilityService { *; }

# DataStore
-keep class androidx.datastore.preferences.** { *; }
