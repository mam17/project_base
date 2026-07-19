# Giữ nguyên toàn bộ public API của lib để app dùng lib không bị lỗi obfuscate
-keep public interface com.libads.core.** { *; }
-keep public class com.libads.core.AdUnit { *; }
-keep public class com.libads.core.AdType { *; }
-keep public enum com.libads.core.** { *; }

# Giữ các class implement AdProvider (adapter network sau này) để reflection/DI không lỗi
-keep class * implements com.libads.core.provider.AdProvider { *; }

# Coroutines
-keepattributes *Annotation*, InnerClasses
-dontwarn kotlinx.coroutines.**
