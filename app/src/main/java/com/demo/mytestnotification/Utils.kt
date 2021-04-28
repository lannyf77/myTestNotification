package com.demo.mytestnotification

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.service.notification.StatusBarNotification
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.StyleSpan
import android.util.Log
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.EXTRA_TEXT
import androidx.core.app.NotificationCompat.EXTRA_TITLE
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.text.bold
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
                        Log.e("+++", "+++ sortActiveNootifications().compare(), b.getPostTime():" + b!!.postTime + ", a.getPostTime()" + a!!.postTime)

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
                    //printoutNotication(notification, activeNotification, channelId_groupId, i)

                    val notfExtraStr = bundleToString(notification.extras)
                    //Log.i("+++", "+++ [$i]: notification.extras: $notfExtraStr")
                    Log.d(
                        "+++", "+++ @@@ [" + i + "]: id: " + activeNotification.id +
                        ", tag:" + activeNotification.tag +
                        //", getPackageName:" + activeNotification.packageName +
                        ", getPostTime:" + activeNotification.postTime +
                        ", tile: $title" +
                        //", body:" + body +
                        //", getUser:" + activeNotification.user +
                        //", channelId: ${channelId_groupId?.first} " +
                        ""//", notification.group: ${channelId_groupId?.second}"
                    )
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
                    doCancelActiveNotifyByIdAndTag(laetsNofifyByChannelOrder.id, null)
                    Log.e("+++", "+++ !!! notifyWithPurgeLatestFirstAganistChannelOrder(), notification?.channelId: ${channleId}, cancel(${laetsNofifyByChannelOrder.id}), ")

                    break
                }
            }
            // fallback shouldnt happen
            if (laetsNofifyByChannelOrder == null && activeNotifFilterOutGroup.size > maxActiveNoticicationAllowd - 1) {
                Log.e("+++", "+++ !!! notifyWithPurgeLatestFirstAganistChannelOrder() no notify found, do fallback")

                for (i in activeNotifFilterOutGroup.size - 1 downTo maxActiveNoticicationAllowd - 1) {
                    val activeNotification = activeNotifFilterOutGroup[i]
                    doCancelActiveNoification(activeNotification, i)
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
                doCancelActiveNoification(activeNotification, i)
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
                    doCancelActiveNotifyByIdAndTag(activeNotification.id, null)
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

                    println("+++ sortActiveNootifications().compare(), b.getPostTime():" + b!!.postTime + ", a.getPostTime()" + a!!.postTime)

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
                        "+++", "+++ toBeSorted[" + i + "]: id: " + activeNotification.id +
                        //", tag:" + activeNotification.tag +
                        //", getPackageName:" + activeNotification.packageName +
                        ", getPostTime:" + activeNotification.postTime +
                        ", body:" + body +
                        ""//", getUser:" + activeNotification.user
                    )
                }
                if (toBeSorted.size > maxActiveNoticicationAllowd) {
                    for (i in toBeSorted.size - 1 downTo maxActiveNoticicationAllowd) {
                        val activeNotification = toBeSorted[i]
                        doCancelActiveNoification(activeNotification, i)
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

    private fun doCancelActiveNoification(activeNotification: StatusBarNotification, i: Int) {
        if (activeNotification.tag !== "ranker_group") {
            doCancelActiveNotifyByIdAndTag(activeNotification.id, null)
        }
        val notification: Notification = activeNotification.notification
        val body: String = notification.extras.getString(
            EXTRA_TEXT,
            "no found by key android.text"
        )
        Log.v(
            "+++", "+++ [" + i + "]: id: " + activeNotification.id +
            ", tag:" + activeNotification.tag +
            ", getPackageName:" + activeNotification.packageName +
            ", getPostTime:" + activeNotification.postTime +
            ", body:" + body +
            ", getUser:" + activeNotification.user
        )
    }

    private fun doCancelActiveNotifyByIdAndTag(id: Int, tag: String?) {
        NotificationManagerCompat.from(appContext).cancel(tag, id)
    }

    /** Returns the consumer friendly device name  */
    fun getDeviceName(): String? {
        Build.DEVICE
        Build.HARDWARE
        val manufacturer = Build.MANUFACTURER
        val model = (Build.MODEL + " " + Build.BRAND + " (" + Build.VERSION.RELEASE + ")" + " API-" + Build.VERSION.SDK_INT)
        return if (model.startsWith(manufacturer)) {
            model.capitalize()
        } else {
            ("$manufacturer $model").capitalize()
        }
    }

    fun getDeviceName_2(): String {
       return  (if (Build.MODEL.startsWith(Build.MANUFACTURER, ignoreCase = true)) {
            Build.MODEL
       } else {
            "${Build.MANUFACTURER} ${Build.MODEL}"
       }).capitalize()
           .also { ret ->
               val str = android.os.Build::class.java.fields.map { "Build.${it.name} = ${it.get(it.name)}"}.joinToString("\n")
               Log.i("+++", "+++ getDeviceName(), ret: $ret\n$str")
           }
    }

    ///
    private var pathList = listOf<String>("aaa", "bbb", "eee888")
    private fun buildJsonString(): String {
        val lsatValue = "___"
        val spStrBuilder = StringBuilder() // SpannableStringBuilder()
        if (pathList.isNotEmpty()) {
            buildOnePathSb(0, lsatValue, spStrBuilder)
        }
        return spStrBuilder.toString()
    }

    private fun buildOnePathSb(deepth: Int, lastValue: String, spStrBuilder: StringBuilder) {
        if (deepth >= pathList.size) {
            return
        }
        //var spaceStr = "".padStart(deepth*4)
        var spaceStr = ""
        for (i in 0..(deepth*4)) {
            spaceStr += Typography.nbsp
        }
        val pathStr: String = pathList[deepth]
        when (deepth) {
            0 -> {
                spStrBuilder.append("{<br>$spaceStr<b><font color='#77ff0000'>$pathStr</font></b>")
                val valuePart = if (pathList.size > 1) {
                    ": {<br>"
                } else {
                    ": $lastValue<br>}"
                }
                spStrBuilder.append(valuePart)
                if (deepth + 1 < pathList.size) {
                    buildOnePathSb(deepth + 1, lastValue, spStrBuilder)
                    spStrBuilder.append("<br>$spaceStr}")
                }
            }
            (pathList.size - 1) -> {
                spStrBuilder.append("$spaceStr$pathStr")
                val valuePart = ": $lastValue<br>$spaceStr}"
                spStrBuilder.append("$valuePart")
            }
            else -> {
                spStrBuilder.append("$spaceStr$pathStr: {<br>")
                buildOnePathSb(deepth+1, lastValue, spStrBuilder)
                spStrBuilder.append("<br>$spaceStr}")
            }
        }
    }


    // seems not work in Dialog TextView
    private fun boldTheFirstPath(): String {
        val json = "{ \"location\": { \"country\":\"GB\", \"weather\":[ { \"zip\":20202, \"description\":\"sun\", \"temp\":\"80\" } ] } }"
// List of words to be marked with bold
// List of words to be marked with bold
        val boldList: List<String> = Arrays.asList("country", "zip")
        val spannable: Spannable = SpannableString(json)

// Finding match of words in the String

// Finding match of words in the String
        for (word in boldList) {
            var startIndex = json.indexOf(word)
            do {
                val endIndex = startIndex + word.length
                spannable.setSpan(StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                startIndex = json.indexOf(word, endIndex)
            } while (startIndex != -1)
        }

        return spannable.toString()
    }

    private fun buildOnePath(deepth: Int, lastValue: String, spStrBuilder: SpannableStringBuilder) {

        var spaceStr = "".padStart(deepth + 8)
//        for (i in 0..deepth) {
//            spaceStr += " "
//        }
        val pathStr: String = pathList[deepth]
        when (deepth) {
            0 -> {
                //spStrBuilder.color ( Color.CYAN) { append("{\n$spaceStr$pathStr") }
                spStrBuilder.bold { append("{\n$spaceStr$pathStr") }
                val valuePart = if (pathList.size > 1) {
                    ": {"
                } else {
                    "$lastValue}"
                }
                spStrBuilder.append(valuePart + "\n");
            }
            (pathList.size - 1) -> {
                spStrBuilder.append("$spaceStr$pathStr")
                val valuePart = ": $lastValue\n}"
                spStrBuilder.append(valuePart)
            }
            else -> {
                spStrBuilder.append("$spaceStr$pathStr: {\n")
            }
        }
    }

    fun buildMarketIntent(context: Context, appId: String): Intent? {
        var playstoreIntent = Intent(Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$appId"))
        var marketFound = false
        // find all applications able to handle our rateIntent
        val otherApps: List<ResolveInfo> = context.getPackageManager().queryIntentActivities(playstoreIntent, 0)
        for (otherApp in otherApps) {
            Log.v("+++", "+++ buildMarketIntent(), otherApp.activityInfo.applicationInfo.packageName: ${otherApp.activityInfo.applicationInfo.packageName}")
            // look for Google Play application
            if (otherApp.activityInfo.applicationInfo.packageName == "com.android.vending") {
                val otherAppActivity = otherApp.activityInfo
                val componentName = ComponentName(
                    otherAppActivity.applicationInfo.packageName,
                    otherAppActivity.name
                )
                // make sure it does NOT open in the stack of your activity
                playstoreIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                // task reparenting if needed
                playstoreIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                // if the Google Play was already open in a search result
                //  this make sure it still go to the app page you requested
                playstoreIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                // this make sure only the Google Play app is allowed to
                // intercept the intent
                playstoreIntent.component = componentName
                marketFound = true
                break
            }
        }
        return if (marketFound) {
            playstoreIntent
        } else {
            null
        }.also {
            Log.d("+++", "+++ --- exit buildMarketIntent(), ret: ${it}")
        }
    }

    fun buildPlayStoreIntent(context: Context): Intent {
        var appPackageName = context.getPackageName()
        appPackageName = "com.yahoo.mobile.client.android.sportacular" //"com.google.android.apps.maps"

        var intent = buildMarketIntent(context, appPackageName)
        if (intent ==  null) {
            intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")

                //data = Uri.parse("https://play.google.com/store/apps/details?id=${appPackageName}")
                //setPackage("com.android.vending")
            }
        }
        return intent

//        return Intent(Intent.ACTION_VIEW).apply {
//            data = Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
//
//            //data = Uri.parse("https://play.google.com/store/apps/details?id=${appPackageName}")
//            //setPackage("com.android.vending")
//        }
    }

    fun openStore(context: Context, title: String, desc: String) {
        val intent: Intent
        if (true) {
            var appPackageName = context.getPackageName()

            appPackageName = "com.yahoo.mobile.client.android.sportacular" //"com.google.android.apps.maps"
            intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                //data = Uri.parse("https://play.google.com/store/apps/details?id=${appPackageName}")

                //setPackage("com.android.vending")

            }
            ///
//            try {
//                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
//            } catch (e: ActivityNotFoundException) {
//                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
//            }
//            ///

            val notificaionId = 1
            val pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)
            val bigTextNotiStyle: NotificationCompat.BigTextStyle? = null

            val notificationManager: android.app.NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

            val color: Int = ContextCompat.getColor(context, R.color.channel_1_color)

            val builder: NotificationCompat.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationCompat.Builder(context, CHANNEL_ID_1)
            } else {
                NotificationCompat.Builder(context)
            }

            builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(desc)
                .setStyle(bigTextNotiStyle)
                .setAutoCancel(true)
                .setColor(color)
                .setContentIntent(pIntent)
                .setLights(Color.RED, 3000, 3000) as NotificationCompat.Builder
            notificationManager.notify(notificaionId, builder.build())
        }
    }

    fun isNotificationServiceEnabled_2(context: Context): Boolean {
        val theList = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        val theListList = theList.split(":".toRegex()).toTypedArray()
        val me: String = ComponentName(context, MyNotificationListener::class.java).flattenToString()
        for (next in theListList) {
            Log.i("+++", "+++ VerifyNotificationPermission(), me: $me, in enabled_notification_listeners: $next")
            if (me == next) return true
        }
        return false
    }

    fun isNotificationServiceEnabled(context: Context): Boolean {
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners") //Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE)
        Log.e("+++", "+++ isNotificationServiceEnabled(), Settings.Secure.getString(contentResolver, Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE): $flat")

        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":".toRegex()).toTypedArray()
            Log.i("+++", "+++ isNotificationServiceEnabled(), flat.split(\":\".toRegex()).toTypedArray(): ${names}")

            for (i in names.indices) {
                val cn = ComponentName.unflattenFromString(names[i])

                Log.i("+++", "+++ ComponentName.unflattenFromString(names[$i]), packageNmae: ${cn?.packageName}, className: ${cn?.className}")
                if (cn != null) {
                    if (TextUtils.equals(packageName, cn.packageName)) {
                        return true
                    }
                }
            }
        }
        return false
    }
    // does not work
    fun checkPermission(context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE)
            != PackageManager.PERMISSION_GRANTED) {
            Log.d("+++", "+++ permission denied")
        }
        else {
            Log.d("+++", "+++ permission granted")
        }
    }

}
