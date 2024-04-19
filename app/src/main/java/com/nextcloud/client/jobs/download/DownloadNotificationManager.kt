/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2023 Alper Ozturk <alper_ozturk@proton.me>
 * SPDX-FileCopyrightText: 2023 Nextcloud GmbH
 * SPDX-License-Identifier: AGPL-3.0-or-later OR GPL-2.0-only
 */
package com.nextcloud.client.jobs.download

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.owncloud.android.R
import com.owncloud.android.operations.DownloadFileOperation
import com.owncloud.android.ui.notifications.NotificationUtils
import com.owncloud.android.utils.theme.ViewThemeUtils
import java.io.File
import java.security.SecureRandom

@Suppress("TooManyFunctions")
class DownloadNotificationManager(
    private val id: Int,
    private val context: Context,
    viewThemeUtils: ViewThemeUtils
) {
    private var currentOperationTitle: String? = null
    private var notificationBuilder: NotificationCompat.Builder =
        NotificationUtils.newNotificationBuilder(context, viewThemeUtils).apply {
            setTicker(context.getString(R.string.downloader_download_in_progress_ticker))
            setSmallIcon(R.drawable.notification_icon)
            setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.notification_icon))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setChannelId(NotificationUtils.NOTIFICATION_CHANNEL_DOWNLOAD)
            }
        }
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @Suppress("MagicNumber")
    fun prepareForStart(operation: DownloadFileOperation, currentDownloadIndex: Int, totalDownloadSize: Int) {
        currentOperationTitle = String.format(
            context.getString(R.string.downloader_notification_manager_download_text),
            currentDownloadIndex,
            totalDownloadSize,
            File(operation.savePath).name
        )

        notificationBuilder.run {
            setContentTitle(currentOperationTitle)
            setOngoing(false)
            setProgress(100, 0, operation.size < 0)
        }

        showNotification()
    }

    fun prepareForResult() {
        notificationBuilder
            .setAutoCancel(true)
            .setOngoing(false)
            .setProgress(0, 0, false)
    }

    @Suppress("MagicNumber")
    fun updateDownloadProgress(percent: Int, totalToTransfer: Long) {
        val progressText = String.format(
            context.getString(R.string.upload_notification_manager_upload_in_progress_text),
            percent
        )

        notificationBuilder.run {
            setProgress(100, percent, totalToTransfer < 0)
            setContentTitle(currentOperationTitle)
            setContentText(progressText)
        }

        showNotification()
    }

    @Suppress("MagicNumber")
    fun dismissNotification() {
        Handler(Looper.getMainLooper()).postDelayed({
            notificationManager.cancel(id)
        }, 2000)
    }

    fun showNewNotification(text: String) {
        val notifyId = SecureRandom().nextInt()

        notificationBuilder.run {
            setProgress(0, 0, false)
            setContentTitle(text)
            setOngoing(false)
            notificationManager.notify(notifyId, this.build())
        }
    }

    fun setContentIntent(intent: Intent, flag: Int) {
        notificationBuilder.setContentIntent(
            PendingIntent.getActivity(
                context,
                System.currentTimeMillis().toInt(),
                intent,
                flag
            )
        )
    }

    private fun showNotification() {
        notificationManager.notify(id, notificationBuilder.build())
    }

    fun getId(): Int {
        return id
    }

    fun getNotification(): Notification {
        return notificationBuilder.build()
    }
}
