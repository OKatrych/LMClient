@file:OptIn(ExperimentalSettingsApi::class)

package dev.olek.lmclient.data.repositories

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getStringOrNullFlow
import com.russhwolf.settings.set
import dev.olek.lmclient.data.models.AppearanceMode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

interface SettingsRepository {
    fun observeAppearanceMode(): Flow<AppearanceMode>

    suspend fun setAppearanceMode(appearanceMode: AppearanceMode)
}

@Single(binds = [SettingsRepository::class])
internal class SettingsRepositoryImpl(
    private val settings: ObservableSettings,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SettingsRepository {
    override fun observeAppearanceMode(): Flow<AppearanceMode> = settings
        .getStringOrNullFlow(key = Keys.APPEARANCE_MODE)
        .map { it?.let { AppearanceMode.valueOf(it) } ?: AppearanceMode.SYSTEM }
        .flowOn(dispatcher)

    override suspend fun setAppearanceMode(appearanceMode: AppearanceMode) = withContext(dispatcher) {
        settings[Keys.APPEARANCE_MODE] = appearanceMode.name
    }

    private object Keys {
        const val APPEARANCE_MODE = "appearance_mode"
    }
}
