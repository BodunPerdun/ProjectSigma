# Keep Kotlin Serialization DTO Models
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keepclassmembers class * {
    *** Companion;
}
-keepclassmembers class * {
    *** INSTANCE;
}

# Keep LocaPop DTO Models
-keep class com.example.projectsigma.data.remote.dto.** { *; }
-keep class com.example.projectsigma.model.** { *; }

# Keep Ktor Client & OkHttp Classes
-keep class io.ktor.client.** { *; }
-keep class okhttp3.** { *; }
-keep class org.maplibre.android.** { *; }

# Keep Room DB Entities
-keep class com.example.projectsigma.data.local.** { *; }
