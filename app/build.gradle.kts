import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.gradleVersions)
}

android {
    compileSdk = 34

    namespace = "net.mm2d.dev.tabs.browser"
    defaultConfig {
        applicationId = "net.mm2d.dev.tabs.browser"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        debug {
            isDebuggable = true
            enableAndroidTestCoverage = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    lint {
        abortOnError = true
    }
    @Suppress("UnstableApiUsage")
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(libs.kotlinStdlib)
    implementation(libs.kotlinReflect)
    implementation(libs.androidxAppCompat)
    implementation(libs.androidxConstraintLayout)
    implementation(libs.androidxRecyclerView)
    implementation(libs.androidxCore)
    implementation(libs.androidxBrowser)
    implementation(libs.androidxWebkit)
    implementation(libs.material)

    debugImplementation(libs.leakcanary)
    debugImplementation(libs.bundles.flipper)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidxJunit)
}

fun isStable(version: String): Boolean {
    val versionUpperCase = version.uppercase()
    val hasStableKeyword = listOf("RELEASE", "FINAL", "GA").any { versionUpperCase.contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    return hasStableKeyword || regex.matches(version)
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    rejectVersionIf { !isStable(candidate.version) }
}
