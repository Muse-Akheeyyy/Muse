package io.github.museakheeyyy.muse.android.utils

import android.app.Application
import io.github.museakheeyyy.muse.shared.playback.PlaybackClientStats
import io.github.museakheeyyy.muse.shared.settings.Settings

object SentryManager {
    fun initialize(application: Application) = Unit

    // Keeps the launch counter and stored counts bounded; debug builds never send them.
    suspend fun reportPlaybackClientStats() {
        PlaybackClientStats.takeReportOnLaunch()
    }

    fun setEnabled(enabled: Boolean) {
        Settings.shared.sentryEnabled = enabled
    }
}
