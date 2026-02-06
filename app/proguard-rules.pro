# Preserve line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Gson serialized models — keep fields with @SerializedName
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep all Gson-deserialized data classes (they use reflection)
-keep class io.beachforecast.ServerWeatherResponse { *; }
-keep class io.beachforecast.MarineWeatherResponse { *; }
-keep class io.beachforecast.MarineHourlyData { *; }
-keep class io.beachforecast.MarineHourlyUnits { *; }
-keep class io.beachforecast.WeatherResponse { *; }
-keep class io.beachforecast.WeatherHourlyData { *; }
-keep class io.beachforecast.WeatherHourlyUnits { *; }

# Gson internals
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Vico charts library
-dontwarn com.patrykandpatrick.vico.**
-keep class com.patrykandpatrick.vico.** { *; }

# Kotlin coroutines
-dontwarn kotlinx.coroutines.**

# Keep enum values (used by Gson and throughout the app)
-keepclassmembers enum * { *; }

# Timber
-dontwarn org.jetbrains.annotations.**

# Firebase Crashlytics
-keepattributes *Annotation*
