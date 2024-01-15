package com.coai.samin_total

import android.app.ActivityManager
import android.app.Service
import android.content.*
import android.os.*
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.coai.samin_total.GasRoom.SetGasRoomViewData
import com.coai.samin_total.Service.HexDump
import com.coai.samin_total.Service.SerialService
import com.coai.samin_total.databinding.ActivityLoadingBinding
import java.util.ArrayList
import kotlin.system.exitProcess

val loadingTAG = "로딩 태그"
//val mainTAG = "태그"
val serviceTAG = "서비스 태그"

class LoadingActivity : AppCompatActivity() {

    lateinit var mBinding: ActivityLoadingBinding
//    var serialService: SerialService? = null

//    val broadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            when (intent?.action) {
//                SerialService.ACTION_USB_PERMISSION_GRANTED -> {
//                    Toast.makeText(
//                        context,
//                        "시리얼 포트가 정상 연결되었습니다.",
//                        Toast.LENGTH_SHORT
//                    ).show()
//
//                    val next = Intent(this@LoadingActivity, MainActivity::class.java)
//                    startActivity(next)
//                    finish();
//
//                }
//                SerialService.ACTION_USB_PERMISSION_NOT_GRANTED -> Toast.makeText(
//                    context,
//                    "USB Permission Not granted",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }
//    }
//
//    private fun setFilters() {
//        val filter = IntentFilter()
//        filter.addAction(SerialService.ACTION_USB_PERMISSION_GRANTED)
//        filter.addAction(SerialService.ACTION_USB_PERMISSION_NOT_GRANTED)
//        registerReceiver(broadcastReceiver, filter)
//    }

//    val serialServiceConnection = object : ServiceConnection {
//        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//            val binder = service as SerialService.SerialServiceBinder
//            serialService = binder.getService()
//            //핸들러 연결
//            serialService!!.setHandler(datahandler)
//            isSerialSevice = true
//        }
//
//        override fun onServiceDisconnected(name: ComponentName?) {
//            isSerialSevice = false
//            Toast.makeText(this@LoadingActivity, "서비스 연결 해제", Toast.LENGTH_SHORT).show();
//        }
//    }

//    val datahandler = object : Handler(Looper.getMainLooper()) {
//        override fun handleMessage(msg: Message) {
////            Log.d(loadingTAG, "datahandler : ${HexDump.dumpHexString(msg.obj as ByteArray)}")
//
//            super.handleMessage(msg)
//        }
//    }

//    fun bindSerialService() {
////        if (!UsbSerialService.SERVICE_CONNECTED){
////            val startSerialService = Intent(this, UsbSerialService::class.java)
////            startService(startSerialService)
////
////        }
////        Log.d(loadingTAG, "바인드 시작")
//        val usbSerialServiceIntent = Intent(this, SerialService::class.java)
//        bindService(usbSerialServiceIntent, serialServiceConnection, Context.BIND_AUTO_CREATE)
//    }


    private val handler = Handler(Looper.getMainLooper())
    private val runnable = Runnable {
//        finish() // 액티비티 종료
//        Process.killProcess(Process.myPid())
//        exitProcess(10)
        finishAndRemoveTask()
//        android.os.Process.killProcess(android.os.Process.myPid())
//        System.runFinalization()
//        System.exit(0)
    }

//    fun finishAffinity() {
//        android.os.Process.killProcess(android.os.Process.myPid())
//
//        System.runFinalization()
//
//        System.exit(0)
//    }

    fun checkProcess(): Boolean {
        val activityManager = getSystemService(ActivityManager::class.java)
        val runningAppProcesses = activityManager.runningAppProcesses

        Log.d("TAG",  "process list ==")
        for (runningAppProcess in runningAppProcesses) {
            Log.d("TAG",  runningAppProcess.processName)
            val processName = runningAppProcess.processName

            if (processName == "com.coai.samin_total") {
                // 같은 프로세스가 있습니다.
                return true
            }
        }

// 같은 프로세스가 없습니다.
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        checkProcess()
//        if (checkProcess()) {
//            Log.d("TAG",  "process remove ==")
//            android.os.Process.killProcess(android.os.Process.myPid())
//            System.runFinalization()
//            System.exit(0)
//        }

        mBinding = ActivityLoadingBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        handler.postDelayed(runnable, 10000)
    }

    override fun onResume() {
        AppManager.currentActivity = this
//        hideNavigationBar()
//        bindSerialService()
        super.onResume()
        bindMessengerService()
    }

    override fun onPause() {
        super.onPause()
        AppManager.currentActivity = null
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
//        if (hasFocus) {
//            hideNavigationBar()
//        }
        super.onWindowFocusChanged(hasFocus)
    }

    override fun onStop() {
        super.onStop()
//        unbindService(serialServiceConnection)
        unbindMessengerService()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }

//    fun hideNavigationBar() {
//        window.decorView.apply {
//            // Hide both the navigation bar and the status bar.
//            // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
//            // a general rule, you should design your app to hide the status bar whenever you
//            // hide the navigation bar.
//            systemUiVisibility =
//                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
////                        View.STATUS_BAR_VISIBLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
////                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
////                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
//
//
//        }
////        window.navigationBarColor = Color.parseColor("#FF0000")
//    }

    private val serialSVCIPCHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                SerialService.MSG_SERIAL_CONNECT -> {
                    val next = Intent(this@LoadingActivity, MainActivity::class.java)
                    next.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(next)
                    finish();
                }
                else -> super.handleMessage(msg)
            }
        }
    }
    private val serialSVCIPCClient = Messenger(serialSVCIPCHandler)
    private var serialSVCIPCService: Messenger? = null
    private val serialSVCIPCServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            serialSVCIPCService = Messenger(service).apply {
                send(Message.obtain(null, SerialService.MSG_BIND_CLIENT, 0, 0).apply {
                    replyTo = serialSVCIPCClient
                })
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serialSVCIPCService = null
        }
    }

    private fun bindMessengerService() {
        Intent(this, SerialService::class.java).run {
            bindService(this, serialSVCIPCServiceConnection, Service.BIND_AUTO_CREATE)
        }
    }

    private fun unbindMessengerService() {
        serialSVCIPCService?.send(Message.obtain(null, SerialService.MSG_UNBIND_CLIENT, 0, 0).apply {
            replyTo = serialSVCIPCClient
        })
        unbindService(serialSVCIPCServiceConnection)
    }

}