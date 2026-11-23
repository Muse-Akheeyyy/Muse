package io.github.museakheeyyy.muse.android.playback

import io.github.museakheeyyy.muse.shared.model.SongItem
import io.github.museakheeyyy.muse.shared.playback.PlaybackErrorContext
import io.github.museakheeyyy.muse.shared.youtube.playback.PlaybackResource

internal object PlaybackErrorReporter {
    fun setActivePlayback(
        song: SongItem,
        resource: PlaybackResource?,
    ) = Unit

    fun clearActivePlayback() = Unit

    fun report(
        error: Throwable,
        context: PlaybackErrorContext,
    ) = Unit
}
