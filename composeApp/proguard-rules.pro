-keep class androidx.datastore.preferences.core.** { *; }
-keep class androidx.datastore.preferences.** { *; }
-keep class androidx.datastore.core.** { *; }
-keep class kotlinx.coroutines.** { *; }
-keep class kotlin.jvm.internal.** { *; }

# Keep annotations that might be used by libraries for reflection
-keepattributes *Annotation*

# Keep inner classes and their members if necessary
-keepnames class *$* { *; }