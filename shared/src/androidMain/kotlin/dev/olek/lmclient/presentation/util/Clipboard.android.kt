package dev.olek.lmclient.presentation.util

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry

actual fun clipEntryOf(string: String): ClipEntry = ClipEntry(
    clipData = ClipData.newPlainText(
        "LM-Client",
        string,
    )
)
