package io.github.museakheeyyy.muse.shared.settings

import io.github.museakheeyyy.muse.shared.constants.DiscordPendingAuthSessionKey
import io.github.museakheeyyy.muse.shared.constants.LyricsOpenRouterOAuthKey
import io.github.museakheeyyy.muse.shared.constants.LyricsOpenRouterOAuthSessionKey

internal val secureSettingsKeys =
    setOf(
        DiscordPendingAuthSessionKey,
        "innerTubeCookie",
        "visitorData",
        "dataSyncId",
        "innerTubeAuthUser",
        "lastFmSession",
        "listenBrainzToken",
        "lyricsAiApiKey",
        "lyricsAiApiKeys",
        LyricsOpenRouterOAuthKey,
        LyricsOpenRouterOAuthSessionKey,
        "lyricsDeepLApiKey",
        "accountName",
        "accountEmail",
        "accountChannelHandle",
        "accountThumbnail",
        "discordAccessToken",
        "discordRefreshToken",
        "listenTogetherDesktopSession",
        "listenTogetherServerUrl",
        "listenTogetherCustomServers",
        "listenTogetherServerMetadata",
        "deviceConnectPairedDevices",
        "listenTogetherPairedRoomCode",
        "spotifySession",
    )
