package com.bertonoon.movealert.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bertonoon.movealert.MoveService

class Autostart : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val serviceIntent = Intent(context, MoveService::class.java)
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED -> context?.startService(serviceIntent)

        }
    }
}