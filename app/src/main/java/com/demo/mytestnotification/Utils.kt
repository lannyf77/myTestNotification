package com.demo.mytestnotification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import java.util.*
import kotlin.Comparator


object Utils {
    val CHANNEL_ID_1 = "Channel_1"
    val maxNoticicationAllowd = 5 //TODO
    lateinit var appContext: Context
    lateinit var packageName: String

    fun getActiveNotification(): ArrayList<NotificationData> {

        var activeotificationDataList = arrayListOf<NotificationData>()

        val notificationManagerCompat: NotificationManagerCompat = NotificationManagerCompat.from(appContext)
        if (!notificationManagerCompat.areNotificationsEnabled()) {
            opnNotificationSettings(appContext, packageName)
            return activeotificationDataList
        }
        //todo: look at NotificationListenerService::getActiveNotifications()  api 18  (that requires the permission for listening to notifications)
        //https://stackoverflow.com/questions/3630733/how-to-check-which-notifications-are-active-in-status-bar-in-android-dev
        //https://www.tutorialspoint.com/how-to-detect-a-new-android-notification

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nm = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // some device getActiveNotifications()may got NullPointerException
            try {
                val pkNmae: String = appContext.getPackageName()
                val activeNotifications = nm.activeNotifications
                Log.w("+++", "+++ getActiveNotification(),pkNmae:$pkNmae,  active notifications count is " + (activeNotifications?.size
                    ?: -1))
                for (i in activeNotifications!!.indices) {
                    val activeNotification = activeNotifications[i]
                    val notification: Notification = activeNotification.notification
                    val body: String = notification.extras.getString("android.text", "no found by key android.text") //com.oath.mobile.shadowfax.demo.MsgString

                    Log.w("+++", "+++ [" + i + "]: id: " + activeNotification.id +
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
                    val body: String = notification.extras.getString("android.text", "no found by key android.text") //com.oath.mobile.shadowfax.demo.MsgString

                    val notfExtraStr = bundleToString(notification.extras)
                    Log.i("+++", "+++ $notfExtraStr")

                    Log.d("+++", "+++ [" + i + "]: id: " + activeNotification.id +
                        ", tag:" + activeNotification.tag +
                        ", getPackageName:" + activeNotification.packageName +
                        ", getPostTime:" + activeNotification.postTime +
                        ", body:" + body +
                        ", getUser:" + activeNotification.user
                    )


                    NotificationData(activeNotification.id, "title ${activeNotification.key}", "body: $body", activeNotification.postTime).apply {
                        activeotificationDataList.add(this)
                    }
                }
            } catch (e: Exception) {
                Log.e("+++", "NotificationManager.getActiveNotifications error!$e")
            }
        }
        return activeotificationDataList
    }

    fun cancelNotificationIfNeeded(appContext: Context) {

        //tag: ranker_group
        val notificationManagerCompat: NotificationManagerCompat = NotificationManagerCompat.from(appContext)
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
                Log.w("+++", "+++ cleanCacheIfNeeded(),pkNmae:$pkNmae,  active notifications count is " + (activeNotifications?.size
                    ?: -1))
                for (i in activeNotifications!!.indices) {
                    val activeNotification = activeNotifications[i]
                    val notification: Notification = activeNotification.notification
                    val body: String = notification.extras.getString("android.text", "no found by key android.text") //com.oath.mobile.shadowfax.demo.MsgString

                    Log.w("+++", "+++ [" + i + "]: id: " + activeNotification.id +
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
                    val body: String = notification.extras.getString("android.text", "no found by key android.text") //com.oath.mobile.shadowfax.demo.MsgString
                    Log.d("+++", "+++ [" + i + "]: id: " + activeNotification.id +
                        ", tag:" + activeNotification.tag +
                        ", getPackageName:" + activeNotification.packageName +
                        ", getPostTime:" + activeNotification.postTime +
                        ", body:" + body +
                        ", getUser:" + activeNotification.user
                    )
                }
                if (toBeSorted.size > maxNoticicationAllowd) {
                    for (i in toBeSorted.size - 1 downTo maxNoticicationAllowd) {
                        val activeNotification = toBeSorted[i]
                        if (activeNotification.tag !== "ranker_group") {
                            NotificationManagerCompat.from(appContext).cancel(activeNotification.id)
                        }
                        val notification: Notification = activeNotification.notification
                        val body: String = notification.extras.getString("android.text", "no found by key android.text") //com.oath.mobile.shadowfax.demo.MsgString
                        Log.v("+++", "+++ [" + i + "]: id: " + activeNotification.id +
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

}
