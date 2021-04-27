package com.demo.mytestnotification

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class PopupActivity: AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i("+++", "+++ @@@ PopupActivity::onCreate(), savedInstanceState== null: ${savedInstanceState==null}, getIntent(): ${getIntent()}")

        popupDlg()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.e("+++", "+++ @@@ PopupActivity::onNewIntent(), getIntent(): ${getIntent()}")
        popupDlg()
    }

    fun popupDlg() {
        val action = getIntent().getStringExtra("actionKey")
        val id = getIntent().getStringExtra("idKey")
        val title = getIntent().getStringExtra("titleKey")
        val desc = getIntent().getStringExtra("bodyKey")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("$action")
        builder.setMessage("$id - $title - $desc")
            .setIcon(R.drawable.ic_settings)
        builder.setPositiveButton("Okay") {
            dlg, i -> dlg.dismiss()
            finish()
        }
        builder.setNegativeButton("Close") {
            dlg, i ->dlg.dismiss()
            finish()
        }
        val alert = builder.create()
        alert.show()

    }
}
