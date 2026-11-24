package io.github.museakheeyyy.muse.shared

import io.github.museakheeyyy.muse.shared.api.artwork.AnimatedArtworkService
import io.github.museakheeyyy.muse.shared.api.lastfm.LastFM
import io.github.museakheeyyy.muse.shared.auth.AuthManager
import io.github.museakheeyyy.muse.shared.backup.IosBackupRestoreManager
import io.github.museakheeyyy.muse.shared.db.MusicDatabase
import io.github.museakheeyyy.muse.shared.db.daos.DatabaseDao
import io.github.museakheeyyy.muse.shared.db.getDatabaseBuilder
import io.github.museakheeyyy.muse.shared.discord.DiscordRpcManager
import io.github.museakheeyyy.muse.shared.playback.MusicPlayer
import io.github.museakheeyyy.muse.shared.repository.LyricsRepository
import io.github.museakheeyyy.muse.shared.scrobble.ScrobbleSessions
import io.github.museakheeyyy.muse.shared.settings.BlockedContent
import io.github.museakheeyyy.muse.shared.settings.Settings
import io.github.museakheeyyy.muse.shared.state.GlobalYouTube
import io.github.museakheeyyy.muse.shared.state.SongLikeState
import io.github.museakheeyyy.muse.shared.state.coreModule
import io.github.museakheeyyy.muse.shared.youtube.di.sharedModule
import io.github.museakheeyyy.muse.shared.youtube.playback.StreamResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

object MetrolistPlatform {
    private var isInitialized = false

    fun initialize() {
        if (isInitialized) return
        IosBackupRestoreManager().recoverInterruptedRestore()
        isInitialized = true
        startKoin {
            modules(
                coreModule,
                sharedModule,
                module {
                    single<MusicDatabase> { getDatabaseBuilder(null).build() }
                    single<DatabaseDao> { get<MusicDatabase>().dao() }
                    single { AnimatedArtworkService(GlobalYouTube.client) }
                    single { MusicPlayer(animatedArtworkService = get()) }
                },
            )
        }

        LastFM.initialize(
            apiKey = BuildConfig.LASTFM_API_KEY,
            secret = BuildConfig.LASTFM_SECRET,
        )
        LastFM.sessionKey = Settings.shared.lastFmSession.ifBlank { null }
        ScrobbleSessions.restorePersistedAccounts()

        AuthManager().restorePersistedSession()

        val startupScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        startupScope.launch {
            BlockedContent.refreshAiSlopBlocklist()
        }
        startupScope.launch {
            runCatching {
                KoinPlatform.getKoin().get<StreamResolver>().prewarm()
            }
        }
    }

    suspend fun reopenDatabaseAfterRestore(): MusicDatabase {
        val koin = KoinPlatform.getKoin()
        val database = getDatabaseBuilder(null).build()
        koin.declare(database)
        koin.declare(database.dao())
        koin.declare(MusicPlayer(animatedArtworkService = koin.get()))
        koin.declare(
            LyricsRepository(
                lyricsClient = koin.get(),
                lyricsTranslationClient = koin.get(),
                databaseDao = database.dao(),
                youtubeLyricsApi = koin.get(),
                aiModelCatalog = koin.get(),
            ),
        )
        refreshRestoredSettings()
        return database
    }

    suspend fun refreshRestoredSettings() {
        DiscordRpcManager.reloadAfterRestore(GlobalYouTube.client)
        AuthManager().restorePersistedSession()
        ScrobbleSessions.restorePersistedAccounts()
        SongLikeState.refreshFromSettings()
    }
}
