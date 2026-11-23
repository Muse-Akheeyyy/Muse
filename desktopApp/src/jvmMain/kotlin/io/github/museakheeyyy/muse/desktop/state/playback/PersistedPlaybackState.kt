package io.github.museakheeyyy.muse.desktop.state.playback

import io.github.museakheeyyy.muse.shared.model.RepeatMode
import io.github.museakheeyyy.muse.shared.playback.MusicQueueSnapshot
import io.github.museakheeyyy.muse.shared.playback.StoredQueueSnapshotSerializer
import kotlinx.serialization.Serializable

@Serializable
data class PersistedPlaybackState(
    @Serializable(with = StoredQueueSnapshotSerializer::class)
    val queue: MusicQueueSnapshot,
    val positionMs: Long = 0L,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val revision: String? = null,
)
