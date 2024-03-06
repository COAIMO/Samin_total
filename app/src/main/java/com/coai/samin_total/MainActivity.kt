package com.coai.samin_total

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.ProgressDialog
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.PowerManager
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.coai.libmodbus.service.SaminModbusService
import com.coai.samin_total.Dialog.AlertDialogFragment
import com.coai.samin_total.Dialog.AlertPopUpFragment
import com.coai.samin_total.Dialog.ScanDialogFragment
import com.coai.samin_total.Dialog.SetAlertData
import com.coai.samin_total.GasDock.GasDockMainFragment
import com.coai.samin_total.GasDock.GasStorageSettingFragment
import com.coai.samin_total.GasDock.SetGasStorageViewData
import com.coai.samin_total.GasRoom.GasRoomMainFragment
import com.coai.samin_total.GasRoom.GasRoomSettingFragment
import com.coai.samin_total.GasRoom.RoomLeakTestFragment
import com.coai.samin_total.GasRoom.SetGasRoomViewData
import com.coai.samin_total.Logic.Baudrate
import com.coai.samin_total.Logic.ControlData
import com.coai.samin_total.Logic.ParsingData
import com.coai.samin_total.Logic.SaminProtocol
import com.coai.samin_total.Logic.SaminSharedPreference
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
import com.coai.samin_total.database.AlertDAO
import com.coai.samin_total.database.AlertData
import com.coai.samin_total.database.AlertDatabase
import com.coai.samin_total.database.PageViewModel
import com.coai.samin_total.database.SaminDataBase
import com.coai.samin_total.database.ViewModelFactory
import com.coai.samin_total.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.lang.Math.max
import java.lang.Math.min
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Date
import java.util.LinkedList
import java.util.Locale
import java.util.Timer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.cancellation.CancellationException
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor
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
//    lateinit var resetscreenFragment: ResetFragment

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
    val FEEDBACK_SLEEP = AtomicLong(20)
    val mainTAG = "태그"
    val isFirstRun = AtomicBoolean(true)

    val isSharingSetting = AtomicBoolean(false)

//    val isReStartApp = AtomicBoolean(false)
//    val restartCount = AtomicInteger(0)
//    private val executorService = Executors.newSingleThreadScheduledExecutor()


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
    val writesleep = AtomicLong(1)

    private var setFragment: Int = -1
    private lateinit var receiver: BroadcastReceiver

    private var isrunthUIError = AtomicBoolean(true)
    private var updateErrorJob: Job? = null

