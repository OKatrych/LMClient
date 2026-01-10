@file:Suppress("Filename")

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import dev.olek.lmclient.app.AppInitializer
import dev.olek.lmclient.app.KoinApp
import dev.olek.lmclient.presentation.components.AppComponentImpl
import dev.olek.lmclient.presentation.ui.App
import dev.olek.lmclient.presentation.util.readSerializableContainer
import dev.olek.lmclient.presentation.util.runOnUiThread
import java.awt.Dimension
import java.io.File

private const val SAVED_STATE_FILE_NAME = "saved_state.dat"

fun main() {
    val lifecycle = LifecycleRegistry()
    val stateKeeper = StateKeeperDispatcher(File(SAVED_STATE_FILE_NAME).readSerializableContainer())

    AppInitializer.init {
        properties(
            mapOf(
                KoinApp.PROPERTY_IS_DEBUG to (System.getProperty("debug")?.toBoolean() ?: false)
            )
        )
    }

    val appComponent = runOnUiThread {
        AppComponentImpl(
            DefaultComponentContext(
                lifecycle = lifecycle,
                stateKeeper = stateKeeper,
            ),
        )
    }

    application {
        Window(
            title = "LM Client",
            state = rememberWindowState(width = 800.dp, height = 600.dp),
            onCloseRequest = ::exitApplication,
        ) {
            window.minimumSize = Dimension(350, 600)
            App(
                component = appComponent,
            )
        }
    }
}
