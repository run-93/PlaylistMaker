plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-parcelize")
}

android {
    namespace = "com.practicum.playlistmaker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.practicum.playlistmaker"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation ("com.google.android.material:material:1.6.1") // подключаем библиотеку material
    implementation ("com.github.bumptech.glide:glide:4.16.0") // подключаем библиотеку Glide
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0") //подключаем  дополнительный модуль основной библиотеки Glide для корректной компиляции
    implementation ("com.google.code.gson:gson:2.10") //подключаем библиотеку gson
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")// для создание запросов к серверу с использованием API подключаем retrofit
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")//подключаем конвертер для преобразование ответов json в классы KOTLIN и обратно
    implementation ("androidx.constraintlayout:constraintlayout:2.2.0-beta01")// подключаем последнее обнавление constraintlayout
    // To use constraintlayout in compose
    implementation ("androidx.constraintlayout:constraintlayout-compose:1.1.0-beta01")// компонент для constraintlayout

}