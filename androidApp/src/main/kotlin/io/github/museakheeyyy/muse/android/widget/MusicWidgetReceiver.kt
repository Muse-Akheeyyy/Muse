/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package io.github.museakheeyyy.muse.android.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import io.github.museakheeyyy.muse.android.App
import io.github.museakheeyyy.muse.android.playback.MusicService
import io.github.museakheeyyy.muse.android.utils.launchAsync
import kotlinx.coroutines.CancellationException
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class MusicWidgetReceiver :
    AppWidgetProvider(),
    KoinComponent {
    private val widgetManager: MetrolistWidgetManager by inject()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        requestWidgetRefresh(context, appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        requestWidgetRefresh(context, intArrayOf(appWidgetId))
    }

    override fun onDeleted(
        context: Context,
        appWidgetIds: IntArray,
    ) {
        requestWidgetRefresh(context, intArrayOf())
    }

    private fun requestWidgetRefresh(
        context: Context,
        appWidgetIds: IntArray,
    ) {
        launchAsync {
            try {
                (context.applicationContext as App).startup.await()
                widgetManager.refreshProgressDemand()
                if (MusicService.isRunning) {
                    requestServiceSnapshot(context)
                } else {
                    widgetManager.updateIdleMusicWidgets(appWidgetIds)
                    if (MusicService.isRunning) requestServiceSnapshot(context)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Timber.e(error, "Failed to refresh music widgets")
            }
        }
    }

    private fun requestServiceSnapshot(context: Context) {
        context.startService(
            Intent(context, MusicService::class.java).setAction(ACTION_UPDATE_WIDGET),
        )
    }

    companion object {
        const val ACTION_PLAY_PAUSE = "io.github.museakheeyyy.muse.widget.PLAY_PAUSE"
        const val ACTION_LIKE = "io.github.museakheeyyy.muse.widget.LIKE"
        const val ACTION_NEXT = "io.github.museakheeyyy.muse.widget.NEXT"
        const val ACTION_PREVIOUS = "io.github.museakheeyyy.muse.widget.PREVIOUS"
        const val ACTION_UPDATE_WIDGET = "io.github.museakheeyyy.muse.widget.UPDATE_WIDGET"
    }
}