//    private var alertJob: Job? = null
//    private var callbackJob: Job? = null
//    private var callTimeoutJob: Job? = null

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
    private var isCallMainJob = AtomicBoolean(true)
    private var mainJob: Job? = null
    fun callMainJob() {
        mainJob?.cancel()

        isCallMainJob.set(true)

        mainJob = CoroutineScope(Job() + Dispatchers.IO).launch {
            try {
                protocolBuffers.clear()
                val protocol = SaminProtocol()
                val isMirrorMode = mainViewModel.controlData.isMirrorMode

                var lastCallback = System.currentTimeMillis()
                var lastMakeProtocol = System.currentTimeMillis()
                var lastSendAlert = System.currentTimeMillis()
//                var lastWriteAlert = System.currentTimeMillis()
                var indexProtocol = 0

                val ledchanged = ArrayList<Short>()
                val alertchanged = ArrayList<Short>()
                val alertchangedRemind = ArrayList<Short>()
                val currentLedState = HashMap<Short, Byte>()

                while (isCallMainJob.get()) {
                    try {
                        if ((lastMakeProtocol + 1000L) < System.currentTimeMillis()) {
                            lastMakeProtocol = System.currentTimeMillis()
                            protocolBuffers.clear()
                            indexProtocol = 0
                            val processMils = measureTimeMillis {
                                for ((md, ids) in mainViewModel.modelMapInt) {
                                    for (index in ids.indices) {
                                        val model = md.toByte()
                                        val id = ids.get(index)
                                        val key = littleEndianConversion(byteArrayOf(model, id)).toShort()

                                        if (!protocolBuffers.containsKey(key)) {
                                            protocol.feedBack(model, id)
                                            protocolBuffers[key] = protocol.mProtocol.clone()
                                        }
                                    }
                                }
                            }
//                            Log.d("callMainJob", "processMils : $processMils ms")

                            lastMakeProtocol = System.currentTimeMillis()
                        }

                        if (mainViewModel.isDoneLoading.get()) {
                            if (mainViewModel.isCheckTimeOut.get()) {
                                tmp.timeoutAQCheckStep()
                                val usbdetach = mainViewModel.usbdetachetime.get()
                                if (usbdetach != 0L) {
                                    if ((usbdetach + 1000L * 30) < System.currentTimeMillis()) {
                                        Log.d(
                                            "usbdetachetime",
                                            "===================AQ ERROR Time : ${mainViewModel.usbdetachetime.get()} current : ${System.currentTimeMillis()}"
                                        )

                                        val intent = Intent(
                                            applicationContext,
                                            AppRestartReceiver::class.java
                                        )
                                        val pendingIntent = PendingIntent.getBroadcast(
                                            applicationContext, 0, intent,
                                            PendingIntent.FLAG_IMMUTABLE
                                        )
                                        pendingIntent.send()
                                        break
                                    }
                                }
                            }
                        }

                        val diffkeys = mainViewModel.portAlertMapLed.keys.toMutableList()
                        if ((lastSendAlert + 200) < System.currentTimeMillis()) {
                            lastSendAlert = System.currentTimeMillis()

                            if (isrunthAlert.get() && !isAnotherSettingJob.get()) {
                                val elapsed: Long = measureTimeMillis {
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

                                            diffkeys.remove(ledkey)

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
                                                if (tmpBits and (1 shl (port - 1)).toByte() > 0 && model != 6.toByte()) {
                                                    tmpBits = tmpBits xor (1 shl (port - 1)).toByte()
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

                                            diffkeys.remove(ledkey)

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
                                }
//                                Log.d("callMainJob", "elapsed : $elapsed ms")
                            }

                            if (currentLedState.size > 0) {
                                delay(FEEDBACK_SLEEP.get())

                                var model: Byte
                                var id: Byte
                                try {
                                    for ((k, v) in currentLedState) {
                                        id = (k.toInt() shr 8 and 0xFF).toByte()
                                        model = (k and 0xFF).toByte()
                                        val tmplast =
                                            mainViewModel.portAlertMapLed[k] ?: 0b10000.toByte()

                                        if (v > tmplast) {
                                            for (cnt in 0..1) {
                                                protocol.led_AlertStateByte(model, id, v)
                                                sendProtocolToSerial(protocol.mProtocol.clone())
                                                delay(writesleep.get())
                                            }

                                            if (!model.equals((4.toByte())))
                                                tabletSoundAlertOn()

                                            if (mainViewModel.isSoundAlert && !model.equals(4.toByte())) {
                                                protocol.buzzer_On(model, id)
                                                for (cnt in 0..1) {
                                                    sendProtocolToSerial(protocol.mProtocol.clone())
                                                    delay(writesleep.get())
                                                }
                                            }
                                            if (model.equals(4.toByte())) {
                                                for (t in 0..7) {
                                                    for (cnt in 0..1) {
                                                        protocol.buzzer_On(4, t.toByte())
                                                        sendProtocolToSerial(protocol.mProtocol.clone())
                                                        delay(writesleep.get())
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
                                            delay(writesleep.get())
                                        }
                                    }
                                } catch (ex: Exception) {
                                    Log.d("MainActivity", ex.toString())
                                }
                            }

                            if (ledchanged.size > 0) {
                                delay(FEEDBACK_SLEEP.get())

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
                                        delay(writesleep.get())
                                    }
                                    mainViewModel.portAlertMapLed[tmp] = tmpBits
                                }

                                isAnotherJob.set(false)
                            }

                            if (diffkeys.size > 0) {
                                delay(FEEDBACK_SLEEP.get())
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
                                            delay(writesleep.get())
                                        }
                                    }

                                    for (cnt in 0..1) {
                                        protocol.led_AlertStateByte(model, id, 0.toByte())
                                        sendProtocolToSerial(protocol.mProtocol.clone())
                                        delay(writesleep.get())
                                    }
                                }
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

                            lastSendAlert = System.currentTimeMillis()
                        }

                        if((lastCallback + FEEDBACK_SLEEP.get()) < System.currentTimeMillis()) {
                            lastCallback = System.currentTimeMillis()

                            if (!isMirrorMode && isSending.get() && !isAnotherJob.get() && !isAnotherSettingJob.get()) {
                                if (protocolBuffers.size > 0) {
                                    val tmpidx = ((indexProtocol++ % protocolBuffers.size))
                                    val idxs = protocolBuffers.keys().toList()
                                    val tmpdata = protocolBuffers.get(idxs.get(tmpidx))

                                    tmpdata?.let {
                                        sendFeedbackProtocolToSerial(it)
                                    }
                                }
                            }
                            lastCallback = System.currentTimeMillis()
                        }
                    } catch (e : Exception) {
                        e.printStackTrace()
                    }
                    delay(10)
                }
            } catch(ex: CancellationException) {
                ex.printStackTrace()
            }
        }

    }

    fun disCallMainJob() {
        isCallMainJob.set(false)
        mainJob?.cancel()
    }


    fun setRestartAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AppRestartReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
//            set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
//            set(Calendar.HOUR_OF_DAY, 21)
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
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        alarmManager.cancel(pendingIntent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (alarmManager?.canScheduleExactAlarms() == false) {
                Intent().also { intent ->
                    intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    startActivity(intent)
                }
            }
        }

        cancelAllAlarms(this)
        setRestartAlarm(this)


