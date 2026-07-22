import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

configurations.all {
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-android-extensions-runtime")
}

android {
    namespace = "com.example.myapplication"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.longdt.ads"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("key/key_app_base")
            storePassword = "123456"
            keyAlias = "key0"
            keyPassword = "123456"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders["ad_app_id"] = "ca-app-pub-3940256099942544~3347511713"
        }
        debug {

            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            manifestPlaceholders["ad_app_id"] = "ca-app-pub-3940256099942544~3347511713"
            buildConfigField("String", "inter_test", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "native_test", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "reward_test", "\"ca-app-pub-3940256099942544/5224354917\"")
            buildConfigField("String", "reward_inter_test", "\"ca-app-pub-3940256099942544/5354046379\"")
            buildConfigField("String", "appopen_resume_test", "\"ca-app-pub-3940256099942544/9257395921\"")
            buildConfigField("String", "banner_test", "\"ca-app-pub-3940256099942544/2014213617\"")

        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    //noinspection WrongGradleMethod
    kotlin {
        jvmToolchain(17)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}
val formattedDate: String? = SimpleDateFormat("MM.dd.yyyy").format(Date())
base {
    archivesName.set(
        "SMS_WA_v${android.defaultConfig.versionName}(${android.defaultConfig.versionCode})_${formattedDate}"
    )
}
dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Dagger Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.room.rxjava3)
    ksp(libs.room.compiler)

    // Lifecycle
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.extensions)

    // Work
    implementation(libs.androidx.work.runtime.ktx)

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

    // Shimmer
    implementation(libs.shimmer)

    // Dot
    implementation(libs.dotsindicator)

    // Lottie load gif
    implementation(libs.lottie)

    // Rating
    implementation(libs.andratingbar)

    // Retrofit + OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.firestore)

    implementation(project(":libads-core"))

    //AdMob
    implementation("com.google.android.gms:play-services-ads:24.7.0")

    //Facebook SDK
    implementation("com.facebook.android:facebook-android-sdk:18.1.3")

    //mediation admob
//    implementation("com.google.ads.mediation:facebook:6.20.0.0")
//    implementation("com.google.ads.mediation:applovin:13.3.1.0")
//    implementation("com.google.ads.mediation:vungle:7.5.0.0")
//    implementation("com.google.ads.mediation:pangle:7.2.0.6.0")
//    implementation("com.google.ads.mediation:mintegral:16.9.71.0")
//    implementation("com.google.ads.mediation:inmobi:10.6.1.0")
//    implementation("com.google.ads.mediation:ironsource:8.2.0.0")
//
//    implementation("com.appsflyer:af-android-sdk:6.17.0")
//    implementation("com.appsflyer:adrevenue:6.9.0")
//    implementation("com.android.installreferrer:installreferrer:2.2")
//    implementation("com.google.android.gms:play-services-appset:16.1.0")
//
//    implementation("com.android.billingclient:billing-ktx:8.2.1")
}
