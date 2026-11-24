package io.github.museakheeyyy.muse.shared.youtube.playback

import io.github.museakheeyyy.muse.shared.model.SongItem

fun interface LocalAudioSource {
    suspend fun resolve(
        videoId: String,
        song: SongItem?,
    ): PlaybackResource?
}
