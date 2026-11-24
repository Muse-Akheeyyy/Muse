/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package io.github.museakheeyyy.muse.desktop.di

import com.metrolist.innertubex.extraction.TokenProvider
import io.github.museakheeyyy.muse.desktop.state.playback.PlayerState
import io.github.museakheeyyy.muse.desktop.storage.DownloadManager
import io.github.museakheeyyy.muse.desktop.storage.SongCacheManager
import io.github.museakheeyyy.muse.desktop.storage.desktopSongOfflineAvailability
import io.github.museakheeyyy.muse.desktop.youtube.DesktopPageBoundTokenRuntime
import io.github.museakheeyyy.muse.shared.db.MusicDatabase
import io.github.museakheeyyy.muse.shared.db.OfflineSongAvailability
import io.github.museakheeyyy.muse.shared.db.daos.DatabaseDao
import io.github.museakheeyyy.muse.shared.db.getDatabaseBuilder
import io.github.museakheeyyy.muse.shared.youtube.playback.DesktopTokenProvider
import io.github.museakheeyyy.muse.shared.youtube.playback.LocalAudioSource
import org.koin.dsl.module

val composeStateModule =
    module {
        single<MusicDatabase> { getDatabaseBuilder().build() }
        single<LocalAudioSource> {
            LocalAudioSource { id, song ->
                DownloadManager.getLocalPlaybackResource(id, song) ?: SongCacheManager.getCachedPlaybackResource(id, song)
            }
        }
        single<DatabaseDao> { get<MusicDatabase>().dao() }
        single<OfflineSongAvailability> { desktopSongOfflineAvailability(get()) }
        single { DesktopPageBoundTokenRuntime() }
        single<TokenProvider> {
            DesktopTokenProvider(get<DesktopPageBoundTokenRuntime>()) {
                val queue = PlayerState.queue.snapshots.value
                queue.displayedItems
                    .asSequence()
                    .drop(queue.currentIndex + 1)
                    .take(2)
                    .map { it.id }
                    .toList()
            }
        }
    }
