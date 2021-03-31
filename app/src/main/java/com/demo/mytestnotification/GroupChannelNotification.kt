package com.demo.mytestnotification

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demo.mytestnotification.Utils.CHANNEL_ID_1
import com.demo.mytestnotification.Utils.CHANNEL_ID_2
import com.demo.mytestnotification.Utils.CHANNEL_ID_3
import com.demo.mytestnotification.Utils.CHANNEL_ID_4
import com.demo.mytestnotification.Utils.GROUP_A
import com.demo.mytestnotification.Utils.GROUP_B
import com.demo.mytestnotification.Utils.createChannel
import com.demo.mytestnotification.Utils.deleteAllNotificationGroups
import com.demo.mytestnotification.Utils.maxActiveNoticicationAllowd
import com.demo.mytestnotification.Utils.notifyWithPurgeLatestFirst
import com.demo.mytestnotification.Utils.notifyWithReplaceLatestFirst
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.security.SecureRandom


class GroupChannelNotification : AppCompatActivity() {

    private lateinit var notificationManager: NotificationManagerCompat

    lateinit var recyclerViewGaC1: RecyclerView
    lateinit var recyclerViewGaC2: RecyclerView
    lateinit var recyclerViewGbC3: RecyclerView
    lateinit var recyclerViewGbC4: RecyclerView
    var adapterNotificationDataListGaC1 = arrayListOf<NotificationData>()
    var adapterNotificationDataListGaC2 = arrayListOf<NotificationData>()
    var adapterNotificationDataListGbC3 = arrayListOf<NotificationData>()
    var adapterNotificationDataListGbC4 = arrayListOf<NotificationData>()

    init {
        Log.i("+++", "+++ GroupChannelNotification::init{}, $this")
        NotificationManagerCompat.from(Utils.appContext).cancelAll()  //???
        createNotificationChannels()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("+++", "+++ GroupChannelNotification::onCreate(savedInstanceState==null: ${savedInstanceState==null}), $this")
        setContentView(R.layout.group_channel_notification)
        notificationManager = NotificationManagerCompat.from(this)
        setupRecyclerView()
    }

    fun setupRecyclerView() {
        recyclerViewGaC1 = findViewById<RecyclerView>(R.id.recycler_list_ga_c1)
        recyclerViewGaC1?.let {
            setupRecyclerView(recyclerViewGaC1, adapterNotificationDataListGaC1, GROUP_A, CHANNEL_ID_1)
        }

        recyclerViewGaC2 = findViewById<RecyclerView>(R.id.recycler_list_ga_c2)
        recyclerViewGaC2?.let {
            setupRecyclerView(recyclerViewGaC2, adapterNotificationDataListGaC2, GROUP_A, CHANNEL_ID_2)
        }

        recyclerViewGbC3 = findViewById<RecyclerView>(R.id.recycler_list_gb_c3)
        recyclerViewGbC3?.let {
            setupRecyclerView(recyclerViewGbC3, adapterNotificationDataListGbC3, GROUP_B, CHANNEL_ID_3)
        }

        recyclerViewGbC4 = findViewById<RecyclerView>(R.id.recycler_list_gb_c4)
        recyclerViewGbC4?.let {
            setupRecyclerView(recyclerViewGbC4, adapterNotificationDataListGbC4, GROUP_B, CHANNEL_ID_4)
        }

    }

    private fun setupRecyclerView(recyclerView: RecyclerView, dataList: ArrayList<NotificationData>,
                                  groupId: String, channelId: String) {
        recyclerView.setLayoutManager(LinearLayoutManager(this))
        recyclerView.adapter = GroupChannelAdapter(dataList, groupId, channelId)
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.i("+++", "+++ +++ GroupChannelNotification::onNewIntent($intent), $this")

        updateActivNotifsInRV()
    }

