@file:Suppress("FunctionName")

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.museakheeyyy.muse.shared

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.compose.LifecycleResumeEffect
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.network.DeDupeConcurrentRequestStrategy
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.github.museakheeyyy.muse.shared.backup.BackupArchiveLimits
import io.github.museakheeyyy.muse.shared.backup.IosBackupRestoreManager
import io.github.museakheeyyy.muse.shared.backup.defaultBackupFilename
import io.github.museakheeyyy.muse.shared.backup.foreignImportMessage
import io.github.museakheeyyy.muse.shared.backup.notifyIosAutomaticBackupSettingsChanged
import io.github.museakheeyyy.muse.shared.crash.IosCrashRecovery
import io.github.museakheeyyy.muse.shared.db.MusicDatabase
import io.github.museakheeyyy.muse.shared.discord.IosDiscordPresenceEffect
import io.github.museakheeyyy.muse.shared.generated.resources.Res
import io.github.museakheeyyy.muse.shared.generated.resources.back
import io.github.museakheeyyy.muse.shared.generated.resources.backup_filename_prefix
import io.github.museakheeyyy.muse.shared.generated.resources.import_playlist
import io.github.museakheeyyy.muse.shared.generated.resources.playlist_imports_title
import io.github.museakheeyyy.muse.shared.listentogether.IosListenTogetherPlayback
import io.github.museakheeyyy.muse.shared.listentogether.ListenTogetherLogLevel
import io.github.museakheeyyy.muse.shared.listentogether.ListenTogetherLogger
import io.github.museakheeyyy.muse.shared.listentogether.ListenTogetherManager
import io.github.museakheeyyy.muse.shared.listentogether.ListenTogetherPresentationText
import io.github.museakheeyyy.muse.shared.listentogether.ListenTogetherTrackResolver
import io.github.museakheeyyy.muse.shared.listentogether.toSongItem
import io.github.museakheeyyy.muse.shared.network.createHttpClient
import io.github.museakheeyyy.muse.shared.playback.IosPlaybackController
import io.github.museakheeyyy.muse.shared.playback.IosPlayerPlatformServices
import io.github.museakheeyyy.muse.shared.playback.MusicPlayer
import io.github.museakheeyyy.muse.shared.playlist.IosPlaylistCoverMapper
import io.github.museakheeyyy.muse.shared.playlist.migrateIosPlaylistCovers
import io.github.museakheeyyy.muse.shared.playlist.removeStoredPlaylistCover
import io.github.museakheeyyy.muse.shared.playlist.setStoredPlaylistCover
import io.github.museakheeyyy.muse.shared.playlist.storeIosPlaylistCover
import io.github.museakheeyyy.muse.shared.playlist.uploadStoredPlaylistCover
import io.github.museakheeyyy.muse.shared.settings.Settings
import io.github.museakheeyyy.muse.shared.state.GlobalYouTubeRepository
import io.github.museakheeyyy.muse.shared.state.SongLikeState
import io.github.museakheeyyy.muse.shared.state.SongMetadataState
import io.github.museakheeyyy.muse.shared.state.closestContentCountry
import io.github.museakheeyyy.muse.shared.state.contentCountryCodes
import io.github.museakheeyyy.muse.shared.storage.IosAudioDownloadManager
import io.github.museakheeyyy.muse.shared.storage.ResizableImageDiskCache
import io.github.museakheeyyy.muse.shared.ui.crash.CrashLogsScreen
import io.github.museakheeyyy.muse.shared.ui.listentogether.listenTogetherPresentationMessages
import io.github.museakheeyyy.muse.shared.ui.mobile.IosLoginScreen
import io.github.museakheeyyy.muse.shared.ui.mobile.MobileApp
import io.github.museakheeyyy.muse.shared.ui.mobile.iosLyricsShareAction
import io.github.museakheeyyy.muse.shared.ui.mobile.pickIosPlaylistCover
import io.github.museakheeyyy.muse.shared.ui.mobile.rememberVoiceOverEnabled
import io.github.museakheeyyy.muse.shared.ui.onboarding.IosOnboarding
import io.github.museakheeyyy.muse.shared.ui.settings.BackupRestorePage
import io.github.museakheeyyy.muse.shared.ui.settings.BackupRestoreStatus
import io.github.museakheeyyy.muse.shared.ui.settings.IosNativeFiles
import io.github.museakheeyyy.muse.shared.ui.settings.IosPlaylistImportSettings
import io.github.museakheeyyy.muse.shared.ui.settings.IosStorageSettingsPage
import io.github.museakheeyyy.muse.shared.ui.settings.LocalSettingsPageBottomPadding
import io.github.museakheeyyy.muse.shared.ui.settings.PlaylistImportFile
import io.github.museakheeyyy.muse.shared.ui.settings.SettingsChoice
import io.github.museakheeyyy.muse.shared.ui.settings.SettingsPageId
import io.github.museakheeyyy.muse.shared.ui.theme.MetrolistTheme
import io.github.museakheeyyy.muse.shared.ui.theme.parseThemeSeedColor
import io.github.museakheeyyy.muse.shared.update.IosUpdateNotice
import io.github.museakheeyyy.muse.shared.utils.ArtworkSizingInterceptor
import io.github.museakheeyyy.muse.shared.utils.Logger
import io.github.museakheeyyy.muse.shared.utils.formatHistoryMonth
import io.github.museakheeyyy.muse.shared.utils.isCrashReportingAvailable
import io.github.museakheeyyy.muse.shared.youtube.playback.IosPageBoundTokenHost
import io.github.museakheeyyy.muse.shared.youtube.playback.IosPlaybackNetworkMonitor
import io.github.museakheeyyy.muse.shared.youtube.playback.StreamResolver
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okio.Path.Companion.toPath
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.mp.KoinPlatform
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSFileManager
import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleCountryCode
import platform.Foundation.NSLocaleIdentifier
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDomainMask
import platform.Foundation.countryCode
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.localeIdentifier
import platform.Foundation.preferredLanguages
import platform.UIKit.UIApplicationDidReceiveMemoryWarningNotification
import platform.UIKit.UIViewController
import platform.darwin.NSObjectProtocol
import kotlin.coroutines.resume
import kotlin.time.Clock
import kotlin.time.TimeSource

