package com.coai.samin_total

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.coai.libmodbus.service.SaminModbusService
import com.coai.samin_total.Dialog.*
import com.coai.samin_total.GasDock.GasDockMainFragment
import com.coai.samin_total.GasDock.GasStorageSettingFragment
import com.coai.samin_total.GasDock.SetGasStorageViewData
import com.coai.samin_total.GasRoom.GasRoomMainFragment
import com.coai.samin_total.GasRoom.GasRoomSettingFragment
import com.coai.samin_total.GasRoom.RoomLeakTestFragment
import com.coai.samin_total.GasRoom.SetGasRoomViewData
import com.coai.samin_total.Logic.*
import com.coai.samin_total.Oxygen.OxygenMainFragment
import com.coai.samin_total.Oxygen.OxygenSettingFragment
import com.coai.samin_total.Oxygen.SetOxygenViewData
import com.coai.samin_total.Service.HexDump
import com.coai.samin_total.Service.SerialService
import com.coai.samin_total.Steamer.SetSteamerViewData
import com.coai.samin_total.Steamer.SteamerMainFragment
import com.coai.samin_total.Steamer.SteamerSettingFragment
import com.coai.samin_total.TempHum.SetTempHumViewData
import com.coai.samin_total.TempHum.TempHumMainFragment
import com.coai.samin_total.TempHum.TempHumSettingFragment
import com.coai.samin_total.WasteLiquor.SetWasteLiquorViewData
import com.coai.samin_total.WasteLiquor.WasteLiquorMainFragment
import com.coai.samin_total.WasteLiquor.WasteWaterSettingFragment
import com.coai.samin_total.database.*
import com.coai.samin_total.databinding.ActivityMainBinding
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {

    private val activityScope = CoroutineScope(Dispatchers.Default + Job())

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
    lateinit var gasRoomLeakTestFragment: RoomLeakTestFragment
    lateinit var alertPopUpFragment: AlertPopUpFragment
    lateinit var tempHumFragment: TempHumMainFragment
    lateinit var tempHumSettingFragment: TempHumSettingFragment

    lateinit var loadingscreenFragment: LoadingscreenFragment

    private lateinit var mainViewModel: MainViewModel
    lateinit var db: SaminDataBase
    lateinit var shared: SaminSharedPreference
    lateinit var tmp: AQDataParser

    lateinit var viewModel: PageViewModel
//    private lateinit var pageListAdapter: PageListAdapter

    private var protocolBuffers = ConcurrentHashMap<Short, ByteArray>()

    // 설정 수신 버퍼
    private val recvGasStorageBuffers = HashMap<Int, ByteArray>()
    private val recvGasRoomBuffers = HashMap<Int, ByteArray>()
    private val recvWasteBuffers = HashMap<Int, ByteArray>()
    private val recvOxygenBuffers = HashMap<Int, ByteArray>()
    private val recvSteamerBuffers = HashMap<Int, ByteArray>()
    private val recvOxygenMSTBuffers = HashMap<Int, ByteArray>()
    private val recvModemapBuffers = HashMap<Int, ByteArray>()
    private val recvLabNameBuffers = HashMap<Int, ByteArray>()
    private val recvTempHumBuffers = HashMap<Int, ByteArray>()

    // 보드별 최종 전송시간
//    private val alertsendLastTime = HashMap<Int, Long>()
    private val alertBoardsendLastTime = HashMap<Short, Long>()

    // 요청주기
    private var FEEDBACK_SLEEP: Long? = null

    companion object {
//        var SERVICE_CONNECTED = false
//
//        const val SETTING_TCP_PORT = 0
//        const val SETTING_UDP_PORT = 1
//        const val SETTING_SLAVE_ID = 2
//        const val SETTING_SERIAL_BAUD = 3
//        const val SETTING_SERIAL_DATABIT = 4
//        const val SETTING_SERIAL_STOPBIT = 5
//        const val SETTING_SERIAL_PARITYBIT = 6
//        const val CHANGE_INPUT_DATA = 7
//        const val CHANGE_INPUT_REGISTER = 8
//        const val START_SERIAL_SERVICE = 9
//        const val ANOTHERJOB_SLEEP: Long = 40
    }
    var baudrate: Baudrate = Baudrate.BPS_1000000

    private var setFragment: Int = -1
    private lateinit var receiver: BroadcastReceiver

//    fun scheduleAppRestart(context: Context) {
//        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val intent = Intent(context, AppRestartReceiver::class.java)
//        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
//            PendingIntent.FLAG_IMMUTABLE)
//
////        val interval = 1 * 60 * 1000L // 10분을 밀리초로 변환
//        val interval = 15 * 1000L // 10분을 밀리초로 변환
//
//        alarmManager.setExactAndAllowWhileIdle(
//            AlarmManager.ELAPSED_REALTIME_WAKEUP,
//            SystemClock.elapsedRealtime() + interval,
//            pendingIntent
//        )
//    }

    fun setRestartAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AppRestartReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)

        val calendar = Calendar.getInstance().apply {
//            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // 이미 일요일이 지났다면, 다음 주로 설정
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 7)
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    fun cancelAllAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 예를 들어, 알람에 사용된 BroadcastReceiver의 클래스 이름이 MyAlarmReceiver라고 가정
        val intent = Intent(context, AppRestartReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.cancel(pendingIntent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cancelAllAlarms(this)
        setRestartAlarm(this)

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if ("com.coai.samin_total.ACTION_SHUTDOWN" == intent.action) {
                    Log.d("SHUTDOWN", " SHUTDOWN : 앱 꺼짐 ============================================")
                    finishAndRemoveTask()

//                    Process.killProcess(Process.myPid())
//                    exitProcess(10)
                }
            }
        }

        val filter = IntentFilter("com.coai.samin_total.ACTION_SHUTDOWN")
        registerReceiver(receiver, filter)

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        val intent = Intent()
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        if (powerManager.isIgnoringBatteryOptimizations(packageName)) {
            intent.action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
        } else {
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        db = SaminDataBase.getIstance(applicationContext)!!
        callbackThread = Thread()
        shared = SaminSharedPreference(this)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        mainViewModel.controlData =
            shared.loadBoardSetData(SaminSharedPreference.CONTROL) as ControlData
        tmp = AQDataParser(mainViewModel)
        baudrate = Baudrate.codesMap.get(shared.loadBoardSetData(SaminSharedPreference.BAUDRATE) as Int)!!
        Log.d("Activity", "baudrate : ${baudrate.value}")
//
//        mainViewModel.isCheckTimeOut =
//            shared.getTimeOutState()
        mainViewModel.isCheckTimeOut.set(shared.getTimeOutState())
        mainViewModel.isSoundAlert = shared.getAlarmSound()
        FEEDBACK_SLEEP = shared.getFeedbackTiming()
        Log.d("Activity", "FEEDBACK_SLEEP : ${FEEDBACK_SLEEP}")
        this.viewModel = ViewModelProvider(
            this,
            ViewModelFactory(application)
        ).get(PageViewModel::class.java)


        setFragment()
        sendAlert()
        popUpAlertSend()

        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler())
        dao = Room.databaseBuilder(
            application,
            AlertDatabase::class.java,
            "alertLogs"
        ).build().alertDAO()

        mBinding.btnHomepage.setOnClickListener {
            val intentr = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.saminsci.com/"))
            startActivity(intentr)
        }

        Thread.setDefaultUncaughtExceptionHandler { _, ex ->
            try {
                ex.message?.let {
                    Firebase.crashlytics.log(it)
                }
            } catch (e: Exception) {
                // If we couldn't write the crash report, there's not much we can do.
                e.printStackTrace()
            }
        }
    }

    var gasdock_ids_list = mutableListOf<Byte>()
    var gasroom_ids_list = mutableListOf<Byte>()
    var wasteLiquor_ids_list = mutableListOf<Byte>()
    var oxygen_ids_list = mutableListOf<Byte>()
    var steamer_ids_list = mutableListOf<Byte>()
    var temphum_ids_list = mutableListOf<Byte>()

    //    val modelMap = HashMap<String, ByteArray>()
    fun idsListClear() {
        gasdock_ids_list.clear()
        gasroom_ids_list.clear()
        wasteLiquor_ids_list.clear()
        oxygen_ids_list.clear()
        steamer_ids_list.clear()
        temphum_ids_list.clear()
        protocolBuffers.clear()
    }


    private fun sortMapByKey(map: Map<Int, ByteArray>): LinkedHashMap<Int, ByteArray> {
        val entries = LinkedList(map.entries)

        entries.sortBy { it.key }

        val result = LinkedHashMap<Int, ByteArray>()
        for (entry in entries) {
            result[entry.key] = entry.value
        }

        return result
    }

    override fun onStart() {
        super.onStart()
        bindMessengerService()
        if (mainViewModel.controlData.useModbusRTU)
            startModbusService(SaminModbusService::class.java, svcConnection, null)
    }

    override fun onResume() {
        AppManager.currentActivity = this
        hideNavigationBar()
        uiError()
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        AppManager.currentActivity = null
//        GlobalUiTimer.getInstance().activity = this
        isrunthUIError.set(false)
        thUIError.interrupt()
        thUIError.join()
    }

    override fun onStop() {
        super.onStop()
//        unbindMessengerService()
    }

    override fun onDestroy() {

        // callbackThread
        discallFeedback()
        // alertTask
        tabletSoundAlertOff()
        // callTimeoutThread
        discallTimemout()

        isrunthUIError.set(true)
        thUIError.join()
        //alertThread
        isrunthAlert.set(false)
        alertThread?.join()
        // popUpThread
        popUpThreadInterrupt()

        unregisterReceiver(receiver)
        try {
            if (mainViewModel.controlData.useModbusRTU)
                unbindService(svcConnection)
        } catch (ex: Error) {
            ex.printStackTrace()
        }
        unbindMessengerService()

        activityScope.cancel()
        Log.d("SHUTDOWN", " SHUTDOWN : 앱 꺼짐 ============================================ onDestroy")
        super.onDestroy()
        Log.d("SHUTDOWN", " SHUTDOWN : 앱 꺼짐 ============================================ onDestroy super.onDestroy()")
    }

    private fun setFragment() {
        loadingscreenFragment = LoadingscreenFragment()
        supportFragmentManager.beginTransaction().replace(R.id.HostFragment_container, loadingscreenFragment).commit()

        mainFragment = MainFragment()
//        supportFragmentManager.beginTransaction().replace(R.id.HostFragment_container, mainFragment)
//            .commit()
        setFragment = MainViewModel.MAINFRAGMENT
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
        gasRoomLeakTestFragment = RoomLeakTestFragment()
        alertPopUpFragment = AlertPopUpFragment()
        tempHumFragment = TempHumMainFragment()
        tempHumSettingFragment = TempHumSettingFragment()
    }

    fun onFragmentChange(index: Int) {
        setFragment = index
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
            MainViewModel.GASROOMLEAKTESTFRAGMENT -> supportFragmentManager.beginTransaction()
                .replace(R.id.HostFragment_container, gasRoomLeakTestFragment).commit()
            MainViewModel.TEMPHUMMAINFRAGMENT -> supportFragmentManager.beginTransaction()
                .replace(R.id.HostFragment_container, tempHumFragment).commit()
            MainViewModel.TEMPHUMSETTINGFRAGMENT -> supportFragmentManager.beginTransaction()
                .replace(R.id.HostFragment_container, tempHumSettingFragment).commit()
            MainViewModel.LOADINGFRAGMENT -> supportFragmentManager.beginTransaction()
                .replace(R.id.HostFragment_container, loadingscreenFragment).commit()
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

//    @RequiresApi(Build.VERSION_CODES.R)
    fun hideNavigationBar() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    var callbackThread: Thread? = null
    var callTimeoutThread: Thread? = null
//    var isSending = false
    var isSending = AtomicBoolean(false)
    val isAnotherJob = AtomicBoolean(false)

    fun feedBackThreadInterrupt() {
//        isSending = false
        isSending.set(false)
        callbackThread?.interrupt()
        callbackThread?.join()
    }

    fun deleteExDataSet() {
        shared.removeBoardSetData(SaminSharedPreference.GASSTORAGE)
        shared.removeBoardSetData(SaminSharedPreference.GASROOM)
        shared.removeBoardSetData(SaminSharedPreference.WASTELIQUOR)
        shared.removeBoardSetData(SaminSharedPreference.OXYGEN)
        shared.removeBoardSetData(SaminSharedPreference.STEAMER)
        shared.removeBoardSetData(SaminSharedPreference.MASTEROXYGEN)
        shared.removeBoardSetData(SaminSharedPreference.TEMPHUM)
        mainViewModel.removeModelMap()
        mainViewModel.oxygensData.clear()
        idsListClear()
    }

    /**
     * 에러 유무 확인
     */
//    var isCallTimeout = true
    var isCallTimeout = AtomicBoolean(true)

    fun discallTimemout() {
//        isCallTimeout = false
        isCallTimeout.set(false)
//        callTimeoutThread?.interrupt()
        callTimeoutThread?.join()
        callTimeoutThread = null
    }

    fun callTimemout() {
//        isCallTimeout = false
        isCallTimeout.set(false)
        callTimeoutThread?.interrupt()
        callTimeoutThread?.join()

//        isCallTimeout = true
        isCallTimeout.set(true)
        if (mainViewModel.isCheckTimeOut.get()) {
            callTimeoutThread = Thread {
                while (isCallTimeout.get()) {
                    try {
//                        val elapsed: Long = measureTimeMillis {
//                            tmp.timeoutAQCheckStep()
//                        }
                        tmp.timeoutAQCheckStep()
//                    Log.d("callTimeoutThread", "Time : $elapsed")

                        Thread.sleep(50)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            callTimeoutThread?.start()
        }
    }

    private fun discallFeedback() {
//        isSending = false
        isSending.set(false)
//        callbackThread?.interrupt()
        callbackThread?.join()
    }

    fun callFeedback() {
//        isSending = false
        isSending.set(false)
        callbackThread?.interrupt()
        callbackThread?.join()

//        isSending = true
        isSending.set((true))
        callbackThread = Thread {
            val protocol = SaminProtocol()
            while (isSending.get()) {
                try {
                    while (mainViewModel.controlData.isMirrorMode) {
                        Thread.sleep(10)
                    }

                    while (isAnotherJob.get()) {
                        Thread.sleep(10)
                    }
                    mainViewModel.setCurrnetDate(LocalDateTime.now())
                    val processMils = measureTimeMillis {
                        for ((md, ids) in mainViewModel.modelMapInt) {
                            for (index in ids.indices) {
                                if (isAnotherJob.get()) {
                                    while (isAnotherJob.get()) {
                                        Thread.sleep(10)
                                    }
                                }

                                val model = md.toByte()
//                                val elapsed: Long = measureTimeMillis {
//                                    val id = ids.get(index)
//                                    val key =
//                                        littleEndianConversion(byteArrayOf(model, id)).toShort()
//
//                                    if (!protocolBuffers.containsKey(key)) {
//                                        protocol.feedBack(model, id)
//                                        protocolBuffers[key] = protocol.mProtocol.clone()
//                                    }
//                                    protocolBuffers[key]?.let {
//                                        sendProtocolToSerial(it)
//                                    }
//                                }
                                val id = ids.get(index)
                                val key =
                                    littleEndianConversion(byteArrayOf(model, id)).toShort()

                                if (!protocolBuffers.containsKey(key)) {
                                    protocol.feedBack(model, id)
                                    protocolBuffers[key] = protocol.mProtocol.clone()
                                }
                                protocolBuffers[key]?.let {
                                    sendProtocolToSerial(it)
                                }
                                Thread.sleep(FEEDBACK_SLEEP!!)
//                                if (model == 4.toByte())
//                                    Thread.sleep(15)
//                            Log.d(mainTAG, "sleep ============= " )
                            }
                        }
                    }
                    val sleeptime = 333 - processMils
                    if (sleeptime < 333 && sleeptime > 0) {
                        Thread.sleep(sleeptime)
                    }

//                    sendProtocolToSerial(byteArrayOf(0.toByte()))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        callbackThread?.start()
    }

    private var alertTask: Timer? = null
//    private var alertCheckTask: Timer? = null
//    private var alertSoundTask: Timer? = null

    var mediaPlayer: android.media.MediaPlayer? = null
    fun tabletSoundAlertOn() {
        if (alertTask == null) {
            alertTask = kotlin.concurrent.timer(period = 2000) {
                if (mediaPlayer == null)
                    mediaPlayer =
                        android.media.MediaPlayer.create(
                            this@MainActivity,
                            R.raw.tada
                        )
//                if (mainViewModel.isSoundAlert)
                mediaPlayer?.let {
                    if (!it.isPlaying)
                        it.start()
                }

            }
        }
    }

    fun tabletSoundAlertOff() {
        alertTask?.cancel()
        alertTask = null
        mediaPlayer?.let {
            if (it.isPlaying)
                it.start()
        }
        mediaPlayer = null
    }

//    var isTabletAlert = false
//    val exData = HashMap<Int, Boolean>()
    val exLastErorr = HashMap<Int, String>()

    private fun littleEndianConversion(bytes: ByteArray): Int {
        var result = 0
        for (i in bytes.indices) {
            result = result or (bytes[i].toUByte().toInt() shl 8 * i)
        }
        return result
    }

    var alertThread: Thread? = null
//    lateinit var alertAQThread: Thread

    fun sendProtocolToSerial(data: ByteArray) {
        if (!mainViewModel.controlData.isMirrorMode) {
            val msg = Message.obtain(null, SerialService.MSG_SERIAL_SEND)
            val bundle = Bundle()
            bundle.putByteArray("", data)
            msg.data = bundle
            serialSVCIPCService?.send(msg)
        }
    }

    private var isrunthAlert = AtomicBoolean(true)
    private fun sendAlert() {

//        alertSoundTask = kotlin.concurrent.timer(period = 100) {
//        alertThread?.interrupt()
        isrunthAlert.set(false)
        alertThread?.join()

        alertThread = Thread {
            val protocol = SaminProtocol()
            val ledchanged = ArrayList<Short>()
            val alertchanged = ArrayList<Short>()
            val alertchangedRemind = ArrayList<Short>()
            val currentLedState = HashMap<Short, Byte>()
//            var prevAlertOxygen: Boolean = false
            while (isrunthAlert.get()) {

                val elapsed: Long = measureTimeMillis {
                    val diffkeys = mainViewModel.portAlertMapLed.keys.toMutableList()

                    ledchanged.clear()
                    alertchanged.clear()
                    alertchangedRemind.clear()
                    currentLedState.clear()
                    for ((key, value) in mainViewModel.alertMap) {
                        val aqInfo = HexDump.toByteArray(key)
                        val model = aqInfo[3]
                        val id = aqInfo[2]
                        val port = aqInfo[1]

                        val ledkey = littleEndianConversion(byteArrayOf(model, id)).toShort()

                        // 경고 로그 DB저장
                        if (!exLastErorr[key].equals(value.time)) {
                            exLastErorr[key] = value.time
                            addLogs(
                                value.time,
                                value.model,
                                value.id,
                                value.content,
                                value.port,
                                value.isAlert
                            )
                        }

                        if (model == 6.toByte()) {
                            if (!value.isAlert) {
                                if (mainViewModel.portAlertMapLed.size == 0)
                                    continue

                                var tmpBits =
                                    mainViewModel.portAlertMapLed[ledkey] ?: 0b10000.toByte()
                                Log.d("LED", "tmpBits 1 = ${tmpBits}")
                                Log.d("alertstate", "alertstate  = ${value.alertState}")

                                tmpBits = tmpBits and value.humtempAlertBit
                                Log.d("tmpBits 제거", "${tmpBits}")
                                if (tmpBits == 16.toByte()) {
                                    tmpBits = 0b00000.toByte()
                                }
                                currentLedState[ledkey] = tmpBits
                                mainViewModel.portAlertMapLed[ledkey] = tmpBits

                                if (!ledchanged.contains(ledkey))
                                    ledchanged.add(ledkey)

                                continue
                            }
                            // LED 켜짐 유무 확인
                            // 기존 경고와의 차이점 식별 가능
                            var tmpBits = currentLedState[ledkey] ?: 0b10000.toByte()
//                            val tmplast = mainViewModel.portAlertMapLed[ledkey] ?: 0b10000.toByte()

                            diffkeys.remove(ledkey)

                            //todo
//                            Log.d("alertstate2", "alertstate  = ${value.alertState}")
//                            mainViewModel.portAlertMapLed.remove(ledkey)
//                            if (value.alertState < 3) {
//                                tmpBits = (tmpBits and 0b11100) or value.humtempAlertBit
//                            }else{
//                                tmpBits = (tmpBits and 0b10011) or value.humtempAlertBit
//                            }

                            tmpBits = tmpBits or value.humtempAlertBit
                            Log.d("tmpBits 알람", "${tmpBits}")

                            currentLedState[ledkey] = tmpBits

                        } else {
                            //isAlert 1개 해결된것을 제외
                            if (!value.isAlert) {
                                if (mainViewModel.portAlertMapLed.size == 0)
                                    continue

                                var tmpBits =
                                    mainViewModel.portAlertMapLed[ledkey] ?: 0b10000.toByte()
//                                Log.d("LED", "tmpBits 1 = ${tmpBits}")
                                if (tmpBits and (1 shl (port - 1)).toByte() > 0 && model != 6.toByte()) {
                                    tmpBits = tmpBits xor (1 shl (port - 1)).toByte()
//                                Log.d("LED", "tmpBits 2 = ${tmpBits}")
                                    currentLedState[ledkey] = tmpBits
                                    mainViewModel.portAlertMapLed[ledkey] = tmpBits

                                    diffkeys.remove(ledkey)
                                    if (!ledchanged.contains(ledkey))
                                        ledchanged.add(ledkey)
                                }
                                continue
                            }

                            // LED 켜짐 유무 확인
                            // 기존 경고와의 차이점 식별 가능
                            var tmpBits = currentLedState[ledkey] ?: 0b10000.toByte()
//                            val tmplast = mainViewModel.portAlertMapLed[ledkey] ?: 0b10000.toByte()

                            diffkeys.remove(ledkey)

//                        tmpBits = tmpBits or (1 shl (port - 1)).toByte()
                            if (model == 4.toByte()) {
                                tmpBits = 0b11111
                            } else if (model == 5.toByte()) {
                                tmpBits = tmpBits or (3 shl (port - 1)).toByte()

                            } else {
                                tmpBits = tmpBits or (1 shl (port - 1)).toByte()
                            }
                            if (id == 8.toByte())
                                continue

                            currentLedState[ledkey] = tmpBits
                        }

                    }

                    // 경고 처리
                    if (currentLedState.size > 0) {
                        isAnotherJob.set(true)
                        Thread.sleep(FEEDBACK_SLEEP!!)
                        var model: Byte
                        var id: Byte
                        try {
                            for ((k, v) in currentLedState) {
                                id = (k.toInt() shr 8 and 0xFF).toByte()
                                model = (k and 0xFF).toByte()
                                val tmplast = mainViewModel.portAlertMapLed[k] ?: 0b10000.toByte()
//                                Log.d("LED", "tmplast = ${tmplast}")
                                if (v > tmplast) {
                                    for (cnt in 0..1) {
                                        protocol.led_AlertStateByte(model, id, v)
//                                        Log.d("LED", "v = ${v}")
                                        sendProtocolToSerial(protocol.mProtocol.clone())
                                        Thread.sleep(5)
                                    }

                                    if (!model.equals((4.toByte())))
                                        tabletSoundAlertOn()

                                    if (mainViewModel.isSoundAlert && !model.equals(4.toByte())) {
//                                        tabletSoundAlertOn()
                                        protocol.buzzer_On(model, id)
                                        for (cnt in 0..1) {
                                            sendProtocolToSerial(protocol.mProtocol.clone())
                                            Thread.sleep(5)
                                        }
                                    }
                                    if (model.equals(4.toByte())) {
                                        for (t in 0..7) {
                                            for (cnt in 0..1) {
                                                protocol.buzzer_On(4, t.toByte())
                                                sendProtocolToSerial(protocol.mProtocol.clone())
                                                Thread.sleep(5)
                                            }
                                        }
                                    }

                                    mainViewModel.portAlertMapLed[k] = v
                                } else if (alertBoardsendLastTime[k] == null || alertBoardsendLastTime[k]!! < (System.currentTimeMillis() - 1000 * 60)) {
                                    alertchangedRemind.add(k)
                                    alertBoardsendLastTime[k] = System.currentTimeMillis()
                                }
                            }
                        } catch (ex: Exception) {
                            Log.d("MainActivity", ex.toString())
                        }

                        try {
                            // 경고 상태 재 전송
                            for (tmp in alertchangedRemind) {
                                id = (tmp.toInt() shr 8 and 0xFF).toByte()
                                model = (tmp and 0xFF).toByte()

                                val tmplast = mainViewModel.portAlertMapLed[tmp] ?: 0b10000.toByte()
                                for (cnt in 0..1) {
                                    protocol.led_AlertStateByte(model, id, tmplast)
                                    sendProtocolToSerial(protocol.mProtocol.clone())
                                    Thread.sleep(5)
                                }
                            }
                        } catch (ex: Exception) {
                            Log.d("MainActivity", ex.toString())
                        }
//                        isAnotherJob = false
                        isAnotherJob.set(false)
                    }

                    // 일부 LED 정상화
                    if (ledchanged.size > 0) {
//                        isAnotherJob = true
                        isAnotherJob.set(true)
                        Thread.sleep(FEEDBACK_SLEEP!!)
                        var model: Byte
                        var id: Byte
                        var tmpBits: Byte
                        for (tmp in ledchanged) {
                            id = (tmp.toInt() shr 8 and 0xFF).toByte()
                            model = (tmp and 0xFF).toByte()

                            tmpBits = currentLedState[tmp] ?: 0b10000.toByte()
                            Log.d("ledtest", "model = ${model}, id = ${id}tmpBits = ${tmpBits}")
                            for (cnt in 0..1) {
                                protocol.led_AlertStateByte(model, id, tmpBits)
                                sendProtocolToSerial(protocol.mProtocol.clone())
                                Thread.sleep(5)
                            }
                            mainViewModel.portAlertMapLed[tmp] = tmpBits
                        }

//                        isAnotherJob = false
                        isAnotherJob.set(false)
                    }

                    // 에러가 사라진 AQ 찾기
                    if (diffkeys.size > 0) {
//                        isAnotherJob = true
                        isAnotherJob.set(true)
                        Thread.sleep(FEEDBACK_SLEEP!!)
                        for (tmp in diffkeys) {
                            val aqInfo = HexDump.toByteArray(tmp)
                            val model = aqInfo[1]
                            val id = aqInfo[0]
                            Log.d("diffkeys", "model = ${model}, id = ${id}")
                            mainViewModel.portAlertMapLed.remove(tmp)
                            if (id == 8.toByte())
                                continue

                            if (!model.equals(4.toByte())) {
                                for (cnt in 0..1) {
                                    protocol.buzzer_Off(model, id)
                                    sendProtocolToSerial(protocol.mProtocol.clone())
                                    Thread.sleep(5)
                                }
                            }

                            for (cnt in 0..1) {
                                protocol.led_AlertStateByte(model, id, 0.toByte())
                                sendProtocolToSerial(protocol.mProtocol.clone())
                                Thread.sleep(5)
                            }
                        }
//                        isAnotherJob = false
                        isAnotherJob.set(false)
                    }

                    val targets = java.util.HashMap<Int, Int>()
                    for (t in mainViewModel.alertMap.values) {
                        if (t.isAlert && !targets.containsKey(t.model)) {
                            targets[t.model] = t.model
                        }
                    }

                    if (targets.size == 0) {
                        tabletSoundAlertOff()
                    }
                }
                Log.d("sendAlert", "measureTimeMillis : $elapsed")

                Thread.sleep(200)
            }

        }
        isrunthAlert.set(true)
        alertThread?.start()

    }


    lateinit var thUIError: Thread
    private var isrunthUIError = AtomicBoolean(true)
    private fun uiError() {
        isrunthUIError.set(true)
        thUIError = Thread {
            try {
                while (isrunthUIError.get()) {
                    // 메인화면 경고 유무 변화
                    val targets = java.util.HashMap<Int, Int>()
                    for (t in mainViewModel.alertMap.values) {
                        if (t.isAlert && !targets.containsKey(t.model)) {
                            targets[t.model] = t.model
                        }
                    }

                    runOnUiThread {
                        try {
                            mainViewModel.gasStorageAlert.value = targets.containsKey(1)
                        } catch (ex: Exception) {
                            Log.d("MainActivity", ex.toString())
                        }
                        try {
                            mainViewModel.gasRoomAlert.value = targets.containsKey(2)
                        } catch (ex: Exception) {
                            Log.d("MainActivity", ex.toString())
                        }
                        try {
                            mainViewModel.wasteAlert.value = targets.containsKey(3)
                        } catch (ex: Exception) {
                            Log.d("MainActivity", ex.toString())
                        }
                        try {
                            mainViewModel.oxyenAlert.value = targets.containsKey(4)
                        } catch (ex: Exception) {
                            Log.d("MainActivity", ex.toString())
                        }
                        try {
                            mainViewModel.steamerAlert.value = targets.containsKey(5)
                        } catch (ex: Exception) {
                            Log.d("MainActivity", ex.toString())
                        }
                        try {
                            mainViewModel.tempHumAlert.value = targets.containsKey(6)
                        } catch (ex: Exception) {
                            Log.d("MainActivity", ex.toString())
                        }
                    }

                    Thread.sleep(100)
                }
            } catch (e: Exception) {
                Log.d("MainActivity", e.toString())
            }
        }
        thUIError.start()
//        thUIError?.let {
//            it.start()
//        }
    }

    //    private var modbusService: SaminModbusService? = null
//    var mHandler: MyHandler? = null

//    var mModelMonitorValues: ModelMonitorValues = ModelMonitorValues()

    private val svcConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(arg0: ComponentName, arg1: IBinder) {
            mainViewModel.modbusService =
                (arg1 as SaminModbusService.SaminModbusServiceBinder).getService()
            mHandler.let { mainViewModel.modbusService?.setHandler(it) }

            mainViewModel.modbusService?.setSlaveID(mainViewModel.controlData.modbusRTUID)

            if (mainViewModel.controlData.useModbusRTU)
                mainViewModel.modbusService?.setSerialPort(
                    mainViewModel.controlData.modbusBaudrate.value,
                    8,
                    1,
                    0
                )

//            mainViewModel.mObserveModelMonitorValues =
//                ObserveModelMonitorValues(mainViewModel.modbusService!!, mainViewModel.mModelMonitorValues)
            mainViewModel.modbusService?.startModbusService()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mainViewModel.modbusService = null
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
        bindService(bindingIntent, serviceConnection, BIND_AUTO_CREATE)
    }

    private val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                SaminModbusService.START_SERIAL_SERVICE -> {
                    mainViewModel.modbusService?.resetProcessImage(mainViewModel.controlData.modbusRTUID)
                    Thread.sleep(500)
                    mainViewModel.refreshModbusModels()
                }
            }
        }
    }

    private fun addLogs(time: String, model: Int, id: Int, content: String, port: Int, isAlert: Boolean) {
        activityScope.launch {
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


    lateinit var dao: AlertDAO


    // 알람 로그 생성
    private suspend fun addAlertLogs(
        time: String,
        model: Int,
        id: Int,
        content: String,
        port: Int,
        isAlert: Boolean
    ) {
        withContext(Dispatchers.IO) {
            val data = AlertData(
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

    inner class ExceptionHandler : Thread.UncaughtExceptionHandler {
        override fun uncaughtException(t: Thread, e: Throwable) {
            e.printStackTrace()
            finishAndRemoveTask()
            Process.killProcess(Process.myPid())
            exitProcess(10)
        }
    }

    val dateformat: SimpleDateFormat =
        SimpleDateFormat("yyyy-mm-dd kk:mm:ss", Locale("ko", "KR"))
    private val serialSVCIPCHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                SerialService.MSG_SERIAL_CONNECT-> {
                    Log.d(mainTAG, "MSG_SERIAL_CONNECT ===========================!!!!!!!!!!!!")
//                    val fragmentManager = supportFragmentManager // 또는 fragmentManager를 사용할 수도 있습니다.
//                    val fragments = fragmentManager.fragments
//
//                    fragments.forEach { fragment ->
//                        if (fragment.isVisible) {
//                            // 현재 활성화된 프래그먼트입니다. 여기서 fragment 변수를 사용하면 됩니다.
//                            // 예: val currentFragmentName = fragment::class.java.simpleName
//                            val currentFragmentName = fragment::class.java.simpleName
//                            Log.d(mainTAG, "MSG_SERIAL_CONNECT ======= ${currentFragmentName}")
//                            if ("LoadingscreenFragment" == currentFragmentName) {
//
//                            }
//                        }
//                    }
                    mainViewModel.scanDone.value = true

                }
                SerialService.MSG_SERIAL_RECV -> {
                    val buff = msg.data.getByteArray("")
//                    Log.d(mainTAG, "serialSVCIPCHandler MSG_SERIAL_RECV : \n${HexDump.dumpHexString(buff)}")
                    if (buff != null) {
                        if (buff.size >= 14) {
                            tmp.Parser(buff)
                        }
                    }
                }
                SerialService.MSG_SERIAL_DISCONNECT -> {
                    val date = Date(System.currentTimeMillis())
                    val latesttime: String = dateformat.format(date)
                    mainViewModel.alertInfo.add(
                        SetAlertData(
                            latesttime,
                            0,
                            0,
                            "시리얼 통신 연결이 끊겼습니다.",
                            0,
                            true
                        )
                    )
                }
                SerialService.MSG_NO_SERIAL -> {
                    Toast.makeText(
                        applicationContext,
                        "connection failed: device not found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                SerialService.MSG_CHECK_VERSION -> {
                    Toast.makeText(this@MainActivity, "펌웨어 버전 : ${msg.arg1}", Toast.LENGTH_SHORT)
                        .show()
                }
                SerialService.MSG_CHECK_PING -> {
                    if (mainViewModel.isScanmode.get()) {
                        when (msg.arg1) {
                            1 -> {
                                gasdock_ids_list.add(msg.arg2.toByte())
                                val ids = gasdock_ids_list.distinct().toByteArray()
                                mainViewModel.modelMap["GasDock"] = ids
                                mainViewModel.modelMapInt[msg.arg1] = ids
                            }
                            2 -> {
                                gasroom_ids_list.add(msg.arg2.toByte())
                                val ids = gasroom_ids_list.distinct().toByteArray()
                                mainViewModel.modelMap["GasRoom"] = ids
                                mainViewModel.modelMapInt[msg.arg1] = ids
                            }
                            3 -> {
                                wasteLiquor_ids_list.add(msg.arg2.toByte())
                                val ids = wasteLiquor_ids_list.distinct().toByteArray()
                                mainViewModel.modelMap["WasteLiquor"] = ids
                                mainViewModel.modelMapInt[msg.arg1] = ids
                            }
                            4 -> {
                                oxygen_ids_list.add(msg.arg2.toByte())
                                val ids = oxygen_ids_list.distinct().toByteArray()
                                mainViewModel.modelMap["Oxygen"] = ids
                                mainViewModel.modelMapInt[msg.arg1] = ids
                            }
                            5 -> {
                                steamer_ids_list.add(msg.arg2.toByte())
                                val ids = steamer_ids_list.distinct().toByteArray()
                                mainViewModel.modelMap["Steamer"] = ids
                                mainViewModel.modelMapInt[msg.arg1] = ids
                            }
                            6 -> {
                                temphum_ids_list.add(msg.arg2.toByte())
                                val ids = temphum_ids_list.distinct().toByteArray()
                                mainViewModel.modelMap["TempHum"] = ids
                                mainViewModel.modelMapInt[msg.arg1] = ids
                            }
                        }
                    }
                }
                SerialService.MSG_SHARE_SETTING -> {
                    val buff = msg.data.getByteArray("")
                    Log.d(mainTAG, "datahandler : \n${HexDump.dumpHexString(buff)}")
                    if (buff != null) {
                        when (buff[2]) {
                            0x11.toByte() -> {
                                // 가스 독
                                recvGasStorageBuffers[buff[7].toInt()] = buff.clone()
                            }
                            0x12.toByte() -> {
                                // 가스 룸
                                recvGasRoomBuffers[buff[7].toInt()] = buff.clone()
                            }
                            0x13.toByte() -> {
                                // 폐액통
                                recvWasteBuffers[buff[7].toInt()] = buff.clone()
                            }
                            0x14.toByte() -> {
                                // 산소농도
                                recvOxygenBuffers[buff[7].toInt()] = buff.clone()
                            }
                            0x15.toByte() -> {
                                // 스팀기
                                recvSteamerBuffers[buff[7].toInt()] = buff.clone()
                            }
                            0x16.toByte() -> {
                                // 산소농도 대표
                                recvOxygenMSTBuffers[buff[7].toInt()] = buff.clone()
                            }
                            0x17.toByte() -> {
                                recvModemapBuffers[buff[7].toInt()] = buff.clone()
                            }
                            0x18.toByte() -> {
                                recvLabNameBuffers[buff[7].toInt()] = buff.clone()
                            }
                            0x19.toByte() -> {
                                recvTempHumBuffers[buff[7].toInt()] = buff.clone()
                            }
                            0x20.toByte() -> {
                                // 설정 데이터 전송 완료
                                Log.d(mainTAG, "설정 데이터 전송 완료 ================")
                                mainViewModel.clearPopUP()
                                var allDone = true
                                // 가스독 설정 복원
                                var tmpgas = ByteArray(0)
                                val sortGas = sortMapByKey(recvGasStorageBuffers)
                                for (t in sortGas.values) {
                                    tmpgas = tmpgas.plus(t.sliceArray(8 until t.size))
                                }
                                //                            Log.d(mainTAG, "tmpgas : ${HexDump.dumpHexString(tmpgas)}")
                                try {
                                    @OptIn(ExperimentalSerializationApi::class)
                                    val objgas = ProtoBuf.decodeFromByteArray<List<SetGasStorageViewData>>(
                                            tmpgas
                                        )
                                    mainViewModel.GasStorageDataLiveList.clear(true)
                                    for (t in objgas) {
                                        t.pressureLeft = 0f
                                        t.pressure = 0f
                                        t.pressureRight = 0f
                                        t.isAlert = false
                                        t.isAlertLeft = false
                                        t.isAlertRight = false
                                        t.heartbeatCount = 0u
                                        mainViewModel.GasStorageDataLiveList.add(t)
                                    }
                                    val tmpbuff = mutableListOf<SetGasStorageViewData>()
                                    for (i in mainViewModel.GasStorageDataLiveList.value!!) {
                                        tmpbuff.add(i)
                                    }
                                    shared.saveBoardSetData(SaminSharedPreference.GASSTORAGE, tmpbuff)

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    allDone = false
                                }

                                // 가스룸 설정 복원
                                var tmpgasroom = ByteArray(0)
                                val sortGasroom = sortMapByKey(recvGasRoomBuffers)
                                for (t in sortGasroom.values) {
                                    tmpgasroom = tmpgasroom.plus(t.sliceArray(8 until t.size))
                                }
                                //                            Log.d(mainTAG, "tmpgasroom : ${HexDump.dumpHexString(tmpgasroom)}")
                                try {
                                    @OptIn(ExperimentalSerializationApi::class)
                                    val objgas = ProtoBuf.decodeFromByteArray<List<SetGasRoomViewData>>(
                                            tmpgasroom
                                        )
                                    mainViewModel.GasRoomDataLiveList.clear(true)
                                    for (t in objgas) {
                                        t.pressure = 0f
                                        t.isAlert = false
                                        t.heartbeatCount = 0u
                                        mainViewModel.GasRoomDataLiveList.add(t)
                                    }
                                    val tmpbuff = mutableListOf<SetGasRoomViewData>()
                                    for (i in mainViewModel.GasRoomDataLiveList.value!!) {
                                        tmpbuff.add(i)
                                    }
                                    shared.saveBoardSetData(SaminSharedPreference.GASROOM, tmpbuff)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    allDone = false
                                }

                                // 폐액통 설정 복원
                                var tmpwaste = ByteArray(0)
                                val sortWaste = sortMapByKey(recvWasteBuffers)
                                for (t in sortWaste.values) {
                                    tmpwaste = tmpwaste.plus(t.sliceArray(8 until t.size))
                                }
                                Log.d(mainTAG, "tmpwaste : ${HexDump.dumpHexString(tmpwaste)}")
                                try {
                                    @OptIn(ExperimentalSerializationApi::class)
                                    val objgas = ProtoBuf.decodeFromByteArray<List<SetWasteLiquorViewData>>(
                                            tmpwaste
                                        )
                                    mainViewModel.WasteLiquorDataLiveList.clear(true)
                                    for (t in objgas) {
                                        t.isAlert = false
                                        t.heartbeatCount = 0u
                                        mainViewModel.WasteLiquorDataLiveList.add(t)
                                    }
                                    val tmpbuff = mutableListOf<SetWasteLiquorViewData>()
                                    for (i in mainViewModel.WasteLiquorDataLiveList.value!!) {
                                        tmpbuff.add(i)
                                    }
                                    shared.saveBoardSetData(SaminSharedPreference.WASTELIQUOR, tmpbuff)

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    allDone = false
                                }

                                // 산소농도 설정 복원
                                var tmpOxygen = ByteArray(0)
                                val sortOxygen = sortMapByKey(recvOxygenBuffers)
                                for (t in sortOxygen.values) {
                                    tmpOxygen = tmpOxygen.plus(t.sliceArray(8 until t.size))
                                }
                                //                            Log.d(mainTAG, "tmpOxygen : ${HexDump.dumpHexString(tmpOxygen)}")
                                try {
                                    @OptIn(ExperimentalSerializationApi::class)
                                    val objgas = ProtoBuf.decodeFromByteArray<List<SetOxygenViewData>>(
                                            tmpOxygen
                                        )
                                    mainViewModel.OxygenDataLiveList.clear(true)
                                    for (t in objgas) {
                                        t.isAlert = false
                                        t.setValue = 0f
                                        t.heartbeatCount = 0u
                                        mainViewModel.OxygenDataLiveList.add(t)
                                    }
                                    val tmpbuff = mutableListOf<SetOxygenViewData>()
                                    for (i in mainViewModel.OxygenDataLiveList.value!!) {
                                        tmpbuff.add(i)
                                    }
                                    shared.saveBoardSetData(SaminSharedPreference.OXYGEN, tmpbuff)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    allDone = false
                                }

                                // 스팀기 설정 복원
                                var tmpSteamer = ByteArray(0)
                                val sortSteamer = sortMapByKey(recvSteamerBuffers)
                                for (t in sortSteamer.values) {
                                    tmpSteamer = tmpSteamer.plus(t.sliceArray(8 until t.size))
                                }
                                //                            Log.d(mainTAG, "tmpSteamer : ${HexDump.dumpHexString(tmpSteamer)}")
                                try {
                                    @OptIn(ExperimentalSerializationApi::class)
                                    val objgas = ProtoBuf.decodeFromByteArray<List<SetSteamerViewData>>(
                                            tmpSteamer
                                        )
                                    mainViewModel.SteamerDataLiveList.clear(true)
                                    for (t in objgas) {
                                        t.isAlertLow = false
                                        t.isAlertTemp = false
                                        t.isTemp = 0
                                        t.heartbeatCount = 0u
                                        mainViewModel.SteamerDataLiveList.add(t)
                                    }
                                    val tmpbuff = mutableListOf<SetSteamerViewData>()
                                    for (i in mainViewModel.SteamerDataLiveList.value!!) {
                                        tmpbuff.add(i)
                                    }
                                    shared.saveBoardSetData(SaminSharedPreference.STEAMER, tmpbuff)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    allDone = false
                                }

                                // 온습도 설정 복원
                                var tmpTempHum = ByteArray(0)
                                val sortTempHum = sortMapByKey(recvTempHumBuffers)
                                for (t in sortTempHum.values) {
                                    tmpTempHum = tmpTempHum.plus(t.sliceArray(8 until t.size))
                                }
                                //                            Log.d(mainTAG, "tmpSteamer : ${HexDump.dumpHexString(tmpSteamer)}")
                                try {
                                    @OptIn(ExperimentalSerializationApi::class)
                                    val objgas = ProtoBuf.decodeFromByteArray<List<SetTempHumViewData>>(
                                            tmpTempHum
                                        )
                                    mainViewModel.TempHumDataLiveList.clear(true)
                                    for (t in objgas) {
                                        t.isAlert = false
                                        t.isTempAlert = false
                                        t.isHumAlert = false
                                        t.temp = 0f
                                        t.hum = 0f
                                        t.heartbeatCount = 0u
                                        mainViewModel.TempHumDataLiveList.add(t)
                                    }
                                    val tmpbuff = mutableListOf<SetTempHumViewData>()
                                    for (i in mainViewModel.TempHumDataLiveList.value!!) {
                                        tmpbuff.add(i)
                                    }
                                    shared.saveBoardSetData(SaminSharedPreference.TEMPHUM, tmpbuff)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    allDone = false
                                }


                                var tmpModemap = ByteArray(0)
                                val sortModemap = sortMapByKey(recvModemapBuffers)
                                for (t in sortModemap.values) {
                                    tmpModemap = tmpModemap.plus(t.sliceArray(8 until t.size))
                                }
                                mainViewModel.modelMap.clear()
                                try {
                                    @OptIn(ExperimentalSerializationApi::class)
                                    val objgas = ProtoBuf.decodeFromByteArray<HashMap<String, ByteArray>>(
                                            tmpModemap
                                        )
                                    for (t in objgas) {
                                        mainViewModel.modelMap[t.key] = t.value
                                        val id = when (t.key) {
                                          "GasDock" -> 1
                                          "GasRoom" -> 2
                                          "WasteLiquor" -> 3
                                          "Oxygen" -> 4
                                          "Steamer" -> 5
                                          "TempHum" -> 6
                                          else -> 1
                                        }

                                        mainViewModel.modelMapInt[id] = t.value.clone()
                                    }
                                    shared.saveHashMap(mainViewModel.modelMap)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    allDone = false
                                }

                                var tmpLabname = ByteArray(0)
                                val sortLabname = sortMapByKey(recvLabNameBuffers)
                                for (t in sortLabname.values) {
                                    tmpLabname = tmpLabname.plus(t.sliceArray(8 until t.size))
                                }
                                try {
                                    @OptIn(ExperimentalSerializationApi::class)
                                    val objLabname = ProtoBuf.decodeFromByteArray<String>(tmpLabname)
                                    SaminSharedPreference(this@MainActivity).labNameSave(
                                        SaminSharedPreference.LABNAME,
                                        objLabname
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    allDone = false
                                }

                                discallFeedback()
                                discallTimemout()

                                tmp.LoadSetting()
//                                tmp.hmapLastedDate.keys.forEach{
//                                    mainViewModel.hasKey.put(it, it)
//                                }
                                for (tmp in tmp.hmapLastedDate.keys) {
                                    mainViewModel.hasKey.put(tmp, tmp)
                                }

                                callFeedback()
                                callTimemout()

                                if (allDone) {
                                    recvGasStorageBuffers.clear()
                                    recvGasRoomBuffers.clear()
                                    recvWasteBuffers.clear()
                                    recvOxygenBuffers.clear()
                                    recvSteamerBuffers.clear()
                                    recvOxygenMSTBuffers.clear()
                                    recvModemapBuffers.clear()
                                    recvTempHumBuffers.clear()
                                    onFragmentChange(MainViewModel.MAINSETTINGFRAGMENT)
                                }
                            }
                        }
                    }
                }
                SerialService.MSG_GASDOCK -> {
                    val recvdata = (msg.data.getSerializable("") as ParsingData)
                    val id = recvdata.id
//                    val model = recvdata.model
                    val time = recvdata.time
                    val datas = recvdata.datas

                    tmp.ParserGas(id, datas, time)
                }
                SerialService.MSG_GASROOM -> {
                    val recvdata = (msg.data.getSerializable("") as ParsingData)
                    val id = recvdata.id
                    val model = recvdata.model
                    val time = recvdata.time
                    val datas = recvdata.datas

                    var loop = 1
                    for (t in datas) {
                        //아이디 1개당 포트 4개 추가
                        val port = loop++.toByte()
                        //키는 아이디 포트
                        val key =
                            littleEndianConversion(byteArrayOf(model, id, port))
                        tmp.hmapLastedDate[key] = time
                        tmp.ProcessGasRoom(key, t)
                    }
                }
                SerialService.MSG_WASTE -> {
                    val recvdata = (msg.data.getSerializable("") as ParsingData)
                    val id = recvdata.id
                    val model = recvdata.model
                    val time = recvdata.time
                    val datas = recvdata.datas

                    var loop = 1
                    for (t in datas) {
                        //아이디 1개당 포트 4개 추가
                        val port = loop++.toByte()
                        //키는 아이디 포트
                        val key =
                            littleEndianConversion(byteArrayOf(model, id, port))
                        tmp.hmapLastedDate[key] = time
                        tmp.ProcessWasteLiquor(key, t)
                    }
                }
                SerialService.MSG_OXYGEN -> {
                    val recvdata = (msg.data.getSerializable("") as ParsingData)
                    val id = recvdata.id
                    val model = recvdata.model
                    val time = recvdata.time
                    val datas = recvdata.datas

                    val port = 1.toByte()
                    val key = littleEndianConversion(byteArrayOf(model, id, port))
                    tmp.hmapLastedDate[key] = time
                    tmp.ProcessOxygen(key, datas[0])
                }
                SerialService.MSG_STEMER -> {
                    val recvdata = (msg.data.getSerializable("") as ParsingData)
                    val id = recvdata.id
                    val model = recvdata.model
                    val time = recvdata.time
                    val datas = recvdata.datas

                    for (loop in 1..2) {
                        val tempData = datas[loop - 1]
                        val levelData = datas[loop + 1]
                        val key =
                            littleEndianConversion(
                                byteArrayOf(
                                    model,
                                    id,
                                    loop.toByte()
                                )
                            )
                        tmp.hmapLastedDate[key] = time
                        tmp.ProcessSteamer(key, tempData, levelData)
                    }
                }
                SerialService.MSG_TEMPHUM -> {
                    val recvdata = (msg.data.getSerializable("") as ParsingData)
                    val id = recvdata.id
                    val model = recvdata.model
                    val time = recvdata.time
                    val datas = recvdata.datas

                    val port = 1.toByte()
                    val key = littleEndianConversion(byteArrayOf(model, id, port))
                    tmp.hmapLastedDate[key] = time
                    val hum = String.format("%.1f", (datas[0].toFloat() / 1000000f)).toFloat()
                    val temp = String.format("%.1f", (datas[1].toFloat() / 1000000f)).toFloat()
                    tmp.ProcessTempHum(key, temp, hum)
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
        serialSVCIPCService?.send(
            Message.obtain(null, SerialService.MSG_UNBIND_CLIENT, 0, 0).apply {
                replyTo = serialSVCIPCClient
            })
        unbindService(serialSVCIPCServiceConnection)
    }

    fun setBaudrate(baudrate: Int) {
        shared.saveBoardSetData(SaminSharedPreference.BAUDRATE, baudrate)
        val msg = Message.obtain(null, SerialService.MSG_BAUDRATE_CHANGE)
        val bundle = Bundle()
        bundle.putInt("", baudrate)
        msg.data = bundle
        serialSVCIPCService?.send(msg)
    }

    private var popUpThread: Thread? = null
//    var isPopUp = false
    val isPopup = AtomicBoolean(false)
    fun popUpAlertSend() {
        isPopup.set(false)
//        popUpThread?.interrupt()
        popUpThread?.join()
//        isPopUp = true
        isPopup.set(true)
        popUpThread = Thread {
            val alertChanged = ArrayList<Short>()
            val alertChangedRemind = ArrayList<Short>()
            val lstValues = ArrayList<Int>()
            val exData = ConcurrentHashMap<Int, SetAlertData>()
            val alertList = mutableListOf<SetAlertData>()
            val removeList = mutableListOf<SetAlertData>()
            val removeMap = ConcurrentHashMap<Int, SetAlertData>()
            val alertRemovelist = mutableListOf<SetAlertData>()

            while (isPopup.get()) {
                try {
                    val elapsed: Long = measureTimeMillis {
                        alertChanged.clear()
                        alertChangedRemind.clear()

                        lstValues.clear()
                        for ((key, value) in mainViewModel.alertMap) {
                            if (exData.containsKey(key)) {
                                if (exData[key]?.isAlert != value.isAlert ||
                                    exData[key]?.alertState != value.alertState ||
                                    exData[key]?.time != value.time
                                ) {
                                    removeList.add(exData[key]!!)
                                    removeMap[key] = exData[key]!!
                                    exData.remove(key)
                                }

                            }


                            if (!exData.containsKey(key)) {
                                //  신규
                                if (value.isAlert) {
                                    if (!lstValues.contains(value.model))
                                        lstValues.add(value.model)
                                    exData[key] = value
                                    alertList.add(value)
                                    if (removeMap.containsKey(key)) {
                                        for (i in removeMap) {
                                            alertRemovelist.add(i.value)
                                        }
                                    }
                                }
                            } else {
                                // 변경
                                if (value.isAlert &&
                                    exData[key]?.isAlert == value.isAlert &&
                                    exData[key]?.alertState != value.alertState
                                ) {
                                    alertList.add(value)
                                    exData[key] = value
                                    if (removeMap.containsKey(key)) {
                                        for (i in removeMap) {
                                            alertRemovelist.add(i.value)
                                        }
                                    }
                                }
                            }
                        }

                        try {
                            val tmp = mainViewModel.errorlivelist
                            if (!mainViewModel.alertDialogFragment.isAdded) {
                                for (i in removeList) {
                                    tmp.remove(i)

                                }
                                removeList.clear()
                            }
                            else {
                                for (i in alertRemovelist) {
                                    tmp.remove(i)
                                }
                                alertRemovelist.clear()
                            }
                            tmp.addAll(alertList)
                            alertList.clear()
                        } catch (e : Exception) {
                            e.printStackTrace()
                            removeList.clear()
                            alertList.clear()
                        }

                        var chk = false
                        for (i in lstValues) {
                            if (setFragment != MainViewModel.MAINFRAGMENT) {
                                when (i) {
                                    1 -> {
                                        if (setFragment != MainViewModel.GASDOCKMAINFRAGMENT) {
                                            chk = true
                                        }
                                    }
                                    2 -> {
                                        if (setFragment != MainViewModel.GASROOMMAINFRAGMENT) {
                                            chk = true
                                        }
                                    }
                                    3 -> {
                                        if (setFragment != MainViewModel.WASTELIQUORMAINFRAGMENT) {
                                            chk = true
                                        }
                                    }
                                    4 -> {
                                        if (setFragment != MainViewModel.OXYGENMAINFRAGMENT) {
                                            chk = true
                                        }
                                    }
                                    5 -> {
                                        if (setFragment != MainViewModel.STEAMERMAINFRAGMENT) {
                                            chk = true
                                        }
                                    }
                                    6 -> {
                                        if (setFragment != MainViewModel.TEMPHUMMAINFRAGMENT) {
                                            chk = true
                                        }
                                    }
                                }
                            }
                        }

                        if (chk) {
                            runOnUiThread {
                                try {
                                    if (!alertPopUpFragment.isAdded)
                                        alertPopUpFragment.show(
                                            supportFragmentManager,
                                            ""
                                        )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                    Log.d("error처리 루틴", "처리시간 : ${elapsed}")
                    Thread.sleep(200)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }

        popUpThread?.start()

    }

    fun popUpThreadInterrupt() {
//        isPopUp = false
        isPopup.set(false)
//        popUpThread?.interrupt()
        popUpThread?.join()
        popUpThread = null
    }

}