    override fun onResume() {
        super.onResume()
        findViewById<RadioGroup>(R.id.typeSelectorRadioGroup)?.setOnCheckedChangeListener { group, checkedId ->
            NotificationManagerCompat.from(Utils.appContext).cancelAll()
        }
    }

    fun clearRvs() {
        adapterNotificationDataListGaC1.clear()
        (recyclerViewGaC1.adapter as? GroupChannelAdapter)?.updateList(adapterNotificationDataListGaC1)
        adapterNotificationDataListGaC2.clear()
        (recyclerViewGaC2.adapter as? GroupChannelAdapter)?.updateList(adapterNotificationDataListGaC2)
        adapterNotificationDataListGbC3.clear()
        (recyclerViewGbC3.adapter as? GroupChannelAdapter)?.updateList(adapterNotificationDataListGbC3)
        adapterNotificationDataListGbC4.clear()
        (recyclerViewGbC4.adapter as? GroupChannelAdapter)?.updateList(adapterNotificationDataListGbC4)
    }

    fun updateActivNotifsInRV() {
        adapterNotificationDataListGaC1.clear()
        adapterNotificationDataListGaC2.clear()
        adapterNotificationDataListGbC3.clear()
        adapterNotificationDataListGbC4.clear()
        runOnUiThread(Runnable() {
            val arr = Utils.getActiveNotification().first

            for (notif: NotificationData in arr) {
                Log.d("+++", "+++ updateActivNotifsInRV(), $notif,  arr.size: ${arr.size}")
                notif.channelId?.let {
                    updateListByChannelId(notif.channelId, notif)
                }
            }
        })
    }

    private fun updateListByChannelId(channelId: String, notifData: NotificationData) {

        val rvPair = when (channelId) {
            CHANNEL_ID_1 -> {
                Pair(adapterNotificationDataListGaC1, recyclerViewGaC1)
            }
            CHANNEL_ID_2 -> {
                Pair(adapterNotificationDataListGaC2, recyclerViewGaC2)
            }
            CHANNEL_ID_3 -> {
                Pair(adapterNotificationDataListGbC3, recyclerViewGbC3)
            }
            CHANNEL_ID_4 -> {
                Pair(adapterNotificationDataListGbC4, recyclerViewGbC4)
            }
            else -> {
                Log.e("+++", "+++ !!! updateListByChannelId() wrong channelId: $channelId")
                null
            }
        }
        rvPair?.let {
            updateList(rvPair.first, rvPair.second, notifData)
        }
    }

    private fun updateList(adapterNotificationDataList: ArrayList<NotificationData>, recyclerView: RecyclerView, notifData: NotificationData) {
        adapterNotificationDataList.add(notifData)

        (recyclerView.adapter as? GroupChannelAdapter)?.updateList(adapterNotificationDataList)
        val notiSize = (recyclerView.adapter?.itemCount) ?: adapterNotificationDataList.size
        recyclerView.scrollToPosition(notiSize - 1);

        Log.d("+++", "+++ --- exit updateList(), adapterNotificationDataList: ${adapterNotificationDataList.size}")
    }

    fun startNotify(view: View) {

        clearRvs()

        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            Utils.opnNotificationSettings(this, packageName)
            return
        }

