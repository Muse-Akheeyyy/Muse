package io.github.museakheeyyy.muse.android.ui.menu

import androidx.compose.runtime.Composable
import io.github.museakheeyyy.muse.shared.db.MusicDatabase
import io.github.museakheeyyy.muse.shared.model.YTItem
import io.github.museakheeyyy.muse.shared.ui.menu.SpeedDialToggle
import org.koin.compose.koinInject
import io.github.museakheeyyy.muse.shared.ui.menu.rememberSpeedDialToggle as rememberSharedSpeedDialToggle

@Composable
internal fun rememberSpeedDialToggle(item: YTItem): SpeedDialToggle = rememberSharedSpeedDialToggle(item, koinInject<MusicDatabase>())
