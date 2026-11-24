package io.github.museakheeyyy.muse.shared

import io.github.museakheeyyy.muse.shared.utils.isCrashReportingAvailable

/** Sideloading changes bundle IDs and drops version suffixes, so neither identifies the release. */
fun iosSentryRelease(): String = "io.github.museakheeyyy.muse.ios@${BuildConfig.APP_VERSION}"

fun iosSentrySourceRevision(): String = BuildConfig.SOURCE_REVISION

fun iosCrashReportingAvailable(): Boolean = isCrashReportingAvailable()
