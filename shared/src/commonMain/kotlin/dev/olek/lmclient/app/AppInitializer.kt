package dev.olek.lmclient.app

import io.sentry.kotlin.multiplatform.Sentry
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes
import org.koin.ksp.generated.startKoin

object AppInitializer {
    fun init(coinConfiguration: KoinAppDeclaration? = null) {
        initKoin(coinConfiguration)
        initCrashReporting()
    }

    private fun initKoin(coinConfiguration: KoinAppDeclaration? = null) {
        KoinApp.startKoin {
            includes(coinConfiguration)
        }
    }

    @Suppress("MaxLineLength")
    private fun initCrashReporting() {
        Sentry.init { options ->
            options.dsn = "https://0bc9d3a67b975ef1fcb464f696fcb7f0@o4509916556558336.ingest.de.sentry.io/4510647833264208"
            options.sendDefaultPii = false
        }
    }
}
