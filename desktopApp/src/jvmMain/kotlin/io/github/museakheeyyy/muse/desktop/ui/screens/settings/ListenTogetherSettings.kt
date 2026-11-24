package io.github.museakheeyyy.muse.desktop.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.museakheeyyy.muse.desktop.state.app.AppState
import io.github.museakheeyyy.muse.shared.listentogether.ListenTogetherManager
import io.github.museakheeyyy.muse.shared.ui.listentogether.ListenTogetherBlockedUsersScreen
import io.github.museakheeyyy.muse.shared.ui.listentogether.ListenTogetherLogsScreen
import io.github.museakheeyyy.muse.shared.ui.listentogether.ListenTogetherServerScreen
import io.github.museakheeyyy.muse.shared.ui.listentogether.ListenTogetherSettingsScreen as SharedListenTogetherSettingsScreen

private enum class ListenTogetherSettingsPage {
    ROOT,
    SERVER,
    BLOCKED_USERS,
    LOGS,
}

@Composable
internal fun ListenTogetherSettingsContent(
    manager: ListenTogetherManager,
    onNavigateBack: () -> Unit,
) {
    var page by remember { mutableStateOf(ListenTogetherSettingsPage.ROOT) }
    val goToRoot = { page = ListenTogetherSettingsPage.ROOT }

    when (page) {
        ListenTogetherSettingsPage.ROOT -> {
            SharedListenTogetherSettingsScreen(
                manager = manager,
                onNavigateBack = onNavigateBack,
                onNavigateToServer = { page = ListenTogetherSettingsPage.SERVER },
                onNavigateToBlockedUsers = { page = ListenTogetherSettingsPage.BLOCKED_USERS },
                onNavigateToLogs = { page = ListenTogetherSettingsPage.LOGS },
            )
        }

        ListenTogetherSettingsPage.SERVER -> {
            ListenTogetherServerScreen(
                manager = manager,
                onNavigateBack = goToRoot,
            )
        }

        ListenTogetherSettingsPage.BLOCKED_USERS -> {
            ListenTogetherBlockedUsersScreen(
                manager = manager,
                onNavigateBack = goToRoot,
            )
        }

        ListenTogetherSettingsPage.LOGS -> {
            ListenTogetherLogsScreen(
                manager = manager,
                onNavigateBack = goToRoot,
                onCopyText = { _, text -> AppState.copyToClipboard(text) },
            )
        }
    }
}
