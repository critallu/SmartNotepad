# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.smartnotepad.model.** { *; }
-keep class com.google.gson.** { *; }

# Kotlin
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
