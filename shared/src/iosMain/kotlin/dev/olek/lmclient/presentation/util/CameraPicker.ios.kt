package dev.olek.lmclient.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher

@Composable
actual fun rememberCameraPickerLauncher(onResult: (PlatformFile?) -> Unit): PhotoResultLauncher? {
    val platformLauncher = rememberCameraPickerLauncher(onResult = onResult)

    return remember(platformLauncher) {
        PhotoResultLauncher(onLaunch = { platformLauncher.launch() })
    }
}
