package android.mobdev.com.photogallery

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.util.Log
import java.util.concurrent.TimeUnit
import android.os.SystemClock
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.app.RemoteInput
import android.app.Activity


class PollService : IntentService(TAG) {

    val CHANNEL_ID = "CHANNEL"

    override fun onHandleIntent(intent: Intent?) {
        if (!isNetworkAvailableAndConnected()) {
            return
        }
        val query = QueryPreferences.getStoredQuery(this)
        val lastResultId = QueryPreferences.getLastResultId(this)
        val items: List<GalleryItem>
        if (query == null) {
            items = FlickrFetchr().fetchRecentPhotos()
        } else {
            items = FlickrFetchr().searchPhotos(query)
        }
        if (items.isEmpty()) {
            return
        }
        val resultId = items[0].mId
        if (resultId == lastResultId) {
            Log.i(TAG, "Got an old result: $resultId")
        } else {
            Log.i(TAG, "Got a new result: $resultId")

            createNotificationChannel()
            val KEY_TEXT_REPLY = "key_text_reply"
            var replyLabel: String = resources.getString(R.string.reply_label)
            var remoteInput: RemoteInput = RemoteInput.Builder(KEY_TEXT_REPLY).run {
                setLabel(replyLabel)
                build()
            }

            val resources = resources
            val i = PhotoGalleryActivity.newIntent(this)
            val pi = PendingIntent.getActivity(this, 0, i, 0)
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setTicker(resources.getString(R.string.new_pictures_title))
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(resources.getString(R.string.new_pictures_title))
                .setContentText(resources.getString(R.string.new_pictures_text))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build()
            sendBroadcast(Intent(ACTION_SHOW_NOTIFICATION), PERM_PRIVATE)
            showBackgroundNotification(0, notification)
        }
        QueryPreferences.setLastResultId(this, resultId)
    }

    private fun showBackgroundNotification(requestCode: Int, notification: Notification) {
        val i = Intent(ACTION_SHOW_NOTIFICATION)
        i.putExtra(REQUEST_CODE, requestCode)
        i.putExtra(NOTIFICATION, notification)
        sendOrderedBroadcast(
            i, PERM_PRIVATE, null, null,
            Activity.RESULT_OK, null, null
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("NewApi")
    fun isNetworkAvailableAndConnected(): Boolean {
        var cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var isNetworkAvailable = cm.activeNetwork != null
        return isNetworkAvailable && cm.activeNetworkInfo.isConnected
    }

    companion object {
        val TAG = "PollService"
        val ACTION_SHOW_NOTIFICATION = "com.bignerdranch.android.photogallery.SHOW_NOTIFICATION"
        val PERM_PRIVATE = "com.bignerdranch.android.photogallery.PRIVATE"
        val REQUEST_CODE = "REQUEST_CODE"
        val NOTIFICATION = "NOTIFICATION"
        // 60 секунд
        private val POLL_INTERVAL_MS = TimeUnit.MINUTES.toMillis(1)

        fun newIntent(context: Context): Intent {
            return Intent(context, PollService::class.java)
        }

        fun setServiceAlarm(context: Context, isOn: Boolean) {
            val i = PollService.newIntent(context)
            val pi = PendingIntent.getService(context, 0, i, 0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (isOn) {
                alarmManager.setRepeating(
                    AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                    POLL_INTERVAL_MS, pi
                )
            } else {
                alarmManager.cancel(pi)
                pi.cancel()
            }
        }

        fun isServiceAlarmOn(context: Context): Boolean {
            val i = PollService.newIntent(context)
            val pi = PendingIntent.getService(
                context, 0,
                i, PendingIntent.FLAG_NO_CREATE
            )
            return pi != null
        }
    }
}