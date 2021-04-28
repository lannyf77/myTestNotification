package com.demo.mytestnotification

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.demo.mytestnotification.Utils.bundleToString

/**
 * Listener service to listen to notifications for below API 23 version
 */
class MyNotificationListener : NotificationListenerService() {

    companion object {
        var activeNotificationsList = arrayListOf<StatusBarNotification>()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        doPopup(sbn, "onNotificationPosted")
        updateActiveNotifications()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        //doPopup(sbn, "onNotificationRemoved")
        updateActiveNotifications()
    }
    /**
       Ensure that the service is back alive if it is killed by the Os and there is
       enough memory available
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("+++", "+++ onStartCommand $intent")
        return Service.START_STICKY
    }

    private fun updateActiveNotifications() {
        activeNotificationsList = activeNotifications.filter {
            it.packageName.equals(applicationContext.packageName)
        }.toCollection(ArrayList())
    }

    fun doPopup(sbn: StatusBarNotification?, action: String) {

        return

        ////////////////////////////////

        val notfExtraStr = bundleToString(sbn?.notification?.extras)
        Log.i("+++", "+++ @@@ doPopup() $action, notification.extras: $notfExtraStr")

        Handler(Looper.getMainLooper()).postDelayed({
            sbn?.notification?.let { notif ->
                val title: String = notif.extras.getString(
                    NotificationCompat.EXTRA_TITLE,
                    "--no found by key ${NotificationCompat.EXTRA_TITLE}"
                )
                val body: String = notif.extras.getString(
                    NotificationCompat.EXTRA_TEXT,
                    "no found by key android.text"
                )
                val intent = Intent(this, PopupActivity::class.java).apply{
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP // .FLAG_ACTIVITY_CLEAR_TASK
                }
                intent.putExtra("actionKey", action)
                intent.putExtra("idKey", sbn.id)
                intent.putExtra("titleKey", title)
                intent.putExtra("bodyKey", body)
                startActivity(intent)
            }
        }, 2000)
    }
}
