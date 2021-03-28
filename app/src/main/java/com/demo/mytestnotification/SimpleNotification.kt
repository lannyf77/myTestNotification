package com.demo.mytestnotification

import android.app.Notification
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demo.mytestnotification.Utils.CHANNEL_ID_1
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.security.SecureRandom


class SimpleNotification : AppCompatActivity() {

    private lateinit var notificationManager: NotificationManagerCompat
    private var staticNotificationData = arrayListOf<NotificationData>()

    lateinit var recyclerView: RecyclerView
    var adapterNotificationDataList = arrayListOf<NotificationData>()

    init {
        val secureRandom = SecureRandom()
        for (i in 0..100) {
            NotificationData(secureRandom.nextInt(100), "title $i", "body: $i", System.currentTimeMillis()).apply {
                staticNotificationData.add(this)

//                if (i<3) {
//                    adapterNotificationDataList.add(this)
//                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("+++", "+++ onCreate(savedInstanceState==null: ${savedInstanceState==null}), $this")
        setContentView(R.layout.simple_notification)
        notificationManager = NotificationManagerCompat.from(this)
        setupRecyclerView()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.i("+++", "+++ onNewIntent($intent), $this")

        //updateList(notifData: NotificationData)
        updateActivNotifsInRV()
    }

    fun setupRecyclerView() {
        recyclerView = findViewById<RecyclerView>(R.id.recycler_list)
        recyclerView.setLayoutManager(LinearLayoutManager(this))
        recyclerView.adapter = CustomAdapter(adapterNotificationDataList)
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        //recyclerView?.adapter?.notifyDataSetChanged()
    }

    fun updateActivNotifsInRV() {
        adapterNotificationDataList.clear()
        val arr = Utils.getActiveNotification()
        for (notif: NotificationData in arr) {
            updateList(notif)
        }
    }

    fun updateList(notifData: NotificationData) {
        Log.d("+++", "+++ nter updateList(), adapterNotificationDataList: ${adapterNotificationDataList.size}")
        adapterNotificationDataList.add(notifData)
        runOnUiThread(Runnable() {
            (recyclerView.adapter as? CustomAdapter)?.updateList(adapterNotificationDataList)
            val notiSize = (recyclerView.adapter?.itemCount) ?: adapterNotificationDataList.size
            recyclerView.scrollToPosition(notiSize - 1);
        })
        Log.d("+++", "+++ --- exit updateList(), adapterNotificationDataList: ${adapterNotificationDataList.size}")

    }

    fun startNotify(view: View) {

        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            Utils.opnNotificationSettings(this, packageName)
            return
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            val name = "Shadowfax Notifications"
            val descriptionText = "This is Channel 1 for notifications for ..."
            val importance = android.app.NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID_1, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: android.app.NotificationManager =
                this.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        GlobalScope.launch {
            Log.e("+++", "+++ +++ +++ bf for (i: Int in 0..8)")
            for (i: Int in 0..8) {
                SystemClock.sleep(2000)
                Log.e("+++", "+++ +++ in for ($i: Int in 0..8)")
                val notiItem = staticNotificationData[i]
                sendNotificationToUser(this@SimpleNotification, notiItem)
                Log.e("+++", "+++ cached[$i]: notify(${notiItem}")

            }
        }
        Log.e("+++", "+++ +++ after for (i: Int in 0..8)")
    }

    fun sendNotificationToUser(context: Context, notiItem: NotificationData) {

        //Check notification status
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: android.app.NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            val channel = notificationManager.getNotificationChannel(CHANNEL_ID_1)
            if (channel.importance == android.app.NotificationManager.IMPORTANCE_NONE) {
                // Please pass exact message title you are sending it to the user in notification as a messageText for logNotificationDiscardedEvent(...)

                android.util.Log.e("+++", "+++ !!! sendNotificationToUser(), channel.importance == android.app.NotificationManager.IMPORTANCE_NONE")
                Utils.opnNotificationSettings(this, packageName)
                return
            }
        } else {
            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                Utils.opnNotificationSettings(this, packageName)
                return
            }
        }

        val builder: NotificationCompat.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(context, CHANNEL_ID_1)
        } else {
            NotificationCompat.Builder(context)
        }

        val theId = notiItem.id
        val theTitle = notiItem.title
        val theBody = notiItem.body + " " + notiItem.time

        //Add the flags that will fire the user engagement event when it is opened.
        val notificationIntent = Intent(context, SimpleNotification::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
//        notificationIntent.putExtra(SendNotificationActivity.MESSAGE_ID_KEY, id)
//        notificationIntent.putExtra(SendNotificationActivity.MESSAGE_STRING_KEY, body)
//        notificationIntent.putExtra(SendNotificationActivity.MESSAGE_TYPE_KEY, AUTO_MODE_TYPE)
//        notificationIntent.putExtra(SendNotificationActivity.RIVENDELL_METADATA, remoteMessage)
//        // Attach Notification Title with specific key (for Ex: MESSAGE_TITLE) in intent for Logging Shadowfax Analytics
//        notificationIntent.putExtra(SendNotificationActivity.MESSAGE_TITLE, title)

        val pendingIntent = PendingIntent.getActivity(context, 1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        builder
            .setSmallIcon(R.drawable.ic_one)   //.setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(theTitle)
            .setContentIntent(pendingIntent)
            .setContentText(theBody)
            .setDefaults(Notification.DEFAULT_ALL).priority = NotificationCompat.PRIORITY_HIGH

        builder.setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)
        // notificationId is a unique int for each notification that you must define
        //val theId = if (id != null) id else SecureRandom().nextInt(100)
        notificationManager.notify(theId, builder.build())

        //updateList(notiItem)
    }
}
