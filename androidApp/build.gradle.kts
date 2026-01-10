plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.sentry.android)
}

kotlin {
    jvmToolchain(libs.versions.jvmTarget.get().toInt())
}
android {
    namespace = "dev.olek.lmclient"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()

        applicationId = "dev.olek.lmclient.androidApp"
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()

        buildFeatures {
            buildConfig = true
            compose = true
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            // Configure release signing in local.properties or CI environment
            // signingConfig = signingConfigs.getByName("release")
        }
    }
    packaging {
        resources {
            // Fix for https://github.com/JetBrains/koog/issues/672
            excludes += "/META-INF/INDEX.LIST"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "/META-INF/io.netty.versions.properties"
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.activityCompose)
    implementation(libs.androidx.splashscreen)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.decompose)
    implementation(libs.koin.android)
}


sentry {
    // Prevent Sentry dependencies from being included in the Android app through the AGP.
    autoInstallation {
        enabled.set(false)
    }

    // The slug of the Sentry organization to use for uploading proguard mappings/source contexts.
    org.set("olekdev")
    // The slug of the Sentry project to use for uploading proguard mappings/source contexts.
    projectName.set("lmclient")
    // Upload source code to Sentry so it is shown as part of the stack traces
    includeSourceContext = true
    // The authentication token to use for uploading proguard mappings/source contexts.
    // WARNING: Do not expose this token in your build.gradle files, but rather set an environment
    // variable and read it into this property.
    authToken.set(System.getenv("SENTRY_AUTH_TOKEN"))
}
