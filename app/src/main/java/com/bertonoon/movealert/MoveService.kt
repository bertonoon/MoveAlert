package com.bertonoon.movealert

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bertonoon.movealert.broadcast.AlarmReceiver
import com.bertonoon.movealert.db.ConfirmationDao
import com.bertonoon.movealert.db.ConfirmationEntity
import com.bertonoon.movealert.db.DatabaseMove
import com.bertonoon.movealert.network.RetrofitInstance
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.io.IOException


class MoveService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private  var notificationBuilder : NotificationCompat.Builder? = null
    private lateinit var confirmationDao: ConfirmationDao

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        confirmationDao = DatabaseMove.getInstance(application.applicationContext).confirmationDao()
        super.onCreate()

    }

    override fun onDestroy() {
        setNewAlarm()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        setNewAlarm()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.getStringExtra("CONFIRM_EXTRA")?.isNotEmpty() == true) {
            val confirmFromActivity = intent.getBooleanExtra("CONFIRM", false)
            if (confirmFromActivity) {
                Log.i("Msg", confirmFromActivity.toString())
                scope.launch {
                    confirmAlarm()
                }
            }
        }

        var isMove = false
        scope.launch {
            isMove = refreshMoveStatus()
            Log.i("moveTag", "Is move = $isMove")
            if (isMove && !confirmed() ) createNotification()
            else if (!isMove) resetConfirmation()
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private suspend fun resetConfirmation(): Boolean {
        return insertConfirmation(false)
    }

    private suspend fun confirmAlarm(): Boolean {
        return insertConfirmation(true)
    }

    private suspend fun confirmed(): Boolean {
        var result = false
        val waitFor = scope.async {
            try {
                val dbAnswer = confirmationDao.fetch()
                result = dbAnswer.confirmed
                Log.i("DbRoom", "Confirmed $result")
                dbAnswer.confirmed
            } catch (e: Exception) {
                Log.e("DbRoom", "Failed in isNotConfirmed: $e")
            }
            return@async result
        }
        waitFor.await()
        return result
    }

    private suspend fun insertConfirmation(value: Boolean): Boolean {
        var result = false
        val waitFor = scope.async{
            try {
                confirmationDao.insert(ConfirmationEntity(1, value))
                Log.i("DbRoom", "Insert $value")
                result = true
            } catch (e: Exception) {
                Log.e("DbRoom", "Failed in insertConfirmation: $e")
            }
            return@async result
        }
        waitFor.await()
        return result
    }

    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.enableLights(false)
            channel.enableVibration(false)
            channel.setSound(null, null)

            val manager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

         notificationBuilder = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_baseline_camera_indoor_24)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setContentTitle("textTitle")
            setContentText("textContent")
            priority = NotificationCompat.PRIORITY_HIGH
            setContentIntent(pendingIntent)
            setAutoCancel(true)
        }
    }

    private fun createNotification() {
        createNotificationChannel()
        with(NotificationManagerCompat.from(this)) {
            notify(Constants.NOTIFICATION_ID, notificationBuilder!!.build())
        }
    }

    private suspend fun refreshMoveStatus(): Boolean {
        var result = false
        withContext(Dispatchers.IO) {
            val response = try {
                RetrofitInstance.api.getIsMove()
            } catch (e: IOException) {
                Log.e("TAG", "IOException, you might not have internet connection")
                Log.e("TAG", e.toString())
                return@withContext
            } catch (e: HttpException) {
                Log.e("TAG", "HttpException, unexpected response")
                return@withContext
            }
            result = if (response.isSuccessful && response.body() != null) {
                Log.i("Result", response.body()!!.toString())
                response.body()!!.move
            } else {
                Log.e("TAG", "Response not successful")
                false
            }
        }
        return result
    }

    private fun setNewAlarm() {
        val alarmManager = getSystemService(AlarmManager::class.java)
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("EXTRA_MESSAGE", "set")
        }
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + (1000 * 5),
            PendingIntent.getBroadcast(
                this,
                Constants.ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }
}