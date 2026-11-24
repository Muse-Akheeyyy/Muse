/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package io.github.museakheeyyy.muse.android.di

import coil3.ImageLoader
import coil3.imageLoader
import com.metrolist.innertubex.InnerTube
import com.metrolist.innertubex.cipher.RemotePlayerConfigStore
import com.metrolist.innertubex.cipher.YouTubeCipherService
import com.metrolist.innertubex.extraction.InnerTubeExtractor
import com.metrolist.innertubex.extraction.TokenProvider
import com.metrolist.innertubex.extraction.YtConfigParserImpl
import com.metrolist.innertubex.extraction.strategy.ContentAwareFallbackStrategy
import io.github.museakheeyyy.muse.android.playback.AudioCacheManager
import io.github.museakheeyyy.muse.android.playback.ExoPlaybackBenchmarkPlayer
import io.github.museakheeyyy.muse.android.playback.PlayerConnection
import io.github.museakheeyyy.muse.android.playback.SongDownloadManager
import io.github.museakheeyyy.muse.android.widget.MetrolistWidgetManager
import io.github.museakheeyyy.muse.android.widget.PlaylistWidgetManager
import io.github.museakheeyyy.muse.shared.api.YouTubeRepository
import io.github.museakheeyyy.muse.shared.api.musicvideo.MusicVideoResolver
import io.github.museakheeyyy.muse.shared.db.MusicDatabase
import io.github.museakheeyyy.muse.shared.db.daos.DatabaseDao
import io.github.museakheeyyy.muse.shared.db.entities.toSongItem
import io.github.museakheeyyy.muse.shared.db.getDatabaseBuilder
import io.github.museakheeyyy.muse.shared.youtube.benchmark.PlaybackBenchmarkEnvironment
import io.github.museakheeyyy.muse.shared.youtube.benchmark.PlaybackBenchmarkRunner
import io.github.museakheeyyy.muse.shared.youtube.logging.MetrolistInnerTubeLogger
import io.github.museakheeyyy.muse.shared.youtube.playback.DefaultStreamResolver
import io.github.museakheeyyy.muse.shared.youtube.playback.LocalAudioSource
import io.github.museakheeyyy.muse.shared.youtube.strategy.ClientHealthTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule =
    module {
        single<CoroutineScope> {
            CoroutineScope(SupervisorJob() + Dispatchers.Default)
        }

        single<MusicDatabase> {
            getDatabaseBuilder(androidContext()).build()
        }

        single<DatabaseDao> {
            get<MusicDatabase>().dao()
        }

        single<AudioCacheManager> {
            AudioCacheManager(
                context = androidContext(),
                database = get(),
            )
        }

        single<LocalAudioSource> {
            LocalAudioSource { id, song ->
                val storedSong = song ?: get<MusicDatabase>().dao().getSongById(id)?.toSongItem()
                storedSong?.let { get<AudioCacheManager>().getCachedPlaybackResource(it) }
            }
        }

        single<SongDownloadManager> {
            SongDownloadManager(
                context = androidContext(),
                audioCacheManager = get(),
                database = get(),
            )
        }

        single<ImageLoader> {
            androidContext().imageLoader
        }

        single<PlayerConnection> {
            PlayerConnection(
                context = androidContext(),
                scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
            )
        }

        single<PlaylistWidgetManager> {
            PlaylistWidgetManager(
                context = androidContext(),
                database = get(),
                applicationScope = get(),
                imageLoader = get(),
            )
        }

        single<MetrolistWidgetManager> {
            MetrolistWidgetManager(
                context = androidContext(),
                playlistWidgetManager = get(),
                imageLoader = get(),
            )
        }

        single {
            val innerTube = get<InnerTube>()
            val tokenProvider = get<TokenProvider>()
            val cipherService = get<YouTubeCipherService>()
            val musicVideoResolver = get<MusicVideoResolver>()
            PlaybackBenchmarkRunner(
                environmentFor = { anonymousSession ->
                    val benchmarkHealthTracker = ClientHealthTracker(persistenceEnabled = false)
                    val benchmarkInnerTube = innerTube.createIsolatedSession(includeAccount = !anonymousSession)
                    val benchmarkExtractor =
                        InnerTubeExtractor(
                            configParser =
                                YtConfigParserImpl(
                                    innerTube.httpClient,
                                    benchmarkInnerTube,
                                    get<RemotePlayerConfigStore>(),
                                    MetrolistInnerTubeLogger,
                                    cipherService = cipherService,
                                ),
                            cipherService = cipherService,
                            innerTube = benchmarkInnerTube,
                            fallbackStrategy = ContentAwareFallbackStrategy(benchmarkHealthTracker),
                            tokenProvider = tokenProvider,
                            clientHealthMonitor = benchmarkHealthTracker,
                            logger = MetrolistInnerTubeLogger,
                        )
                    PlaybackBenchmarkEnvironment(
                        httpClient = benchmarkInnerTube.httpClient,
                        youtubeRepository = YouTubeRepository(benchmarkInnerTube),
                        // The runner calls this off the main thread; ExoPlayer must be created and configured on main.
                        playerFactory = { runBlocking(Dispatchers.Main) { ExoPlaybackBenchmarkPlayer(androidContext()) } },
                        resolverFor = {
                            DefaultStreamResolver(
                                extractor = benchmarkExtractor,
                                innerTube = benchmarkInnerTube,
                                tokenProvider = tokenProvider,
                                clientHealthTracker = benchmarkHealthTracker,
                                playerConfigRefresh = { cipherService.refreshAfterStreamRejection() },
                                musicVideoResolver = musicVideoResolver,
                            )
                        },
                    )
                },
            )
        }

        includes(viewModelsModule)
    }