private const val MAX_PLAYLIST_IMPORT_BYTES = 16L * 1024L * 1024L

private val oauthCallbackUrlChannel = Channel<String>(Channel.BUFFERED)
private val oauthCallbackUrls = oauthCallbackUrlChannel.receiveAsFlow()

fun handleOpenUrl(url: String) {
    oauthCallbackUrlChannel.trySend(url)
}

@OptIn(ExperimentalForeignApi::class)
@Composable
fun MetrolistIOSApp() {
    val scope = rememberCoroutineScope()
    val presenter = LocalUIViewController.current
    DisposableEffect(Unit) {
        val networkMonitor = IosPlaybackNetworkMonitor()
        networkMonitor.start()
        onDispose { networkMonitor.stop() }
    }
    val koin = remember { KoinPlatform.getKoin() }
    var databaseGeneration by remember { mutableIntStateOf(0) }
    var appGeneration by remember { mutableIntStateOf(0) }
    var showMobileApp by remember { mutableStateOf(true) }
    var showOnboarding by remember { mutableStateOf(!Settings.shared.iosOnboardingComplete) }
    var initialSettingsPage by remember { mutableStateOf<SettingsPageId?>(null) }
    var pendingMobileDispose by remember { mutableStateOf<CompletableDeferred<Unit>?>(null) }
    var sessionClosed by remember { mutableStateOf(false) }
    val latestPendingDispose by rememberUpdatedState(pendingMobileDispose)
    val database = remember(koin, databaseGeneration) { koin.get<MusicDatabase>() }
    LaunchedEffect(database) {
        try {
            migrateIosPlaylistCovers(database)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Logger.w("PlaylistCover", "Managed cover migration failed: ${error::class.simpleName}")
        }
    }
    val player = remember(koin, databaseGeneration) { koin.get<MusicPlayer>() }
    val appJob = remember(databaseGeneration) { SupervisorJob(scope.coroutineContext[Job]) }
    val appScope = remember(appJob) { CoroutineScope(scope.coroutineContext + appJob) }
    LaunchedEffect(database, appScope) {
        SongLikeState.recoverDatabaseLikes(database)
        SongMetadataState.bind(database, appScope)
    }
    val downloadManager = remember(database, appScope) { IosAudioDownloadManager(database, appScope) }
    val playbackController =
        remember(player, appScope, database, downloadManager) {
            IosPlaybackController(
                player,
                koin.get<StreamResolver>(),
                database,
                appScope,
                audioStorage = downloadManager,
                musicVideoResolver = koin.get(),
            )
        }
    IosDiscordPresenceEffect(playbackController.state, player.playbackSpeed)
    val listenTogetherClient = remember(appScope) { createHttpClient() }
    val listenTogetherMessages by rememberUpdatedState(listenTogetherPresentationMessages())
    val listenTogetherManager =
        remember(playbackController, appScope, listenTogetherClient) {
            val monotonicOrigin = TimeSource.Monotonic.markNow()
            val timestampFormatter =
                NSDateFormatter().apply {
                    timeStyle = NSDateFormatterMediumStyle
                    dateStyle = NSDateFormatterNoStyle
                }
            ListenTogetherManager(
                callerScope = appScope,
                httpClient = listenTogetherClient,
                playback = IosListenTogetherPlayback(playbackController, player, appScope),
                resolver =
                    ListenTogetherTrackResolver { track ->
                        try {
                            GlobalYouTubeRepository.instance.getSongMetadata(track.id) ?: track.toSongItem()
                        } catch (error: CancellationException) {
                            throw error
                        } catch (_: Exception) {
                            track.toSongItem()
                        }
                    },
                monotonicClockMs = { monotonicOrigin.elapsedNow().inWholeMilliseconds },
                wallClockMs = { Clock.System.now().toEpochMilliseconds() },
                timestampFormatter = { timestampFormatter.stringFromDate(NSDate.dateWithTimeIntervalSince1970(it / 1000.0)) },
                presentationText = ListenTogetherPresentationText { listenTogetherMessages.getValue(it) },
                logger =
                    ListenTogetherLogger { level, message ->
                        when (level) {
                            ListenTogetherLogLevel.DEBUG -> Logger.d("ListenTogether", message)
                            ListenTogetherLogLevel.INFO -> Logger.i("ListenTogether", message)
                            ListenTogetherLogLevel.WARNING -> Logger.w("ListenTogether", message)
                            ListenTogetherLogLevel.ERROR -> Logger.e("ListenTogether", message)
                        }
                    },
            ).also(playbackController::bindListenTogether)
        }
    val backupManager = remember { IosBackupRestoreManager() }
    var backupStatus by remember { mutableStateOf<BackupRestoreStatus?>(null) }
    var backupFailure by remember { mutableStateOf<Throwable?>(null) }
    var backupBusy by remember { mutableStateOf(false) }
    var foreignImportStatus by remember { mutableStateOf<String?>(null) }
    var showPlaylistImporter by rememberSaveable { mutableStateOf(false) }
    val storageUsage by downloadManager.storageUsageUpdates.collectAsState()
    val cleanupSession by rememberUpdatedState {
        if (!sessionClosed) {
            listenTogetherManager.close()
            listenTogetherClient.close()
            downloadManager.close()
            playbackController.releaseMusicVideoSurfacePlayer()
            appJob.cancel()
            player.release()
        }
    }
    DisposableEffect(Unit) {
        onDispose { cleanupSession() }
    }
    LaunchedEffect(downloadManager, databaseGeneration) {
        if (databaseGeneration > 0) downloadManager.resumeAll()
    }
    val localeChoices by produceState(emptyList<SettingsChoice>() to emptyList<SettingsChoice>()) {
        value = withContext(Dispatchers.Default) { iosContentLocaleChoices() }
    }
    val systemLocale =
        remember {
            val current = NSLocale.currentLocale
            val preferred =
                (NSLocale.preferredLanguages.firstOrNull() as? String)
                    ?.takeIf(String::isNotBlank)
                    ?: current.localeIdentifier
            val languageTag = preferred.replace('_', '-')
            languageTag to closestContentCountry(languageTag, current.countryCode.orEmpty())
        }

    suspend fun closeDatabaseForRestore() {
        val disposed = CompletableDeferred<Unit>()
        pendingMobileDispose = disposed
        showMobileApp = false
        disposed.await()

        var shutdownFailure: Throwable? = null
        runCatching { listenTogetherManager.leaveRoomAndWait() }.onFailure { shutdownFailure = it }
        listenTogetherManager.close()
        listenTogetherClient.close()
        runCatching { downloadManager.pauseAll() }.onFailure { shutdownFailure = it }
        downloadManager.close()
        player.stop()
        playbackController.releaseMusicVideoSurfacePlayer()
        appJob.cancelAndJoin()
        player.release()
        database.close()
        sessionClosed = true
        shutdownFailure?.let { throw it }
    }

    suspend fun reopenDatabaseAfterRestore() {
        MetrolistPlatform.reopenDatabaseAfterRestore()
        databaseGeneration++
        sessionClosed = false
        pendingMobileDispose = null
        showMobileApp = true
    }

    fun exportBackup() {
        if (backupBusy) return
        backupBusy = true
        backupFailure = null
        backupStatus = BackupRestoreStatus.BUSY
        scope.launch {
            val directory = NSTemporaryDirectory() + NSUUID().UUIDString
            val destination = "$directory/${defaultBackupFilename(getString(Res.string.backup_filename_prefix))}.zip"
            val result =
                if (NSFileManager.defaultManager.createDirectoryAtPath(
                        directory,
                        withIntermediateDirectories = false,
                        attributes = null,
                        error = null,
                    )
                ) {
                    backupManager.backup(destination)
                } else {
                    Result.failure(IllegalStateException("Could not create backup directory"))
                }
            if (result.isSuccess) {
                IosNativeFiles.shareFile(presenter, destination) { shared ->
                    backupBusy = false
                    backupStatus = if (shared) BackupRestoreStatus.BACKUP_CREATED else null
                    NSFileManager.defaultManager.removeItemAtPath(directory, error = null)
                }
            } else {
                NSFileManager.defaultManager.removeItemAtPath(directory, error = null)
                backupBusy = false
                backupStatus = BackupRestoreStatus.BACKUP_FAILED
            }
        }
    }

    fun importBackup() {
        if (backupBusy) return
        backupBusy = true
        backupFailure = null
        backupStatus = BackupRestoreStatus.BUSY
        IosNativeFiles.pickDocument(presenter, listOf("public.data")) { url ->
            if (url == null) {
                backupBusy = false
                backupStatus = null
                return@pickDocument
            }
            scope.launch {
                var copiedPath: String? = null
                try {
                    val sourcePath =
                        withContext(Dispatchers.IO) {
                            IosNativeFiles
                                .copyPickedFile(
                                    url,
                                    BackupArchiveLimits.TOTAL_UNCOMPRESSED_BYTES + 2L * 1024L * 1024L,
                                ).also { copiedPath = it }
                        }
                    copiedPath = sourcePath
                    val generationBeforeRestore = databaseGeneration
                    val result =
                        backupManager.restore(
                            sourcePath = sourcePath,
                            closeDatabase = ::closeDatabaseForRestore,
                            reopenDatabase = { reopenDatabaseAfterRestore() },
                        )
                    if (sessionClosed) reopenDatabaseAfterRestore()
                    if (databaseGeneration == generationBeforeRestore && result.isSuccess) {
                        MetrolistPlatform.refreshRestoredSettings()
                        appGeneration++
                    }
                    backupFailure = result.exceptionOrNull()
                    backupStatus = if (result.isSuccess) null else BackupRestoreStatus.RESTORE_FAILED
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    backupFailure = error
                    if (sessionClosed) runCatching { reopenDatabaseAfterRestore() }
                    backupStatus = BackupRestoreStatus.RESTORE_FAILED
                } finally {
                    copiedPath?.let { path ->
                        withContext(NonCancellable + Dispatchers.IO) {
                            NSFileManager.defaultManager.removeItemAtPath(path, error = null)
                        }
                    }
                    backupBusy = false
                }
            }
        }
    }

    fun importForeignBackup() {
        if (backupBusy) return
        backupBusy = true
        backupStatus = BackupRestoreStatus.BUSY
        foreignImportStatus = null
        IosNativeFiles.pickDocument(presenter, listOf("public.data")) { url ->
            if (url == null) {
                backupBusy = false
                backupStatus = null
                return@pickDocument
            }
            scope.launch {
                var copiedPath: String? = null
                val result =
                    try {
                        val sourcePath =
                            withContext(Dispatchers.IO) {
                                IosNativeFiles
                                    .copyPickedFile(
                                        url,
                                        BackupArchiveLimits.TOTAL_UNCOMPRESSED_BYTES + 2L * 1024L * 1024L,
                                    ).also { copiedPath = it }
                            }
                        copiedPath = sourcePath
                        backupManager.importForeignBackup(sourcePath, database)
                    } catch (error: CancellationException) {
                        throw error
                    } catch (error: Exception) {
                        Result.failure(error)
                    } finally {
                        copiedPath?.let { path ->
                            withContext(NonCancellable + Dispatchers.IO) {
                                NSFileManager.defaultManager.removeItemAtPath(path, error = null)
                            }
                        }
                    }
                foreignImportStatus = result.foreignImportMessage()
                backupBusy = false
                backupStatus = null
            }
        }
    }

    suspend fun pickPlaylistFile(): PlaylistImportFile? {
        val url =
            suspendCancellableCoroutine<NSURL?> { continuation ->
                IosNativeFiles.pickDocument(presenter, listOf("public.data")) { picked ->
                    if (continuation.isActive) continuation.resume(picked)
                }
                continuation.invokeOnCancellation { IosNativeFiles.cancelDocumentPicker() }
            } ?: return null
        val name = requireNotNull(url.lastPathComponent)
        val bytes = withContext(Dispatchers.IO) { IosNativeFiles.readPickedFile(url, MAX_PLAYLIST_IMPORT_BYTES) }
        return PlaylistImportFile(name, bytes)
    }

    Box(Modifier.fillMaxSize()) {
        if (showOnboarding) {
            Settings.shared.systemInDarkTheme = isSystemInDarkTheme()
            MetrolistTheme(
                seedColor = parseThemeSeedColor(Settings.shared.selectedThemeColor),
                darkTheme = Settings.shared.getCurrentThemeIsDark(),
                pureBlack = Settings.shared.isPureBlackTheme(),
            ) {
                IosOnboarding { consent, login, restore ->
                    if (isCrashReportingAvailable()) Settings.shared.sentryEnabled = consent
                    Settings.shared.iosOnboardingComplete = true
                    showOnboarding = false
                    if (login) handleOpenUrl("muse://login")
                    if (restore) initialSettingsPage = SettingsPageId.BACKUP_RESTORE
                }
            }
        } else if (showMobileApp) {
            LifecycleResumeEffect(player) {
                player.setUiObserving(true)
                onPauseOrDispose { player.setUiObserving(false) }
            }
            DisposableEffect(Unit) {
                onDispose { latestPendingDispose?.complete(Unit) }
            }
            key(appGeneration) {
                MobileApp(
                    onExportStatistics = { IosNativeFiles.shareImage(presenter, it) },
                    onPickPlaylistCover = { pickIosPlaylistCover(presenter) },
                    onSavePlaylistCover = { playlist, bytes ->
                        try {
                            val repository = GlobalYouTubeRepository.instance
                            if (playlist.isLocal) {
                                val entity = checkNotNull(database.dao().findPlaylistEntity(playlist.id))
                                storeIosPlaylistCover(bytes, entity.customThumbnailUrl) { path ->
                                    if (path == null) {
                                        removeStoredPlaylistCover(database, repository, playlist.id).getOrThrow()
                                    } else {
                                        setStoredPlaylistCover(database, repository, playlist.id, path).getOrThrow()
                                    }
                                }
                            } else if (bytes == null) {
                                removeStoredPlaylistCover(database, repository, playlist.id, playlist).getOrThrow()
                            } else {
                                uploadStoredPlaylistCover(database, repository, playlist.id, bytes, playlist).getOrThrow()
                            }
                            Result.success(Unit)
                        } catch (error: CancellationException) {
                            throw error
                        } catch (error: Exception) {
                            Result.failure(error)
                        }
                    },
                    database = database,
                    initialSettingsPage = initialSettingsPage,
                    playbackController = playbackController,
                    listenTogetherManager = listenTogetherManager,
                    playerServices = remember(presenter) { IosPlayerPlatformServices(presenter) },
                    lyricsShareAction = { iosLyricsShareAction(it, presenter) },
                    touchExplorationEnabled = rememberVoiceOverEnabled(),
                    resolveAndPlayVideo = playbackController::resolveAndPlayVideo,
                    systemLanguage = systemLocale.first,
                    systemCountry = systemLocale.second,
                    contentLanguageChoices = localeChoices.first,
                    contentCountryChoices = localeChoices.second,
                    appLanguageChoices = localeChoices.first,
                    storageTotalBytes = storageUsage?.totalBytes,
                    storageFreeBytes = storageUsage?.freeBytes,
                    onRemoveDownloads = { ids -> appScope.async { ids.count { downloadManager.removeDownload(it) } }.await() },
                    onDownloadSongs = { songs -> appScope.async { downloadManager.downloadAll(songs) }.await() },
                    onDownloadPlaylist = { playlistId -> appScope.async { downloadManager.downloadPlaylist(playlistId) }.await() },
                    onRemovePlaylistDownloads = { playlistId, ids ->
                        appScope
                            .async {
                                downloadManager.stopTrackingPlaylist(playlistId)
                                ids.count { downloadManager.removeDownload(it) }
                            }.await()
                    },
                    iosSettingsPageContent = { page, onDownloads ->
                        when (page) {
                            SettingsPageId.STORAGE -> {
                                Column(
                                    Modifier
                                        .fillMaxSize()
                                        .verticalScroll(
                                            rememberScrollState(),
                                        ).padding(bottom = LocalSettingsPageBottomPadding.current),
                                ) {
                                    IosStorageSettingsPage(downloadManager, onDownloads)
                                }
                            }

                            SettingsPageId.BACKUP_RESTORE -> {
                                Column(
                                    Modifier
                                        .fillMaxSize()
                                        .verticalScroll(
                                            rememberScrollState(),
                                        ).padding(bottom = LocalSettingsPageBottomPadding.current),
                                ) {
                                    if (showPlaylistImporter) {
                                        TextButton(onClick = { showPlaylistImporter = false }, shapes = ButtonDefaults.shapes()) {
                                            Text(stringResource(Res.string.back))
                                        }
                                        IosPlaylistImportSettings(
                                            database = database,
                                            onPickPlaylistFile = ::pickPlaylistFile,
                                            scrollable = false,
                                        )
                                    } else {
                                        BackupRestorePage(
                                            onExport = ::exportBackup,
                                            onImport = ::importBackup,
                                            onImportForeign = ::importForeignBackup,
                                            status = backupStatus,
                                            failure = backupFailure,
                                            onAutoBackupSettingsChanged = ::notifyIosAutomaticBackupSettingsChanged,
                                            foreignImportStatus = foreignImportStatus,
                                        )
                                        Text(
                                            stringResource(Res.string.playlist_imports_title),
                                            modifier = Modifier.padding(horizontal = 20.dp),
                                            style = MaterialTheme.typography.titleMedium,
                                        )
                                        Button(
                                            onClick = { showPlaylistImporter = true },
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                                            enabled = !backupBusy,
                                            shapes = ButtonDefaults.shapes(),
                                        ) {
                                            Text(stringResource(Res.string.import_playlist))
                                        }
                                    }
                                }
                            }

                            else -> {
                            }
                        }
                    },
                    formatHistoryMonth = { formatHistoryMonth(it) },
                    oauthCallbackUrls = oauthCallbackUrls,
                    loginContent = { switching, onBack, onComplete, onTokenLogin ->
                        IosLoginScreen(switching, onBack, onComplete, onTokenLogin)
                    },
                )
            }
            IosUpdateNotice()
        }
        IosPageBoundTokenHost()
    }
}

