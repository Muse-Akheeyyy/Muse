package io.github.museakheeyyy.muse.desktop.storage

import io.github.museakheeyyy.muse.shared.db.MusicDatabase
import io.github.museakheeyyy.muse.shared.db.entities.toSongItem
import io.github.museakheeyyy.muse.shared.model.SongItem
import io.github.museakheeyyy.muse.shared.model.isPodcast
import io.github.museakheeyyy.muse.shared.utils.Logger
import kotlinx.coroutines.CancellationException

internal suspend fun storedAudioReportingSong(
    song: SongItem?,
    database: MusicDatabase?,
): SongItem? {
    if (song == null || song.isLocal || song.isUploaded || song.isPodcast || song.id.startsWith("local_")) return song
    return try {
        database?.dao()?.getSongById(song.id)?.toSongItem() ?: song
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        Logger.w("StoredAudioHistory", "Could not read playback reporting metadata", error)
        null
    }
}
