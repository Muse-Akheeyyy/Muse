package io.github.museakheeyyy.muse.android.listentogether

import io.github.museakheeyyy.muse.shared.listentogether.ListenTogetherTrackResolver
import io.github.museakheeyyy.muse.shared.listentogether.toSongItem
import io.github.museakheeyyy.muse.shared.model.SongItem

internal fun androidListenTogetherTrackResolver(
    metadata: suspend (String) -> SongItem?,
    fallbackArtist: suspend () -> String,
): ListenTogetherTrackResolver =
    ListenTogetherTrackResolver { track ->
        metadata(track.id) ?: track.toSongItem(
            fallbackArtist = fallbackArtist(),
            omitNonPositiveDuration = true,
        )
    }
