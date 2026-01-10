@file:Suppress("Filename")

import androidx.compose.ui.window.ComposeUIViewController
import dev.olek.lmclient.presentation.components.AppComponent
import dev.olek.lmclient.presentation.ui.App
import platform.UIKit.UIViewController

// Called from Swift code
@Suppress("FunctionName")
fun MainViewController(component: AppComponent): UIViewController = ComposeUIViewController {
    App(component)
}
