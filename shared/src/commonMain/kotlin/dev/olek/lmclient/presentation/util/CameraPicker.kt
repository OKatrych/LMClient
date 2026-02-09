package dev.olek.lmclient.presentation.util

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile

@Composable
expect fun rememberCameraPickerLauncher(onResult: (PlatformFile?) -> Unit): PhotoResultLauncher?

class PhotoResultLauncher(
    private val onLaunch: () -> Unit,
) {
    fun launch() {
        onLaunch()
    }
}
