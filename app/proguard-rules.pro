-ignorewarnings

-keep class * extends com.raizlabs.android.dbflow.config.DatabaseHolder { *; }
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

-keepclassmembers class * extends java.lang.Enum {
    public *;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}