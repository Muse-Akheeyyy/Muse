package io.github.museakheeyyy.muse.shared.utils

import io.github.museakheeyyy.muse.shared.BuildConfig

fun isCrashReportingAvailable(appVersion: String = BuildConfig.APP_VERSION): Boolean = '+' !in appVersion
