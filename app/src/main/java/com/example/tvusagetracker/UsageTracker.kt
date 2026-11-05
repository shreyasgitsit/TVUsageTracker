
package com.example.tvusagetracker

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.PowerManager
import java.util.Calendar

class UsageTracker(context: Context) {

    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    private val powerManager =
        context.getSystemService(Context.POWER_SERVICE) as PowerManager

    data class DailyUsage(
        val youtubeMinutes: Long,
        val smartTubeMinutes: Long
    )

    private val youtubePackages = setOf(
        "com.google.android.youtube.tv",
        "com.google.android.youtube.tvmusic"
    )

    private val smartTubePackage = "org.smarttube.stable"

    private fun isTrackedPackage(packageName: String?): Boolean {
        return packageName in youtubePackages ||
                packageName == smartTubePackage
    }

    fun getTodayUsage(): DailyUsage {

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startOfDay = calendar.timeInMillis
        val now = System.currentTimeMillis()

        // Read a little history before midnight to recover sessions
        // that were already active when the day began.
        val queryStart = startOfDay - 24 * 60 * 60 * 1000L

        val events = usageStatsManager.queryEvents(queryStart, now)
        val event = UsageEvents.Event()

        var foregroundPackage: String? = null
        var screenInteractive = false
        var sessionStart = 0L

        var youtubeTime = 0L
        var smartTubeTime = 0L

        fun startSession(timestamp: Long) {
            if (screenInteractive &&
                isTrackedPackage(foregroundPackage) &&
                sessionStart == 0L
            ) {
                sessionStart = timestamp
            }
        }

        fun closeSession(timestamp: Long) {
            if (sessionStart == 0L) return

            val clippedStart = maxOf(sessionStart, startOfDay)
            val clippedEnd = minOf(timestamp, now)

            if (clippedEnd > clippedStart) {
                val duration = clippedEnd - clippedStart

                when (foregroundPackage) {
                    in youtubePackages -> youtubeTime += duration
                    smartTubePackage -> smartTubeTime += duration
                }
            }

            sessionStart = 0L
        }

        while (events.hasNextEvent()) {
            events.getNextEvent(event)

            when (event.eventType) {

                UsageEvents.Event.SCREEN_INTERACTIVE -> {
                    screenInteractive = true
                    startSession(event.timeStamp)
                }

                UsageEvents.Event.SCREEN_NON_INTERACTIVE -> {
                    closeSession(event.timeStamp)
                    screenInteractive = false
                }

                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    closeSession(event.timeStamp)
                    foregroundPackage = event.packageName

                    // An activity becoming resumed means the screen
                    // is interactive, even if an earlier event is absent.
                    screenInteractive = true
                    startSession(event.timeStamp)
                }

                UsageEvents.Event.ACTIVITY_PAUSED -> {
                    if (event.packageName == foregroundPackage) {
                        closeSession(event.timeStamp)
                        foregroundPackage = null
                    }
                }
            }
        }

        // Include the ongoing session only when the TV screen is interactive.
        if (powerManager.isInteractive) {
            startSession(sessionStart.takeIf { it > 0L } ?: now)
            closeSession(now)
        } else {
            closeSession(now)
        }

        return DailyUsage(
            youtubeMinutes = youtubeTime / 60_000L,
            smartTubeMinutes = smartTubeTime / 60_000L
        )
    }
}
