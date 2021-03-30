package com.demo.mytestnotification

import android.util.Log
import androidx.multidex.MultiDexApplication

class DemoApplication : MultiDexApplication()  {

    override fun onCreate() {
        super.onCreate()
        Log.e("+++", "+++ DemoApplication.onCreate()")
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        Utils.appContext = this
        Utils.packageName = packageName
    }
}
