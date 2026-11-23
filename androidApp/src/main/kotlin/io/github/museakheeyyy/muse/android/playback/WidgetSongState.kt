package io.github.museakheeyyy.muse.android.playback

import io.github.museakheeyyy.muse.android.widget.WidgetState
import io.github.museakheeyyy.muse.shared.cast.CastState
import io.github.museakheeyyy.muse.shared.model.ArtistTiny
import io.github.museakheeyyy.muse.shared.model.SongItem
import io.github.museakheeyyy.muse.shared.settings.displayName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

internal fun widgetSongState(
    currentSong: Flow<SongItem?>,
    likedSongs: Flow<List<SongItem>>,
): Flow<Pair<SongItem?, Boolean>> =
    combine(currentSong, likedSongs) { song, likes ->
        song to (song != null && likes.any { it.id == song.id })
    }.distinctUntilChanged()

internal fun widgetPlaybackRequested(
    wantsPlayback: Flow<Boolean>,
    castState: Flow<CastState>,
): Flow<Boolean> =
    combine(wantsPlayback, castState) { local, remote ->
        remote.takeIf { it.isConnected }?.isPlaying ?: local
    }.distinctUntilChanged()

internal fun playbackWidgetState(
    song: SongItem?,
    isPlaying: Boolean,
    isLiked: Boolean,
    duration: Long,
    currentPosition: Long,
    idleTitle: String,
    idleArtist: String,
    unknownArtist: String,
): WidgetState =
    WidgetState(
        title = song?.title ?: idleTitle,
        artist =
            song?.let {
                it.artists
                    .map(ArtistTiny::displayName)
                    .filter(String::isNotBlank)
                    .joinToString()
                    .ifEmpty { unknownArtist }
            }
                ?: idleArtist,
        artworkUri = song?.thumbnail,
        isPlaying = song != null && isPlaying,
        isLiked = song != null && isLiked,
        duration = if (song != null) duration else 0,
        currentPosition = if (song != null) currentPosition else 0,
    )
