package io.github.museakheeyyy.muse.desktop.state

import androidx.compose.runtime.Composable
import io.github.museakheeyyy.muse.shared.generated.resources.Res
import io.github.museakheeyyy.muse.shared.generated.resources.app_language_system
import io.github.museakheeyyy.muse.shared.ui.settings.SettingsChoice
import org.jetbrains.compose.resources.stringResource

@Composable
fun appLanguageChoices(): List<SettingsChoice> =
    DesktopLocaleManager.languages().map { language ->
        SettingsChoice(
            language.tag,
            when {
                language.isSystem -> stringResource(Res.string.app_language_system)
                else -> requireNotNull(language.displayName)
            },
        )
    }
