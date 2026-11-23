package io.github.museakheeyyy.muse.android.ui.menu

import androidx.compose.runtime.Composable
import io.github.museakheeyyy.muse.shared.model.PodcastItem
import io.github.museakheeyyy.muse.shared.ui.menu.MediaDownloadState
import io.github.museakheeyyy.muse.shared.ui.menu.MediaMenuSheetContent
import io.github.museakheeyyy.muse.shared.ui.menu.podcastMediaMenuModel

@Composable
fun PodcastMenu(
    podcast: PodcastItem,
    onDismiss: () -> Unit,
    onPlay: (() -> Unit)? = null,
    onShuffle: (() -> Unit)? = null,
    onPlayOldestFirst: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    onPlayNext: (() -> Unit)? = null,
    onAddToQueue: (() -> Unit)? = null,
    onDownload: (() -> Unit)? = null,
    onRemoveFromLibrary: (() -> Unit)? = null,
) {
    val download = rememberMediaDownloadAction(podcast, onDismiss, onDownload)
    val pin = rememberSpeedDialToggle(podcast)
    val model =
        podcastMediaMenuModel(
            podcast = podcast,
            onDismiss = { if (!download.isConfirming()) onDismiss() },
            isPinned = pin.isPinned,
            downloadState = if (download.isDownloaded) MediaDownloadState.DOWNLOADED else MediaDownloadState.NOT_DOWNLOADED,
            onPlay = onPlay,
            onShuffle = onShuffle,
            onPlayOldestFirst = onPlayOldestFirst,
            onShare = onShare,
            onPlayNext = onPlayNext,
            onAddToQueue = onAddToQueue,
            onTogglePin = pin.toggle,
            onDownload = download.invoke,
            onRemoveFromLibrary = onRemoveFromLibrary,
        )
    MediaMenuBottomSheet(model = model, onDismissRequest = onDismiss, canDismiss = { !download.isConfirming() }) {
        MediaMenuSheetContent(model)
    }
    download.confirmationDialog(onDismiss)
}
