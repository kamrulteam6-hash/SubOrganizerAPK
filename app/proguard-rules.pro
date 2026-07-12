# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keepclassmembers class com.suborganizer.android.data.model.** { *; }
-keep,includedescriptorclasses class com.suborganizer.android.**$$serializer { *; }
-keepclassmembers class com.suborganizer.android.** {
    *** Companion;
}
-keepclasseswithmembers class com.suborganizer.android.** {
    kotlinx.serialization.KSerializer serializer(...);
}
