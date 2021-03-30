package com.demo.mytestnotification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat.EXTRA_TEXT
import androidx.core.app.NotificationCompat.EXTRA_TITLE
import androidx.core.app.NotificationManagerCompat
import java.util.*
import kotlin.Comparator


object Utils {
    val GROUP_A = "Group_A"
    val GROUP_B = "Group_B"
    val CHANNEL_ID_1 = "Channel_1"
    val CHANNEL_ID_2 = "Channel_2"
    val CHANNEL_ID_3 = "Channel_3"
    val CHANNEL_ID_4 = "Channel_4"
    var maxActiveNoticicationAllowd = 5
    lateinit var appContext: Context
    lateinit var packageName: String

    enum class ThrottleStrategy {
        NONE,
        PURGE_LAST,
        REPLACE_LAST
    }

    fun deleteAllNotificationGroups() {

        val notificationManagerCompat: NotificationManagerCompat = NotificationManagerCompat.from(appContext)
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

        val notificationManagerCompat: NotificationManagerCompat = NotificationManagerCompat.from(appContext)
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

    fun getActiveNotification(): Pair<ArrayList<NotificationData>, MutableList<StatusBarNotification>> {

        var activeotificationDataList = arrayListOf<NotificationData>()
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
                val activeNotifications = nm.activeNotifications.filter {
                    it.tag != "ranker_group"
                }
                Log.w(
                    "+++",
                    "+++ getActiveNotification(),pkNmae:$pkNmae,  active notifications count is " + (activeNotifications?.size
                        ?: -1)
                )
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
                toBeSorted = activeNotifications.toMutableList()// Arrays.asList(activeNotifications)
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
                    val title: String = notification.extras.getString(
                        EXTRA_TITLE,
                        "--no found by key $EXTRA_TITLE"
                    )
                    val body: String = notification.extras.getString(
                        EXTRA_TEXT,
                        "no found by key android.text"
                    )

                    val notfExtraStr = bundleToString(notification.extras)
                    Log.i("+++", "+++ [$i]: notification.extras: $notfExtraStr")
                    Log.d(
                        "+++", "+++ [" + i + "]: id: " + activeNotification.id +
                                ", tag:" + activeNotification.tag +
                                ", getPackageName:" + activeNotification.packageName +
                                ", getPostTime:" + activeNotification.postTime +
                                ", tile: $title" +
                                ", body:" + body +
                                ", getUser:" + activeNotification.user
                    )
                    val notifItem = NotificationData(
                        activeNotification.id,
                        title, body, activeNotification.postTime
                    )

                    activeotificationDataList.add(notifItem)

                }
            } catch (e: Exception) {
                Log.e("+++", "NotificationManager.getActiveNotifications error!$e")
            }
        }
        return Pair(activeotificationDataList, toBeSorted)
    }

    fun notifyWithPurgeLatestFirst(context: Context, theId: Int, newNtify: Notification) {

        val p = getActiveNotification()
        val sortedActiveNotifs = p.second


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

    fun logIntent(intent: Intent, TAG: String) {
        val bundle = if (intent != null) intent.extras else null
        val s: String = bundleToString(bundle)
        Log.i(TAG, s)
    }

    fun createChannel(channelId: String, name: String, descriptionText: String, importance: Int, groupId: String?=null) {
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
}
