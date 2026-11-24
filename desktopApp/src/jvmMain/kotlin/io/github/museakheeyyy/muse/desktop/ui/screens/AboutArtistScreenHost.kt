package io.github.museakheeyyy.muse.desktop.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.museakheeyyy.muse.desktop.state.app.AppState
import io.github.museakheeyyy.muse.desktop.state.content.ArtistState
import io.github.museakheeyyy.muse.desktop.utils.saveArtistPhoto
import io.github.museakheeyyy.muse.shared.model.ArtistItem
import io.github.museakheeyyy.muse.shared.settings.Settings
import io.github.museakheeyyy.muse.shared.ui.navigation.LocalNavigator
import io.github.museakheeyyy.muse.shared.ui.screens.AboutArtistContent
import io.github.museakheeyyy.muse.shared.ui.screens.ArtistPhotoActions
import io.github.museakheeyyy.muse.shared.viewmodels.AboutArtistViewModel
import io.github.museakheeyyy.muse.shared.viewmodels.artistCatalogTitles
import kotlinx.coroutines.launch

@Composable
fun AboutArtistScreenHost(
    artistId: String,
    artistName: String,
    initialInfo: ArtistItem? = null,
) {
    val navigator = LocalNavigator.current
    val scope = rememberCoroutineScope()
    val viewModel = remember(artistId, artistName) { AboutArtistViewModel(artistId, artistName, scope) }
    val uiState by viewModel.uiState.collectAsState()
    val catalogTitles = remember(ArtistState.artistData) { artistCatalogTitles(ArtistState.artistData) }
    LaunchedEffect(viewModel, catalogTitles, ArtistState.isArtistLoading, Settings.shared.lastResolvedContentLanguage) {
        if (!ArtistState.isArtistLoading) viewModel.load(catalogTitles)
    }
    val photoActions =
        remember(artistName) {
            ArtistPhotoActions(
                share = { AppState.shareLink(it, artistName) },
                save = { saveArtistPhoto(it, artistName) },
                showMessage = { message -> AppState.scope.launch { AppState.snackbarHostState.showSnackbar(message) } },
            )
        }
    AboutArtistContent(
        artistName = artistName,
        info = ArtistState.artistData["header"]?.firstOrNull() as? ArtistItem ?: initialInfo,
        uiState = uiState,
        photoActions = photoActions,
        onBackClick = { navigator.goBack() },
    )
}
