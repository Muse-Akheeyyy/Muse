package io.github.museakheeyyy.muse.desktop.ui.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import io.github.museakheeyyy.muse.desktop.state.app.AppState
import io.github.museakheeyyy.muse.desktop.state.playback.PlayerState
import io.github.museakheeyyy.muse.shared.model.SongItem
import kotlinx.coroutines.launch
import io.github.museakheeyyy.muse.shared.ui.components.SongSwipeActions as SharedSongSwipeActions

@Composable
fun SongSwipeActions(
    song: SongItem,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    SharedSongSwipeActions(
        song = song,
        onFeedback = { message ->
            AppState.snackbarHostState.currentSnackbarData?.dismiss()
            AppState.scope.launch {
                AppState.snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
            }
        },
        enabled = enabled && !PlayerState.isListenTogetherGuest(),
        content = content,
    )
}
