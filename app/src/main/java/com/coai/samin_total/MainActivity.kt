package com.coai.samin_total

import android.annotation.SuppressLint
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.coai.libmodbus.service.SaminModbusService
import com.coai.libsaminmodbus.model.ModelMonitorValues
import com.coai.libsaminmodbus.model.ObserveModelMonitorValues
import com.coai.samin_total.Dialog.AlertDialogFragment
import com.coai.samin_total.Dialog.ScanDialogFragment
import com.coai.samin_total.Dialog.SetAlertData
import com.coai.samin_total.GasDock.GasDockMainFragment
import com.coai.samin_total.GasDock.GasStorageSettingFragment
import com.coai.samin_total.GasRoom.GasRoomMainFragment
import com.coai.samin_total.GasRoom.GasRoomSettingFragment
import com.coai.samin_total.Logic.*
import com.coai.samin_total.Oxygen.OxygenMainFragment
import com.coai.samin_total.Oxygen.OxygenSettingFragment
import com.coai.samin_total.Service.HexDump
import com.coai.samin_total.Service.SerialService
import com.coai.samin_total.Steamer.SteamerMainFragment
import com.coai.samin_total.Steamer.SteamerSettingFragment
import com.coai.samin_total.WasteLiquor.WasteLiquorMainFragment
import com.coai.samin_total.WasteLiquor.WasteWaterSettingFragment
import com.coai.samin_total.database.*
import com.coai.samin_total.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {

    lateinit var mBinding: ActivityMainBinding
    lateinit var mainFragment: MainFragment
    lateinit var mainsettingFragment: MainSettingFragment
    lateinit var alertdialogFragment: AlertDialogFragment
    lateinit var scandialogFragment: ScanDialogFragment
    lateinit var adminFragment: AdminFragment
    lateinit var alertLogFragment: AlertLogFragment
    lateinit var aqSettingFragment: AqSettingFragment
    lateinit var controlFragment: ControlFragment
    lateinit var versionFragment: VersionFragment
    lateinit var passwordFragment: PasswordFragment
    lateinit var gasdockmainFragment: GasDockMainFragment
    lateinit var gasroommainFragment: GasRoomMainFragment
    lateinit var wasteLiquorMainFragment: WasteLiquorMainFragment
    lateinit var steamerMainFragment: SteamerMainFragment
    lateinit var oxygenMainFragment: OxygenMainFragment
    lateinit var gasStorageSettingFragment: GasStorageSettingFragment
    lateinit var layoutFragment: LayoutFragment
    lateinit var connectTestFragment: ConnectTestFragment
    lateinit var gasRoomSettingFragment: GasRoomSettingFragment
    lateinit var oxygenSettingFragment: OxygenSettingFragment
    lateinit var steamerSettingFragment: SteamerSettingFragment
    lateinit var wasteLiquorSettingFragment: WasteWaterSettingFragment
    private lateinit var mainViewModel: MainViewModel
    lateinit var db: SaminDataBase
    lateinit var shared: SaminSharedPreference
    lateinit var tmp: AQDataParser

    lateinit var viewModel : PageViewModel
    private lateinit var pageListAdapter : PageListAdapter

    private var protocolBuffers = ConcurrentHashMap<Short, ByteArray>()

    companion object {
        var SERVICE_CONNECTED = false

        const val SETTING_TCP_PORT = 0
        const val SETTING_UDP_PORT = 1
        const val SETTING_SLAVE_ID = 2
        const val SETTING_SERIAL_BAUD = 3
        const val SETTING_SERIAL_DATABIT = 4
        const val SETTING_SERIAL_STOPBIT = 5
        const val SETTING_SERIAL_PARITYBIT = 6
        const val CHANGE_INPUT_DATA = 7
        const val CHANGE_INPUT_REGISTER = 8
        const val START_SERIAL_SERVICE = 9
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        db = SaminDataBase.getIstance(applicationContext)!!
        callbackThread = Thread()
        shared = SaminSharedPreference(this)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        tmp = AQDataParser(mainViewModel)

        this.viewModel = ViewModelProvider(
            this,
            ViewModelFactory(application)
        ).get(PageViewModel::class.java)


        setFragment()
        sendAlert()
//        alertAQThread = Thread {
//            while (true) {
//                try {
//                    tmp.timeoutAQCheckStep()
//                    Thread.sleep(50)
//                }
//                catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//        }
//        alertAQThread?.start()
    }

    fun bindSerialService() {
//        if (!UsbSerialService.SERVICE_CONNECTED){
//            val startSerialService = Intent(this, UsbSerialService::class.java)
//            startService(startSerialService)
//
//        }
        val usbSerialServiceIntent = Intent(this, SerialService::class.java)
//        val usbSerialServiceIntent = Intent(this, UsbSerialService::class.java)
        bindService(usbSerialServiceIntent, serialServiceConnection, Context.BIND_AUTO_CREATE)
    }

    var serialService: SerialService? = null

    //    var usbSerialService: UsbSerialService? = null
    var isSerialSevice = false

    @ExperimentalUnsignedTypes
    val serialServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as SerialService.SerialServiceBinder
            serialService = binder.getService()
//            val binder = service as UsbSerialService.UsbSerialServiceBinder
//            usbSerialService = binder.getService()

            //핸들러 연결
            serialService!!.setHandler(datahandler)
            isSerialSevice = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isSerialSevice = false
            Toast.makeText(this@MainActivity, "서비스 연결 해제", Toast.LENGTH_SHORT).show()
        }
    }

    var gasdock_ids_list = mutableListOf<Byte>()
    var gasroom_ids_list = mutableListOf<Byte>()
    var wasteLiquor_ids_list = mutableListOf<Byte>()
    var oxygen_ids_list = mutableListOf<Byte>()
    var steamer_ids_list = mutableListOf<Byte>()

    //    val modelMap = HashMap<String, ByteArray>()
    fun idsListClear() {
        gasdock_ids_list.clear()
        gasroom_ids_list.clear()
        wasteLiquor_ids_list.clear()
        oxygen_ids_list.clear()
        steamer_ids_list.clear()
        protocolBuffers.clear()
    }

    @ExperimentalUnsignedTypes
    val datahandler = object : Handler(Looper.getMainLooper()) {
        @SuppressLint("SimpleDateFormat")
        @RequiresApi(Build.VERSION_CODES.O)
        override fun handleMessage(msg: Message) {
            if (msg.what == 2) {
                val dateformat: SimpleDateFormat =
                    SimpleDateFormat("yyyy-mm-dd kk:mm:ss", Locale("ko", "KR"))
                val date: Date = Date(System.currentTimeMillis())
                val latest_time: String = dateformat.format(date)
                mainViewModel.alertInfo.add(
                    SetAlertData(
                        latest_time,
                        0,
                        0,
                        "시리얼 통신 연결이 끊겼습니다.",
                        0,
                        true
                    )
                )
            }
//            Log.d(mainTAG, "datahandler : ${HexDump.dumpHexString(msg.obj as ByteArray)}")
            val receiveParser = SaminProtocol()
            receiveParser.parse(msg.obj as ByteArray)

            if (receiveParser.packetName == "CheckProductPing") {
                val model = receiveParser.modelName
                val input_id = receiveParser.mProtocol.get(3)
                when (model) {
                    "GasDock" -> {
                        val id = receiveParser.mProtocol.get(3)
                        gasdock_ids_list.add(id)
                        val ids = gasdock_ids_list.distinct().toByteArray()
                        mainViewModel.modelMap[model] = ids
                        mainViewModel.modelMapInt[1] = ids
                    }
                    "GasRoom" -> {
                        val id = receiveParser.mProtocol.get(3)
                        gasroom_ids_list.add(id)
                        val ids = gasroom_ids_list.distinct().toByteArray()
                        mainViewModel.modelMap[model] = ids
                        mainViewModel.modelMapInt[2] = ids
                    }
                    "WasteLiquor" -> {
                        val id = receiveParser.mProtocol.get(3)
                        wasteLiquor_ids_list.add(id)
                        val ids = wasteLiquor_ids_list.distinct().toByteArray()
                        mainViewModel.modelMap[model] = ids
                        mainViewModel.modelMapInt[3] = ids
                    }
                    "Oxygen" -> {
                        val id = receiveParser.mProtocol.get(3)
                        oxygen_ids_list.add(id)
                        val ids = oxygen_ids_list.distinct().toByteArray()
                        mainViewModel.modelMap[model] = ids
                        mainViewModel.modelMapInt[4] = ids
                    }
                    "Steamer" -> {
                        val id = receiveParser.mProtocol.get(3)
                        steamer_ids_list.add(id)
                        val ids = steamer_ids_list.distinct().toByteArray()
                        mainViewModel.modelMap[model] = ids
                        mainViewModel.modelMapInt[5] = ids
                    }
                    "Temp_Hum" -> {

                    }
                }

            } else if (receiveParser.packetName == "RequestFeedBackPing") {
                if (receiveParser.mProtocol.size >= 14) {
                    tmp.Parser(receiveParser.mProtocol)
                }
            }
            super.handleMessage(msg)
        }
    }


    override fun onResume() {
        hideNavigationBar()
        bindSerialService()
//        GlobalUiTimer.getInstance().activity = this
        startModbusService(SaminModbusService::class.java, svcConnection, null)
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
//        GlobalUiTimer.getInstance().activity = this
    }

    private fun setFragment() {
        mainFragment = MainFragment()
        supportFragmentManager.beginTransaction().replace(R.id.HostFragment_container, mainFragment)
            .commit()
        mainsettingFragment = MainSettingFragment()
        alertdialogFragment = AlertDialogFragment()
        scandialogFragment = ScanDialogFragment()
        alertLogFragment = AlertLogFragment()
        adminFragment = AdminFragment()
        aqSettingFragment = AqSettingFragment()
        controlFragment = ControlFragment()
        versionFragment = VersionFragment()
        passwordFragment = PasswordFragment()
        gasdockmainFragment = GasDockMainFragment()
        gasroommainFragment = GasRoomMainFragment()
        wasteLiquorMainFragment = WasteLiquorMainFragment()
        steamerMainFragment = SteamerMainFragment()
        oxygenMainFragment = OxygenMainFragment()
        gasStorageSettingFragment = GasStorageSettingFragment()
        layoutFragment = LayoutFragment()
        connectTestFragment = ConnectTestFragment()
        gasRoomSettingFragment = GasRoomSettingFragment()
        oxygenSettingFragment = OxygenSettingFragment()
        steamerSettingFragment = SteamerSettingFragment()
        wasteLiquorSettingFragment = WasteWaterSettingFragment()
    }

    fun onFragmentChange(index: Int) {
        when (index) {
            MainViewModel.MAINFRAGMENT -> supportFragmentManager.beginTransaction()
                .replace(R.id.HostFragment_container, mainFragment).commit()
            MainViewModel.MAINSETTINGFRAGMENT -> supportFragmentManager.beginTransaction()
                .replace(R.id.HostFragment_container, mainsettingFragment).commit()
            MainViewModel.ALERTDIALOGFRAGMENT -> alertdialogFragment.show(
                supportFragmentManager,
                "Alert"
            )
            MainViewModel.SCANDIALOGFRAGMENT -> scandialogFragment.show(
                supportFragmentManager,
                "Scan"
            )
            MainViewModel.ALERTLOGFRGAMENT -> supportFragmentManager.beginTransaction()
                .replace(R.id.HostFragment_container, alertLogFragment).commit()
            MainViewModel.ADMINFRAGMENT -> supportFragmentManager.beginTransaction()
                .replace(R.id.HostFragment_container, adminFragment).commit()
            MainViewModel.AQSETTINGFRAGMENT -> supportFragmentManager.beginTransaction()
                .replace(R.id.HostFragment_container, aqSettingFragment).commit()
            MainViewModel.CONTROLFRAGMENT -> supportFragmentManager.beginTransaction()
                .replace(R.id.HostFragment_container, controlFragment).commit()
            MainViewModel.VERSIONFRAGMENT -> supportFragmentManager.beginTransaction()
                .replace(R.id.HostFragment_container, versionFragment).commit()
            MainViewModel.PASSWORDFRAGMENT -> supportFragmentManager.beginTransaction()
                .replace(R.id.HostFragment_container, passwordFragment).commit()
            MainViewModel.GASDOCKMAINFRAGMENT -> supportFragmentManager.beginTransaction()
                .replace(R.id.HostFragment_container, gasdockmainFragment).commit()
            MainViewModel.GASROOMMAINFRAGMENT -> supportFragmentManager.beginTransaction()
                .replace(R.id.HostFragment_container, gasroommainFragment).commit()
            MainViewModel.WASTELIQUORMAINFRAGMENT -> supportFragmentManager.beginTransaction()
                .replace(R.id.HostFragment_container, wasteLiquorMainFragment).commit()
            MainViewModel.OXYGENMAINFRAGMENT -> supportFragmentManager.beginTransaction()
                .replace(R.id.HostFragment_container, oxygenMainFragment).commit()
            MainViewModel.STEAMERMAINFRAGMENT -> supportFragmentManager.beginTransaction()
                .replace(R.id.HostFragment_container, steamerMainFragment).commit()
            MainViewModel.GASSTORAGESETTINGFRAGMENT -> supportFragmentManager.beginTransaction()
                .replace(R.id.HostFragment_container, gasStorageSettingFragment).commit()
            MainViewModel.LAYOUTFRAGMENT -> supportFragmentManager.beginTransaction()
                .replace(R.id.HostFragment_container, layoutFragment).commit()
            MainViewModel.CONNECTTESTFRAGEMNT -> supportFragmentManager.beginTransaction()
                .replace(R.id.HostFragment_container, connectTestFragment).commit()
            MainViewModel.GASROOMSETTINGFRAGMENT -> supportFragmentManager.beginTransaction()
                .replace(R.id.HostFragment_container, gasRoomSettingFragment).commit()
            MainViewModel.OXYGENSETTINGFRAGMENT -> supportFragmentManager.beginTransaction()
                .replace(R.id.HostFragment_container, oxygenSettingFragment).commit()
            MainViewModel.STEAMERSETTINGFRAGMENT -> supportFragmentManager.beginTransaction()
                .replace(R.id.HostFragment_container, steamerSettingFragment).commit()
            MainViewModel.WASTELIQUORSETTINGFRAGMENT -> supportFragmentManager.beginTransaction()
                .replace(R.id.HostFragment_container, wasteLiquorSettingFragment).commit()
            else -> supportFragmentManager.beginTransaction()
                .replace(R.id.HostFragment_container, mainFragment).commit()
        }
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
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
//                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
//                        View.SYSTEM_UI_FLAG_FULLSCREEN

        }
    }

    var callbackThread: Thread? = null
    var callTimeoutThread: Thread? = null
    var isSending = false
    var isAnotherJob = false

    fun feedBackThreadInterrupt() {
        isSending = false
        callbackThread?.interrupt()
        callbackThread?.join()
    }

    fun deleteExDataSet() {
        shared.removeBoardSetData(SaminSharedPreference.GASSTORAGE)
        shared.removeBoardSetData(SaminSharedPreference.GASROOM)
        shared.removeBoardSetData(SaminSharedPreference.WASTELIQUOR)
        shared.removeBoardSetData(SaminSharedPreference.OXYGEN)
        shared.removeBoardSetData(SaminSharedPreference.STEAMER)
        mainViewModel.removeModelMap()
        idsListClear()
    }

    /**
     * 에러 유무 확인
     */
    var isCallTimeout = true
    fun callTimemout() {
        isCallTimeout = false
        callTimeoutThread?.interrupt()
        callTimeoutThread?.join()
        isCallTimeout = true

        callTimeoutThread = Thread {
            while (isCallTimeout) {
                try {
                    val elapsed: Long = measureTimeMillis {
                        tmp.timeoutAQCheckStep()
                    }

                    Thread.sleep(50)
                }
                catch (e : Exception)
                {
                    e.printStackTrace()
                }
            }
        }
        callTimeoutThread?.start()
    }

    fun callFeedback() {
        isSending = false
        callbackThread?.interrupt()
        callbackThread?.join()

        isSending = true
        callbackThread = Thread {
            val protocol = SaminProtocol()
            while (isSending) {
                try {
                    if (isAnotherJob) {
                        while (isAnotherJob) {
                            Thread.sleep(10)
                        }
                    }

//                    for ((model, ids) in mainViewModel.modelMap) {
                    for ((md, ids) in mainViewModel.modelMapInt) {
                        for (index in ids.indices) {
                            if (isAnotherJob) {
                                while (isAnotherJob) {
                                    Thread.sleep(10)
                                }
                            }

                            val model = md.toByte()
                            val elapsed: Long = measureTimeMillis {
                                val id = ids.get(index)
                                val key = littleEndianConversion(byteArrayOf(model, id)).toShort()

                                if (!protocolBuffers.containsKey(key)) {
                                    protocol.feedBack(model, id)
                                    protocolBuffers[key] = protocol.mProtocol.clone()
                                }
                                protocolBuffers[key]?.let {
                                    serialService?.sendData(it)
                                }
                            }
//                            Log.d(mainTAG, "measureTimeMillis : $elapsed")

                            Thread.sleep(20)
                            if (model == 4.toByte())
                                Thread.sleep(15)
                        }
                    }
                } catch (e: Exception) {

                }
            }
        }
        callbackThread?.start()
    }

    private var alertTask: Timer? = null
    private var alertCheckTask: Timer? = null
    private var alertSoundTask: Timer? = null

    fun tabletSoundAlertOn() {
        alertTask = kotlin.concurrent.timer(period = 2000) {
            val mediaPlayer: android.media.MediaPlayer? =
                android.media.MediaPlayer.create(this@MainActivity, R.raw.tada)
            mediaPlayer?.start()
        }
    }

    fun tabletSoundAlertOff() {
        alertTask?.cancel()
    }

    var isTabletAlert = false
    val exData = HashMap<Int, Boolean>()
    val exLastErorr = HashMap<Int, String>()

    private fun littleEndianConversion(bytes: ByteArray): Int {
        var result = 0
        for (i in bytes.indices) {
            result = result or (bytes[i].toUByte().toInt() shl 8 * i)
        }
        return result
    }

    var alertThread: Thread? = null
    lateinit var alertAQThread: Thread
    private fun sendAlert() {

//        alertSoundTask = kotlin.concurrent.timer(period = 100) {
        alertThread?.interrupt()
        alertThread?.join()

        alertThread = Thread {
            val protocol = SaminProtocol()
            while (true) {

                val elapsed: Long = measureTimeMillis {
                    val diffkeys = mainViewModel.portAlertMapLed.keys.toMutableList()

                    for ((key, value) in mainViewModel.alertMap) {
                        val aqInfo = HexDump.toByteArray(key)
                        val model = aqInfo[3]
                        val id = aqInfo[2]
                        val port = aqInfo[1]

                        val ledkey = littleEndianConversion(byteArrayOf(model, id)).toShort()

                        // 경고 로그 DB저장
                        if (!exLastErorr[key].equals(value.content)) {
                            exLastErorr[key] = value.content
                            addLogs(
                                value.time,
                                value.model,
                                value.id,
                                value.content,
                                value.port,
                                value.isAlert
                            )
                        }

                        //isAlert 1개 해결된것을 제외
                        if (!value.isAlert) {
                            if (mainViewModel.portAlertMapLed.size == 0)
                                continue

                            var tmpBits = mainViewModel.portAlertMapLed[ledkey] ?: 0b10000.toByte()
                            if (tmpBits and (1 shl (port - 1)).toByte() > 0) {
                                tmpBits = tmpBits xor (1 shl (port - 1)).toByte()
                                mainViewModel.portAlertMapLed[ledkey] = tmpBits
                            }
                            continue
                        }

                        // LED 켜짐 유무 확인
                        // 기존 경고와의 차이점 식별 가능
                        var tmpBits = mainViewModel.portAlertMapLed[ledkey] ?: 0b10000.toByte()
                        val tmplast = mainViewModel.portAlertMapLed[ledkey] ?: 0b10000.toByte()

                        diffkeys.remove(ledkey)

                        tmpBits = tmpBits or (1 shl (port - 1)).toByte()

                        isAnotherJob = true
                        Thread.sleep(100)

                        // LED 경고 상태를 전달.
                        for (cnt in 0..2) {
                            protocol.led_AlertStateByte(model, id, tmpBits)
                            serialService?.sendData(protocol.mProtocol.clone())
                            Thread.sleep(35)
                        }
                        Log.d("Test", "tmpBit: $tmpBits")

                        if (!tmpBits.equals(tmplast)) {
                            // 경고음 처리전
                            if (mainViewModel.isSoundAlert) {
                                tabletSoundAlertOn()
                                protocol.buzzer_On(model, id)
                                for (cnt in 0..2) {
                                    serialService?.sendData(protocol.mProtocol.clone())
                                    Thread.sleep(35)
                                }
                            }
                        }
                        mainViewModel.portAlertMapLed[ledkey] = tmpBits

                        isAnotherJob = false
                    }
                    // 에러가 사라진 AQ 찾기
                    if (diffkeys.size > 0) {
                        isAnotherJob = true
                        Thread.sleep(100)

                        for (tmp in diffkeys) {
                            val aqInfo = HexDump.toByteArray(tmp)
                            val model = aqInfo[1]
                            val id = aqInfo[0]

                            tabletSoundAlertOff()
                            for (cnt in 0..2) {
                                protocol.buzzer_Off(model, id)
                                serialService?.sendData(protocol.mProtocol.clone())
                                Thread.sleep(35)

                                protocol.led_AlertStateByte(model, id, 0.toByte())
                                serialService?.sendData(protocol.mProtocol.clone())
                                Thread.sleep(35)
                            }
                            mainViewModel.portAlertMapLed.remove(tmp)
                        }
                        isAnotherJob = false
                    }
                }
//                Log.d(mainTAG, "measureTimeMillis : $elapsed")

                Thread.sleep(200)
            }

        }

        alertThread?.start()

    }

    private var modbusService: SaminModbusService? = null
    var mHandler: MyHandler? = null
    var mObserveModelMonitorValues: ObserveModelMonitorValues? = null
    var mModelMonitorValues: ModelMonitorValues = ModelMonitorValues()

    private val svcConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(arg0: ComponentName, arg1: IBinder) {
            modbusService = (arg1 as SaminModbusService.SaminModbusServiceBinder).getService()
            mHandler?.let { modbusService?.setHandler(it) }

            modbusService?.svcHandler?.let {
//                val msg2 = it.obtainMessage(SaminModbusService.SETTING_SLAVE_ID, 2)
//                it.sendMessage(msg2)
//
//                val msg7 = it.obtainMessage(SaminModbusService.CHANGE_INPUT_DATA, InputData(0, true))
//                it.sendMessage(msg7)
//                val msg8 = it.obtainMessage(SaminModbusService.CHANGE_INPUT_DATA, InputData(1, true))
//                it.sendMessage(msg8)
//                val msg9 = it.obtainMessage(SaminModbusService.CHANGE_INPUT_DATA, InputData(3, true))
//                it.sendMessage(msg9)
//                val msg10 = it.obtainMessage(SaminModbusService.CHANGE_INPUT_REGISTER, InputRegister(100, 1000))
//                it.sendMessage(msg10)
//                val msg11 = it.obtainMessage(SaminModbusService.CHANGE_INPUT_REGISTER, InputRegister(1100, 1000))
//                it.sendMessage(msg11)
//                val msg12 = it.obtainMessage(SaminModbusService.CHANGE_INPUT_REGISTER, InputRegister(2100, 1000))
//                it.sendMessage(msg12)
//                val msg13 = it.obtainMessage(SaminModbusService.CHANGE_INPUT_REGISTER, InputRegister(3100, 1000))
//                it.sendMessage(msg13)
            }

            modbusService?.setTCPPort(1502)
            modbusService?.setUDPPort(5502)
            modbusService?.setSlaveID(2)
            modbusService?.setSerialPort(921600, 8, 1, 0)
            mObserveModelMonitorValues =
                ObserveModelMonitorValues(modbusService!!, mModelMonitorValues)
            modbusService?.startModbusService()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            modbusService = null
        }
    }

    private fun startModbusService(
        service: Class<*>,
        serviceConnection: ServiceConnection,
        extras: Bundle?
    ) {
        if (!SaminModbusService.SERVICE_CONNECTED) {
            val startService = Intent(this, service)
            if (extras != null && !extras.isEmpty) {
                val keys = extras.keySet()
                for (key in keys) {
                    val extra = extras.getString(key)
                    startService.putExtra(key, extra)
                }
            }
            startService(startService)
        }
        val bindingIntent = Intent(this, service)
        bindService(bindingIntent, serviceConnection, AppCompatActivity.BIND_AUTO_CREATE)
    }

    class MyHandler(activity: MainActivity?) : Handler() {
        private val mActivity: WeakReference<MainActivity>
        override fun handleMessage(msg: Message) {
            when (msg.what) {

            }
        }

        init {
            mActivity = WeakReference(activity)
        }
    }

    fun addLogs(time: String, model: Int, id: Int, content: String, port: Int, isAlert: Boolean) {
        GlobalScope.launch {
            addAlertLogs(
                time,
                model,
                id,
                content,
                port,
                isAlert
            )
        }
    }

    // 알람 로그 생성
    suspend fun addAlertLogs(time: String, model: Int, id: Int, content: String, port: Int, isAlert: Boolean) {
        withContext(Dispatchers.IO) {
            var dao = Room.databaseBuilder(
                application,
                AlertDatabase::class.java,
                "alertLogs")
                .build()
                .alertDAO()
            var data: AlertData = AlertData(
                time,
                model,
                id,
                content,
                port,
                isAlert
            )
            dao.insertData(data)
        }
    }
}


