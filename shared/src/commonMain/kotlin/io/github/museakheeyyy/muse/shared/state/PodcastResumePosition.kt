package io.github.museakheeyyy.muse.shared.state

import io.github.museakheeyyy.muse.shared.db.entities.isEpisodeComplete
import io.github.museakheeyyy.muse.shared.model.SongItem
import io.github.museakheeyyy.muse.shared.model.isPodcast

fun podcastResumePosition(
    song: SongItem,
    savedPositionMs: Long?,
    durationMs: Long = song.duration?.times(1000L) ?: 0L,
    requestedPositionMs: Long? = null,
): Long {
    requestedPositionMs?.let { return it.coerceAtLeast(0L) }
    if (!song.isPodcast) return 0L
    val position =
        savedPositionMs?.takeIf { it >= 0L }
            ?: song.playbackPositionMs?.takeIf { it > 0L }
            ?: song.playbackProgressPercent?.takeIf { it in 0..100 && durationMs > 0L }?.let { durationMs * it / 100L }
            ?: 0L
    return if (isEpisodeComplete(position, durationMs)) 0L else position
}
