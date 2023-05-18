package com.bertonoon.movealert

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bertonoon.movealert.broadcast.AlarmReceiver
import com.bertonoon.movealert.broadcast.ConfirmReceiver
import com.bertonoon.movealert.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            defaultViewModelProviderFactory
        )[MainViewModel::class.java]

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestPermissions()
    }

    override fun onResume() {
        val intent = Intent(applicationContext,ConfirmReceiver::class.java).apply {
            putExtra("EXTRA_MESSAGE", "set")
            putExtra("CONFIRM", true)
        }
        sendBroadcast(intent)
        super.onResume()
    }

    override fun onDestroy() {
        val intent = Intent(applicationContext,ConfirmReceiver::class.java).apply {
            putExtra("EXTRA_MESSAGE", "set")
            putExtra("CONFIRM", true)
        }
        sendBroadcast(intent)
        super.onDestroy()
    }

    private fun hasNotificationPermission() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

    private fun hasForegroundServicePermission() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.FOREGROUND_SERVICE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (!hasNotificationPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (!hasForegroundServicePermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                permissionsToRequest.add(Manifest.permission.FOREGROUND_SERVICE)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            var showDialog = false

            if (permissionsToRequest.contains(Manifest.permission.FOREGROUND_SERVICE)) {
                showDialog = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.FOREGROUND_SERVICE
                    )
                } else {
                    true
                }
            }

            if (permissionsToRequest.contains(Manifest.permission.POST_NOTIFICATIONS)) {
                showDialog = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                } else {
                    true
                }
            }
            if (!showDialog) {
                val builder = AlertDialog.Builder(this)
                builder
                    .setTitle(getString(R.string.notificationRequestTitle))
                    .setMessage(getString(R.string.notificationRequestMessage))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.notificationRequestButtonText))
                    { dialog, _ ->
                        dialog.dismiss()
                        ActivityCompat.requestPermissions(
                            this,
                            permissionsToRequest.toTypedArray(),
                            Constants.REQUEST_PERMISSION_CODE
                        )
                    }
                builder.create().show()
            }
        }
    }

}