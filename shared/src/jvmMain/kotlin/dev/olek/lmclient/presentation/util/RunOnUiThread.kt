package dev.olek.lmclient.presentation.util

import javax.swing.SwingUtilities

/**
 * https://github.com/arkivanov/Decompose/blob/master/sample/app-desktop/src/jvmMain/kotlin/com/arkivanov/sample/app/Utils.kt
 */
internal fun <T> runOnUiThread(block: () -> T): T {
    if (SwingUtilities.isEventDispatchThread()) {
        return block()
    }

    var error: Throwable? = null
    var result: T? = null

    SwingUtilities.invokeAndWait {
        try {
            result = block()
        } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
            // Capture any error from the block to re-throw on the calling thread
            error = e
        }
    }

    error?.also { throw it }

    @Suppress("UNCHECKED_CAST")
    return result as T
}
