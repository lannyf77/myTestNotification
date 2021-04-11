package com.demo.mytestnotification

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.service.notification.StatusBarNotification
import android.text.Spanned
import android.util.Log
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.app.NotificationCompat.EXTRA_TEXT
import androidx.core.app.NotificationCompat.EXTRA_TITLE
import androidx.core.app.NotificationManagerCompat
import java.security.SecureRandom
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


object Utils {
    val GROUP_A = "Group_A"
    val GROUP_B = "Group_B"
    val CHANNEL_ID_1 = "Channel_1"
    val CHANNEL_ID_2 = "Channel_2"
    val CHANNEL_ID_3 = "Channel_3"
    val CHANNEL_ID_4 = "Channel_4"

    val chanelIdOrderList: ArrayList<String> = ArrayList<String>(4).apply {
        add(0, CHANNEL_ID_1)
        add(1, CHANNEL_ID_2)
        add(2, CHANNEL_ID_3)
        add(3, CHANNEL_ID_4)
    }

    var maxActiveNoticicationAllowd = 5
    lateinit var appContext: Context
    lateinit var packageName: String

    enum class ThrottleStrategy {
        NONE,
        PURGE_LAST,
        REPLACE_LAST
    }

    fun deleteAllNotificationGroups() {

        val notificationManagerCompat: NotificationManagerCompat = NotificationManagerCompat.from(
            appContext
        )
        val notificationChannelGroups: List<NotificationChannelGroup> = notificationManagerCompat.notificationChannelGroups
        for (group in notificationChannelGroups) {
            if (Build.VERSION.SDK_INT >= 26) {
                notificationManagerCompat.deleteNotificationChannelGroup(group.id)
            }
        }

        //TEST_ML
        deleteAllNotificationChannels()
    }

    fun deleteAllNotificationChannels() {

        val notificationManagerCompat: NotificationManagerCompat = NotificationManagerCompat.from(
            appContext
        )
        val channelList: List<NotificationChannel> = notificationManagerCompat.notificationChannels
        for (channel in channelList) {
            if (Build.VERSION.SDK_INT >= 26) {
                notificationManagerCompat.deleteNotificationChannel(channel.id)
            }
        }

//        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val id: String = "my_channel_01"
//        notificationManager.deleteNotificationChannel(id)
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val manager = getSystemService(NotificationManager::class.java)
//            manager.deleteNotificationChannel(CHANNEL_3_ID);
//            //manager.deleteNotificationChannelGroup(DemoApplication.GROUP_1_ID)
//        }
    }

    private fun printOutActiveNotifications(activeNotifications: ArrayList<StatusBarNotification>) {
        for (i in activeNotifications!!.indices) {
            val activeNotification = activeNotifications[i]
            val notification: Notification = activeNotification.notification
            val title: String = notification.extras.getString(
                EXTRA_TITLE,
                "no found by key $EXTRA_TITLE"
            )
            val body: String = notification.extras.getString(
                EXTRA_TEXT,
                "no found by key android.text"
            ) //com.oath.mobile.shadowfax.demo.MsgString

            //notification.group
            //notification.channelId

            Log.w(
                "+++", "+++ [" + i + "]: id: " + activeNotification.id +
                        ", tag:" + activeNotification.tag +
                        ", getPackageName:" + activeNotification.packageName +
                        ", getPostTime:" + activeNotification.postTime +
                        ", body:" + body +
                        ", tile: $title" +
                        ", groupKey:" + activeNotification.groupKey +
                        ", key:" + activeNotification.key +
                        ", n.grp:" + notification.getGroup() +
                        ", getUser:" + activeNotification.user + "\nnotification: $notification"
            )
        }
    }

