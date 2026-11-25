/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.museakheeyyy.muse.android.ui.menu

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.github.museakheeyyy.muse.android.ui.component.AdaptiveModalBottomSheet
import io.github.museakheeyyy.muse.android.ui.component.Material3MenuGroup
import io.github.museakheeyyy.muse.android.ui.component.Material3MenuItemData
import io.github.museakheeyyy.muse.shared.api.lyrics.LyricsUtils
import io.github.museakheeyyy.muse.shared.generated.resources.Res
import io.github.museakheeyyy.muse.shared.generated.resources.ai_translation_for_track
import io.github.museakheeyyy.muse.shared.generated.resources.ai_translation_new_desc
import io.github.museakheeyyy.muse.shared.generated.resources.ai_translation_new_title
import io.github.museakheeyyy.muse.shared.generated.resources.cancel
import io.github.museakheeyyy.muse.shared.generated.resources.copied
import io.github.museakheeyyy.muse.shared.generated.resources.copy
import io.github.museakheeyyy.muse.shared.generated.resources.edit
import io.github.museakheeyyy.muse.shared.generated.resources.edit_lyrics
import io.github.museakheeyyy.muse.shared.generated.resources.lyrics_offset
import io.github.museakheeyyy.muse.shared.generated.resources.lyrics_romanization_show_original
import io.github.museakheeyyy.muse.shared.generated.resources.lyrics_romanize_title
import io.github.museakheeyyy.muse.shared.generated.resources.lyrics_show_translations
import io.github.museakheeyyy.muse.shared.generated.resources.no_lyrics_provider_results
import io.github.museakheeyyy.muse.shared.generated.resources.refetch
import io.github.museakheeyyy.muse.shared.generated.resources.save
import io.github.museakheeyyy.muse.shared.generated.resources.sources
import io.github.museakheeyyy.muse.shared.model.LyricsEntry
import io.github.museakheeyyy.muse.shared.model.LyricsProvider
import io.github.museakheeyyy.muse.shared.model.LyricsProviderResult
import io.github.museakheeyyy.muse.shared.repository.LyricsProviderSearch
import io.github.museakheeyyy.muse.shared.settings.Settings
import io.github.museakheeyyy.muse.shared.ui.components.AlertDialog
import io.github.museakheeyyy.muse.shared.ui.components.ExpressiveSwitch
import io.github.museakheeyyy.muse.shared.ui.components.PlayerMenuAction
import io.github.museakheeyyy.muse.shared.ui.components.PlayerMenuPrimaryActions
import io.github.museakheeyyy.muse.shared.ui.components.PopupVisibility
import io.github.museakheeyyy.muse.shared.ui.components.lyrics.LyricsEditorScreen
import io.github.museakheeyyy.muse.shared.ui.icons.MetrolistIcons
import io.github.museakheeyyy.muse.shared.ui.menu.ExpressiveMediaMenuHeader
import io.github.museakheeyyy.muse.shared.ui.menu.LyricsProviderDialog
import io.github.museakheeyyy.muse.shared.ui.menu.LyricsRefreshDialog
import io.github.museakheeyyy.muse.shared.ui.menu.LyricsSearchDialog
import io.github.museakheeyyy.muse.shared.ui.theme.ComponentShapes
import io.github.museakheeyyy.muse.shared.ui.utils.setText
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsMenu(
    songId: String,
    lyrics: List<LyricsEntry>,
    rawLyrics: String? = null,
    songTitle: String,
    artistName: String,
    searchTitle: String = songTitle,
    searchArtist: String = artistName,
    artworkUrl: String? = null,
    lyricsOffsetMs: Int = 0,
    manualAiTranslationEnabled: Boolean = false,
    providerSearch: LyricsProviderSearch? = null,
    onDismiss: () -> Unit,
    onRefetchLyrics: ((title: String?, artist: String?) -> Unit)? = null,
    onLoadProviderResults: ((title: String?, artist: String?, provider: LyricsProvider?) -> Unit)? = null,
    onSelectProviderResult: ((LyricsProviderResult) -> Unit)? = null,
    onShowLyricsOffset: (() -> Unit)? = null,
    onManualAiTranslationChange: (Boolean) -> Unit = {},
    onLyricsChanged: (() -> Unit)? = null,
    onNewAiTranslation: () -> Unit = {},
) {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val copiedMessage = stringResource(Res.string.copied)
    var showSearchDialog by rememberSaveable(songId) { mutableStateOf(false) }
    var editorSong by remember { mutableStateOf(songId to songTitle) }
    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    var editText by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue(text = rawLyrics.orEmpty())) }
    var showProviderDialog by rememberSaveable(songId) { mutableStateOf(false) }
    var showRefreshMenu by remember(songId) { mutableStateOf(false) }
    var providerDialogFromRefetch by rememberSaveable(songId) { mutableStateOf(false) }

    PopupVisibility(visible = showSearchDialog) {
        LyricsSearchDialog(
            initialTitle = searchTitle,
            initialArtist = searchArtist,
            onDismiss = { showSearchDialog = false },
            onSearch = { title, artist ->
                onLoadProviderResults?.invoke(title, artist, null)
                showSearchDialog = false
                providerDialogFromRefetch = false
                showProviderDialog = true
            },
            onSearchOnline = { query ->
                try {
                    context.startActivity(Intent(Intent.ACTION_WEB_SEARCH).putExtra(SearchManager.QUERY, query))
                    showSearchDialog = false
                    onDismiss()
                } catch (_: ActivityNotFoundException) {
                }
            },
        )
    }

    val romanizeAsMain = Settings.shared.isLyricsRomanizationActive && Settings.shared.lyricsRomanizeAsMain
    val toggleChecked =
        if (romanizeAsMain) {
            Settings.shared.lyricsShowOriginalLyrics
        } else {
            Settings.shared.lyricsRomanizationEnabled
        }
    var manualAiTranslationChecked by remember(manualAiTranslationEnabled) { mutableStateOf(manualAiTranslationEnabled) }

    PopupVisibility(visible = showEditDialog) {
        LyricsEditorScreen(
            songId = editorSong.first,
            songTitle = editorSong.second,
            rawLyrics = editText.text,
            onChanged = { onLyricsChanged?.invoke() },
            onDismiss = {
                showEditDialog = false
                onDismiss()
            },
        )
    }

    PopupVisibility(visible = showRefreshMenu) {
        LyricsRefreshDialog(
            onDismiss = { showRefreshMenu = false },
            onAutomatic = {
                onRefetchLyrics?.invoke(searchTitle, searchArtist)
                onDismiss()
            },
            onProvider = { provider ->
                onLoadProviderResults?.invoke(searchTitle, searchArtist, provider)
                providerDialogFromRefetch = true
                showProviderDialog = true
            },
        )
    }

    PopupVisibility(visible = showProviderDialog) {
        LyricsProviderDialog(
            search = providerSearch,
            noResultsText = stringResource(Res.string.no_lyrics_provider_results),
            onDismiss = {
                showProviderDialog = false
                if (providerDialogFromRefetch) {
                    providerDialogFromRefetch = false
                    showRefreshMenu = true
                }
            },
            onSelect = { result ->
                onSelectProviderResult?.invoke(result)
                providerDialogFromRefetch = false
                showProviderDialog = false
                onDismiss()
            },
        )
    }

    val menuCardColors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        )

    AdaptiveModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = ComponentShapes.BottomSheetShape,
    ) {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding =
                PaddingValues(
                    bottom = 8.dp + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding(),
                ),
        ) {
            item {
                ExpressiveMediaMenuHeader(
                    title = songTitle,
                    subtitle = artistName,
                    thumbnail = artworkUrl,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            item {
                PlayerMenuPrimaryActions(
                    actions =
                        listOfNotNull(
                            onLyricsChanged?.let {
                                PlayerMenuAction(
                                    icon = MetrolistIcons.Outlined.Edit,
                                    title = stringResource(Res.string.edit),
                                    onClick = {
                                        editText = TextFieldValue(LyricsUtils.lyricsForEditing(rawLyrics, lyrics))
                                        editorSong = songId to songTitle
                                        showEditDialog = true
                                    },
                                )
                            },
                            onRefetchLyrics?.let { refetch ->
                                PlayerMenuAction(
                                    icon = MetrolistIcons.Outlined.Cached,
                                    title = stringResource(Res.string.refetch),
                                    onClick = {
                                        if (onLoadProviderResults != null) {
                                            showRefreshMenu = true
                                        } else {
                                            refetch(searchTitle, searchArtist)
                                            onDismiss()
                                        }
                                    },
                                )
                            },
                            onLoadProviderResults?.let {
                                PlayerMenuAction(
                                    icon = MetrolistIcons.Outlined.Search,
                                    title = stringResource(Res.string.sources),
                                    onClick = {
                                        showSearchDialog = true
                                    },
                                )
                            },
                            PlayerMenuAction(
                                icon = MetrolistIcons.Outlined.ContentCopy,
                                title = stringResource(Res.string.copy),
                                onClick = {
                                    scope.launch {
                                        clipboard.setText(LyricsUtils.toPlainText(lyrics))
                                        Toast.makeText(context, copiedMessage, Toast.LENGTH_SHORT).show()
                                    }
                                },
                            ),
                        ),
                )
            }

            item {
                Material3MenuGroup(
                    items =
                        listOfNotNull(
                            Material3MenuItemData(
                                title = { Text(stringResource(Res.string.ai_translation_for_track)) },
                                cardColors = menuCardColors,
                                icon = {
                                    Icon(
                                        imageVector = MetrolistIcons.Outlined.Translate,
                                        contentDescription = null,
                                    )
                                },
                                onClick = {
                                    if (Settings.shared.lyricsAiTranslationEnabled) {
                                        manualAiTranslationChecked = !manualAiTranslationChecked
                                        onManualAiTranslationChange(manualAiTranslationChecked)
                                    }
                                },
                                trailingContent = {
                                    ExpressiveSwitch(
                                        checked = manualAiTranslationChecked,
                                        enabled = Settings.shared.lyricsAiTranslationEnabled,
                                        onCheckedChange = {
                                            manualAiTranslationChecked = it
                                            onManualAiTranslationChange(it)
                                        },
                                    )
                                },
                            ),
                            if (manualAiTranslationChecked) {
                                Material3MenuItemData(
                                    title = { Text(stringResource(Res.string.ai_translation_new_title)) },
                                    description = { Text(stringResource(Res.string.ai_translation_new_desc)) },
                                    cardColors = menuCardColors,
                                    icon = {
                                        Icon(
                                            imageVector = MetrolistIcons.Outlined.Cached,
                                            contentDescription = null,
                                        )
                                    },
                                    onClick = {
                                        onNewAiTranslation()
                                    },
                                )
                            } else {
                                null
                            },
                            if (lyrics.any { !it.translatedText.isNullOrBlank() || !it.backgroundTranslatedText.isNullOrBlank() }) {
                                Material3MenuItemData(
                                    title = { Text(stringResource(Res.string.lyrics_show_translations)) },
                                    cardColors = menuCardColors,
                                    icon = {
                                        Icon(
                                            imageVector = MetrolistIcons.Outlined.Translate,
                                            contentDescription = null,
                                        )
                                    },
                                    onClick = {
                                        Settings.shared.lyricsShowTranslations = !Settings.shared.lyricsShowTranslations
                                    },
                                    trailingContent = {
                                        ExpressiveSwitch(
                                            checked = Settings.shared.lyricsShowTranslations,
                                            onCheckedChange = { Settings.shared.lyricsShowTranslations = it },
                                        )
                                    },
                                )
                            } else {
                                null
                            },
                            Material3MenuItemData(
                                title = {
                                    Text(
                                        stringResource(
                                            if (romanizeAsMain) {
                                                Res.string.lyrics_romanization_show_original
                                            } else {
                                                Res.string.lyrics_romanize_title
                                            },
                                        ),
                                    )
                                },
                                cardColors = menuCardColors,
                                icon = {
                                    Icon(
                                        imageVector = MetrolistIcons.Outlined.Translate,
                                        contentDescription = null,
                                    )
                                },
                                onClick = {
                                    if (romanizeAsMain) {
                                        Settings.shared.lyricsShowOriginalLyrics = !toggleChecked
                                    } else {
                                        Settings.shared.lyricsRomanizationEnabled = !toggleChecked
                                    }
                                },
                                trailingContent = {
                                    ExpressiveSwitch(
                                        checked = toggleChecked,
                                        onCheckedChange = {
                                            if (romanizeAsMain) {
                                                Settings.shared.lyricsShowOriginalLyrics = it
                                            } else {
                                                Settings.shared.lyricsRomanizationEnabled = it
                                            }
                                        },
                                    )
                                },
                            ),
                            onShowLyricsOffset?.takeIf { lyrics.any { it.time > 0L } }?.let { showOffset ->
                                Material3MenuItemData(
                                    title = { Text(stringResource(Res.string.lyrics_offset)) },
                                    description = { Text("${lyricsOffsetMs}ms") },
                                    cardColors = menuCardColors,
                                    icon = {
                                        Icon(
                                            imageVector = MetrolistIcons.Outlined.FastForward,
                                            contentDescription = null,
                                        )
                                    },
                                    onClick = {
                                        onDismiss()
                                        showOffset()
                                    },
                                )
                            },
                        ),
                )
            }
        }
    }
}
