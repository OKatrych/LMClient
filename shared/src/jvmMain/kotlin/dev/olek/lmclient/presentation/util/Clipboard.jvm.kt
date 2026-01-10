package dev.olek.lmclient.presentation.util

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import java.awt.datatransfer.StringSelection

@ExperimentalComposeUiApi
actual fun clipEntryOf(string: String): ClipEntry = ClipEntry(StringSelection(string))
