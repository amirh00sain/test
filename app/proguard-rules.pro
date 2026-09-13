-keep class com.spider.vpn.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static ** INSTANCE;
}
-dontwarn kotlinx.coroutines.**
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
