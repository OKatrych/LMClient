import dev.detekt.gradle.Detekt

plugins {
    alias(libs.plugins.multiplatform).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.hotReload).apply(false)
    alias(libs.plugins.kotlinx.serialization).apply(false)
    alias(libs.plugins.ksp).apply(false)
    alias(libs.plugins.sqldelight).apply(false)
    alias(libs.plugins.build.konfig).apply(false)
    alias(libs.plugins.aboutlibraries).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.multiplatform.library).apply(false)
    alias(libs.plugins.sentry).apply(false)
    alias(libs.plugins.sentry.android).apply(false)
    alias(libs.plugins.detekt)
}

dependencies {
    detektPlugins(libs.detekt.compose)
}

// https://github.com/RevenueCat/purchases-android/blob/0096eb96c8c17b9eda2c137030320e00d5f88bea/build.gradle.kts#L15
tasks.register<Detekt>("detektAll") {
    description = "Runs over the whole codebase without the startup overhead for each module."
    buildUponDefaultConfig = true
    autoCorrect = true
    parallel = true
    setSource(files(rootDir))
    include("**/*.kt", "**/*.kts")
    exclude(
        "**/build/**",
        "**/test/**/*.kt",
        "**/testDefaults/**/*.kt",
        "**/testCustomEntitlementComputation/**/*.kt",
    )
    config.setFrom("$rootDir/detekt.yml")
}
