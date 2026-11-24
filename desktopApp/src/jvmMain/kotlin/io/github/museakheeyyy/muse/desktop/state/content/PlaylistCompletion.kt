package io.github.museakheeyyy.muse.desktop.state.content

import io.github.museakheeyyy.muse.shared.db.entities.PlaylistEntity
import io.github.museakheeyyy.muse.shared.db.hasCompleteStoredSongs
import io.github.museakheeyyy.muse.shared.model.SongItem
import kotlinx.coroutines.Job

internal fun hasCompleteStoredPlaylist(
    playlist: PlaylistEntity,
    songs: List<SongItem>,
): Boolean = playlist.hasCompleteStoredSongs(songs)

internal suspend fun awaitCompletePlaylist(
    loadJob: Job?,
    hasMore: () -> Boolean,
): Boolean {
    if (hasMore()) loadJob?.join()
    return !hasMore()
}