    fun getActiveNotification(): Pair<ArrayList<MyNotificationData>, MutableList<StatusBarNotification>> {

        var activeotificationDataList = arrayListOf<MyNotificationData>()
        var toBeSorted: MutableList<StatusBarNotification> = mutableListOf()

        val notificationManagerCompat: NotificationManagerCompat = NotificationManagerCompat.from(
            appContext
        )
        if (!notificationManagerCompat.areNotificationsEnabled()) {
            opnNotificationSettings(appContext, packageName)
            return Pair(activeotificationDataList, toBeSorted)
        }
        //todo: look at NotificationListenerService::getActiveNotifications()  api 18  (that requires the permission for listening to notifications)
        //https://stackoverflow.com/questions/3630733/how-to-check-which-notifications-are-active-in-status-bar-in-android-dev
        //https://www.tutorialspoint.com/how-to-detect-a-new-android-notification

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nm = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // some device getActiveNotifications()may got NullPointerException
            try {
                val pkNmae: String = appContext.getPackageName()

                /** the nm.activeNotifications
                 *
                 * Recover a list of active notifications: ones that have been posted by the calling app that
                 * have not yet been dismissed by the user or {@link #cancel(String, int)}ed by the app.
                 *
                 * <p><Each notification is embedded in a StatusBarNotification object, including the
                 * original tag and id supplied to
                 * notify(String, int, Notification)
                 * and as well as a copy of the original
                 * Notification object (via StatusBarNotification#getNotification()).
                 *
                 * From {@link Build.VERSION_CODES#Q}, will also return notifications you've posted as an
                 * app's notification delegate via
                 * NotificationManager.notifyAsPackage(String, String, int, Notification)}.
                 *
                 * @return An array of StatusBarNotification.
                 */

                val activeNotifications = nm.activeNotifications.filter {
                    it.tag != "ranker_group"
                }
//                Log.w(
//                    "+++",
//                    "+++ getActiveNotification(),pkNmae:$pkNmae,  active notifications count is " + (activeNotifications?.size
//                        ?: -1)
//                )
                printOutActiveNotifications(activeNotifications as ArrayList<StatusBarNotification>)

                toBeSorted = activeNotifications.toMutableList()// Arrays.asList(activeNotifications)
                Collections.sort(toBeSorted, Comparator<StatusBarNotification?> { a, b ->
                    if (a != null && b != null) {
                        java.lang.Long.valueOf(b.postTime).compareTo(a.postTime)
                    } else {
                        1
                    }
                })
                for (i in toBeSorted.indices) {
                    val activeNotification: StatusBarNotification = toBeSorted[i]
                    val notification: Notification = activeNotification.notification
                    val title: String = notification.extras.getString(
                        EXTRA_TITLE,
                        "--no found by key $EXTRA_TITLE"
                    )
                    val body: String = notification.extras.getString(
                        EXTRA_TEXT,
                        "no found by key android.text"
                    )

                    val channelId_groupId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val notificationManager = NotificationManagerCompat.from(Utils.appContext)
                        val channel = notificationManager.getNotificationChannel(notification.channelId)
//                        Log.v(
//                            "+++",
//                            "+++ group_channelId(), notification.channelId: ${notification.channelId} --> channel.id: ${channel?.id}\n" +
//                                    "notification.group: ${notification.group} --> channel?.group: ${channel?.group} "
//                        )
                        if (channel != null) {
                            Pair(channel.id, channel.group)
                        } else {
                            null
                        }

                    } else {
                        null
                    }
                    printoutNotication(notification, activeNotification, channelId_groupId, i)

//                    val notfExtraStr = bundleToString(notification.extras)
//                    Log.i("+++", "+++ [$i]: notification.extras: $notfExtraStr")
//                    Log.d(
//                        "+++", "+++ @@@ [" + i + "]: id: " + activeNotification.id +
//                                ", tag:" + activeNotification.tag +
//                                ", getPackageName:" + activeNotification.packageName +
//                                ", getPostTime:" + activeNotification.postTime +
//                                ", tile: $title" +
//                                ", body:" + body +
//                                ", getUser:" + activeNotification.user +
//                                ", channelId: ${channelId_groupId?.first} " +
//                                ", notification.group: ${channelId_groupId?.second}"
//                    )
                    val notifItem = MyNotificationData(
                        activeNotification.id,
                        title,
                        body,
                        activeNotification.postTime,
                        channelId_groupId?.first,  //channel_id
                        channelId_groupId?.second, //group_id
                        (i == (toBeSorted.size - 1))
                    )

                    activeotificationDataList.add(notifItem)

                }
            } catch (e: Exception) {
                Log.e("+++", "NotificationManager.getActiveNotifications error!$e")
            }
        }
        return Pair(activeotificationDataList, toBeSorted)
    }

    /**
     * if lower than Build.VERSION_CODES.O return null
     */
    fun findLatestNotifInChannel(allActiveNotifs: List<StatusBarNotification>, channelId: String): StatusBarNotification? {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val activeNotifFilteroutByChannelId = allActiveNotifs.filter{ statusBarNoification ->
                (statusBarNoification.notification != null && (statusBarNoification.notification?.channelId.toString() == channelId))
                    .also {
                        Log.d("+++", "+++ findLatestNotifInChannel($channelId), ret: $it, statusBarNoification.notification!= null: ${statusBarNoification.notification != null}, statusBarNoification.notification?.channelId: ${statusBarNoification.notification?.channelId}\n" +
                                "(notification?.channelId == channelId): ${(statusBarNoification.notification?.channelId.toString() == channelId)}")
                    }
            }

            if (activeNotifFilteroutByChannelId.isNotEmpty()) {
                val toBeSorted = activeNotifFilteroutByChannelId.toMutableList()
                Collections.sort(toBeSorted, Comparator<StatusBarNotification?> { a, b ->
                    if (a != null && b != null) {
                        java.lang.Long.valueOf(b.postTime).compareTo(a.postTime)
                    } else {
                        1
                    }
                })

                return toBeSorted[toBeSorted.size - 1]

//                    .also {
//
//                        for (item in toBeSorted) {
//                            Log.v(
//                                "+++",
//                                "+++ findLatestNotifInChannel()," +
//                                        "toBeSorted.size: ${toBeSorted.size}), ${item.notification?.channelId}"
//                            )
//                        }
//
//                    }
            }
        }
        return null.also {
            Log.e(
                "+++",
                "+++ !!! findLatestNotifInChannel(), ret == null" +
                        "allActiveNotifs.size: ${allActiveNotifs.size}"
            )
        }
    }

    ///
    fun notifyWithPurgeLatestFirstAganistChannelOrder(context: Context, theId: Int, newNtify: Notification) {

        val pair = getActiveNotification()  //Pair<ArrayList<MyNotificationData>, MutableList<StatusBarNotification>>
        val sortedActiveNotifs = pair.second

        val activeNotifFilterOutGroup = sortedActiveNotifs.filter{
            it.tag != "ranker_group"
        }

        Log.d(
            "+++",
            "+++ notifyWithPurgeLatestFirstAganistChannelOrder(), activeNotifFilterOutGroup.size: ${activeNotifFilterOutGroup.size}, maxActiveNoticicationAllowd: $maxActiveNoticicationAllowd"
        )
        ///
        val chanelIdOrderList: ArrayList<String> = ArrayList<String>(4)
        chanelIdOrderList.add(0, CHANNEL_ID_1)
        chanelIdOrderList.add(1, CHANNEL_ID_2)
        chanelIdOrderList.add(2, CHANNEL_ID_3)
        chanelIdOrderList.add(3, CHANNEL_ID_4)

        if (activeNotifFilterOutGroup.size > maxActiveNoticicationAllowd - 1) {
            var laetsNofifyByChannelOrder: StatusBarNotification? = null
            for (channleId in chanelIdOrderList) {
                laetsNofifyByChannelOrder = findLatestNotifInChannel(activeNotifFilterOutGroup, channleId)
                if (laetsNofifyByChannelOrder != null) {
                    NotificationManagerCompat.from(appContext).cancel(laetsNofifyByChannelOrder.id)

                    Log.e("+++", "+++ !!! notifyWithPurgeLatestFirstAganistChannelOrder(), notification?.channelId: ${channleId}, cancel(${laetsNofifyByChannelOrder.id}), ")

                    break
                }
            }
            // fallback shouldnt happen
            if (laetsNofifyByChannelOrder == null && activeNotifFilterOutGroup.size > maxActiveNoticicationAllowd - 1) {
                Log.e("+++", "+++ !!! notifyWithPurgeLatestFirstAganistChannelOrder() no notify found, do fallback")

                for (i in activeNotifFilterOutGroup.size - 1 downTo maxActiveNoticicationAllowd - 1) {
                    val activeNotification = activeNotifFilterOutGroup[i]
                    if (activeNotification.tag !== "ranker_group") {
                        NotificationManagerCompat.from(appContext).cancel(activeNotification.id)
                    }
                    val notification: Notification = activeNotification.notification
                    val body: String = notification.extras.getString(
                        EXTRA_TEXT,
                        "no found by key android.text"
                    ) //com.oath.mobile.shadowfax.demo.MsgString

                    Log.v(
                        "+++", "+++ [" + i + "]: id: " + activeNotification.id +
                                ", tag:" + activeNotification.tag +
                                ", getPackageName:" + activeNotification.packageName +
                                ", getPostTime:" + activeNotification.postTime +
                                ", body:" + body +
                                ", getUser:" + activeNotification.user
                    )
                }
            }
        }
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(theId, newNtify)
    }
    ///

    fun notifyWithPurgeLatestFirst(context: Context, theId: Int, newNtify: Notification) {

        val pair = getActiveNotification()  //Pair<ArrayList<MyNotificationData>, MutableList<StatusBarNotification>>
        val sortedActiveNotifs = pair.second

        val activeNotifFilterOutGroup = sortedActiveNotifs.filter{
            it.tag != "ranker_group"
        }

        Log.d(
            "+++",
            "+++ notifyWithPurgeLatestFirst(), activeNotifFilterOutGroup.size: ${activeNotifFilterOutGroup.size}, maxActiveNoticicationAllowd: $maxActiveNoticicationAllowd"
        )

        if (activeNotifFilterOutGroup.size > maxActiveNoticicationAllowd - 1) {
            for (i in activeNotifFilterOutGroup.size - 1 downTo maxActiveNoticicationAllowd - 1) {
                val activeNotification = activeNotifFilterOutGroup[i]
                if (activeNotification.tag !== "ranker_group") {
                    NotificationManagerCompat.from(appContext).cancel(activeNotification.id)
                }
                val notification: Notification = activeNotification.notification
                val body: String = notification.extras.getString(
                    EXTRA_TEXT,
                    "no found by key android.text"
                ) //com.oath.mobile.shadowfax.demo.MsgString
                Log.v(
                    "+++", "+++ [" + i + "]: id: " + activeNotification.id +
                            ", tag:" + activeNotification.tag +
                            ", getPackageName:" + activeNotification.packageName +
                            ", getPostTime:" + activeNotification.postTime +
                            ", body:" + body +
                            ", getUser:" + activeNotification.user
                )
            }
        }

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(theId, newNtify)
    }

    fun notifyWithReplaceLatestFirst(context: Context, theId: Int, newNtify: Notification) {

        val p = getActiveNotification()
        val sortedActiveNotifs = p.second
        var useThisId = theId

        val activeNotifFilterOutGroup = sortedActiveNotifs.filter{
            it.tag != "ranker_group"
        }

            Log.d(
                "+++",
                "+++ notifyWithReplaceLatestFirst(), activeNotifFilterOutGroup.size: ${activeNotifFilterOutGroup.size}"
            )


        if (activeNotifFilterOutGroup.size > maxActiveNoticicationAllowd - 1) {
            for (i in activeNotifFilterOutGroup.size - 1 downTo maxActiveNoticicationAllowd - 1) {
                val activeNotification = activeNotifFilterOutGroup[i]
                if (activeNotification.tag !== "ranker_group") {
                    NotificationManagerCompat.from(appContext).cancel(activeNotification.id)
                    useThisId = activeNotification.id

                    val notification: Notification = activeNotification.notification
                    val body: String = notification.extras.getString(
                        EXTRA_TEXT,
                        "no found by key android.text"
                    ) //com.oath.mobile.shadowfax.demo.MsgString
                    Log.v(
                        "+++", "+++ [" + i + "]: id: " + activeNotification.id +
                                ", tag:" + activeNotification.tag +
                                ", getPackageName:" + activeNotification.packageName +
                                ", getPostTime:" + activeNotification.postTime +
                                ", body:" + body +
                                ", getUser:" + activeNotification.user
                    )
                    break
                }
            }
        }

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(useThisId, newNtify)
    }

    fun cancelNotificationIfNeeded(appContext: Context, strategy: ThrottleStrategy) {

        //tag: ranker_group
        val notificationManagerCompat: NotificationManagerCompat = NotificationManagerCompat.from(
            appContext
        )
        if (!notificationManagerCompat.areNotificationsEnabled()) {
            Utils.opnNotificationSettings(appContext, packageName)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nm = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationEnabled = notificationManagerCompat.areNotificationsEnabled()

            // some device getActiveNotifications()may got NullPointerException
            try {
                val pkNmae: String = appContext.getPackageName()
                val activeNotifications = nm.activeNotifications
                Log.w(
                    "+++",
                    "+++ cleanCacheIfNeeded(),pkNmae:$pkNmae,  active notifications count is " + (activeNotifications?.size
                        ?: -1)
                )
                for (i in activeNotifications!!.indices) {
                    val activeNotification = activeNotifications[i]
                    val notification: Notification = activeNotification.notification
                    val body: String = notification.extras.getString(
                        EXTRA_TEXT,
                        "no found by key android.text"
                    ) //com.oath.mobile.shadowfax.demo.MsgString

                    Log.w(
                        "+++", "+++ [" + i + "]: id: " + activeNotification.id +
                                ", tag:" + activeNotification.tag +
                                ", getPackageName:" + activeNotification.packageName +
                                ", getPostTime:" + activeNotification.postTime +
                                ", body:" + body +
                                ", groupKey:" + activeNotification.groupKey +
                                ", key:" + activeNotification.key +
                                ", n.grp:" + notification.getGroup() +
                                ", getUser:" + activeNotification.user
                    )
                }
                val toBeSorted: MutableList<StatusBarNotification> = activeNotifications.toMutableList()// Arrays.asList(activeNotifications)
                Collections.sort(toBeSorted, Comparator<StatusBarNotification?> { a, b ->
                    if (a != null && b != null) {
                        java.lang.Long.valueOf(b.postTime).compareTo(a.postTime)
                    } else {
                        1
                    }
                })
                for (i in toBeSorted.indices) {
                    val activeNotification = toBeSorted[i]
                    val notification: Notification = activeNotification.notification
                    val body: String = notification.extras.getString(
                        EXTRA_TEXT,
                        "no found by key android.text"
                    ) //com.oath.mobile.shadowfax.demo.MsgString
                    Log.d(
                        "+++", "+++ [" + i + "]: id: " + activeNotification.id +
                                ", tag:" + activeNotification.tag +
                                ", getPackageName:" + activeNotification.packageName +
                                ", getPostTime:" + activeNotification.postTime +
                                ", body:" + body +
                                ", getUser:" + activeNotification.user
                    )
                }
                if (toBeSorted.size > maxActiveNoticicationAllowd) {
                    for (i in toBeSorted.size - 1 downTo maxActiveNoticicationAllowd) {
                        val activeNotification = toBeSorted[i]
                        if (activeNotification.tag !== "ranker_group") {
                            NotificationManagerCompat.from(appContext).cancel(activeNotification.id)
                        }
                        val notification: Notification = activeNotification.notification
                        val body: String = notification.extras.getString(
                            EXTRA_TEXT,
                            "no found by key android.text"
                        ) //com.oath.mobile.shadowfax.demo.MsgString
                        Log.v(
                            "+++", "+++ [" + i + "]: id: " + activeNotification.id +
                                    ", tag:" + activeNotification.tag +
                                    ", getPackageName:" + activeNotification.packageName +
                                    ", getPostTime:" + activeNotification.postTime +
                                    ", body:" + body +
                                    ", getUser:" + activeNotification.user
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("+++", "NotificationManager.getActiveNotifications error!$e")
            }
        }
    }

    fun opnNotificationSettings(context: Context, packageName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            context.startActivity(intent)
        } else {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            context.startActivity(intent)
        }
    }

    fun blinkView(v: TextView, txt: Spanned) {
        v.post {
            v.text = txt
            val anim: Animation = AlphaAnimation(0.0f, 1.0f)
            anim.duration = 1500 //You can manage the blinking time with this parameter

            anim.startOffset = 20
            anim.repeatMode = Animation.REVERSE
            anim.repeatCount = 0 //Animation.INFINITE
            v.startAnimation(anim)
        }
    }

//    private val postedNotifMap: MutableMap<Int, NotificationData> = mutableMapOf<Int, NotificationData>()
//    fun registerPostedNotif(notifId: Int, notifItem: NotificationData) {
//        postedNotifMap[notifId] = notifItem
//        Log.v("+++", "+++ registerPostedNotif($notifId, $notifItem), postedNotifMap.size: ${postedNotifMap.size}")
//    }
//    fun clearPostedNotfiMap() = postedNotifMap.clear()

    ///
    fun reflectMember() {
        Notification::class.typeParameters

//        val kClass = Class.forName(ownerClassName).kotlin
//// Get the object OR a new instance if it doesn't exist
//        val instance = kClass.objectInstance ?: kClass.java.newInstance()
//
//        val member = kClass.memberProperties.filterIsInstance<KMutableProperty<*>>()
//            .firstOrNull { it.name == fieldName }
    }
    ///

    fun bundleToString(bundle: Bundle?): String {
        val body = StringBuilder()
        if (bundle != null) {
            for (key in bundle.keySet()) {
                val value = bundle[key]
                body.append(key).append("=").append(value.toString()).append("\n")
            }
        }
        return body.toString()
    }

    private fun printoutNotication(notification: Notification, activeNotification: StatusBarNotification, channelId_groupId: Pair<String, String>?, i: Int) {
        val notfExtraStr = bundleToString(notification.extras)
        Log.i("+++", "+++ [$i]: notification.extras: $notfExtraStr")
        val title: String = notification.extras.getString(
            EXTRA_TITLE,
            "--no found by key $EXTRA_TITLE"
        )
        val body: String = notification.extras.getString(
            EXTRA_TEXT,
            "no found by key android.text"
        )
        Log.d(
            "+++", "+++ @@@ [" + i + "]: id: " + activeNotification.id +
                    ", tag:" + activeNotification.tag +
                    ", getPackageName:" + activeNotification.packageName +
                    ", getPostTime:" + activeNotification.postTime +
                    ", tile: $title" +
                    ", body:" + body +
                    ", getUser:" + activeNotification.user +
                    ", channelId: ${channelId_groupId?.first} " +
                    ", notification.group: ${channelId_groupId?.second}"
        )
    }

    fun logIntent(intent: Intent, TAG: String) {
        val bundle = if (intent != null) intent.extras else null
        val s: String = bundleToString(bundle)
        Log.i(TAG, s)
    }

    fun createChannel(
        channelId: String,
        name: String,
        descriptionText: String,
        importance: Int,
        groupId: String? = null
    ) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notificationManager = NotificationManagerCompat.from(Utils.appContext)
            //this.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                if (groupId != null) {
                    var theGroup = notificationManager.getNotificationChannelGroup(groupId)
                    if (theGroup == null) {
                        theGroup = NotificationChannelGroup(groupId, groupId)
                        notificationManager.createNotificationChannelGroup(theGroup)
                    }
                    group = groupId
                }
            }
            // Register the channel with the system
            notificationManager.createNotificationChannel(channel)
        }
    }
    var globalId = 100000
    val usedIds: HashMap<Int, Boolean> = HashMap(100)
    val secureRandom = SecureRandom()
    fun getNextNoitfyId(): Int {
        var id = secureRandom.nextInt(999)
        var loop = 0
        while (usedIds.get(id) != null && loop < 1000) {
            //Log.e("+++", "+++ !!! getNextNoitfyId() clash, $id, at loop: $loop")
            id = secureRandom.nextInt(999)
            loop++
        }
        if (loop >= 1000) {
            id = globalId++
            usedIds.put(id, true)
        }
        return id
//            .also {
//                Log.e("+++", "+++ !!! getNextNoitfyId() ret: $id")
//            }
    }
    fun resetNoitfyIdMap() {
        usedIds.clear()
    }

    fun closeKeyboard(context: Activity) {
        context.currentFocus?.let { view ->
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.let { imm ->
                if(imm.isAcceptingText) {
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }
        }
    }
}