        GlobalScope.launch {
            //Log.e("+++", "+++ +++ +++ bf for (i: Int in 0..8)")
            for (i: Int in 0..20) {//0..8
                startNotifToChannel(i)
            }
        }
        //Log.e("+++", "+++ +++ after for (i: Int in 0..8)")
    }

    fun startNotifToChannel(baseIndx: Int) {
        val secureRandom = SecureRandom()
        for (i: Int in 0..3) {
            SystemClock.sleep(2000)
            val Group_Channel = when(i) {
                0 -> Pair(GROUP_A, CHANNEL_ID_1)
                1 -> Pair(GROUP_A, CHANNEL_ID_2)
                2 -> Pair(GROUP_B, CHANNEL_ID_3)
                3 -> Pair(GROUP_B, CHANNEL_ID_4)
                else -> Pair(GROUP_A, CHANNEL_ID_1)
            }
            val notiItem = NotificationData(secureRandom.nextInt(99999),
                "title ${i+1}$baseIndx", "body: ${i+1}$baseIndx", System.currentTimeMillis(), Group_Channel.second, Group_Channel.first )

            Log.e("+++", "+++ startNotifToChannel($i), $notiItem")
            sendNotificationToUser(this@GroupChannelNotification, Group_Channel.second, notiItem)
        }
    }


    fun sendNotificationToUser(context: Context, channelId: String, notiItem: NotificationData) {

        //Check notification status
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: android.app.NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            val channel = notificationManager.getNotificationChannel(channelId)
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
            NotificationCompat.Builder(context, channelId)
        } else {
            NotificationCompat.Builder(context)
        }

        val theId = notiItem.id
        val theTitle = notiItem.title
        val theBody = notiItem.body

        //Add the flags that will fire the user engagement event when it is opened.
        val notificationIntent = Intent(context, GroupChannelNotification::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
//        notificationIntent.putExtra(SendNotificationActivity.MESSAGE_ID_KEY, id)
//        notificationIntent.putExtra(SendNotificationActivity.MESSAGE_STRING_KEY, body)
//        notificationIntent.putExtra(SendNotificationActivity.MESSAGE_TYPE_KEY, AUTO_MODE_TYPE)
//        notificationIntent.putExtra(SendNotificationActivity.RIVENDELL_METADATA, remoteMessage)
//        // Attach Notification Title with specific key (for Ex: MESSAGE_TITLE) in intent for Logging Shadowfax Analytics
//        notificationIntent.putExtra(SendNotificationActivity.MESSAGE_TITLE, title)

        val pendingIntent = PendingIntent.getActivity(context, 1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val channelIcon = when (channelId) {
            CHANNEL_ID_1 -> R.drawable.ic_one
            CHANNEL_ID_2 -> R.drawable.ic_two
            CHANNEL_ID_3 -> R.drawable.ic_three
            CHANNEL_ID_4 -> R.drawable.ic_four
            else -> R.drawable.ic_one
        }

        val channelColor = when (channelId) {
            CHANNEL_ID_1 -> R.color.channel_1_color
            CHANNEL_ID_2 -> R.color.channel_2_color
            CHANNEL_ID_3 -> R.color.channel_3_color
            CHANNEL_ID_4 -> R.color.channel_4_color
            else -> R.color.channel_color
        }

        builder
            .setSmallIcon(channelIcon)   //.setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(theTitle)
            .setContentIntent(pendingIntent)
            .setContentText(theBody)
            .setDefaults(Notification.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(resources.getColor(channelColor))
            .setColorized(true)


        ///

            //.setColor(resources.getColor(R.color.colorPrimary))
            //.setColorized(true)
            //.setStyle(androidx.media.app.NotificationCompat.DecoratedMediaCustomViewStyle())
        ///

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
    }

    private fun createNotificationChannels() {
        deleteAllNotificationGroups()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(CHANNEL_ID_1, "Channel 1", "This is Channel 1", NotificationManager.IMPORTANCE_HIGH, GROUP_A)
            createChannel(CHANNEL_ID_2, "Channel 2", "This is Channel 2", NotificationManager.IMPORTANCE_HIGH, GROUP_A)
            createChannel(CHANNEL_ID_3, "Channel 3", "This is Channel 3", NotificationManager.IMPORTANCE_HIGH, GROUP_B)
            createChannel(CHANNEL_ID_4, "Channel 4", "This is Channel 4", NotificationManager.IMPORTANCE_HIGH, GROUP_B)
        }
    }

    fun opnNotificationSettings(view: View) {
        Utils.opnNotificationSettings(this, packageName)
    }
}