//        val ttt = Intent(this, Watchdog::class.java)
//        ContextCompat.startForegroundService(this, ttt)

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if ("com.coai.samin_total.ACTION_SHUTDOWN" == intent.action) {
                    Log.d("SHUTDOWN", " SHUTDOWN : 앱 꺼짐 ============================================")
                    finishAndRemoveTask()
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
//        callbackThread = Thread()
        shared = SaminSharedPreference(this@MainActivity)
        mainViewModel = ViewModelProvider(this@MainActivity).get(MainViewModel::class.java)
        mainViewModel.controlData =
            shared.loadBoardSetData(SaminSharedPreference.CONTROL) as ControlData
        tmp = AQDataParser(mainViewModel)
        baudrate = Baudrate.codesMap.get(shared.loadBoardSetData(SaminSharedPreference.BAUDRATE) as Int)!!
        FEEDBACK_SLEEP.set(shared.getFeedbackTiming())

        Log.d("Activity", "baudrate : ${baudrate.value}")
        var feedbacks:Long = 20
        when(baudrate) {
            Baudrate.BPS_2400 -> {
                feedbacks = max(100, shared.getFeedbackTiming())
                writesleep.set(31)
            }
            Baudrate.BPS_4800 -> {
                feedbacks = max(50, shared.getFeedbackTiming())
                writesleep.set(16)
            }
            Baudrate.BPS_9600 -> {
                feedbacks = max(30, shared.getFeedbackTiming())
                writesleep.set(9)
            }
            Baudrate.BPS_14400 -> {
                feedbacks = max(20, shared.getFeedbackTiming())
                writesleep.set(6)
            }
            else -> {
                feedbacks = max(20, shared.getFeedbackTiming())
                writesleep.set(5)
            }
        }

        if (feedbacks != shared.getFeedbackTiming()) {
            FEEDBACK_SLEEP.set(feedbacks)
            shared.SaveFeedbackTiming(feedbacks)
        }

        Log.d("Activity", "FEEDBACK_SLEEP : ${FEEDBACK_SLEEP.get()}")
//
//        mainViewModel.isCheckTimeOut =
//            shared.getTimeOutState()
        mainViewModel.isCheckTimeOut.set(shared.getTimeOutState())
        mainViewModel.isSoundAlert = shared.getAlarmSound()


        this.viewModel = ViewModelProvider(
            this,
            ViewModelFactory(application)
        ).get(PageViewModel::class.java)


        setFragment()
        sendAlert()
        popUpAlertSend()

//        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler())
        dao = Room.databaseBuilder(
            application,
            AlertDatabase::class.java,
            "alertLogs"
        ).build().alertDAO()

        mBinding.btnHomepage.setOnClickListener {
            val intentr = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.saminsci.com/"))
            startActivity(intentr)
        }

        val restartIntent = Intent(applicationContext, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        callMainJob()

        Thread.setDefaultUncaughtExceptionHandler { _, ex ->
            try {
                ex.printStackTrace()

                CoroutineScope(Dispatchers.IO).launch {
                    val date = Date(System.currentTimeMillis())
                    val latesttime: String = dateformat.format(date)
                    val data = AlertData(
                        latesttime,
                        0,
                        0,
                        "${ex.stackTrace}\n${ex.message.toString()}",
                        0,
                        true
                    )

                    dao.insertData(data)
                    applicationContext.startActivity(restartIntent)

                    android.os.Process.killProcess(android.os.Process.myPid())
                    System.exit(10)
                }
            } catch (e: Exception) {
                // If we couldn't write the crash report, there's not much we can do.
                e.printStackTrace()
            }

        }


//        val handler = Handler(Looper.getMainLooper())
//        handler.postDelayed({
//            throw RuntimeException("이거 잡히나?")
//        }, 1000 * 10)

//        executorService.scheduleAtFixedRate({
//            if (isReStartApp.get()) {
//                if (restartCount.getAndAdd(1) > 5) {
//                    Log.d("Watchdog","start App =========================================================<<<<<<<<<<<<<<<");
//
////                    val intent = Intent(applicationContext, AppStartReceiver::class.java)
////                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, intent,
////                        PendingIntent.FLAG_IMMUTABLE)
////                    pendingIntent.send()
//                    finishAndRemoveTask()
//                    val restartIntent = applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
//                    restartIntent?.let {
//                        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                        applicationContext.startActivity(it)
//                    }
//
//                    executorService.shutdown()
//                }
//            }
//        }, 0, 1, TimeUnit.SECONDS)
//        Thread.sleep(2000000)
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

/*
        isrunthUIError.set(false)
        thUIError.interrupt()
        thUIError.join()*/


        isrunthUIError.set(false)
        updateErrorJob?.cancel()

    }

    override fun onStop() {
        super.onStop()
//        unbindMessengerService()
    }

    override fun onDestroy() {
        discallFeedback()
        // alertTask
        tabletSoundAlertOff()
        // callTimeoutThread
        discallTimemout()

        disCallMainJob()

/*
        isrunthUIError.set(true)
        thUIError.join()
*/
        isrunthUIError.set(false)
        updateErrorJob?.cancel()

        //alertThread
        isrunthAlert.set(false)
//        alertThread?.join()
//        alertJob?.cancel()

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
//        resetscreenFragment = ResetFragment()
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

//    var callbackThread: Thread? = null

//    var callTimeoutThread: Thread? = null
//    var isSending = false
    var isSending = AtomicBoolean(false)
    val isAnotherJob = AtomicBoolean(false)
    val isAnotherSettingJob = AtomicBoolean(false)

    fun feedBackThreadInterrupt() {
//        isSending = false
        isSending.set(false)
//        callbackThread?.interrupt()
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
//        callTimeoutJob?.cancel()
    }

    private fun triggerException() {
        val result = 2 / 0 // 이 코드는 ArithmeticException을 발생시킵니다.
    }
    fun callTimemout() {
//        callTimeoutJob?.cancel()

        isCallTimeout.set(true)
        /*if (mainViewModel.isCheckTimeOut.get()) {
            callTimeoutJob = CoroutineScope(Dispatchers.Default).launch {
                try {
                    while (isCallTimeout.get()) {
                        try {
                            tmp.timeoutAQCheckStep()

                            val usbdetach = mainViewModel.usbdetachetime.get()
                            if (usbdetach != 0L) {
                                if ((usbdetach + 1000L * 30) < System.currentTimeMillis()) {
                                    Log.d(
                                        "usbdetachetime",
                                        "=================== Time : ${mainViewModel.usbdetachetime.get()} current : ${System.currentTimeMillis()}"
                                    )

                                    val intent =
                                        Intent(applicationContext, AppRestartReceiver::class.java)
                                    val pendingIntent = PendingIntent.getBroadcast(
                                        applicationContext, 0, intent,
                                        PendingIntent.FLAG_IMMUTABLE
                                    )
                                    pendingIntent.send()
                                    break;
                                }
                            }

                            delay(50)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            throw e
                        }
                    }
                } catch(ex: CancellationException) {
                    ex.printStackTrace()
                }
            }

            Log.d("MainAct", "callTimeoutJob id : ${callTimeoutJob}")
        }*/
    }

    private fun discallFeedback() {
        isSending.set(false)
        isAnotherSettingJob.set(false)
        isAnotherJob.set(false)
//        callbackThread?.interrupt()
    }

    fun callFeedback() {
        isSending.set(false)
        isAnotherSettingJob.set(false)
        isAnotherJob.set(false)
//        callbackThread?.interrupt()

        isSending.set(true)
        isAnotherSettingJob.set(false)
        isAnotherJob.set(false)

        /*callbackThread = Thread {
            protocolBuffers.clear()
            val protocol = SaminProtocol()
            while (isSending.get()) {
                try {
                    while (mainViewModel.controlData.isMirrorMode) {
                        Thread.sleep(10)
                    }

                    while (isAnotherJob.get()) {
                        Thread.sleep(10)
                    }

                    while (isAnotherSettingJob.get()) {
                        Thread.sleep(10)
                    }

                    mainViewModel.setCurrnetDate(LocalDateTime.now())
                    val processMils = measureTimeMillis {
                        for ((md, ids) in mainViewModel.modelMapInt) {
                            for (index in ids.indices) {
                                while (isAnotherJob.get()) {
                                    Thread.sleep(10)
                                }

                                while (isAnotherSettingJob.get()) {
                                    Thread.sleep(10)
                                }

                                val model = md.toByte()
                                val id = ids.get(index)
                                val key =
                                    littleEndianConversion(byteArrayOf(model, id)).toShort()

                                if (!protocolBuffers.containsKey(key)) {
                                    protocol.feedBack(model, id)
                                    protocolBuffers[key] = protocol.mProtocol.clone()
                                }
                                protocolBuffers[key]?.let {
                                    sendFeedbackProtocolToSerial(it)
                                }
                                Thread.sleep(FEEDBACK_SLEEP.get())
                            }
                        }
                    }
                    val sleeptime = 333 - processMils
                    if (sleeptime < 333 && sleeptime > 0) {
                        Thread.sleep(sleeptime)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        callbackThread?.start()*/
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

//    var alertThread: Thread? = null
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

    fun sendFeedbackProtocolToSerial(data: ByteArray) {
        if (!mainViewModel.controlData.isMirrorMode) {
            val msg = Message.obtain(null, SerialService.MSG_SERIAL_FEEDBACK_SEND)
            val bundle = Bundle()
            bundle.putByteArray("", data)
            msg.data = bundle
            serialSVCIPCService?.send(msg)
        }
    }

    private var isrunthAlert = AtomicBoolean(true)
    private fun sendAlert() {
        isrunthAlert.set(false)
        isrunthAlert.set(true)
    }


/*    lateinit var thUIError: Thread
    private var isrunthUIError = AtomicBoolean(true)*/
    private fun uiError() {
        updateErrorJob?.cancel()

        isrunthUIError.set(true)
        updateErrorJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                while (isrunthUIError.get()) {
                    try {
                        // 메인화면 경고 유무 변화
                        val targets = java.util.HashMap<Int, Int>()
                        for (t in mainViewModel.alertMap.values) {
                            if (t.isAlert && !targets.containsKey(t.model)) {
                                targets[t.model] = t.model
                            }
                        }

                        mainViewModel.gasStorageAlert.value = targets.containsKey(1)
                        mainViewModel.gasRoomAlert.value = targets.containsKey(2)
                        mainViewModel.wasteAlert.value = targets.containsKey(3)
                        mainViewModel.oxyenAlert.value = targets.containsKey(4)
                        mainViewModel.steamerAlert.value = targets.containsKey(5)
                        mainViewModel.tempHumAlert.value = targets.containsKey(6)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        throw ex
                    }

                    delay(100)
                }
            }
            catch(e: CancellationException) {
                e.printStackTrace()
            }
        }

    Log.d("MainAct", "updateErrorJob id : ${updateErrorJob}")
        /*
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
         */

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
//            e.printStackTrace()
//            finishAndRemoveTask()
//            Process.killProcess(Process.myPid())
//            exitProcess(10)
            try {
                val date = Date(System.currentTimeMillis())
                val latesttime: String = dateformat.format(date)
                mainViewModel.usbdetachetime.set(System.currentTimeMillis())
//                mainViewModel.addAlertInfo(
//                    0,
//                    SetAlertData(
//                        latesttime,
//                        0,
//                        0,
//                        e.message.toString(),
//                        0,
//                        true
//                    )
//                )
                addLogs(
                    latesttime,
                    0,
                    0,
                    e.message.toString(),
                    0,
                    true
                )

            } catch (ex: Exception) {}
            Thread.sleep(1000)
            val intent = Intent(applicationContext, AppRestartReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, intent,
                PendingIntent.FLAG_IMMUTABLE)
            pendingIntent.send()
        }
    }

    private fun getBase64Decode(arg: ByteArray): ByteArray {
        return Base64.decode(arg, 0)
    }
    val dateformat: SimpleDateFormat =
        SimpleDateFormat("yyyy-MM-dd kk:mm:ss.SSS", Locale("ko", "KR"))
    private val serialSVCIPCHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
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
                    shared.setNoSerialCount(0)
                    mainViewModel.scanDone.value = true
                    mainViewModel.usbdetachetime.set(0)
                    val date = Date(System.currentTimeMillis())
                    val latesttime: String = dateformat.format(date)
                    mainViewModel.addAlertInfo(
                        0,
                        SetAlertData(
                            latesttime,
                            0,
                            0,
                            "시리얼 통신 연결이 되었습니다.",
                            0,
                            false
                        )
                    )
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
                    Log.d(mainTAG, "MSG_SERIAL_DISCONNECT ")
                    val date = Date(System.currentTimeMillis())
                    val latesttime: String = dateformat.format(date)
                    mainViewModel.usbdetachetime.set(System.currentTimeMillis())
                    mainViewModel.addAlertInfo(
                        0,
                        SetAlertData(
                            latesttime,
                            0,
                            0,
                            "시리얼 통신 연결이 끊겼습니다.",
                            0,
                            true
                        )
                    )

