# ProGuard 规则
# 保持模型类
-keep class com.example.hawidget.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# AndroidX
-keep class androidx.** { *; }
-dontwarn androidx.**