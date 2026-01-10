package dev.olek.lmclient

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.defaultComponentContext
import dev.olek.lmclient.presentation.components.AppComponentImpl
import dev.olek.lmclient.presentation.ui.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appComponent = AppComponentImpl(defaultComponentContext())
        setContent {
            App(
                component = appComponent,
                onThemeChange = { isDarkMode ->
                    val systemBarStyle = SystemBarStyle.auto(
                        lightScrim = Color.TRANSPARENT,
                        darkScrim = Color.TRANSPARENT,
                        detectDarkMode = { isDarkMode },
                    )
                    enableEdgeToEdge(
                        statusBarStyle = systemBarStyle,
                        navigationBarStyle = systemBarStyle,
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // Don't enforce scrim https://issuetracker.google.com/issues/298296168
                        window.isNavigationBarContrastEnforced = false
                    }
                },
            )
        }
    }
}
