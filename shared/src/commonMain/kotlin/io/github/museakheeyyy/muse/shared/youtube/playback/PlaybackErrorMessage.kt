package io.github.museakheeyyy.muse.shared.youtube.playback

import com.metrolist.innertubex.extraction.StreamResolveException
import com.metrolist.innertubex.sabr.SabrFailureKind
import com.metrolist.innertubex.sabr.findSabrProtocolException
import io.github.museakheeyyy.muse.shared.generated.resources.Res
import io.github.museakheeyyy.muse.shared.generated.resources.playback_error_age_restricted
import io.github.museakheeyyy.muse.shared.generated.resources.playback_error_explicit_sign_in
import io.github.museakheeyyy.muse.shared.generated.resources.playback_error_explicit_signed_in
import io.github.museakheeyyy.muse.shared.generated.resources.playback_error_network
import io.github.museakheeyyy.muse.shared.generated.resources.playback_error_no_music_video
import io.github.museakheeyyy.muse.shared.generated.resources.playback_error_no_playable_stream
import io.github.museakheeyyy.muse.shared.generated.resources.playback_error_sabr_attestation
import io.github.museakheeyyy.muse.shared.generated.resources.playback_error_track_unavailable
import io.github.museakheeyyy.muse.shared.generated.resources.playback_error_unknown
import io.github.museakheeyyy.muse.shared.generated.resources.playback_error_unknown_resolution
import org.jetbrains.compose.resources.StringResource

fun Throwable.playbackErrorResource(signedIn: Boolean): StringResource {
    if (findSabrProtocolException()?.kind == SabrFailureKind.ATTESTATION_REQUIRED) return Res.string.playback_error_sabr_attestation
    val failure =
        generateSequence(this) { it.cause?.takeUnless { cause -> cause === it } }
            .filterIsInstance<StreamResolveException>()
            .firstOrNull()
    return when (failure?.reason) {
        StreamResolveException.Reason.EXPLICIT_UNSUPPORTED -> {
            if (signedIn) Res.string.playback_error_explicit_signed_in else Res.string.playback_error_explicit_sign_in
        }

        StreamResolveException.Reason.NO_PLAYABLE_STREAM -> {
            Res.string.playback_error_no_playable_stream
        }

        StreamResolveException.Reason.UNAVAILABLE -> {
            Res.string.playback_error_track_unavailable
        }

        StreamResolveException.Reason.AGE_RESTRICTED -> {
            Res.string.playback_error_age_restricted
        }

        StreamResolveException.Reason.NO_MUSIC_VIDEO -> {
            Res.string.playback_error_no_music_video
        }

        StreamResolveException.Reason.NETWORK -> {
            Res.string.playback_error_network
        }

        StreamResolveException.Reason.UNKNOWN -> {
            Res.string.playback_error_unknown_resolution
        }

        null -> {
            Res.string.playback_error_unknown
        }
    }
}
