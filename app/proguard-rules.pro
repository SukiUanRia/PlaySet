# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Android\sdk\tools\proguard\proguard-android-optimize.txt
# You can remove the previous line and specify your own optimization flags.
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name of your Application class.
#-keepclassmembers class fqcn.of.your.application.Application {
#    <init>(...);
#}

# You can add more rules here if you have specific needs.

# Gson specific rules to preserve generic type information
# 解决 TypeToken 泛型信息丢失问题
-keepattributes Signature
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken

# Preserve the AppInfo class and its members for Gson serialization/deserialization
# 确保 AppInfo 类及其成员不被混淆或移除，因为 Gson 需要反射访问它们
-keep class org.maria.playset.MainActivity$AppInfo { *; }
-keepclassmembers class org.maria.playset.MainActivity$AppInfo {
    <init>(...);
}

# General rules for AndroidX and Material components (usually handled by default, but good to ensure)
-dontwarn android.support.**
-dontwarn androidx.**
-dontwarn com.google.android.material.**
-keep class androidx.** { *; }
-keep class com.google.android.material.** { *; }

# Keep all classes that are annotated with @Keep
-keepclasseswithmembers public class * {
    @androidx.annotation.Keep <methods>;
}
-keepclasseswithmembers public class * {
    @androidx.annotation.Keep <fields>;
}

# For classes that might be accessed by reflection, keep them
# Example: if you use reflection to access certain methods/fields
# -keep class your.package.name.YourClass { *; }
