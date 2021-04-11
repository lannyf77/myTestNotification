package com.demo.mytestnotification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.*
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demo.mytestnotification.Utils.CHANNEL_ID_1
import com.demo.mytestnotification.Utils.blinkView
import com.demo.mytestnotification.Utils.deleteAllNotificationGroups
import com.demo.mytestnotification.Utils.getNextNoitfyId
import com.demo.mytestnotification.Utils.maxActiveNoticicationAllowd
import com.demo.mytestnotification.Utils.notifyWithPurgeLatestFirst
import com.demo.mytestnotification.Utils.notifyWithReplaceLatestFirst
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class SimpleNotification : AppCompatActivity() {

    private lateinit var notificationManager: NotificationManagerCompat

    lateinit var recyclerView: RecyclerView
    var adapterNotificationDataList = arrayListOf<MyNotificationData>()

    init {
        Log.i("+++", "+++ SimpleNotification::init{}, $this")
        NotificationManagerCompat.from(Utils.appContext).cancelAll()
        deleteAllNotificationGroups()
    }

    var interval: Long = 2
    private fun setupNotifyInterval() {
        setNotifyButtonText()
        findViewById<EditText>(R.id.interval)?.apply {
            this.addTextChangedListener {
                if (this.hasFocus() && it != null && (it.toString().toIntOrNull() != null)) {
                    setNotifyButtonText()
                }
            }
        }
    }

    private fun setNotifyButtonText() {
        val intervalEditTxt = findViewById<EditText>(R.id.interval)
        intervalEditTxt?.let {
            interval = (it.text?.toString()?.toLongOrNull() ?: 0)//.toLong()
            intervalEditTxt.post {
                findViewById<Button>(R.id.start_notify)?.apply {
                    text = "Start notify - $interval sec"
                }
            }
        }
    }

    private fun setup() {
        notificationManager = NotificationManagerCompat.from(this)
        findViewById<TextView>(R.id.description)?.apply {
            text = "list all active notifications"
        }
        setupNotifyInterval()
        setupRecyclerView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("+++", "+++ onCreate(savedInstanceState==null: ${savedInstanceState==null}), $this")
//        Utils.clearPostedNotfiMap()
        setContentView(R.layout.simple_notification)
        setup()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.i("+++", "+++ +++ onNewIntent($intent), $this")
        updateActivNotifsInRV()
    }

    override fun onResume() {
        super.onResume()
        findViewById<RadioGroup>(R.id.typeSelectorRadioGroup)?.setOnCheckedChangeListener { group, checkedId ->
            NotificationManagerCompat.from(Utils.appContext).cancelAll()

            findViewById<TextView>(R.id.description)?.apply {
                when (checkedId) {
                    R.id.strategy_replace -> {
                        text = "new notification will use oldset one's id, and the 2nd oldest becomes the oldest"
                    }
                    R.id.strategy_purge -> {
                        text = "the oldest notification will be purged, the 2nd oldest becomes the oldest"
                    }
                    else -> {
                        text = "list all active notifications"
                    }

                }
            }
        }
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
        runOnUiThread(Runnable() {
            val arr = Utils.getActiveNotification().first
            Log.d("+++", "+++ updateActivNotifsInRV(), arr.size: ${arr.size}")
            for (notif: MyNotificationData in arr) {
                updateList(notif)
            }

            findViewById<TextView>(R.id.cavtive_notif_count)?.let {
                it.text = "current active notifications count: ${arr.size}"
            }
        })
    }

    private fun updateList(notifDataMy: MyNotificationData) {

        adapterNotificationDataList.add(notifDataMy)

        (recyclerView.adapter as? CustomAdapter)?.updateList(adapterNotificationDataList)
        val notiSize = (recyclerView.adapter?.itemCount) ?: adapterNotificationDataList.size
        recyclerView.scrollToPosition(notiSize - 1);

        //Log.d("+++", "+++ --- exit updateList(), adapterNotificationDataList: ${adapterNotificationDataList.size}")
    }

    var postingJob: Job? = null
    fun startNotify(view: View) {
        Utils.closeKeyboard(this)
        if (postingJob != null) {
            postingJob?.cancel()
            postingJob = null
            setNotifyButtonText()
            Log.e("+++", "+++ !!! startNotify() cancel")
            return
        }

        findViewById<Button>(R.id.start_notify)?.let {
            it.text = "Tap to stop notify"
        }

        NotificationManagerCompat.from(Utils.appContext).cancelAll()
        findViewById<TextView>(R.id.cavtive_notif_count)?.let {
            it.text = "current active notifications count:"
        }

        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            Utils.opnNotificationSettings(this, packageName)
            return
        }

        Utils.createChannel(
            CHANNEL_ID_1,
            "Notifications channel 1",
            "This is Channel 1 for notifications for ...",
            NotificationManager.IMPORTANCE_HIGH,
            null
        )

        postingJob = GlobalScope.launch {
            Utils.resetNoitfyIdMap()
            val pushCount = (findViewById<EditText>(R.id.total_post_count)?.text?.toString()?.toInt() ?: 80) - 1

            for (i: Int in 0..pushCount) {
                if (!this.isActive) {
                    Log.e("+++", "+++ !!! startNotify() in lobalScope.launch, this.isActive == false, break")
                    break
                }
                val notiItem = MyNotificationData(getNextNoitfyId(),
                    "title ${i+1}", "body: ${i+1}", System.currentTimeMillis())
                if (sendNotificationToUser(this@SimpleNotification, notiItem)) {
                    findViewById<TextView>(R.id.description)?.let {
                        blinkView(it, Html.fromHtml("<b>${i+1} times</b> <i>posted to notification drawer</i>"))
                    }
                }
                SystemClock.sleep(interval*2000)
            }
            postingJob?.cancel()
            postingJob = null
            setNotifyButtonText()
        }
    }


    private fun sendNotificationToUser(context: Context, notiItem: MyNotificationData): Boolean {

        //Check notification status
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: android.app.NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            val channel = notificationManager.getNotificationChannel(CHANNEL_ID_1)
            if (channel.importance == android.app.NotificationManager.IMPORTANCE_NONE) {
                // Please pass exact message title you are sending it to the user in notification as a messageText for logNotificationDiscardedEvent(...)

                android.util.Log.e("+++", "+++ !!! sendNotificationToUser(), channel.importance == android.app.NotificationManager.IMPORTANCE_NONE")
                Utils.opnNotificationSettings(this, packageName)
                return false
            }
        } else {
            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                Utils.opnNotificationSettings(this, packageName)
                return false
            }
        }

        val builder: NotificationCompat.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(context, CHANNEL_ID_1)
        } else {
            NotificationCompat.Builder(context)
        }

        val theId = notiItem.id
        val theTitle = notiItem.title
        val theBody = notiItem.body

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

        maxActiveNoticicationAllowd = findViewById<EditText>(R.id.max_active_notification_count)?.text?.toString()?.toInt() ?: 5
        when(findViewById<RadioGroup>(R.id.typeSelectorRadioGroup)?.checkedRadioButtonId){
            R.id.strategy_purge -> {
                notifyWithPurgeLatestFirst(context, theId, builder.build())
            }
            R.id.strategy_replace -> {
                notifyWithReplaceLatestFirst(context, theId, builder.build())
            }
            else -> {
                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.notify(theId, builder.build())
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            updateActivNotifsInRV()
        }, 200)

        return true
    }

    fun opnNotificationSettings(view: View) {
        Utils.opnNotificationSettings(this, packageName)
    }
}
