package com.coai.samin_total

import android.annotation.SuppressLint
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.nfc.Tag
import android.os.*
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.coai.libmodbus.service.SaminModbusService
import com.coai.samin_total.Dialog.AlertDialogFragment
import com.coai.samin_total.Dialog.ScanDialogFragment
import com.coai.samin_total.Dialog.SetAlertData
import com.coai.samin_total.GasDock.GasDockMainFragment
import com.coai.samin_total.GasDock.GasStorageSettingFragment
import com.coai.samin_total.GasDock.SetGasStorageViewData
import com.coai.samin_total.GasRoom.GasRoomMainFragment
import com.coai.samin_total.GasRoom.GasRoomSettingFragment
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
import com.coai.samin_total.WasteLiquor.SetWasteLiquorViewData
import com.coai.samin_total.WasteLiquor.WasteLiquorMainFragment
import com.coai.samin_total.WasteLiquor.WasteWaterSettingFragment
import com.coai.samin_total.database.*
import com.coai.samin_total.databinding.ActivityMainBinding
import com.coai.uikit.GlobalUiTimer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
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

    lateinit var viewModel: PageViewModel
    private lateinit var pageListAdapter: PageListAdapter

    private var protocolBuffers = ConcurrentHashMap<Short, ByteArray>()

    // 설정 수신 버퍼
    private val recvGasStorageBuffers = HashMap<Int, ByteArray>()
    private val recvGasRoomBuffers = HashMap<Int, ByteArray>()
    private val recvWasteBuffers = HashMap<Int, ByteArray>()
    private val recvOxygenBuffers = HashMap<Int, ByteArray>()
    private val recvSteamerBuffers = HashMap<Int, ByteArray>()
    private val recvOxygenMSTBuffers = HashMap<Int, ByteArray>()
    private val recvModemapBuffers = HashMap<Int, ByteArray>()

    // 보드별 최종 전송시간
    private val alertsendLastTime = HashMap<Int, Long>()

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
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler())
        dao = Room.databaseBuilder(
            application,
            AlertDatabase::class.java,
            "alertLogs"
        ).build().alertDAO()
    }

//    fun bindSerialService() {
////        if (!UsbSerialService.SERVICE_CONNECTED){
////            val startSerialService = Intent(this, UsbSerialService::class.java)
////            startService(startSerialService)
////
////        }
//        val usbSerialServiceIntent = Intent(this, SerialService::class.java)
////        val usbSerialServiceIntent = Intent(this, UsbSerialService::class.java)
//        bindService(usbSerialServiceIntent, serialServiceConnection, Context.BIND_AUTO_CREATE)
//    }

//    var serialService: SerialService? = null

    //    var usbSerialService: UsbSerialService? = null
//    var isSerialSevice = false

