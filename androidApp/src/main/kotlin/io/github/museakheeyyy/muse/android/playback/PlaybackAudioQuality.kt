package io.github.museakheeyyy.muse.android.playback

import com.metrolist.innertubex.extraction.AudioQuality
import io.github.museakheeyyy.muse.shared.settings.Settings

internal fun playbackAudioQuality(): AudioQuality =
    try {
        AudioQuality.valueOf(Settings.shared.audioQuality)
    } catch (_: IllegalArgumentException) {
        AudioQuality.AUTO
    }
