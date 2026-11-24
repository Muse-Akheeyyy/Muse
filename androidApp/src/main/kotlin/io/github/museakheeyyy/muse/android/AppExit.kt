package io.github.museakheeyyy.muse.android

import android.app.Activity
import io.github.museakheeyyy.muse.android.utils.isAndroidTv
import io.github.museakheeyyy.muse.shared.settings.Settings

internal fun Activity.exitApp() {
    if (isAndroidTv() && Settings.shared.stopMusicOnTaskClear) {
        // TV has no recents gesture; a confirmed root Back exit removes the task instead.
        finishAndRemoveTask()
    } else {
        finish()
    }
}
