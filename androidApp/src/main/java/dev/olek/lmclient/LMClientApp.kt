package dev.olek.lmclient

import android.app.Application
import dev.olek.lmclient.app.AppInitializer
import dev.olek.lmclient.app.KoinApp
import org.koin.android.ext.koin.androidContext

class LMClientApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppInitializer.init(
            coinConfiguration = {
                androidContext(androidContext = applicationContext)
                properties(mapOf(KoinApp.PROPERTY_IS_DEBUG to BuildConfig.DEBUG))
            }
        )
        if (BuildConfig.DEBUG) {
            // StrictMode.enableDefaults()
        }
    }
}
