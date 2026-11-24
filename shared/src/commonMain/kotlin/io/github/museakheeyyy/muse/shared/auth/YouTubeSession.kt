package io.github.museakheeyyy.muse.shared.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.metrolist.innertubex.InnerTube
import io.github.museakheeyyy.muse.shared.playback.PendingYouTubePlaybackReportStore
import io.github.museakheeyyy.muse.shared.playlist.LibraryDeletionTracker
import io.github.museakheeyyy.muse.shared.settings.Settings
import io.github.museakheeyyy.muse.shared.state.SongLikeState
import io.github.museakheeyyy.muse.shared.youtube.logging.YtLogger

object YouTubeSessionState {
    var showExpiredSessionNotice by mutableStateOf(false)
        private set

    internal fun notifyExpiredSession() {
        YouTubeSyncState.clear()
        showExpiredSessionNotice = true
    }

    fun dismissExpiredSessionNotice() {
        showExpiredSessionNotice = false
    }
}

internal fun isExpiredYouTubeSessionResponse(
    statusCode: Int,
    hasAuthenticatedSession: Boolean,
    forbiddenExpiresSession: Boolean = false,
): Boolean =
    hasAuthenticatedSession &&
        (statusCode == 401 || (forbiddenExpiresSession && statusCode == 403))

internal fun invalidateYouTubeSession(innerTube: InnerTube) {
    YouTubeSyncState.clear()
    clearStoredYouTubeSession()
    innerTube.replaceSession(
        cookie = null,
        visitorData = null,
        dataSyncId = null,
        authUser = "0",
        useLoginForBrowse = false,
    )
    LibraryDeletionTracker.clear()
}

internal fun invalidateYouTubeSessionIfCurrent(
    innerTube: InnerTube,
    expectedSession: InnerTube.SessionSnapshot,
): Boolean = innerTube.clearSessionIfMatches(expectedSession)

private fun clearStoredYouTubeSession() {
    YtLogger
        .w("YouTubeSession", "Clearing stored YouTube session data from Settings")
    runCatching { Settings.shared.lastHomePageJson = "" }
    Settings.shared.applyStringBatch(
        mapOf(
            "innerTubeCookie" to "",
            "visitorData" to "",
            "dataSyncId" to "",
            "innerTubeAuthUser" to "0",
            "accountName" to "",
            "accountEmail" to "",
            "accountChannelHandle" to "",
            "accountThumbnail" to "",
        ),
    )
    runCatching { Settings.shared.lastLibraryRefreshEpoch = 0L }
    runCatching { Settings.shared.lastLibraryRefreshAccount = "" }
    runCatching { Settings.shared.lastRemoteLibraryItemKeysJson = "[]" }
    runCatching { Settings.shared.pendingRemoteLibraryRemovalKeysJson = "[]" }
    runCatching { Settings.shared.deletedPlaylistIdsJson = "[]" }
    runCatching { PendingYouTubePlaybackReportStore.clear() }
    runCatching { SongLikeState.activateCurrentAccount() }
}
