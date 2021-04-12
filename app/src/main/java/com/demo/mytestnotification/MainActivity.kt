package com.demo.mytestnotification

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.demo.mytestnotification.Utils.getDeviceName

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getDeviceName()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            findViewById<Button>(R.id.groupNotification)?.apply {
                visibility = View.GONE
            }
        }
    }

    fun onSimpleNotificaion(view: View) {

        val intent = Intent(this, SimpleNotification::class.java).apply {
            //putExtra(EXTRA_MESSAGE, message)
        }
        startActivity(intent)
    }
    fun onNotificationGroupChannels(view: View) {
        val intent = Intent(this, GroupChannelNotification::class.java).apply {
            //putExtra(EXTRA_MESSAGE, message)
        }
        startActivity(intent)
    }

    fun opnNotificationSettings(view: View) {

        Utils.opnNotificationSettings(this, packageName)

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
//            intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
//            startActivity(intent)
//        } else {
//            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//            intent.data = Uri.parse("package:$packageName")
//            startActivity(intent)
//        }
    }
}
