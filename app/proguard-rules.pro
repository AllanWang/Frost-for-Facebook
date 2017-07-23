-ignorewarnings
-dontwarn kotlin.**
-keep class * extends com.raizlabs.android.dbflow.config.DatabaseHolder { *; }
-keepattributes *Annotation*
# Enums
#-keepclassmembers class * extends java.lang.Enum {
#    public *;
#    public static **[] values();
#    public static ** valueOf(java.lang.String);
#}
# Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**
# JavaScript Interface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes JavascriptInterface
# Jsoup
-keeppackagenames org.jsoup.nodes
# IAB
-keep class com.android.vending.billing.**
# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}