package dev.olek.lmclient.presentation.util

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry

@ExperimentalComposeUiApi
actual fun clipEntryOf(string: String): ClipEntry = ClipEntry.withPlainText(string)
