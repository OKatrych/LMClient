package dev.olek.lmclient.presentation.util

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile

@Composable
actual fun rememberCameraPickerLauncher(onResult: (PlatformFile?) -> Unit): PhotoResultLauncher? {
    return null
}