//    @ExperimentalUnsignedTypes
//    val serialServiceConnection = object : ServiceConnection {
//        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//            val binder = service as SerialService.SerialServiceBinder
//            serialService = binder.getService()
////            val binder = service as UsbSerialService.UsbSerialServiceBinder
////            usbSerialService = binder.getService()
//
//            //핸들러 연결
//            serialService!!.setHandler(datahandler)
//            isSerialSevice = true
//
////            sendSettingValues()
//        }
//
//        override fun onServiceDisconnected(name: ComponentName?) {
//            isSerialSevice = false
//            Toast.makeText(this@MainActivity, "서비스 연결 해제", Toast.LENGTH_SHORT).show()
//        }
//    }

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



    private fun sortMapByKey(map: Map<Int, ByteArray>): LinkedHashMap<Int, ByteArray> {
        val entries = LinkedList(map.entries)

        entries.sortBy { it.key }

        val result = LinkedHashMap<Int, ByteArray>()
        for (entry in entries) {
            result[entry.key] = entry.value
        }

        return result
    }

    var countSettingRecive = 0
    @ExperimentalUnsignedTypes
    val datahandler = object : Handler(Looper.getMainLooper()) {
        @SuppressLint("SimpleDateFormat")
        @RequiresApi(Build.VERSION_CODES.O)
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                1 -> {
                    try {
        //                Log.d(mainTAG, "datahandler : \n${HexDump.dumpHexString(msg.obj as ByteArray)}")
                        val receiveParser = SaminProtocol()
                        receiveParser.parse(msg.obj as ByteArray)

        //            Log.d(mainTAG, "receiveParser.packetName : ${receiveParser.packetName}")
        //            if (receiveParser.packetName == "CheckProductPing") {
                        if (receiveParser.packet == SaminProtocolMode.CheckProductPing.byte) {
//                            val model = receiveParser.modelName
//                            val input_id = receiveParser.mProtocol.get(3)
//                            when (model) {
//                                "GasDock" -> {
//                                    val id = receiveParser.mProtocol.get(3)
//                                    gasdock_ids_list.add(id)
//                                    val ids = gasdock_ids_list.distinct().toByteArray()
//                                    mainViewModel.modelMap[model] = ids
//                                    mainViewModel.modelMapInt[1] = ids
//                                }
//                                "GasRoom" -> {
//                                    val id = receiveParser.mProtocol.get(3)
//                                    gasroom_ids_list.add(id)
//                                    val ids = gasroom_ids_list.distinct().toByteArray()
//                                    mainViewModel.modelMap[model] = ids
//                                    mainViewModel.modelMapInt[2] = ids
//                                }
//                                "WasteLiquor" -> {
//                                    val id = receiveParser.mProtocol.get(3)
//                                    wasteLiquor_ids_list.add(id)
//                                    val ids = wasteLiquor_ids_list.distinct().toByteArray()
//                                    mainViewModel.modelMap[model] = ids
//                                    mainViewModel.modelMapInt[3] = ids
//                                }
//                                "Oxygen" -> {
//                                    val id = receiveParser.mProtocol.get(3)
//                                    oxygen_ids_list.add(id)
//                                    val ids = oxygen_ids_list.distinct().toByteArray()
//                                    mainViewModel.modelMap[model] = ids
//                                    mainViewModel.modelMapInt[4] = ids
//                                }
//                                "Steamer" -> {
//                                    val id = receiveParser.mProtocol.get(3)
//                                    steamer_ids_list.add(id)
//                                    val ids = steamer_ids_list.distinct().toByteArray()
//                                    mainViewModel.modelMap[model] = ids
//                                    mainViewModel.modelMapInt[5] = ids
//                                }
//                                "Temp_Hum" -> {
//
//                                }
//                            }

                        } else if (receiveParser.packet == SaminProtocolMode.RequestFeedBackPing.byte) {

                            if (receiveParser.mProtocol.size >= 14) {
                                tmp.Parser(receiveParser.mProtocol)
                            }
                        } else if (receiveParser.packet == SaminProtocolMode.SettingShare.byte) {

//                            Log.d(mainTAG, "datahandler : \n${HexDump.dumpHexString(receiveParser.mProtocol)}")
//                            when(receiveParser.mProtocol.get(2)) {
//                                0x11.toByte() -> {
//                                    // 가스 독
//                                    recvGasStorageBuffers[receiveParser.mProtocol.get(7).toInt()] = receiveParser.mProtocol.clone()
//                                }
//                                0x12.toByte() -> {
//                                    // 가스 룸
//                                    recvGasRoomBuffers[receiveParser.mProtocol.get(7).toInt()] = receiveParser.mProtocol.clone()
//                                }
//                                0x13.toByte() -> {
//                                    // 폐액통
//                                    recvWasteBuffers[receiveParser.mProtocol.get(7).toInt()] = receiveParser.mProtocol.clone()
//                                }
//                                0x14.toByte() -> {
//                                    // 산소농도
//                                    recvOxygenBuffers[receiveParser.mProtocol.get(7).toInt()] = receiveParser.mProtocol.clone()
//                                }
//                                0x15.toByte() -> {
//                                    // 스팀기
//                                    recvSteamerBuffers[receiveParser.mProtocol.get(7).toInt()] = receiveParser.mProtocol.clone()
//                                }
//                                0x16.toByte() -> {
//                                    // 산소농도 대표
//                                    recvOxygenMSTBuffers[receiveParser.mProtocol.get(7).toInt()] = receiveParser.mProtocol.clone()
//                                }
//                                0x17.toByte() -> {
//                                    recvModemapBuffers[receiveParser.mProtocol.get(7).toInt()] = receiveParser.mProtocol.clone()
//                                }
//                                0x20.toByte() -> {
//                                    // 설정 데이터 전송 완료
//                                    Log.d(mainTAG, "설정 데이터 전송 완료 ================")
//
//                                    // 가스독 설정 복원
//                                    var tmpgas = ByteArray(0)
//                                    val sortGas = sortMapByKey(recvGasStorageBuffers)
//                                    for (t in sortGas.values){
//                                        tmpgas = tmpgas.plus(t.sliceArray(8..t.size-1))
//                                    }
////                            Log.d(mainTAG, "tmpgas : ${HexDump.dumpHexString(tmpgas)}")
//                                    try {
//                                        val objgas =
//                                            ProtoBuf.decodeFromByteArray<List<SetGasStorageViewData>>(tmpgas)
//                                        mainViewModel.GasStorageDataLiveList.clear(true)
//                                        for(t in objgas) {
//                                            t.pressureLeft = 0f
//                                            t.pressure = 0f
//                                            t.pressureRight = 0f
//                                            t.isAlert = false
//                                            t.isAlertLeft = false
//                                            t.isAlertRight = false
//                                            t.heartbeatCount = 0u
//                                            mainViewModel.GasStorageDataLiveList.add(t)
//                                        }
//                                        val buff = mutableListOf<SetGasStorageViewData>()
//                                        for (i in mainViewModel.GasStorageDataLiveList.value!!) {
//                                            buff.add(i)
//                                        }
//                                        shared.saveBoardSetData(SaminSharedPreference.GASSTORAGE, buff)
//
//                                    } catch(e : Exception) {
//                                        e.printStackTrace()
//                                    }
//
//                                    // 가스룸 설정 복원
//                                    var tmpgasroom = ByteArray(0)
//                                    val sortGasroom = sortMapByKey(recvGasRoomBuffers)
//                                    for (t in sortGasroom.values){
//                                        tmpgasroom = tmpgasroom.plus(t.sliceArray(8..t.size-1))
//                                    }
////                            Log.d(mainTAG, "tmpgasroom : ${HexDump.dumpHexString(tmpgasroom)}")
//                                    try {
//                                        val objgas =
//                                            ProtoBuf.decodeFromByteArray<List<SetGasRoomViewData>>(tmpgasroom)
//                                        mainViewModel.GasRoomDataLiveList.clear(true)
//                                        for(t in objgas) {
//                                            t.pressure = 0f
//                                            t.isAlert = false
//                                            t.heartbeatCount = 0u
//                                            mainViewModel.GasRoomDataLiveList.add(t)
//                                        }
//                                        val buff = mutableListOf<SetGasRoomViewData>()
//                                        for (i in mainViewModel.GasRoomDataLiveList.value!!) {
//                                            buff.add(i)
//                                        }
//                                        shared.saveBoardSetData(SaminSharedPreference.GASROOM, buff)
//                                    } catch(e : Exception) {
//                                        e.printStackTrace()
//                                    }
//
//                                    // 폐액통 설정 복원
//                                    var tmpwaste= ByteArray(0)
//                                    val sortWaste = sortMapByKey(recvWasteBuffers)
//                                    for (t in sortWaste.values){
//                                        tmpwaste = tmpwaste.plus(t.sliceArray(8..t.size-1))
//                                    }
//                                    Log.d(mainTAG, "tmpwaste : ${HexDump.dumpHexString(tmpwaste)}")
//                                    try {
//                                        val objgas =
//                                            ProtoBuf.decodeFromByteArray<List<SetWasteLiquorViewData>>(tmpwaste)
//                                        mainViewModel.WasteLiquorDataLiveList.clear(true)
//                                        for(t in objgas) {
//                                            t.isAlert = false
//                                            t.heartbeatCount = 0u
//                                            mainViewModel.WasteLiquorDataLiveList.add(t)
//                                        }
//                                        val buff = mutableListOf<SetWasteLiquorViewData>()
//                                        for (i in mainViewModel.WasteLiquorDataLiveList.value!!) {
//                                            buff.add(i)
//                                        }
//                                        shared.saveBoardSetData(SaminSharedPreference.WASTELIQUOR, buff)
//                                    } catch(e : Exception) {
//                                        e.printStackTrace()
//                                    }
//
//                                    // 산소농도 설정 복원
//                                    var tmpOxygen= ByteArray(0)
//                                    val sortOxygen = sortMapByKey(recvOxygenBuffers)
//                                    for (t in sortOxygen.values){
//                                        tmpOxygen = tmpOxygen.plus(t.sliceArray(8..t.size-1))
//                                    }
////                            Log.d(mainTAG, "tmpOxygen : ${HexDump.dumpHexString(tmpOxygen)}")
//                                    try {
//                                        val objgas =
//                                            ProtoBuf.decodeFromByteArray<List<SetOxygenViewData>>(tmpOxygen)
//                                        mainViewModel.OxygenDataLiveList.clear(true)
//                                        for(t in objgas) {
//                                            t.isAlert = false
//                                            t.setValue = 0f
//                                            t.heartbeatCount = 0u
//                                            mainViewModel.OxygenDataLiveList.add(t)
//                                        }
//                                        val buff = mutableListOf<SetOxygenViewData>()
//                                        for (i in mainViewModel.OxygenDataLiveList.value!!) {
//                                            buff.add(i)
//                                        }
//                                        shared.saveBoardSetData(SaminSharedPreference.OXYGEN, buff)
//                                    } catch(e : Exception) {
//                                        e.printStackTrace()
//                                    }
//
//                                    // 스팀기 설정 복원
//                                    var tmpSteamer = ByteArray(0)
//                                    var sortSteamer = sortMapByKey(recvSteamerBuffers)
//                                    for (t in sortSteamer.values){
//                                        tmpSteamer = tmpSteamer.plus(t.sliceArray(8..t.size-1))
//                                    }
////                            Log.d(mainTAG, "tmpSteamer : ${HexDump.dumpHexString(tmpSteamer)}")
//                                    try {
//                                        val objgas =
//                                            ProtoBuf.decodeFromByteArray<List<SetSteamerViewData>>(tmpSteamer)
//                                        mainViewModel.SteamerDataLiveList.clear(true)
//                                        for(t in objgas) {
//                                            t.isAlertLow = false
//                                            t.isAlertTemp = false
//                                            t.isTemp = 0
//                                            t.heartbeatCount = 0u
//                                            mainViewModel.SteamerDataLiveList.add(t)
//                                        }
//                                        val buff = mutableListOf<SetSteamerViewData>()
//                                        for (i in mainViewModel.SteamerDataLiveList.value!!) {
//                                            buff.add(i)
//                                        }
//                                        shared.saveBoardSetData(SaminSharedPreference.STEAMER, buff)
//                                    } catch(e : Exception) {
//                                        e.printStackTrace()
//                                    }
//
//                                    // 산소농도 대표 설정 복원
//                                    var tmpOxyMST = ByteArray(0)
//                                    var sortOxymst = sortMapByKey(recvOxygenMSTBuffers)
//                                    for (t in sortOxymst.values){
//                                        tmpOxyMST = tmpOxyMST.plus(t.sliceArray(8..t.size-1))
//                                    }
////                            Log.d(mainTAG, "tmpOxyMST : ${HexDump.dumpHexString(tmpOxyMST)}")
//                                    try {
//                                        val objgas =
//                                            ProtoBuf.decodeFromByteArray<SetOxygenViewData>(tmpOxyMST)
//                                        objgas.setValue = 0f
//                                        objgas.isAlert = false
//                                        objgas.heartbeatCount = 0u
//                                        mainViewModel.oxygenMasterData = objgas
//                                        shared.saveBoardSetData(SaminSharedPreference.MASTEROXYGEN, mainViewModel.oxygenMasterData!!)
//                                    } catch(e : Exception) {
//                                        e.printStackTrace()
//                                    }
//
//                                    var tmpModemap = ByteArray(0)
//                                    var sortModemap = sortMapByKey(recvModemapBuffers)
//                                    for (t in sortModemap.values){
//                                        tmpModemap = tmpModemap.plus(t.sliceArray(8..t.size-1))
//                                    }
//                                    mainViewModel.modelMap.clear()
//                                    try {
//                                        val objgas =
//                                            ProtoBuf.decodeFromByteArray<HashMap<String, ByteArray>>(tmpModemap)
//                                        for (t in objgas) {
//                                            mainViewModel.modelMap[t.key] = t.value
//                                            var id = when {
//                                                t.key.equals("GasDock") -> 1
//                                                t.key.equals("GasRoom") -> 2
//                                                t.key.equals("WasteLiquor") -> 3
//                                                t.key.equals("Oxygen") -> 4
//                                                t.key.equals("Steamer") -> 5
//                                                else -> 1
//                                            }
//
//                                            mainViewModel.modelMapInt[id] = t.value.clone()
//                                        }
//                                        shared.saveHashMap(mainViewModel.modelMap)
//                                    } catch(e : Exception) {
//                                        e.printStackTrace()
//                                    }
//
//                                    discallFeedback()
//                                    discallTimemout()
//
//                                    tmp.LoadSetting()
//                                    tmp.hmapLastedDate.keys.forEach{
//                                        mainViewModel.hasKey.put(it, it)
//                                    }
//
//                                    callFeedback()
//                                    callTimemout()
//                                    onFragmentChange(MainViewModel.MAINSETTINGFRAGMENT)
//                                }
//                            }
                        } else if (receiveParser.packet == SaminProtocolMode.CheckVersion.byte){
//                            val version = receiveParser.mProtocol.get(7).toInt()
//                            Toast.makeText(this@MainActivity, "펌웨어 버전 : $version",Toast.LENGTH_SHORT).show()
                        }
                    } catch (Ex: Exception) {
                        Ex.printStackTrace()
                    }
                }
                2 -> {
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
                else -> {

                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        bindMessengerService()
        if (mainViewModel.controlData.useModbusRTU)
            startModbusService(SaminModbusService::class.java, svcConnection, null)
    }

    override fun onResume() {
        hideNavigationBar()
//        GlobalUiTimer.getInstance().activity = this
//        if (mainViewModel.controlData.useSettingShare)
//        sendSettingValues()

        uiError()
        super.onResume()
    }


    private fun sendMultipartSend(model: Byte, data: ByteArray? = null) {
        val protocol = SaminProtocol()

        if (data != null) {
            val chunked = data!!.asSequence().chunked(40) { t ->
                t.toByteArray()
            }
            var idx = 0
            for (tmp in chunked) {
                protocol.BuildProtocoOld(
                    model,
                    chunked.count().toByte(),
                    SaminProtocolMode.SettingShare.byte,
                    byteArrayOf(idx.toByte()) + tmp
                )
                sendProtocolToSerial(protocol.mProtocol.clone())
                idx++
            }
        } else {
            protocol.buildProtocol(model, 0.toByte(), SaminProtocolMode.SettingShare.byte, null)
            sendProtocolToSerial(protocol.mProtocol.clone())
        }
    }

    override fun onPause() {
        super.onPause()
//        GlobalUiTimer.getInstance().activity = this
        isrunthUIError = false
        thUIError.interrupt()
        thUIError.join()
    }

    override fun onStop() {
        super.onStop()
//        unbindMessengerService()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindMessengerService()
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
        shared.removeBoardSetData(SaminSharedPreference.MASTEROXYGEN)
        mainViewModel.removeModelMap()
        mainViewModel.oxygensData.clear()
        idsListClear()
    }

    /**
     * 에러 유무 확인
     */
    var isCallTimeout = true

    private fun discallTimemout() {
        isCallTimeout = false
        callTimeoutThread?.interrupt()
        callTimeoutThread?.join()
    }
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
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        callTimeoutThread?.start()
    }

    private fun discallFeedback() {
        isSending = false
        callbackThread?.interrupt()
        callbackThread?.join()
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
                    while (mainViewModel.controlData.isMirrorMode) {
                        Thread.sleep(10)
                    }

                    while (isAnotherJob) {
                        Thread.sleep(10)
                    }

                    val processMils = measureTimeMillis {
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
                                    val key =
                                        littleEndianConversion(byteArrayOf(model, id)).toShort()

                                    if (!protocolBuffers.containsKey(key)) {
                                        protocol.feedBack(model, id)
                                        protocolBuffers[key] = protocol.mProtocol.clone()
                                    }
                                    protocolBuffers[key]?.let {
                                        sendProtocolToSerial(it)
                                    }
                                }
                                Thread.sleep(25)
                                if (model == 4.toByte())
                                    Thread.sleep(10)
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
    private var alertCheckTask: Timer? = null
    private var alertSoundTask: Timer? = null

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
                if (mainViewModel.isSoundAlert)
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
        mediaPlayer = null
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

    fun sendProtocolToSerial(data: ByteArray) {
        if (!mainViewModel.controlData.isMirrorMode) {
            val msg = Message.obtain(null, SerialService.MSG_SERIAL_SEND)
            val bundle = Bundle()
            bundle.putByteArray("", data)
            msg.data = bundle
            serialSVCIPCService?.send(msg)
        }
    }

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

                        mainViewModel.portAlertMapLed[ledkey] = tmpBits
                        if (id == 11.toByte())
                            continue


//                        if (alertsendLastTime[key] == null || alertsendLastTime[key]!! < (System.currentTimeMillis() - 1000 * 5)) {
                        isAnotherJob = true
                        Thread.sleep(20)

                        if (!tmpBits.equals(tmplast)) {
                            // LED 경고 상태를 전달.
//                            sendProtocolToSerial(byteArrayOf(0.toByte()))

                            for (cnt in 0..1) {
                                protocol.led_AlertStateByte(model, id, tmpBits)
                                sendProtocolToSerial(protocol.mProtocol.clone())
                                Thread.sleep(5)
                            }

                            // 경고음 처리전
                            if (mainViewModel.isSoundAlert) {
                                tabletSoundAlertOn()
                                protocol.buzzer_On(model, id)
//                                sendProtocolToSerial(byteArrayOf(0.toByte()))
                                for (cnt in 0..1) {
//                                    serialService?.sendData(protocol.mProtocol.clone())
                                    sendProtocolToSerial(protocol.mProtocol.clone())
                                    Thread.sleep(5)
                                }
                            }
                        } else if (alertsendLastTime[key] == null || alertsendLastTime[key]!! < (System.currentTimeMillis() - 1000 * 60)) {
//                            sendProtocolToSerial(byteArrayOf(0.toByte()))
                            for (cnt in 0..1) {
                                protocol.led_AlertStateByte(model, id, tmpBits)
                                sendProtocolToSerial(protocol.mProtocol.clone())
                                Thread.sleep(5)
                            }

                            alertsendLastTime[key] = System.currentTimeMillis()
                        }

                        isAnotherJob = false
                    }
                    // 에러가 사라진 AQ 찾기
                    if (diffkeys.size > 0) {


                        isAnotherJob = true
                        Thread.sleep(20)


                        for (tmp in diffkeys) {
                            val aqInfo = HexDump.toByteArray(tmp)
                            val model = aqInfo[1]
                            val id = aqInfo[0]

                            mainViewModel.portAlertMapLed.remove(tmp)
                            if (id == 11.toByte())
                                continue
//                            tabletSoundAlertOff()
//                            sendProtocolToSerial(byteArrayOf(0.toByte()))
                            for (cnt in 0..2) {
                                protocol.buzzer_Off(model, id)
//                                serialService?.sendData(protocol.mProtocol.clone())
                                sendProtocolToSerial(protocol.mProtocol.clone())
//                                Thread.sleep(35)
                            }

//                            sendProtocolToSerial(byteArrayOf(0.toByte()))
                            for (cnt in 0..1) {
                                protocol.led_AlertStateByte(model, id, 0.toByte())
//                                serialService?.sendData(protocol.mProtocol.clone())
                                sendProtocolToSerial(protocol.mProtocol.clone())
                                Thread.sleep(10)
                            }
                        }
                        isAnotherJob = false
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
//                Log.d(mainTAG, "measureTimeMillis : $elapsed")

                Thread.sleep(200)
            }

        }

        alertThread?.start()

    }


    lateinit var thUIError: Thread
    var isrunthUIError = true
    private fun uiError() {
        isrunthUIError = true
        thUIError = Thread {
            try {
                while (isrunthUIError) {
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
                        }
                        try {
                            mainViewModel.gasRoomAlert.value = targets.containsKey(2)
                        } catch (ex: Exception) {
                        }
                        try {
                            mainViewModel.wasteAlert.value = targets.containsKey(3)
                        } catch (ex: Exception) {
                        }
                        try {
                            mainViewModel.oxyenAlert.value = targets.containsKey(4)
                        } catch (ex: Exception) {
                        }
                        try {
                            mainViewModel.steamerAlert.value = targets.containsKey(5)
                        } catch (ex: Exception) {
                        }
                    }

                    Thread.sleep(100)
                }
            } catch (e: Exception) {

            }
        }
        thUIError?.start()
    }

    //    private var modbusService: SaminModbusService? = null
    var mHandler: MyHandler? = null