private fun iosContentLocaleChoices(): Pair<List<SettingsChoice>, List<SettingsChoice>> {
    val locale = NSLocale.currentLocale
    val languages =
        BuildConfig.BUNDLED_LANGUAGE_TAGS
            .split(',')
            .filter(String::isNotBlank)
            .map { code -> SettingsChoice(code, NSLocale(code).displayNameForKey(NSLocaleIdentifier, code) ?: code) }
            .sortedBy(SettingsChoice::label)
    val countries =
        contentCountryCodes
            .asSequence()
            .map { code -> SettingsChoice(code, locale.displayNameForKey(NSLocaleCountryCode, code) ?: code) }
            .sortedBy(SettingsChoice::label)
            .toList()
    return languages to countries
}

fun MainViewController(): UIViewController {
    if (IosCrashRecovery.hasPendingReport) {
        return ComposeUIViewController {
            val presenter = LocalUIViewController.current
            var showingReport by remember { mutableStateOf(true) }
            if (showingReport) {
                MaterialTheme {
                    CrashLogsScreen(IosCrashRecovery, onClose = {
                        IosCrashRecovery.acknowledge()
                        MetrolistPlatform.initialize()
                        notifyIosAutomaticBackupSettingsChanged()
                        configureIosImageLoader()
                        showingReport = false
                    }, onShare = { IosNativeFiles.shareText(presenter, it) })
                }
            } else {
                MetrolistIOSApp()
            }
        }
    }
    configureIosImageLoader()
    return ComposeUIViewController { MetrolistIOSApp() }
}

