/*
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package io.github.museakheeyyy.muse.shared.settings

import android.util.Log
import io.github.museakheeyyy.muse.shared.youtube.playback.PlaybackEventLevel
import io.github.museakheeyyy.muse.shared.youtube.playback.PlaybackEventLog

internal fun logSettingsFailure(
    message: String,
    error: Throwable,
) {
    PlaybackEventLog.captureApplicationFailure(PlaybackEventLevel.ERROR, "Settings", message, error)
    Log.e("Settings", message, error)
}
