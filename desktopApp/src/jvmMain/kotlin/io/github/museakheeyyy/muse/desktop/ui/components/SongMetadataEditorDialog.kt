package io.github.museakheeyyy.muse.desktop.ui.components

import androidx.compose.runtime.Composable
import io.github.museakheeyyy.muse.desktop.state.app.AppState
import io.github.museakheeyyy.muse.shared.db.MusicDatabase
import io.github.museakheeyyy.muse.shared.model.SongItem
import io.github.museakheeyyy.muse.shared.model.editSongMetadata
import io.github.museakheeyyy.muse.shared.model.resetSongMetadata
import io.github.museakheeyyy.muse.shared.state.SongMetadataState
import io.github.museakheeyyy.muse.shared.ui.menu.EditSongDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.koin.core.context.GlobalContext

@Composable
internal fun SongMetadataEditorDialog(
    song: SongItem,
    onSaved: () -> Unit,
    onDismiss: () -> Unit,
) {
    val effectiveSong = SongMetadataState.apply(song)
    EditSongDialog(
        title = effectiveSong.title,
        artist =
            effectiveSong.artists
                .firstOrNull()
                ?.name
                .orEmpty(),
        onSave = { title, artist ->
            AppState.scope
                .async(Dispatchers.IO) {
                    editSongMetadata(GlobalContext.get().get<MusicDatabase>(), song, title, artist)
                }.await()
        },
        onSaved = onSaved,
        onDismiss = onDismiss,
        onReset = {
            AppState.scope
                .async(Dispatchers.IO) {
                    resetSongMetadata(GlobalContext.get().get<MusicDatabase>(), song.id)
                }.await()
        },
    )
}
