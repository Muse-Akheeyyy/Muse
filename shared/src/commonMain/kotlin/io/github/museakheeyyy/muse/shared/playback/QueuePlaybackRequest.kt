package io.github.museakheeyyy.muse.shared.playback

import io.github.museakheeyyy.muse.shared.model.SongItem

data class QueuePlaybackRequest(
    val requestId: Long,
    val song: SongItem,
    val positionMs: Long = 0L,
    val resumePlayback: Boolean = true,
)
