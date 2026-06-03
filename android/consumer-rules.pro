# react-native-nitro-google-signin
# Nitro Hybrid Objects and generated bindings are created from C++/JNI and cannot be traced by R8.

-keep class com.margelo.nitro.nitrogooglesignin.** { *; }
-keep class com.nitrogooglesignin.** { *; }

# HybridObject subclasses (Nitro Modules)
-keep class * extends com.margelo.nitro.core.HybridObject { *; }
-keep class * implements com.margelo.nitro.HybridObject { *; }

-dontwarn com.margelo.nitro.**
