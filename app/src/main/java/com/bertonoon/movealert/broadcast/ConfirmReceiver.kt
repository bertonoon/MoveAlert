package com.bertonoon.movealert.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bertonoon.movealert.MoveService

class ConfirmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val message = intent?.getStringExtra("EXTRA_MESSAGE") ?: return

        val confirm = intent.getBooleanExtra("CONFIRM",false)

        val startServiceIntent = Intent(context, MoveService::class.java)
        if (confirm){
            startServiceIntent.putExtra("CONFIRM_EXTRA","set")
            startServiceIntent.putExtra("CONFIRM", true)
        }
        context?.startService(startServiceIntent);

    }
}