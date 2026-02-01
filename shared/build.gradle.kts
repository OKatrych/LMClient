@file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalSwiftExportDsl::class)

import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.INT
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import com.mikepenz.aboutlibraries.plugin.DuplicateMode
import com.mikepenz.aboutlibraries.plugin.DuplicateRule
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.swiftexport.ExperimentalSwiftExportDsl

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.hotReload)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.build.konfig)
    alias(libs.plugins.aboutlibraries)
    alias(libs.plugins.sentry)
}

kotlin {
    jvmToolchain(libs.versions.jvmTarget.get().toInt())

    jvm()
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "LMClientShared"
            isStatic = true
            export(libs.decompose)
            export(libs.essenty.lifecycle)
            export(libs.essenty.statekeeper)
            export(libs.essenty.backhandler)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.annotations)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.resources)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.composeunstyled)

            implementation(libs.markdown.core)
            implementation(libs.markdown.ui)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.client.logging)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)

            implementation(libs.decompose)
            implementation(libs.decompose.compose)

            implementation(libs.coil)
            implementation(libs.coil.network.ktor)

            implementation(libs.haze)
            implementation(libs.haze.materials)

            implementation(libs.sqldelight.coroutines)
            implementation(libs.kermit)
            implementation(libs.compose.pipette)
            implementation(libs.arrow.core)
            implementation(libs.openai.client)
            implementation(libs.aboutlibraries.core)

            implementation(libs.settings)
            implementation(libs.settings.observable)
            implementation(libs.settings.coroutines)
            implementation(libs.filekit.core)
            implementation(libs.koog.agents.get().toString()) {
                // TODO remove when fixed on koog side
                exclude(group = "io.modelcontextprotocol", module = "kotlin-sdk-core-jvm")
            }
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            implementation(libs.compose.ui.tooling)
            implementation(libs.androidx.activityCompose)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.cio)
            implementation(libs.sqldelight.android)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.cio)
            implementation(libs.sqldelight.jvm)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native)
            // These dependencies have to be declared as `api` because they are being exported
            // to iOS side
            api(libs.decompose)
            api(libs.essenty.lifecycle)
            api(libs.essenty.statekeeper)
            api(libs.essenty.backhandler)
        }

        commonMain.configure {
            // https://insert-koin.io/docs/setup/annotations
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        }
    }

    // Remove warning about expect/actual
    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
    }

    android {
        namespace = "dev.olek.lmclient.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        androidResources.enable = true
    }
}

dependencies {
    // https://insert-koin.io/docs/setup/annotations
    add("kspCommonMainMetadata", libs.koin.ksp.compiler)
    add("kspAndroid", libs.koin.ksp.compiler)
    add("kspIosArm64", libs.koin.ksp.compiler)
    add("kspIosSimulatorArm64", libs.koin.ksp.compiler)
}

// Trigger Common Metadata Generation from Native tasks
tasks.matching {
    it.name.startsWith("ksp") && it.name != "kspCommonMainKotlinMetadata"
}.configureEach {
    dependsOn("kspCommonMainKotlinMetadata")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "LM Client"
            packageVersion = libs.versions.versionName.get()

            linux {
                iconFile.set(project.file("desktopAppIcons/LinuxIcon.png"))
            }
            windows {
                iconFile.set(project.file("desktopAppIcons/WindowsIcon.ico"))
            }
            macOS {
                iconFile.set(project.file("desktopAppIcons/MacosIcon.icns"))
                bundleID = "dev.olek.lmclient.desktopApp"
            }
        }
    }
}

sqldelight {
    databases {
        create("Database") {
            dialect(libs.sqldelight.dialect)
            packageName.set("dev.olek.lmclient.shared.data")
            schemaOutputDirectory.set(file("src/main/sqldelight/databases"))
            verifyMigrations.set(true)
            verifyDefinitions.set(true)
            generateAsync.set(true)
        }
    }
}

buildkonfig {
    packageName = "dev.olek.lmclient"

    defaultConfigs {
        buildConfigField(STRING, "appVersionName", libs.versions.versionName.get())
        buildConfigField(INT, "appVersionCode", libs.versions.versionCode.get())
    }
}

aboutLibraries {
    // ./gradlew :shared:exportLibraryDefinitions
    library {
        duplicationMode = DuplicateMode.MERGE
        duplicationRule = DuplicateRule.SIMPLE
    }
    export {
        outputFile = file("src/commonMain/composeResources/files/aboutlibraries.json")
        prettyPrint = false
    }
}
