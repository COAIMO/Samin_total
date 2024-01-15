package com.coai.samin_total

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.lang.RuntimeException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Watchdog : Service() {

  private val className = "com.coai.samin_total.MainActivity" // 수정 필요
  private val executorService = Executors.newSingleThreadScheduledExecutor()

  private var count = 0;

  override fun onCreate() {
    super.onCreate()
    startForegroundService()

    executorService.scheduleAtFixedRate({
      if (!isAppRunning()) {

        if (count++ > 15) {
          Log.d("Watchdog","start App");
//          val handler = Handler(Looper.getMainLooper())
//          handler.postDelayed({
//            startApp()
//          }, 10)
          startApp()
          count = 0;
        }
      }
    }, 0, 1, TimeUnit.SECONDS)
  }

  private fun startForegroundService() {
    val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      createNotificationChannel("my_service", "My Background Service")
    } else {
      ""
    }

    val notificationBuilder = NotificationCompat.Builder(this, channelId)
    val notification = notificationBuilder.setOngoing(true)
      .setContentTitle("Service is running in the background")
      .setContentText("App WatchDog")
      // 알림 아이콘 설정 필요
      .setSmallIcon(R.drawable.ic_smartlab_logo)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(NotificationCompat.CATEGORY_SERVICE)
      .build()

    startForeground(1, notification)
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun createNotificationChannel(channelId: String, channelName: String): String{
    val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
    chan.lightColor = Color.BLUE
    chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
    val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    service.createNotificationChannel(chan)
    return channelId
  }

  override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    return START_STICKY
  }

  private fun isAppRunning(): Boolean {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val tasks = activityManager.getRunningTasks(Integer.MAX_VALUE)

    for (task in tasks) {
      if (className == task.baseActivity!!.className) {
        Log.d("Watchdog","Check");
          return true
      }
      else {
        Log.d("Watchdog","not Check");
        Log.d("Watchdog",task.baseActivity!!.className);
      }
    }
    return false
  }

  private fun startApp() {

//    val intent = Intent("android.hardware.usb.action.USB_DEVICE_ATTACHED")
//    sendBroadcast(intent)

    val restartIntent = packageManager.getLaunchIntentForPackage("com.coai.samin_total")
    Log.d("Watchdog","restartIntent == null ${restartIntent == null}");

    if (restartIntent != null) {
      restartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      startActivity(restartIntent)
    }
    else {
      val intent = Intent(this, MainActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      startActivity(intent)
    }
//      val intent = Intent(applicationContext, AppRestartReceiver::class.java)
//      val pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, intent,
//          PendingIntent.FLAG_IMMUTABLE)
//      pendingIntent.send()

//            val intent = Intent(applicationContext, AppRestartReceiver::class.java)
//            val pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, intent,
//                PendingIntent.FLAG_IMMUTABLE)
//            pendingIntent.send()
  }

  override fun onBind(intent: Intent): IBinder? {
    return null
  }
}

