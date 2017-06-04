-ignorewarnings

-keep class * extends com.raizlabs.android.dbflow.config.DatabaseHolder { *; }
-keepattributes *Annotation*
#EventBus
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
#Enums
-keepclassmembers class * extends java.lang.Enum {
    public *;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
#Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**