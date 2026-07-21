# QR Forge ProGuard Rules

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Room
-keep class com.qrforge.database.entity.** { *; }

# QRCode-Kotlin
-keep class io.github.g0dkar.qrcodekotlin.** { *; }

# Kotlin Serialization (if used)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
