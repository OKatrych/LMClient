package dev.olek.lmclient.app

import dev.olek.lmclient.data.DataModule
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Configuration
@Module(includes = [DataModule::class])
class AppModule
