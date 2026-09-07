# Keep emulator cores
-keep class com.retroengine.app.** { *; }
-keep class org.libretro.** { *; }
-keepclassmembers class * {
    native <methods>;
}
-keepattributes *Annotation*