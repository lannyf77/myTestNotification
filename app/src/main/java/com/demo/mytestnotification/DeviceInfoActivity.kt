package com.demo.mytestnotification

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DeviceInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.devicee_info_layout)

        setDeviceInfo()
    }

    fun setDeviceInfo() {
        val sb = StringBuilder()
        sb.append("Build.MANUFACTURER: ${Build.MANUFACTURER}\n")
        sb.append("Build.MODEL: ${Build.MODEL}\n")
        sb.append("Build.VERSION.SDK_INT: ${Build.VERSION.SDK_INT}\n")

        findViewById<TextView>(R.id.device_info)?.apply {
            text = sb.toString()
        }

        val sb2 = StringBuilder()
        val str = android.os.Build::class.java.fields.map { "Build.${it.name} = ${it.get(it.name)}"}.joinToString("\n")
        sb2.append("$str\n")

        val str2 = android.os.Build.VERSION::class.java.fields.map { "Build.VERSION${it.name} = ${it.get(it.name)}"}.joinToString("\n")
        sb2.append(str2)

        findViewById<TextView>(R.id.device_info_all)?.apply {
            text = sb2.toString()
        }


    }

    fun doCopyDeviceInfo(view: View) {
        val sb = StringBuilder()
        sb.append("Build.MANUFACTURER: ${Build.MANUFACTURER}\n")
        sb.append("Build.MODEL: ${Build.MODEL}\n")
        sb.append("Build.VERSION.SDK_INT: ${Build.VERSION.SDK_INT}\n")


        val str = android.os.Build::class.java.fields.map { "Build.${it.name} = ${it.get(it.name)}"}.joinToString("\n")
        sb.append("\n === all ===\n$str\n")

        val str2 = android.os.Build.VERSION::class.java.fields.map { "Build.VERSION${it.name} = ${it.get(it.name)}"}.joinToString("\n")
        sb.append(str2)

        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.setPrimaryClip(ClipData.newPlainText("device info", sb.toString()))
    }
}
