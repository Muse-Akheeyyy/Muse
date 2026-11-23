package io.github.museakheeyyy.muse.shared.youtube.di

import com.metrolist.innertubex.InnerTube
import com.metrolist.innertubex.cipher.RemotePlayerConfigStore
import com.metrolist.innertubex.cipher.YouTubeCipherService
import com.metrolist.innertubex.extraction.InnerTubeExtractor
import com.metrolist.innertubex.extraction.StreamExtractor
import com.metrolist.innertubex.extraction.TokenProvider
import com.metrolist.innertubex.extraction.YtConfigParser
import com.metrolist.innertubex.extraction.YtConfigParserImpl
import com.metrolist.innertubex.extraction.strategy.ClientFallbackStrategy
import com.metrolist.innertubex.extraction.strategy.ClientHealthMonitor
import com.metrolist.innertubex.extraction.strategy.ContentAwareFallbackStrategy
import io.github.museakheeyyy.muse.shared.BuildConfig
import io.github.museakheeyyy.muse.shared.api.lyrics.AiModelCatalog
import io.github.museakheeyyy.muse.shared.api.lyrics.LyprioLyricsProvider
import io.github.museakheeyyy.muse.shared.api.lyrics.LyricsClient
import io.github.museakheeyyy.muse.shared.api.lyrics.LyricsFetcher
import io.github.museakheeyyy.muse.shared.api.lyrics.LyricsTranslationClient
import io.github.museakheeyyy.muse.shared.api.lyrics.YouTubeLyricsApi
import io.github.museakheeyyy.muse.shared.api.musicvideo.MusicVideoResolver
import io.github.museakheeyyy.muse.shared.repository.LyricsRepository
import io.github.museakheeyyy.muse.shared.state.GlobalYouTube
import io.github.museakheeyyy.muse.shared.youtube.logging.MetrolistInnerTubeLogger
import io.github.museakheeyyy.muse.shared.youtube.lyrics.InnerTubeLyricsApi
import io.github.museakheeyyy.muse.shared.youtube.playback.DefaultStreamResolver
import io.github.museakheeyyy.muse.shared.youtube.playback.LocalAudioSource
import io.github.museakheeyyy.muse.shared.youtube.playback.MetrolistPlayerConfigRepository
import io.github.museakheeyyy.muse.shared.youtube.playback.PlatformTokenProvider
import io.github.museakheeyyy.muse.shared.youtube.playback.StreamResolver
import io.github.museakheeyyy.muse.shared.youtube.strategy.ClientHealthTracker
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val sharedModule =
    module {
        single {
            Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            }
        }
        single { RemotePlayerConfigStore(get(), MetrolistPlayerConfigRepository, MetrolistInnerTubeLogger) }
        single<YtConfigParser> {
            YtConfigParserImpl(
                get(),
                get(),
                get(),
                MetrolistInnerTubeLogger,
                cipherService = get<YouTubeCipherService>(),
            )
        }

        single { ClientHealthTracker() }
        single<ClientHealthMonitor> { get<ClientHealthTracker>() }
        single<ClientFallbackStrategy> { ContentAwareFallbackStrategy(get()) }

        single { YouTubeCipherService(get(), get(), MetrolistInnerTubeLogger) }

        single<TokenProvider> { PlatformTokenProvider() }

        single<StreamExtractor> {
            InnerTubeExtractor(
                configParser = get(),
                cipherService = get<YouTubeCipherService>(),
                innerTube = get<InnerTube>(),
                fallbackStrategy = get(),
                tokenProvider = get<TokenProvider>(),
                clientHealthMonitor = get(),
                logger = MetrolistInnerTubeLogger,
            )
        }

        single<StreamResolver> {
            DefaultStreamResolver(
                extractor = get(),
                innerTube = get(),
                tokenProvider = getOrNull(),
                clientHealthTracker = get(),
                playerConfigRefresh = { get<YouTubeCipherService>().refreshAfterStreamRejection() },
                musicVideoResolver = get(),
                localAudioSource = getOrNull<LocalAudioSource>(),
            )
        }

        single<MusicVideoResolver> { MusicVideoResolver() }

        single { GlobalYouTube.repository }
        single<YouTubeLyricsApi> { InnerTubeLyricsApi(get()) }
        single {
            LyricsClient(
                httpClient = get(),
                youtubeLyricsApi = get(),
                lyprioLyricsProvider =
                    LyprioLyricsProvider.createOrNull(
                        httpClient = get(),
                        baseUrl = BuildConfig.LYPRIO_BASE_URL,
                        signingKeyBase64 = BuildConfig.LYPRIO_HMAC_KEY,
                        allowInsecureHttp = BuildConfig.DEBUG,
                    ),
            )
        }
        single<LyricsFetcher> { get<LyricsClient>() }
        single { LyricsTranslationClient(get(), get()) }
        single { AiModelCatalog(get()) }
        single {
            LyricsRepository(
                lyricsClient = get(),
                lyricsTranslationClient = get(),
                aiModelCatalog = get(),
                databaseDao = getOrNull(),
                youtubeLyricsApi = get(),
            )
        }
    }
