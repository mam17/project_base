import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.example.myapplication"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["ad_app_id"] = "ca-app-pub-3940256099942544~3347511713"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders["ad_app_id"] = "ca-app-pub-3940256099942544~3347511713"
            buildConfigField("String", "inter_splash", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "inter_splash_2f", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "inter_home", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "inter_home_2f", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "inter_back", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "native_fs_splash", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_fs_splash_2f", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_fs_splash_2", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_fs_splash_2_2f", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_fs_1", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_ob_1", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_ob_2", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_ob_3", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_fs_1_2f", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_fs_2", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_fs_2_2f", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_language_1_1", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_language_1_2", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_feature", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "inter_feature", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "appopen_resume", "\"ca-app-pub-3940256099942544/9257395921\"")
            buildConfigField("String", "banner_splash", "\"ca-app-pub-3940256099942544/2014213617\"")
            buildConfigField("String", "banner_home", "\"ca-app-pub-3940256099942544/2014213617\"")
            buildConfigField("String", "banner_home_2f", "\"ca-app-pub-3940256099942544/2014213617\"")
            buildConfigField("String", "native_language_2_1", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_language_2_2", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "reward_feature", "\"ca-app-pub-3940256099942544/5224354917\"")
            buildConfigField("String", "reward_feature_2f", "\"ca-app-pub-3940256099942544/5224354917\"")
            buildConfigField("String", "native_full_screen_1", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_full_screen_1_2f", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_full_screen_2", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_full_screen_2_2f", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_home", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_home_2f", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_gen", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_gen_2f", "\"ca-app-pub-3940256099942544/2247696110\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    //noinspection WrongGradleMethod
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Dagger Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.lifecycle.process)
    ksp(libs.hilt.compiler)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.room.rxjava3)
    ksp(libs.room.compiler)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.kotlinx.coroutines.android)

    // RxJava
    implementation(libs.rxjava)
    implementation(libs.rxandroid)

    // Gson
    implementation(libs.gson)

    // Glide
    implementation(libs.glide)
    ksp(libs.glide.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //shimmer
    implementation(libs.shimmer)

    //Dot
    implementation(libs.dotsindicator)

    //Firebase
    implementation("com.google.firebase:firebase-config-ktx:22.1.2")
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    implementation("com.google.firebase:firebase-appcheck-debug")

    //AdMob
    implementation("com.google.android.gms:play-services-ads:25.2.0")

    //UMP
    implementation("com.google.android.ump:user-messaging-platform:4.0.0")

    //Facebook SDK
    implementation("com.facebook.android:facebook-android-sdk:18.1.3")

    //Mediation admob
    implementation("com.google.ads.mediation:facebook:6.20.0.0")
    implementation("com.google.ads.mediation:applovin:13.3.1.0")
    implementation("com.google.ads.mediation:vungle:7.5.0.0")
    implementation("com.google.ads.mediation:pangle:7.9.1.3.0")
    implementation("com.google.ads.mediation:mintegral:16.9.71.0")
    implementation("com.google.ads.mediation:inmobi:10.6.1.0")
    implementation("com.google.ads.mediation:ironsource:8.2.0.0")

    //Adjust
    implementation("com.adjust.sdk:adjust-android:5.4.6")

    //Appsflyer
    implementation("com.appsflyer:af-android-sdk:6.17.0")
    implementation("com.android.installreferrer:installreferrer:2.2")
    implementation("com.google.android.gms:play-services-appset:16.1.0")

    //Koin for dependency injection
    implementation("io.insert-koin:koin-android:4.2.1")
}