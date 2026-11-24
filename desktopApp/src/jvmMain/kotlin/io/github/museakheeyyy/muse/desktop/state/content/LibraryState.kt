/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package io.github.museakheeyyy.muse.desktop.state.content

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.museakheeyyy.muse.desktop.state.app.AppState
import io.github.museakheeyyy.muse.desktop.state.app.DesktopNavigation
import io.github.museakheeyyy.muse.desktop.storage.DownloadManager
import io.github.museakheeyyy.muse.desktop.storage.DownloadedFile
import io.github.museakheeyyy.muse.shared.settings.LibraryViewType
import io.github.museakheeyyy.muse.shared.settings.NavItem
import io.github.museakheeyyy.muse.shared.settings.Settings
import io.github.museakheeyyy.muse.shared.settings.desktopDefaultOpenTab
import io.github.museakheeyyy.muse.shared.settings.desktopSidebarNavItems
import io.github.museakheeyyy.muse.shared.settings.updateDefaultOpenTab
import io.github.museakheeyyy.muse.shared.ui.navigation.Screen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

object LibraryState {
    var downloadStates by mutableStateOf(DownloadManager.downloadStates.value)
    var downloadProgress by mutableStateOf(DownloadManager.downloadProgress.value)
    var downloadedFiles by mutableStateOf<List<DownloadedFile>>(DownloadManager.downloadedFiles.value)
        private set
    var isDownloadManagerLoaded by mutableStateOf(false)
    var sessionCompletedCount by mutableStateOf(DownloadManager.sessionCompletedCount.value)
    var downloadTitles by mutableStateOf(DownloadManager.downloadTitles.value)

    init {
        AppState.scope.launch {
            DownloadManager.downloadStates.collectLatest { states ->
                downloadStates = states
            }
        }
        AppState.scope.launch {
            DownloadManager.downloadProgress.collectLatest { progress ->
                downloadProgress = progress
            }
        }
        AppState.scope.launch {
            DownloadManager.downloadTitles.collectLatest { titles ->
                downloadTitles = titles
            }
        }
        AppState.scope.launch {
            DownloadManager.sessionCompletedCount.collectLatest { count ->
                sessionCompletedCount = count
            }
        }
        AppState.scope.launch {
            DownloadManager.downloadedFiles.collectLatest { files ->
                downloadedFiles = files
                isDownloadManagerLoaded = true
            }
        }
    }

    var libraryViewType by mutableStateOf(Settings.shared.libraryViewType)
        private set

    fun updateLibraryViewType(type: LibraryViewType) {
        libraryViewType = type
        Settings.shared.libraryViewType = type
    }

    var sidebarNavItems by mutableStateOf(Settings.shared.desktopSidebarNavItems())

    var isEditingSidebar by mutableStateOf(false)

    fun saveSidebarNavItems(items: List<NavItem>) {
        sidebarNavItems = items
        val stringValue = items.joinToString(",") { "${it.id}:${it.visible}" }
        Settings.shared.sidebarNavItems = stringValue
        Settings.shared.updateDefaultOpenTab(Settings.shared.desktopDefaultOpenTab())
    }

    fun navigateToAutoPlaylist(type: String) {
        DesktopNavigation.navigate(Screen.AutoPlaylist(type))
    }
}
