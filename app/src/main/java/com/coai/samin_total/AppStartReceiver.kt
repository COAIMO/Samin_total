package com.coai.samin_total

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log

class AppStartReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {

    Log.d("onReceive", " intent.acti =>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
    val restartIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    restartIntent?.let {
      it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(it)
    }
  }
}