package com.demo.mytestnotification

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.device_info -> {
                popupDeviceInfo()
                true
            }
            R.id.notification_setting -> {
                Utils.opnNotificationSettings(this, packageName)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun popupDeviceInfo() {

        val intent = Intent(this, DeviceInfoActivity::class.java).apply {
            //putExtra(EXTRA_MESSAGE, message)
        }
        startActivity(intent)
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
