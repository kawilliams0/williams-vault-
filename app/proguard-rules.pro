# Android & Kotlin Coroutines
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * extends java.lang.Enum {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Room Database & SQLite
-keep class androidx.room.** { *; }
-dontwarn androidx.room.paging.**
-keep class * extends androidx.room.RoomDatabase
-keep class com.example.data.db.** { *; }
-keep class com.example.data.model.** { *; }
-keepclassmembers class * {
    @androidx.room.Dao *;
    @androidx.room.Entity *;
    @androidx.room.Query *;
    @androidx.room.Insert *;
    @androidx.room.Delete *;
    @androidx.room.Update *;
}

# Jetpack Compose & ViewModel
-keep class androidx.compose.** { *; }
-keep class com.example.ui.** { *; }
-keepclassmembers class com.example.ui.FinanceViewModel { *; }

# Serialization & Reflection support
-dontwarn java.lang.instrument.**
-dontwarn sun.misc.**
