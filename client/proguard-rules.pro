# Keep all public classes and methods in the tracking SDK
-keep public class com.inhose.client.** { *; }

# Keep Gson classes
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Keep model classes
-keep class com.inhose.client.models.** { *; }

# Keep OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# Keep Install Referrer
-keep class com.android.installreferrer.** { *; }