plugins {
    alias(libs.plugins.android.application) apply false
    id("com.android.library") version libs.versions.agp.get() apply false
    id("org.jetbrains.kotlin.android") version libs.versions.kotlin.get() apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version libs.versions.kotlin.get() apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
    id("com.google.firebase.crashlytics") version "3.0.7" apply false
}
