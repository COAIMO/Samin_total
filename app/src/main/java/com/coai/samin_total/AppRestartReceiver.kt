package com.coai.samin_total

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log

class AppRestartReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {

    Log.d("onReceive", " intent.action : ${intent.action}")
    // 앱을 종료합니다.
    val shutdownIntent = Intent("com.coai.samin_total.ACTION_SHUTDOWN")
    context.sendBroadcast(shutdownIntent)

    Handler().postDelayed({
      // 앱을 재시작합니다.
      val restartIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
      restartIntent?.let {
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(it)
      }
    }, 1000)
  }
}
