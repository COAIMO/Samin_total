package com.coai.samin_total.Service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Process
import android.util.Log
import com.coai.samin_total.AppManager
import kotlin.system.exitProcess

class ShutdownReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    if ("com.coai.samin_total.ACTION_SHUTDOWN".equals(intent.action)) {
      Log.d("SHUTDOWN", " SHUTDOWN : 앱 꺼짐")
      // 장치가 꺼질 때 수행할 작업
      AppManager.currentActivity?.finishAndRemoveTask()
      Process.killProcess(Process.myPid())
      exitProcess(10)
    }

    Log.d("SHUTDOWN", " intent.action : ${intent.action}")
  }
}