//                    addLogs(
//                        latesttime,
//                        0,
//                        0,
//                        "시리얼 통신 연결이 끊겼습니다.",
//                        0,
//                        true
//                    )
                }
                SerialService.MSG_NO_SERIAL -> {
                    val noserialcount = shared.getNoSerialCount()
                    shared.setNoSerialCount(min(noserialcount + 1, 10))

                    if (noserialcount > 5) {
                        Toast.makeText(
                            applicationContext,
                            "connection failed: 전원 스위치를 껏다 켜 주세요.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else {
                        Toast.makeText(
                            applicationContext,
                            "connection failed: device not found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    if (shared.getNoSerialCount() == 1) {
                        val date = Date(System.currentTimeMillis())
                        val latesttime: String = dateformat.format(date)
                        addLogs(
                            latesttime,
                            0,
                            0,
                            "연결할 수 있는 시리얼 통신이 없습니다.\n전원 스위치를 껏다켜면 문제가 해결될 수 있습니다.(USBHUB)",
                            0,
                            true
                        )
                    }

//                    CoroutineScope(Dispatchers.IO).launch {
//                        val date = Date(System.currentTimeMillis())
//                        val latesttime: String = dateformat.format(date)
//                        val data = AlertData(
//                            latesttime,
//                            0,
//                            0,
//                            "연결할 수 있는 시리얼 통신이 없습니다.\n전원 스위치를 껏다켜면 문제가 해결될 수 있습니다.(USBHUB)",
//                            0,
//                            true
//                        )
//
//                        dao.insertData(data)
//                    }
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
                    if (!mainViewModel.controlData.isMirrorMode)
                        return

                    val buff = msg.data.getByteArray("")
//                    Log.d(mainTAG,"===================================================================================")
//                    Log.d(mainTAG, "datahandler : \n${HexDump.dumpHexString(buff)}")
                    if (!isSharingSetting.get()) {
                        isSharingSetting.set(true)
                        val progress_Dialog = ProgressDialog(this@MainActivity)
                        progress_Dialog.setTitle("설정 복제 중") //팝업창 타이틀 지정
                        progress_Dialog.setIcon(R.mipmap.samin_launcher_ic) //팝업창 아이콘 지정
                        progress_Dialog.setMessage(
                            "잠시만 기다려주세요 ...\n" +
                                "진행 중입니다 ...\n" +
                            "1분 이상 팝업이 유지되면 설정 복제를 다시 하시기 바랍니다."
                        ) //팝업창 내용 지정
                        progress_Dialog.setCancelable(false) //외부 레이아웃 클릭시도 팝업창이 사라지지않게 설정
                        progress_Dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER) //프로그레스 원형 표시 설정
                        progress_Dialog.setButton(
                            /* whichButton = */ DialogInterface.BUTTON_POSITIVE,
                            /* text = */ "취소",
                        ) { _, _ ->
                            try {
                                progress_Dialog.dismiss()
                                progress_Dialog.cancel()
                                isSharingSetting.set(false)
                            } catch (e: Exception) {
                            }
                        }

                        try {
                            progress_Dialog.show()
                        } catch (progesse: Exception) {
                            progesse.printStackTrace()
                        }
                    }
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
//                                tmpgas = tmpgas.plus(255.toByte())
                                for (t in sortGas.values) {
                                    tmpgas = tmpgas.plus(t.sliceArray(8 until t.size))
                                }

                                Log.d(mainTAG, "tmpgas : ${HexDump.dumpHexString(tmpgas)}")
                                try {
                                    tmpgas = getBase64Decode(tmpgas)
                                } catch (ee: Exception) {
                                    tmpgas = ByteArray(0)
                                    allDone = false
                                }
                                Log.d(mainTAG, "tmpgas : ${HexDump.dumpHexString(tmpgas)}")
                                Log.d(mainTAG, "설정 데이터 전송 완료 ================ 1")

                                try {
                                    @OptIn(ExperimentalSerializationApi::class)
                                    val objgas =
                                        ProtoBuf.decodeFromByteArray<List<SetGasStorageViewData>>(
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
                                    shared.saveBoardSetData(
                                        SaminSharedPreference.GASSTORAGE,
                                        tmpbuff
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    allDone = false
                                }
                                Log.d(mainTAG, "설정 데이터 전송 완료 ================ 2")
                                // 가스룸 설정 복원
                                var tmpgasroom = ByteArray(0)
                                val sortGasroom = sortMapByKey(recvGasRoomBuffers)
                                for (t in sortGasroom.values) {
                                    tmpgasroom = tmpgasroom.plus(t.sliceArray(8 until t.size))
                                }
                                Log.d(mainTAG, "tmpgasroom : ${HexDump.dumpHexString(tmpgasroom)}")
                                try {
                                    tmpgasroom = getBase64Decode(tmpgasroom)
                                } catch (ee: Exception) {
                                    tmpgasroom = ByteArray(0)
                                    allDone = false
                                }
                                Log.d(mainTAG, "tmpgasroom : ${HexDump.dumpHexString(tmpgasroom)}")
                                try {
                                    @OptIn(ExperimentalSerializationApi::class)
                                    val objgas =
                                        ProtoBuf.decodeFromByteArray<List<SetGasRoomViewData>>(
                                            tmpgasroom
                                        )
                                    mainViewModel.GasRoomDataLiveList.clear(true)
                                    for (t in objgas) {
                                        t.pressure = 0f
                                        t.isAlert = false
                                        t.heartbeatCount = 0u
                                        mainViewModel.GasRoomDataLiveList.add(t)
                                        Log.d(mainTAG, "${t.gasName} ${t.gasColor}")
                                    }
                                    val tmpbuff = mutableListOf<SetGasRoomViewData>()
                                    for (i in mainViewModel.GasRoomDataLiveList.value!!)
                                    {
                                        Log.d(mainTAG, "${i.gasName} ${i.gasColor}")
                                        tmpbuff.add(i)
                                    }
                                    shared.saveBoardSetData(
                                        SaminSharedPreference.GASROOM,
                                        tmpbuff
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    allDone = false
                                }
                                Log.d(mainTAG, "설정 데이터 전송 완료 ================ 3")
                                // 폐액통 설정 복원
                                var tmpwaste = ByteArray(0)
                                val sortWaste = sortMapByKey(recvWasteBuffers)
                                for (t in sortWaste.values) {
                                    tmpwaste = tmpwaste.plus(t.sliceArray(8 until t.size))
                                }
                                Log.d(mainTAG, "tmpwaste : ${HexDump.dumpHexString(tmpwaste)}")
                                try {
                                    tmpwaste = getBase64Decode(tmpwaste)
                                } catch (ee: Exception) {
                                    tmpwaste = ByteArray(0)
                                    allDone = false
                                }
                                Log.d(mainTAG, "tmpwaste : ${HexDump.dumpHexString(tmpwaste)}")
                                try {
                                    @OptIn(ExperimentalSerializationApi::class)
                                    val objgas =
                                        ProtoBuf.decodeFromByteArray<List<SetWasteLiquorViewData>>(
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
                                    shared.saveBoardSetData(
                                        SaminSharedPreference.WASTELIQUOR,
                                        tmpbuff
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    allDone = false
                                }
                                Log.d(mainTAG, "설정 데이터 전송 완료 ================ 4")
                                // 산소농도 설정 복원
                                var tmpOxygen = ByteArray(0)
                                val sortOxygen = sortMapByKey(recvOxygenBuffers)
                                for (t in sortOxygen.values) {
                                    tmpOxygen = tmpOxygen.plus(t.sliceArray(8 until t.size))
                                }
                                Log.d(mainTAG, "tmpOxygen : ${HexDump.dumpHexString(tmpOxygen)}")
                                try {
                                    tmpOxygen = getBase64Decode(tmpOxygen)
                                } catch (ee: Exception) {
                                    tmpOxygen = ByteArray(0)
                                    allDone = false
                                }
                                Log.d(mainTAG, "tmpOxygen : ${HexDump.dumpHexString(tmpOxygen)}")
                                try {
                                    @OptIn(ExperimentalSerializationApi::class)
                                    val objgas =
                                        ProtoBuf.decodeFromByteArray<List<SetOxygenViewData>>(
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
                                    shared.saveBoardSetData(
                                        SaminSharedPreference.OXYGEN,
                                        tmpbuff
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    allDone = false
                                }
                                Log.d(mainTAG, "설정 데이터 전송 완료 ================ 5")
                                // 스팀기 설정 복원
                                var tmpSteamer = ByteArray(0)
                                val sortSteamer = sortMapByKey(recvSteamerBuffers)
                                for (t in sortSteamer.values) {
                                    tmpSteamer = tmpSteamer.plus(t.sliceArray(8 until t.size))
                                }
                                Log.d(mainTAG, "tmpSteamer : ${HexDump.dumpHexString(tmpSteamer)}")
                                try {
                                    tmpSteamer = getBase64Decode(tmpSteamer)
                                } catch (ee: Exception) {
                                    tmpSteamer = ByteArray(0)
                                    allDone = false
                                }
                                Log.d(mainTAG, "tmpSteamer : ${HexDump.dumpHexString(tmpSteamer)}")
                                try {
                                    @OptIn(ExperimentalSerializationApi::class)
                                    val objgas =
                                        ProtoBuf.decodeFromByteArray<List<SetSteamerViewData>>(
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
                                    shared.saveBoardSetData(
                                        SaminSharedPreference.STEAMER,
                                        tmpbuff
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    allDone = false
                                }
                                Log.d(mainTAG, "설정 데이터 전송 완료 ================ 6")
                                // 온습도 설정 복원
                                var tmpTempHum = ByteArray(0)
                                val sortTempHum = sortMapByKey(recvTempHumBuffers)
                                for (t in sortTempHum.values) {
                                    tmpTempHum = tmpTempHum.plus(t.sliceArray(8 until t.size))
                                }
//                                tmpTempHum = Base64.getDecoder().decode(tmpTempHum)
                                Log.d(mainTAG, "tmpSteamer : ${HexDump.dumpHexString(tmpSteamer)}")
                                try {
                                    tmpTempHum = getBase64Decode(tmpTempHum)
                                } catch (ee: Exception) {
                                    tmpTempHum = ByteArray(0)
                                    allDone = false
                                }
                                Log.d(mainTAG, "tmpSteamer : ${HexDump.dumpHexString(tmpSteamer)}")
                                try {
                                    @OptIn(ExperimentalSerializationApi::class)
                                    val objgas =
                                        ProtoBuf.decodeFromByteArray<List<SetTempHumViewData>>(
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
                                    shared.saveBoardSetData(
                                        SaminSharedPreference.TEMPHUM,
                                        tmpbuff
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    allDone = false
                                }

                                Log.d(mainTAG, "설정 데이터 전송 완료 ================ 7")
                                var tmpModemap = ByteArray(0)
                                val sortModemap = sortMapByKey(recvModemapBuffers)
                                for (t in sortModemap.values) {
                                    tmpModemap = tmpModemap.plus(t.sliceArray(8 until t.size))
                                }
                                Log.d(mainTAG, "tmpModemap : ${HexDump.dumpHexString(tmpModemap)}")
                                try {
                                    tmpModemap = getBase64Decode(tmpModemap)
                                } catch (ee: Exception) {
                                    tmpModemap = ByteArray(0)
                                    allDone = false
                                }
                                Log.d(mainTAG, "tmpModemap : ${HexDump.dumpHexString(tmpModemap)}")
//                                mainViewModel.modelMap.clear()
                                val modelMap = HashMap<String, ByteArray>()
                                try {
                                    @OptIn(ExperimentalSerializationApi::class)
                                    val objgas =
                                        ProtoBuf.decodeFromByteArray<HashMap<String, ByteArray>>(
                                            tmpModemap
                                        )
                                    for (t in objgas) {
                                        modelMap[t.key] = t.value
                                        val id = when (t.key) {
                                            "GasDock" -> 1
                                            "GasRoom" -> 2
                                            "WasteLiquor" -> 3
                                            "Oxygen" -> 4
                                            "Steamer" -> 5
                                            "TempHum" -> 6
                                            else -> 1
                                        }
//                                        mainViewModel.modelMapInt[id] = t.value.clone()
                                    }

                                    shared.saveHashMap(modelMap)
                                    val tmp = shared.loadHashMap()
                                    Log.d(mainTAG, "설정 데이터 전송 완료 ================ loadHashMap ${tmp.size}")
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    allDone = false
                                }

                                Log.d(mainTAG, "설정 데이터 전송 완료 ================ 8")
                                var tmpLabname = ByteArray(0)
                                val sortLabname = sortMapByKey(recvLabNameBuffers)
                                for (t in sortLabname.values) {
                                    tmpLabname = tmpLabname.plus(t.sliceArray(8 until t.size))
                                }
//                                tmpLabname = Base64.getDecoder().decode(tmpLabname)
                                try {
                                    tmpLabname = getBase64Decode(tmpLabname)
                                } catch (ee: Exception) {
                                    tmpLabname = ByteArray(0)
                                    allDone = false
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
                                Log.d(mainTAG, "설정 데이터 전송 완료 ================ 9")
//                                tmp.LoadSetting()
//                                discallFeedback()
//                                discallTimemout()

//                                tmp.LoadSetting()
////                                tmp.hmapLastedDate.keys.forEach{
////                                    mainViewModel.hasKey.put(it, it)
////                                }
//                                for (tmp in tmp.hmapLastedDate.keys) {
//                                    mainViewModel.hasKey.put(tmp, tmp)
//                                }

//                                CoroutineScope(Dispatchers.IO).launch {
//                                    Log.d(mainTAG, "설정 데이터 전송 완료 ================ 코루틴 실행")
//                                    val intent = Intent(applicationContext, AppRestartReceiver::class.java)
//                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, intent,
//                                        PendingIntent.FLAG_IMMUTABLE)
//                                    pendingIntent.send()
//                                }

//                                val intent =
//                                    Intent(applicationContext, AppRestartReceiver::class.java)
//                                val pendingIntent = PendingIntent.getBroadcast(
//                                    applicationContext, 0, intent,
//                                    PendingIntent.FLAG_IMMUTABLE
//                                )
//                                pendingIntent.send()
//
//                                android.os.Process.killProcess(android.os.Process.myPid())
//                                System.exit(10)

                                if (allDone) {
                                    val restartIntent =
                                        Intent(applicationContext, MainActivity::class.java).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        }
                                    CoroutineScope(Dispatchers.IO).launch {
                                        applicationContext.startActivity(restartIntent)
                                        android.os.Process.killProcess(android.os.Process.myPid())
                                        System.exit(10)
                                    }
                                }
                                Log.d(mainTAG, "설정 데이터 전송 완료 ================ 종료")
                            }
                        }
                    }
                }
                SerialService.MSG_GASDOCK -> {
                    val bundle = msg.data
                    bundle.classLoader = ParsingData::class.java.classLoader
                    val (id, model, time, datas) = msg.data.getParcelable<ParsingData>("")!!
//                    val (id, model, time, datas) = msg.obj as SerialDataInfo
//                    val (id, model, time, datas) = msg.data.getSerializable("") as ParsingData
//                    val (id, model, time, datas) = ProtoBuf.decodeFromByteArray<ParsingData>(msg.data.getByteArray("") as ByteArray)
                    tmp.ParserGas(id, datas, time)
                    mainViewModel.setCurrnetDate(LocalDateTime.now())
                }
                SerialService.MSG_GASROOM -> {
                    val bundle = msg.data
                    bundle.classLoader = ParsingData::class.java.classLoader
                    val (id, model, time, datas) = msg.data.getParcelable<ParsingData>("")!!
//                    val (id, model, time, datas) = msg.data.getSerializable("") as ParsingData
//                    val (id, model, time, datas) = ProtoBuf.decodeFromByteArray<ParsingData>(msg.data.getByteArray("") as ByteArray)
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
                    mainViewModel.setCurrnetDate(LocalDateTime.now())
                }
                SerialService.MSG_WASTE -> {
                    val bundle = msg.data
                    bundle.classLoader = ParsingData::class.java.classLoader
                    val (id, model, time, datas) = msg.data.getParcelable<ParsingData>("")!!
//                    val (id, model, time, datas) = msg.data.getSerializable("") as ParsingData
//                    val (id, model, time, datas) = ProtoBuf.decodeFromByteArray<ParsingData>(msg.data.getByteArray("") as ByteArray)
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
                    mainViewModel.setCurrnetDate(LocalDateTime.now())

                }
                SerialService.MSG_OXYGEN -> {
                    val bundle = msg.data
                    bundle.classLoader = ParsingData::class.java.classLoader
                    val (id, model, time, datas) = msg.data.getParcelable<ParsingData>("")!!
//                    val (id, model, time, datas) = msg.data.getSerializable("") as ParsingData
//                    val (id, model, time, datas) = ProtoBuf.decodeFromByteArray<ParsingData>(msg.data.getByteArray("") as ByteArray)
                    val port = 1.toByte()
                    val key = littleEndianConversion(byteArrayOf(model, id, port))
                    tmp.hmapLastedDate[key] = time
                    tmp.ProcessOxygen(key, datas[0])
                    mainViewModel.setCurrnetDate(LocalDateTime.now())
                }
                SerialService.MSG_STEMER -> {
                    val bundle = msg.data
                    bundle.classLoader = ParsingData::class.java.classLoader
                    val (id, model, time, datas) = msg.data.getParcelable<ParsingData>("")!!
//                    val (id, model, time, datas) = msg.data.getSerializable("") as ParsingData
//                    val (id, model, time, datas) = ProtoBuf.decodeFromByteArray<ParsingData>(msg.data.getByteArray("") as ByteArray)
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
                    mainViewModel.setCurrnetDate(LocalDateTime.now())
                }
                SerialService.MSG_TEMPHUM -> {
                    val bundle = msg.data
                    bundle.classLoader = ParsingData::class.java.classLoader
                    val (id, model, time, datas) = msg.data.getParcelable<ParsingData>("")!!
//                    val (id, model, time, datas) = msg.data.getSerializable("") as ParsingData
//                    val (id, model, time, datas) = ProtoBuf.decodeFromByteArray<ParsingData>(msg.data.getByteArray("") as ByteArray)
                    val port = 1.toByte()
                    val key = littleEndianConversion(byteArrayOf(model, id, port))
                    tmp.hmapLastedDate[key] = time
                    val hum = String.format("%.1f", (datas[0].toFloat() / 1000000f)).toFloat()
                    val temp = String.format("%.1f", (datas[1].toFloat() / 1000000f)).toFloat()
                    tmp.ProcessTempHum(key, temp, hum)
                    mainViewModel.setCurrnetDate(LocalDateTime.now())
                }
                SerialService.MSG_ERROR -> {

//                    Log.d("MSG_ERROR", " MSG_ERROR ================================================================================")
                    CoroutineScope(Dispatchers.IO).launch {
                        val date = Date(System.currentTimeMillis())
                        val latesttime: String = dateformat.format(date)
                        val data = AlertData(
                            latesttime,
                            0,
                            0,
                            "시리얼통신 서비스에서 예외처리되지 않은 에러 발생",
                            0,
                            true
                        )

                        dao.insertData(data)
                        val intent = Intent(applicationContext, AppRestartReceiver::class.java)
                        val pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, intent,
                            PendingIntent.FLAG_IMMUTABLE)
                        pendingIntent.send()
                    }

                }

                else -> {
//                    super.handleMessage(msg)
                    Log.d("MSG_ERROR", " MSG_ERROR ================================================================================")
                }
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

//    private var popUpThread: Thread? = null
//    var isPopUp = false
    val isPopup = AtomicBoolean(true)
    private var updatePopupJob: Job? = null
    fun popUpAlertSend() {
        updatePopupJob?.cancel()

        isPopup.set(true)
        updatePopupJob = CoroutineScope(Dispatchers.Main).launch {
            try {
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
                                } else {
                                    for (i in alertRemovelist) {
                                        tmp.remove(i)
                                    }
                                    alertRemovelist.clear()
                                }
                                tmp.addAll(alertList)
                                alertList.clear()
                            } catch (e: Exception) {
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
                                            if (setFragment != MainViewModel.GASROOMMAINFRAGMENT && setFragment != MainViewModel.GASROOMLEAKTESTFRAGMENT) {
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
                    } catch (e: Exception) {
                        e.printStackTrace()
                        throw e
                    }

                    delay(200)
                }
            }
            catch(ex: CancellationException) {
                ex.printStackTrace()
            }
        }
    }

    fun popUpThreadInterrupt() {
        isPopup.set(false)
        updatePopupJob?.cancel()
    }


}


