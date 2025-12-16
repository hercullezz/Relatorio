plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
<<<<<<< HEAD
    // Firebase não é mais necessário
    // id("com.google.gms.google-services")
    id("kotlin-kapt")
=======
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
>>>>>>> f969efbb0a5b1a468ff385c3f3c78fc47956aa19
}

android {
    namespace = "com.example.relatoriomanutencao"
    compileSdk = 34
<<<<<<< HEAD

    defaultConfig {
        applicationId = "com.example.relatoriomanutencao"
        minSdk = 26
=======
    lint {
        disable += setOf("LintBaseline", "MissingDimensionBuildVariant")
        checkOnly += setOf("MissingTranslation")
        abortOnError = false
    }

    defaultConfig {
        applicationId = "com.example.relatoriomanutencao"
        minSdk = 24
>>>>>>> f969efbb0a5b1a468ff385c3f3c78fc47956aa19
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
<<<<<<< HEAD
        multiDexEnabled = true
=======
>>>>>>> f969efbb0a5b1a468ff385c3f3c78fc47956aa19
    }

    buildTypes {
        release {
            isMinifyEnabled = false
<<<<<<< HEAD
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-Xjvm-default=all",
            "-opt-in=kotlin.RequiresOptIn"
        )
    }
    kapt {
        correctErrorTypes = true
        useBuildCache = false
        javacOptions {
            option("-Xmaxerrs", 1000)
=======
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
>>>>>>> f969efbb0a5b1a468ff385c3f3c78fc47956aa19
        }
    }
    buildFeatures {
        compose = true
    }
<<<<<<< HEAD
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/license.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/notice.txt"
            excludes += "META-INF/ASL2.0"
            excludes += "META-INF/*.kotlin_module" 
=======
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
>>>>>>> f969efbb0a5b1a468ff385c3f3c78fc47956aa19
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
<<<<<<< HEAD
    implementation(platform("androidx.compose:compose-bom:2024.04.01"))
=======
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
>>>>>>> f969efbb0a5b1a468ff385c3f3c78fc47956aa19
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
<<<<<<< HEAD
    implementation("androidx.compose.material:material-icons-extended")
    
    // Navigation & ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    
    // Room (Banco de dados local para cache)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Coil (Imagens da Web)
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // Back4App / Parse
    implementation("com.github.parse-community.Parse-SDK-Android:parse:4.3.0")

    // Excel (Apache POI)
    implementation("org.apache.poi:poi:5.2.5")
    implementation("org.apache.poi:poi-ooxml:5.2.5")
    implementation("com.fasterxml:aalto-xml:1.2.2")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    // A dependência play-services não é mais necessária sem Firebase
    // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3") 
=======
    implementation("androidx.compose.material:material-icons-extended:1.7.5")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.android.gms:play-services-auth:20.7.0") // Google Sign In

    // ViewModel & Navigation
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
>>>>>>> f969efbb0a5b1a468ff385c3f3c78fc47956aa19

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
<<<<<<< HEAD
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.04.01"))
=======
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
>>>>>>> f969efbb0a5b1a468ff385c3f3c78fc47956aa19
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
