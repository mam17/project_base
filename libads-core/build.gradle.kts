plugins {
    id("com.android.library")
    id("maven-publish")
}

android {
    namespace = "com.libads.core"
    compileSdk = 34

    defaultConfig {
        minSdk = 21

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Cho phép các module adapter (admob, facebook...) sau này publish riêng
    // nhưng vẫn dùng chung interface từ core.
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-common:2.8.2")
    implementation("androidx.lifecycle:lifecycle-process:2.8.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}

// Cấu hình publish để repo app khác gọi bằng implement (JitPack tự đọc phần này
// mà không cần thêm gì, hoặc dùng cho GitHub Packages nếu bạn muốn private hơn)
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "com.github.mam17"
                artifactId = "dtl-libads-core"
                version = findProperty("version")?.toString()
                    ?: System.getenv("VERSION_NAME")
                    ?: "1.0.0"
            }
        }
    }
}
