package com.coai.samin_total

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

val loadingTAG = "로딩 태그"
val mainTAG = "태그"
val serviceTAG = "서비스 태그"

class LoadingActivity : AppCompatActivity() {

    lateinit var mBinding: ActivityLoadingBinding
    var serialService: SerialService? = null
    var isSerialSevice = false

    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                SerialService.ACTION_USB_PERMISSION_GRANTED -> {
                    Toast.makeText(
                        context,
                        "시리얼 포트가 정상 연결되었습니다.",
                        Toast.LENGTH_SHORT
                    ).show()

                    val next = Intent(this@LoadingActivity, MainActivity::class.java)
                    startActivity(next)

                }
                SerialService.ACTION_USB_PERMISSION_NOT_GRANTED -> Toast.makeText(
                    context,
                    "USB Permission Not granted",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setFilters() {
        val filter = IntentFilter()
        filter.addAction(SerialService.ACTION_USB_PERMISSION_GRANTED)
        filter.addAction(SerialService.ACTION_USB_PERMISSION_NOT_GRANTED)
        registerReceiver(broadcastReceiver, filter)
    }

    val serialServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as SerialService.SerialServiceBinder
            serialService = binder.getService()
            //핸들러 연결
            serialService!!.setHandler(datahandler)
            isSerialSevice = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isSerialSevice = false
            Toast.makeText(this@LoadingActivity, "서비스 연결 해제", Toast.LENGTH_SHORT).show();
        }
    }

    val datahandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
//            Log.d(loadingTAG, "datahandler : ${HexDump.dumpHexString(msg.obj as ByteArray)}")

            super.handleMessage(msg)
        }
    }

    fun bindSerialService() {
//        if (!UsbSerialService.SERVICE_CONNECTED){
//            val startSerialService = Intent(this, UsbSerialService::class.java)
//            startService(startSerialService)
//
//        }
//        Log.d(loadingTAG, "바인드 시작")
        val usbSerialServiceIntent = Intent(this, SerialService::class.java)
        bindService(usbSerialServiceIntent, serialServiceConnection, Context.BIND_AUTO_CREATE)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        mBinding = ActivityLoadingBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

//        val mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
//        val lst = ArrayList<SetGasRoomViewData>()
//        lst.add(SetGasRoomViewData(
//            model = "GasRoom",
//            id = 1,
//            port = 1
//        ))
//        lst.add(SetGasRoomViewData(
//            model = "GasRoom",
//            id = 1,
//            port = 2
//        ))
//        mainViewModel.GasRoomDataLiveList.addAll(lst)
//        val tmp = AQDataParser(mainViewModel)
//        var start = 0x04
//        for(t in 1..10) {
//            tmp.Parser(
//                byteArrayOf(
//                    0xff.toByte(),
//                    0xfe.toByte(),
//                    0x02.toByte(),
//                    0x01.toByte(),
//                    0x00.toByte(), // Length
//                    0x00.toByte(), // checksum
//                    0x00.toByte(), // protocol mode
//                    0x00.toByte(), // 7
//                    start.toByte(), // 8
//                    0xf0.toByte(), // 9
//                    0x00.toByte(), // 10
//                    0x00.toByte(), // 11
//                    0xf0.toByte(), // 12
//                    0x00.toByte(), // 13
//                    0xf0.toByte() // 14
//                )
//            )
//            start -= 1
//            Thread.sleep(500)
//        }
    }

    override fun onResume() {
        setFilters()
        hideNavigationBar()
        bindSerialService()
        super.onResume()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            hideNavigationBar()
        }
        super.onWindowFocusChanged(hasFocus)
    }

    fun hideNavigationBar() {
        window.decorView.apply {
            // Hide both the navigation bar and the status bar.
            // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
            // a general rule, you should design your app to hide the status bar whenever you
            // hide the navigation bar.
            systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                        View.STATUS_BAR_VISIBLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
//                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN


        }
//        window.navigationBarColor = Color.parseColor("#FF0000")
    }

}