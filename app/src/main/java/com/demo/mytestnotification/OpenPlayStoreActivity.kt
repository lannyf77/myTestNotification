package com.demo.mytestnotification

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity


class OpenPlayStoreActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.e("+++", "+++ OpenPlayStoreActivity::onCreate(), savedInstanceState== null: ${savedInstanceState == null}, getIntent(): ${getIntent()}")

        val playtoreIntent = Utils.buildPlayStoreIntent(this)
        startActivity(playtoreIntent);
        //startActivityForResult(playtoreIntent, 888);
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        Log.e("+++", "+++ OpenPlayStoreActivity:onActivityResult(),requestCode : $requestCode, resultCode: $resultCode, data: $data")
//        finish()
//    }

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
