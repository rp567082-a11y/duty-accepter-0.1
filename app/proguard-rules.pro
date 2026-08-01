# Add project specific ProGuard rules here.

# Retain line numbers and source file attributes for stack trace reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Retain annotations and generic signatures for reflection/deserialization
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep Room entities, DAOs, and database classes
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep @androidx.room.Entity class * { *; }
-keepclassmembers class * {
    @androidx.room.Dao *;
}

# Keep Data Models & Moshi Codegen
-keep class com.example.data.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.* <fields>;
    @com.squareup.moshi.* <methods>;
}

# Keep Jetpack Compose UI state and components
-keep class androidx.compose.** { *; }

# Keep Firebase and Google Play Services models
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep Retrofit and OkHttp dependencies
-dontwarn okhttp3.**
-dontwarn retrofit2.**

# Keep Accessibility Service
-keep class com.example.service.DutyAccepterService { *; }

# Repackage obfuscated classes into root package for enhanced reverse engineering protection
-repackageclasses ''
-allowaccessmodification

