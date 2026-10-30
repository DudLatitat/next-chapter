# kotlinx.serialization: keep generated serializers for the app's and core's models.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers @kotlinx.serialization.Serializable class com.dudlatitat.nextchapter.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.dudlatitat.nextchapter.**$$serializer { *; }
-keepclasseswithmembers class com.dudlatitat.nextchapter.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Shrink and optimise, but keep the real names: crash reports from phones stay readable
# without a mapping file.
-dontobfuscate
-keepattributes SourceFile,LineNumberTable
