package com.bertonoon.movealert.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.bertonoon.movealert.MoveService
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.getStringExtra("EXTRA_MESSAGE") ?: return
        val startServiceIntent = Intent(context, MoveService::class.java)
        context?.startService(startServiceIntent);

    }
}