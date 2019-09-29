-ignorewarnings
-dontwarn kotlin.**
-keepattributes *Annotation*
# Enums
#-keepclassmembers class * extends java.lang.Enum {
#    public *;
#    public static **[] values();
#    public static ** valueOf(java.lang.String);
#}
# JavaScript Interface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes JavascriptInterface
# Jsoup
-keeppackagenames org.jsoup.nodes
# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}