//    var mModelMonitorValues: ModelMonitorValues = ModelMonitorValues()

    private val svcConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(arg0: ComponentName, arg1: IBinder) {
            mainViewModel.modbusService =
                (arg1 as SaminModbusService.SaminModbusServiceBinder).getService()
            mHandler?.let { mainViewModel.modbusService?.setHandler(it) }

//            mainViewModel.modbusService?.svcHandler?.let {
////                val msg2 = it.obtainMessage(SaminModbusService.SETTING_SLAVE_ID, 2)
////                it.sendMessage(msg2)
////
////                val msg7 = it.obtainMessage(SaminModbusService.CHANGE_INPUT_DATA, InputData(0, true))
////                it.sendMessage(msg7)
////                val msg8 = it.obtainMessage(SaminModbusService.CHANGE_INPUT_DATA, InputData(1, true))
////                it.sendMessage(msg8)
////                val msg9 = it.obtainMessage(SaminModbusService.CHANGE_INPUT_DATA, InputData(3, true))
////                it.sendMessage(msg9)
////                val msg10 = it.obtainMessage(SaminModbusService.CHANGE_INPUT_REGISTER, InputRegister(100, 1000))
////                it.sendMessage(msg10)
////                val msg11 = it.obtainMessage(SaminModbusService.CHANGE_INPUT_REGISTER, InputRegister(1100, 1000))
////                it.sendMessage(msg11)
////                val msg12 = it.obtainMessage(SaminModbusService.CHANGE_INPUT_REGISTER, InputRegister(2100, 1000))
////                it.sendMessage(msg12)
////                val msg13 = it.obtainMessage(SaminModbusService.CHANGE_INPUT_REGISTER, InputRegister(3100, 1000))
////                it.sendMessage(msg13)
//            }

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
            mainViewModel.refreshModbusModels()
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


    lateinit var dao: AlertDAO


    // 알람 로그 생성
    suspend fun addAlertLogs(
        time: String,
        model: Int,
        id: Int,
        content: String,
        port: Int,
        isAlert: Boolean
    ) {
        withContext(Dispatchers.IO) {
//             val dao = Room.databaseBuilder(
//                application,
//                AlertDatabase::class.java,
//                "alertLogs"
//            )
//                .build()
//                .alertDAO()

//            dao = Room.databaseBuilder(
//                application,
//                AlertDatabase::class.java,
//                "alertLogs"
//            )
//                .build()
//                .alertDAO()

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

    inner class ExceptionHandler : Thread.UncaughtExceptionHandler {
        override fun uncaughtException(t: Thread, e: Throwable) {
            e.printStackTrace()
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(10)
        }
    }

    val dateformat: SimpleDateFormat =
        SimpleDateFormat("yyyy-mm-dd kk:mm:ss", Locale("ko", "KR"))
    private val serialSVCIPCHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
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
                    val date: Date = Date(System.currentTimeMillis())
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
                    Toast.makeText(this@MainActivity, "펌웨어 버전 : ${msg.arg1}",Toast.LENGTH_SHORT).show()
                }
                SerialService.MSG_CHECK_PING -> {
                    when(msg.arg1) {
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
                    }
                }
                SerialService.MSG_SHARE_SETTING -> {
                    val buff = msg.data.getByteArray("")
                    Log.d(mainTAG, "datahandler : \n${HexDump.dumpHexString(buff)}")
                    if (buff != null) {
                        when(buff.get(2)) {
                            0x11.toByte() -> {
                                // 가스 독
                                recvGasStorageBuffers[buff.get(7).toInt()] = buff.clone()
                            }
                            0x12.toByte() -> {
                                // 가스 룸
                                recvGasRoomBuffers[buff.get(7).toInt()] = buff.clone()
                            }
                            0x13.toByte() -> {
                                // 폐액통
                                recvWasteBuffers[buff.get(7).toInt()] = buff.clone()
                            }
                            0x14.toByte() -> {
                                // 산소농도
                                recvOxygenBuffers[buff.get(7).toInt()] = buff.clone()
                            }
                            0x15.toByte() -> {
                                // 스팀기
                                recvSteamerBuffers[buff.get(7).toInt()] = buff.clone()
                            }
                            0x16.toByte() -> {
                                // 산소농도 대표
                                recvOxygenMSTBuffers[buff.get(7).toInt()] = buff.clone()
                            }
                            0x17.toByte() -> {
                                recvModemapBuffers[buff.get(7).toInt()] = buff.clone()
                            }
                            0x20.toByte() -> {
                                // 설정 데이터 전송 완료
                                Log.d(mainTAG, "설정 데이터 전송 완료 ================")

                                // 가스독 설정 복원
                                var tmpgas = ByteArray(0)
                                val sortGas = sortMapByKey(recvGasStorageBuffers)
                                for (t in sortGas.values){
                                    tmpgas = tmpgas.plus(t.sliceArray(8..t.size-1))
                                }
                //                            Log.d(mainTAG, "tmpgas : ${HexDump.dumpHexString(tmpgas)}")
                                try {
                                    val objgas =
                                        ProtoBuf.decodeFromByteArray<List<SetGasStorageViewData>>(tmpgas)
                                    mainViewModel.GasStorageDataLiveList.clear(true)
                                    for(t in objgas) {
                                        t.pressureLeft = 0f
                                        t.pressure = 0f
                                        t.pressureRight = 0f
                                        t.isAlert = false
                                        t.isAlertLeft = false
                                        t.isAlertRight = false
                                        t.heartbeatCount = 0u
                                        mainViewModel.GasStorageDataLiveList.add(t)
                                    }
                                    val buff = mutableListOf<SetGasStorageViewData>()
                                    for (i in mainViewModel.GasStorageDataLiveList.value!!) {
                                        buff.add(i)
                                    }
                                    shared.saveBoardSetData(SaminSharedPreference.GASSTORAGE, buff)

                                } catch(e : Exception) {
                                    e.printStackTrace()
                                }

                                // 가스룸 설정 복원
                                var tmpgasroom = ByteArray(0)
                                val sortGasroom = sortMapByKey(recvGasRoomBuffers)
                                for (t in sortGasroom.values){
                                    tmpgasroom = tmpgasroom.plus(t.sliceArray(8..t.size-1))
                                }
                //                            Log.d(mainTAG, "tmpgasroom : ${HexDump.dumpHexString(tmpgasroom)}")
                                try {
                                    val objgas =
                                        ProtoBuf.decodeFromByteArray<List<SetGasRoomViewData>>(tmpgasroom)
                                    mainViewModel.GasRoomDataLiveList.clear(true)
                                    for(t in objgas) {
                                        t.pressure = 0f
                                        t.isAlert = false
                                        t.heartbeatCount = 0u
                                        mainViewModel.GasRoomDataLiveList.add(t)
                                    }
                                    val buff = mutableListOf<SetGasRoomViewData>()
                                    for (i in mainViewModel.GasRoomDataLiveList.value!!) {
                                        buff.add(i)
                                    }
                                    shared.saveBoardSetData(SaminSharedPreference.GASROOM, buff)
                                } catch(e : Exception) {
                                    e.printStackTrace()
                                }

                                // 폐액통 설정 복원
                                var tmpwaste= ByteArray(0)
                                val sortWaste = sortMapByKey(recvWasteBuffers)
                                for (t in sortWaste.values){
                                    tmpwaste = tmpwaste.plus(t.sliceArray(8..t.size-1))
                                }
                                Log.d(mainTAG, "tmpwaste : ${HexDump.dumpHexString(tmpwaste)}")
                                try {
                                    val objgas =
                                        ProtoBuf.decodeFromByteArray<List<SetWasteLiquorViewData>>(tmpwaste)
                                    mainViewModel.WasteLiquorDataLiveList.clear(true)
                                    for(t in objgas) {
                                        t.isAlert = false
                                        t.heartbeatCount = 0u
                                        mainViewModel.WasteLiquorDataLiveList.add(t)
                                    }
                                    val buff = mutableListOf<SetWasteLiquorViewData>()
                                    for (i in mainViewModel.WasteLiquorDataLiveList.value!!) {
                                        buff.add(i)
                                    }
                                    shared.saveBoardSetData(SaminSharedPreference.WASTELIQUOR, buff)
                                } catch(e : Exception) {
                                    e.printStackTrace()
                                }

                                // 산소농도 설정 복원
                                var tmpOxygen= ByteArray(0)
                                val sortOxygen = sortMapByKey(recvOxygenBuffers)
                                for (t in sortOxygen.values){
                                    tmpOxygen = tmpOxygen.plus(t.sliceArray(8..t.size-1))
                                }
                //                            Log.d(mainTAG, "tmpOxygen : ${HexDump.dumpHexString(tmpOxygen)}")
                                try {
                                    val objgas =
                                        ProtoBuf.decodeFromByteArray<List<SetOxygenViewData>>(tmpOxygen)
                                    mainViewModel.OxygenDataLiveList.clear(true)
                                    for(t in objgas) {
                                        t.isAlert = false
                                        t.setValue = 0f
                                        t.heartbeatCount = 0u
                                        mainViewModel.OxygenDataLiveList.add(t)
                                    }
                                    val buff = mutableListOf<SetOxygenViewData>()
                                    for (i in mainViewModel.OxygenDataLiveList.value!!) {
                                        buff.add(i)
                                    }
                                    shared.saveBoardSetData(SaminSharedPreference.OXYGEN, buff)
                                } catch(e : Exception) {
                                    e.printStackTrace()
                                }

                                // 스팀기 설정 복원
                                var tmpSteamer = ByteArray(0)
                                var sortSteamer = sortMapByKey(recvSteamerBuffers)
                                for (t in sortSteamer.values){
                                    tmpSteamer = tmpSteamer.plus(t.sliceArray(8..t.size-1))
                                }
                //                            Log.d(mainTAG, "tmpSteamer : ${HexDump.dumpHexString(tmpSteamer)}")
                                try {
                                    val objgas =
                                        ProtoBuf.decodeFromByteArray<List<SetSteamerViewData>>(tmpSteamer)
                                    mainViewModel.SteamerDataLiveList.clear(true)
                                    for(t in objgas) {
                                        t.isAlertLow = false
                                        t.isAlertTemp = false
                                        t.isTemp = 0
                                        t.heartbeatCount = 0u
                                        mainViewModel.SteamerDataLiveList.add(t)
                                    }
                                    val buff = mutableListOf<SetSteamerViewData>()
                                    for (i in mainViewModel.SteamerDataLiveList.value!!) {
                                        buff.add(i)
                                    }
                                    shared.saveBoardSetData(SaminSharedPreference.STEAMER, buff)
                                } catch(e : Exception) {
                                    e.printStackTrace()
                                }

                                // 산소농도 대표 설정 복원
                                var tmpOxyMST = ByteArray(0)
                                var sortOxymst = sortMapByKey(recvOxygenMSTBuffers)
                                for (t in sortOxymst.values){
                                    tmpOxyMST = tmpOxyMST.plus(t.sliceArray(8..t.size-1))
                                }
                //                            Log.d(mainTAG, "tmpOxyMST : ${HexDump.dumpHexString(tmpOxyMST)}")
                                try {
                                    val objgas =
                                        ProtoBuf.decodeFromByteArray<SetOxygenViewData>(tmpOxyMST)
                                    objgas.setValue = 0f
                                    objgas.isAlert = false
                                    objgas.heartbeatCount = 0u
                                    mainViewModel.oxygenMasterData = objgas
                                    shared.saveBoardSetData(SaminSharedPreference.MASTEROXYGEN, mainViewModel.oxygenMasterData!!)
                                } catch(e : Exception) {
                                    e.printStackTrace()
                                }

                                var tmpModemap = ByteArray(0)
                                var sortModemap = sortMapByKey(recvModemapBuffers)
                                for (t in sortModemap.values){
                                    tmpModemap = tmpModemap.plus(t.sliceArray(8..t.size-1))
                                }
                                mainViewModel.modelMap.clear()
                                try {
                                    val objgas =
                                        ProtoBuf.decodeFromByteArray<HashMap<String, ByteArray>>(tmpModemap)
                                    for (t in objgas) {
                                        mainViewModel.modelMap[t.key] = t.value
                                        var id = when {
                                            t.key.equals("GasDock") -> 1
                                            t.key.equals("GasRoom") -> 2
                                            t.key.equals("WasteLiquor") -> 3
                                            t.key.equals("Oxygen") -> 4
                                            t.key.equals("Steamer") -> 5
                                            else -> 1
                                        }

                                        mainViewModel.modelMapInt[id] = t.value.clone()
                                    }
                                    shared.saveHashMap(mainViewModel.modelMap)
                                } catch(e : Exception) {
                                    e.printStackTrace()
                                }

                                discallFeedback()
                                discallTimemout()

                                tmp.LoadSetting()
                                tmp.hmapLastedDate.keys.forEach{
                                    mainViewModel.hasKey.put(it, it)
                                }

                                callFeedback()
                                callTimemout()
                                onFragmentChange(MainViewModel.MAINSETTINGFRAGMENT)
                            }
                        }
                    }
                }
                SerialService.MSG_GASDOCK -> {
                    val recvdata = (msg.data.getSerializable("") as ParsingData)
                    val id = recvdata.id
                    val model = recvdata.model
                    val time = recvdata.time
                    val datas = recvdata.datas

                    for (i in mainViewModel.GasStorageDataLiveList.value!!) {
                        if ((i as SetGasStorageViewData).ViewType == 1 || (i as SetGasStorageViewData).ViewType == 2) {
                            if ((i as SetGasStorageViewData).port == 1) {
                                val left_value = datas[0]
                                val right_value = datas[1]
                                val key = littleEndianConversion(
                                    byteArrayOf(
                                        model,
                                        id.toByte(),
                                        1
                                    )
                                )
                                tmp.hmapLastedDate[key] = time
                                tmp.ProcessDualGasStorage(key, left_value, right_value)
                            } else if ((i as SetGasStorageViewData).port == 3) {
                                val left_value = datas[2]
                                val right_value = datas[3]
                                val key = littleEndianConversion(
                                    byteArrayOf(
                                        model,
                                        id.toByte(),
                                        3
                                    )
                                )
                                tmp.hmapLastedDate[key] = time
                                tmp.ProcessDualGasStorage(key, left_value, right_value)
                            }
                        } else {
                            var loop = 1
                            for (t in datas) {
                                //아이디 1개당 포트 4개 추가
                                val port = loop++.toByte()
                                //키는 아이디 포트
                                val key = littleEndianConversion(
                                    byteArrayOf(
                                        model,
                                        id.toByte(),
                                        port
                                    )
                                )
                                tmp.hmapLastedDate[key] = time
                                tmp.ProcessSingleGasStorage(key, t)
                            }
                        }
                    }
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
                            littleEndianConversion(byteArrayOf(model, id.toByte(), port))
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
                            littleEndianConversion(byteArrayOf(model, id.toByte(), port))
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
                    val key = littleEndianConversion(byteArrayOf(model, id.toByte(), port))
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
                        val temp_data = datas[loop - 1]
                        val level_data = datas[loop + 1]
                        val key =
                            littleEndianConversion(
                                byteArrayOf(
                                    model,
                                    id.toByte(),
                                    loop.toByte()
                                )
                            )
                        tmp.hmapLastedDate[key] = time
                        tmp.ProcessSteamer(key, temp_data, level_data)
                    }
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


