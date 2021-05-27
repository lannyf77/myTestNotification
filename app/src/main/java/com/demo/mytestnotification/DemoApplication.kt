package com.demo.mytestnotification

import android.util.Log
import androidx.multidex.MultiDexApplication
import com.demo.mytestnotification.Utils.currentNotificationsPermission

class DemoApplication : MultiDexApplication()  {

    override fun onCreate() {
        super.onCreate()
        val ntfEnabled = currentNotificationsPermission(this)
        Log.e("+++", "+++ DemoApplication.onCreate(), ntfEnabled:"+ntfEnabled)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        Utils.appContext = this
        Utils.packageName = packageName
    }
}
