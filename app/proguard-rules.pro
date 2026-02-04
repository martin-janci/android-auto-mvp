# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /sdk/tools/proguard/proguard-android.txt

# Keep Room entities
-keep class com.example.calltasks.data.local.** { *; }

# Keep Koin
-keep class org.koin.** { *; }

# Keep OpenAI client models
-keep class com.aallam.openai.api.** { *; }
