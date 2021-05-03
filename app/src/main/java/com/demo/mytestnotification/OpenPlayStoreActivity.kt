package com.demo.mytestnotification

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity


class OpenPlayStoreActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.e("+++", "+++ OpenPlayStoreActivity::onCreate(), savedInstanceState== null: ${savedInstanceState == null}, getIntent(): ${getIntent()}")

        if (savedInstanceState != null) {//
            finish()
            return
        }
        openAppInPlayStore(this)

//        val playtoreIntent = Utils.buildPlayStoreIntent(this).apply{
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK //or Intent.FLAG_ACTIVITY_SINGLE_TOP
//        }
//        startActivity(playtoreIntent)
    }

    fun openAppInPlayStore(context: Context) {
        // you can also use BuildConfig.APPLICATION_ID
        var appId: String = context.getPackageName()

        ///
        appId = "com.yahoo.mobile.client.android.sportacular"
        ///

        Log.e("+++", "+++ enter openAppInPlayStore()")

        val playstoreWebIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appId"))
            .apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

        // find all applications able to handle our rateIntent
        var playstoreIntent = Utils.buildMarketIntent(context, appId)
        if (playstoreIntent != null) {
            try {
                //startActivity(playstoreIntent)
                startActivityForResult(playstoreIntent, 0)
            } catch (e: Throwable) {
                Log.e("+++", "+++ !!! exp in openAppInPlayStore(), ${e.message}")
                //context.startActivity(playstoreWebIntent)
                startActivityForResult(playstoreWebIntent, 0)
            }
        } else {
            // if GP not present on device, open web browser
            //context.startActivity(playstoreWebIntent)
            startActivityForResult(playstoreWebIntent, 0)
        }
        Log.e("+++", "+++ --- exit openAppInPlayStore()")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("+++", "+++ OpenPlayStoreActivity:onActivityResult(),requestCode : $requestCode, resultCode: $resultCode, data: $data")
        finish()
    }


    override fun onBackPressed() {
        Log.e("+++", "+++ OpenPlayStoreActivity:onBackPressed()")
        super.onBackPressed()
//        finish()
//        val i = Intent(applicationContext, MainActivity::class.java)
//        startActivity(i)
    }
}

/**
 * building an Intent to view a document using implicit intent (ACTION = VIEW) and putting that in a Notification. When the user opens the Notification and taps on it, the Intent which you have constructed is launched. This doesn't launch your app, it launches whatever app the user needs to VIEW the file. Your app doesn't even need to be running.

When the users clicks BACK, it just goes to the HOME screen because your app didn't launch the viewer, Android did.

If you want the user to go BACK to your app, you need to do this in a different way. The Notification should launch YOUR app, and then YOUR app should launch the VIEWer app. Then, when the user goes BACK, he will see YOUR app.
 */
