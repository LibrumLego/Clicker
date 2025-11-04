plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    // ✅ namespace와 applicationId 둘 다 'com.woodangtanglabclicker'로 통일
    namespace = "com.woodangtanglabclicker"

    compileSdk = 35   // ✅ 최신 SDK로 수정

    defaultConfig {
        applicationId = "com.woodangtanglabclicker" // ✅ Play Store용 고유 ID
        minSdk = 21
        targetSdk = 35
        versionCode = 4
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // ✅ 릴리스 최적화 및 가독화 파일 자동 생성
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // 내부 테스트 시 난독화 끔
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ✅ 앱에서 사용하는 기본 라이브러리
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.fragment:fragment-ktx:1.6.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    // ✅ AdMob (Google 광고 SDK)
    implementation("com.google.android.gms:play-services-ads:23.0.0")
}
