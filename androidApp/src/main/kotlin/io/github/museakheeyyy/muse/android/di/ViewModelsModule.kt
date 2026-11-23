package io.github.museakheeyyy.muse.android.di

import android.app.Application
import io.github.museakheeyyy.muse.android.R
import io.github.museakheeyyy.muse.android.data.PlayHistoryRepository
import io.github.museakheeyyy.muse.android.playback.AndroidPlayerPlatformServices
import io.github.museakheeyyy.muse.android.playback.PlayerConnection
import io.github.museakheeyyy.muse.android.viewmodels.AboutArtistViewModel
import io.github.museakheeyyy.muse.android.viewmodels.AccountViewModel
import io.github.museakheeyyy.muse.android.viewmodels.AlbumViewModel
import io.github.museakheeyyy.muse.android.viewmodels.ArtistViewModel
import io.github.museakheeyyy.muse.android.viewmodels.BrowseViewModel
import io.github.museakheeyyy.muse.android.viewmodels.HistoryViewModel
import io.github.museakheeyyy.muse.android.viewmodels.HomeViewModel
import io.github.museakheeyyy.muse.android.viewmodels.LibraryViewModel
import io.github.museakheeyyy.muse.android.viewmodels.PlaybackViewModel
import io.github.museakheeyyy.muse.android.viewmodels.PlaylistViewModel
import io.github.museakheeyyy.muse.android.viewmodels.SearchViewModel
import io.github.museakheeyyy.muse.android.viewmodels.StatsViewModel
import io.github.museakheeyyy.muse.shared.api.artwork.AnimatedArtworkService
import io.github.museakheeyyy.muse.shared.api.artwork.DirectCanvasProvider
import io.github.museakheeyyy.muse.shared.viewmodels.PlayerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelsModule =
    module {
        singleOf(::PlayHistoryRepository)

        viewModel { StatsViewModel(get<PlayHistoryRepository>()) }

        viewModel { params ->
            AlbumViewModel(
                params.getOrNull(),
                get(),
                get(),
                get<CoroutineScope>(),
            )
        }

        singleOf(::AnimatedArtworkService)

        viewModel { params ->
            BrowseViewModel(params[0], params[1], params[2])
        }

        viewModel { params ->
            ArtistViewModel(params.getOrNull(), get(), get(), get<CoroutineScope>())
        }

        viewModel { (id: String, name: String) -> AboutArtistViewModel(id, name) }

        viewModel {
            PlaybackViewModel(
                androidContext().applicationContext as Application,
                get(),
                get(),
            )
        }

        viewModelOf(::AccountViewModel)
        viewModel { HistoryViewModel(get<PlayHistoryRepository>(), get<CoroutineScope>()) }
        viewModelOf(::LibraryViewModel)
        viewModel {
            HomeViewModel(
                database = get(),
                likedSongsName = androidContext().getString(R.string.liked_songs),
            )
        }
        viewModelOf(::SearchViewModel)
        single { DirectCanvasProvider(get()) }

        viewModel { params ->
            PlaylistViewModel(
                params.getOrNull(),
                get(),
                get<CoroutineScope>(),
                thumbnailHint = params[1],
            )
        }

        single {
            PlayerViewModel(
                playerConnection = get<PlayerConnection>(),
                imageLoader = get(),
                platformContext = androidContext().applicationContext,
                platform = AndroidPlayerPlatformServices(androidContext().applicationContext, get(), get()),
                lyricsRepository = get(),
                animatedArtworkService = get(),
                musicVideoResolver = get(),
                viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
            )
        }
    }