private var imageMemoryWarningObserver: NSObjectProtocol? = null

@OptIn(ExperimentalCoilApi::class)
internal fun configureIosImageLoader() {
    if (imageMemoryWarningObserver == null) {
        imageMemoryWarningObserver =
            NSNotificationCenter.defaultCenter.addObserverForName(
                name = UIApplicationDidReceiveMemoryWarningNotification,
                `object` = null,
                queue = NSOperationQueue.mainQueue,
            ) {
                SingletonImageLoader.get(PlatformContext.INSTANCE).memoryCache?.clear()
            }
    }
    SingletonImageLoader.setSafe { context ->
        lateinit var imageLoader: ImageLoader
        imageLoader =
            ImageLoader
                .Builder(context)
                .components {
                    add(IosPlaylistCoverMapper())
                    add(ArtworkSizingInterceptor { imageLoader })
                    add(KtorNetworkFetcherFactory(concurrentRequestStrategy = { DeDupeConcurrentRequestStrategy() }))
                }.diskCache {
                    ResizableImageDiskCache(
                        requireNotNull(
                            NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true).firstOrNull() as? String,
                        ).toPath() /
                            "coil3_disk_cache",
                        Settings.shared.maxImageCacheSize * 1024L * 1024L,
                    )
                }.build()
        imageLoader
    }
}
