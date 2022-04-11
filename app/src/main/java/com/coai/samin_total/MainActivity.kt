package com.coai.samin_total

import android.annotation.SuppressLint
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
import com.coai.libmodbus.service.SaminModbusService
import com.coai.libsaminmodbus.model.ModelMonitorValues
import com.coai.libsaminmodbus.model.ObserveModelMonitorValues
import com.coai.samin_total.DataBase.SaminDataBase
import com.coai.samin_total.Dialog.AlertDialogFragment
import com.coai.samin_total.Dialog.ScanDialogFragment
import com.coai.samin_total.Dialog.SetAlertData
import com.coai.samin_total.GasDock.GasDockMainFragment
import com.coai.samin_total.GasDock.GasStorageSettingFragment
import com.coai.samin_total.GasRoom.GasRoomMainFragment
import com.coai.samin_total.GasRoom.GasRoomSettingFragment
import com.coai.samin_total.GasRoom.TimePSI
import com.coai.samin_total.Logic.*
import com.coai.samin_total.Oxygen.OxygenMainFragment
import com.coai.samin_total.Oxygen.OxygenSettingFragment
import com.coai.samin_total.Service.HexDump
import com.coai.samin_total.Service.SerialService
import com.coai.samin_total.Steamer.SteamerMainFragment
import com.coai.samin_total.Steamer.SteamerSettingFragment
import com.coai.samin_total.WasteLiquor.WasteLiquorMainFragment
import com.coai.samin_total.WasteLiquor.WasteWaterSettingFragment
import com.coai.samin_total.databinding.ActivityMainBinding
import com.coai.uikit.GlobalUiTimer
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

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
        shared = SaminSharedPreference(this)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        tmp = AQDataParser(mainViewModel)

        setFragment()
    }

    fun bindSerialService() {
//        if (!UsbSerialService.SERVICE_CONNECTED){
//            val startSerialService = Intent(this, UsbSerialService::class.java)
//            startService(startSerialService)
//
//        }
        Log.d(mainTAG, "바인드 시작")
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
                        0
                    )
                )
            }
            Log.d(mainTAG, "datahandler : ${HexDump.dumpHexString(msg.obj as ByteArray)}")
            val receiveParser = SaminProtocol()
            receiveParser.parse(msg.obj as ByteArray)

            if (receiveParser.packetName == "CheckProductPing") {
                val model = receiveParser.modelName
                val input_id = receiveParser.mProtocol.get(3)
                Log.d("CheckProductPing", "model:$model // id : $input_id")
                when (model) {
                    "GasDock" -> {
                        val id = receiveParser.mProtocol.get(3)
                        gasdock_ids_list.add(id)
                        val ids = gasdock_ids_list.distinct().toByteArray()
                        mainViewModel.modelMap[model] = ids
                    }
                    "GasRoom" -> {
                        val id = receiveParser.mProtocol.get(3)
                        gasroom_ids_list.add(id)
                        val ids = gasroom_ids_list.distinct().toByteArray()
                        mainViewModel.modelMap[model] = ids
                    }
                    "WasteLiquor" -> {
                        val id = receiveParser.mProtocol.get(3)
                        wasteLiquor_ids_list.add(id)
                        val ids = wasteLiquor_ids_list.distinct().toByteArray()
                        mainViewModel.modelMap[model] = ids
                    }
                    "Oxygen" -> {
                        val id = receiveParser.mProtocol.get(3)
                        oxygen_ids_list.add(id)
                        val ids = oxygen_ids_list.distinct().toByteArray()
                        mainViewModel.modelMap[model] = ids
                    }
                    "Steamer" -> {
                        val id = receiveParser.mProtocol.get(3)
                        steamer_ids_list.add(id)
                        val ids = steamer_ids_list.distinct().toByteArray()
                        mainViewModel.modelMap[model] = ids
                    }
                    "Temp_Hum" -> {

                    }
                }

            } else if (receiveParser.packetName == "RequestFeedBackPing") {
                if (receiveParser.mProtocol.size >= 14) {
                    val aqInfo = receiveParser.mProtocol.slice(2..3)
                    val sensorData = receiveParser.mProtocol.slice(7..14).toByteArray()
                    val currentSensorInfo = CurrentSensorInfo(aqInfo, sensorData)
                    mainViewModel.latestSensorInfo[aqInfo] = currentSensorInfo
                    val aqInfotest = littleEndianConversion(
                        receiveParser.mProtocol.slice(2..3).toByteArray()
                    ).toShort()
                    val inputInfo = receiveParser.mProtocol.slice(7..14).toByteArray()
                    val aqdata =
                        AQData(receiveParser.mProtocol[2], receiveParser.mProtocol[3], inputInfo)
                    mainViewModel.testHashMap.put(aqInfotest, aqdata)

                    tmp.Parser(receiveParser.mProtocol)

//                    when (receiveParser.modelName) {
//
//                        "GasDock" -> {
//
//                            val pin1_data = littleEndianConversion(
//                                receiveParser.mProtocol.slice(7..8).toByteArray()
//                            ).toFloat()
//                            val pin2_data = littleEndianConversion(
//                                receiveParser.mProtocol.slice(9..10).toByteArray()
//                            ).toFloat()
//                            val pin3_data = littleEndianConversion(
//                                receiveParser.mProtocol.slice(11..12).toByteArray()
//                            ).toFloat()
//                            val pin4_data = littleEndianConversion(
//                                receiveParser.mProtocol.slice(13..14).toByteArray()
//                            ).toFloat()
//                            var sensor_1: Float
//                            var sensor_2: Float
//                            var sensor_3: Float
//                            var sensor_4: Float
//
//                            for (i in mainViewModel.GasStorageDataLiveList.value!!) {
//                                if (i.sensorType == "Sensts 142PSI") {
//                                    sensor_1 = calcPSI142(pin1_data, i.rewardValue, i.zeroPoint)
//                                    sensor_2 = calcPSI142(pin2_data, i.rewardValue, i.zeroPoint)
//                                    sensor_3 = calcPSI142(pin3_data, i.rewardValue, i.zeroPoint)
//                                    sensor_4 = calcPSI142(pin4_data, i.rewardValue, i.zeroPoint)
//
//                                } else if (i.sensorType == "Sensts 2000PSI") {
//                                    sensor_1 = calcPSI2000(pin1_data, i.rewardValue, i.zeroPoint)
//                                    sensor_2 = calcPSI2000(pin2_data, i.rewardValue, i.zeroPoint)
//                                    sensor_3 = calcPSI2000(pin3_data, i.rewardValue, i.zeroPoint)
//                                    sensor_4 = calcPSI2000(pin4_data, i.rewardValue, i.zeroPoint)
//                                } else {
//                                    sensor_1 = calcSensor(
//                                        pin1_data,
//                                        i.pressure_Max!!,
//                                        i.rewardValue,
//                                        i.zeroPoint
//                                    )
//                                    sensor_2 = calcSensor(
//                                        pin2_data,
//                                        i.pressure_Max!!,
//                                        i.rewardValue,
//                                        i.zeroPoint
//                                    )
//                                    sensor_3 = calcSensor(
//                                        pin3_data,
//                                        i.pressure_Max!!,
//                                        i.rewardValue,
//                                        i.zeroPoint
//                                    )
//                                    sensor_4 = calcSensor(
//                                        pin4_data,
//                                        i.pressure_Max!!,
//                                        i.rewardValue,
//                                        i.zeroPoint
//                                    )
//                                }
//
//                                //받은 데이터 아이디와 데이터리스트의 아디가 동일한 경우
//                                if (i.id == receiveParser.mProtocol.get(3).toInt()) {
//                                    val port = listOf<Int>(1, i.id, i.port)
//                                    val portInfo = PortInfo(1.toByte(), i.id.toByte(), i.port)
//                                    // 데이터 리스트의 데이터의 뷰타입이 듀얼 또는 오토체인처일 경우
//                                    if (i.ViewType == 1 || i.ViewType == 2) {
//                                        //2,4port 삭제되서 날라옴
//                                        if (i.port == 1) {
//                                            i.pressureLeft = sensor_1
//                                            i.pressureRight = sensor_2
//                                            if (i.pressure_Min!! > i.pressureLeft!!) {
//                                                i.isAlertLeft = true
//                                                mainViewModel.gasStorageAlert.value = true
//
//                                                if (!mainViewModel.mExPortInfo[port]!!.alert_1) {
//                                                    mainViewModel.alertInfo.add(
//                                                        SetAlertData(
//                                                            portInfo.latest_time,
//                                                            portInfo.model.toInt(),
//                                                            portInfo.id.toInt(),
//                                                            "가스 압력 하한 값",
//                                                            portInfo.port
//                                                        )
//                                                    )
////                                                mainViewModel.gasStorageAlert.value = true
//                                                    mainViewModel.mExPortInfo[port]?.alert_1 = true
//                                                }
//                                            } else if (i.pressure_Min!! > i.pressureRight!!) {
//                                                i.isAlertRight = true
//                                                mainViewModel.gasStorageAlert.value = true
//                                                if (!mainViewModel.mExPortInfo[port]!!.alert_1) {
//                                                    mainViewModel.alertInfo.add(
//                                                        SetAlertData(
//                                                            portInfo.latest_time,
//                                                            portInfo.model.toInt(),
//                                                            portInfo.id.toInt(),
//                                                            "가스 압력 하한 값",
//                                                            portInfo.port
//                                                        )
//                                                    )
////                                                mainViewModel.gasStorageAlert.value = true
//                                                    mainViewModel.mExPortInfo[port]?.alert_1 = true
//                                                }
//                                            } else {
//                                                mainViewModel.mExPortInfo[port] = portInfo
//                                                mainViewModel.mPortInfo[port] = portInfo
//                                                mainViewModel.mPortInfo[port]?.alert_1 = false
//                                                mainViewModel.mExPortInfo[port]?.alert_1 = false
//                                                mainViewModel.gasStorageAlert.value = false
//                                            }
//                                        } else {
//                                            i.pressureLeft = sensor_3
//                                            i.pressureRight = sensor_4
//                                            if (i.pressure_Min!! > i.pressureLeft!!) {
//                                                i.isAlertLeft = true
//                                                mainViewModel.gasStorageAlert.value = true
//                                                if (!mainViewModel.mExPortInfo[port]!!.alert_1) {
//                                                    mainViewModel.alertInfo.add(
//                                                        SetAlertData(
//                                                            portInfo.latest_time,
//                                                            portInfo.model.toInt(),
//                                                            portInfo.id.toInt(),
//                                                            "가스 압력 하한 값",
//                                                            portInfo.port
//                                                        )
//                                                    )
////                                                mainViewModel.gasStorageAlert.value = true
//                                                    mainViewModel.mExPortInfo[port]?.alert_1 = true
//                                                }
//                                            } else if (i.pressure_Min!! > i.pressureRight!!) {
//                                                i.isAlertRight = true
//                                                mainViewModel.gasStorageAlert.value = true
//                                                if (!mainViewModel.mExPortInfo[port]!!.alert_1) {
//                                                    mainViewModel.alertInfo.add(
//                                                        SetAlertData(
//                                                            portInfo.latest_time,
//                                                            portInfo.model.toInt(),
//                                                            portInfo.id.toInt(),
//                                                            "가스 압력 하한 값",
//                                                            portInfo.port
//                                                        )
//                                                    )
////                                                mainViewModel.gasStorageAlert.value = true
//                                                    mainViewModel.mExPortInfo[port]?.alert_1 = true
//                                                }
//                                            } else {
//                                                mainViewModel.mExPortInfo[port] = portInfo
//                                                mainViewModel.mPortInfo[port] = portInfo
//                                                mainViewModel.mPortInfo[port]?.alert_1 = false
//                                                mainViewModel.mExPortInfo[port]?.alert_1 = false
//                                                mainViewModel.gasStorageAlert.value = false
//                                            }
//                                        }
//
//                                    } else {
//                                        when (i.port) {
//                                            1 -> {
//                                                i.pressure = sensor_1
//                                                if (i.pressure_Min!! > i.pressure!!) {
//                                                    i.isAlert = true
//                                                    mainViewModel.gasStorageAlert.value = true
//                                                    if (!mainViewModel.mExPortInfo[port]!!.alert_1) {
//                                                        mainViewModel.alertInfo.add(
//                                                            SetAlertData(
//                                                                portInfo.latest_time,
//                                                                portInfo.model.toInt(),
//                                                                portInfo.id.toInt(),
//                                                                "가스 압력 하한 값",
//                                                                portInfo.port
//                                                            )
//                                                        )
////                                                    mainViewModel.gasStorageAlert.value = true
//                                                        mainViewModel.mExPortInfo[port]?.alert_1 =
//                                                            true
//                                                    }
//                                                } else {
//                                                    mainViewModel.mExPortInfo[port] = portInfo
//                                                    mainViewModel.mPortInfo[port] = portInfo
//                                                    mainViewModel.mPortInfo[port]?.alert_1 = false
//                                                    mainViewModel.mExPortInfo[port]?.alert_1 = false
//                                                    mainViewModel.gasStorageAlert.value = false
//                                                }
//
//                                            }
//                                            2 -> {
//                                                i.pressure = sensor_2
//                                                if (i.pressure_Min!! > i.pressure!!) {
//                                                    i.isAlert = true
//                                                    mainViewModel.gasStorageAlert.value = true
//                                                    if (!mainViewModel.mExPortInfo[port]!!.alert_1) {
//                                                        mainViewModel.alertInfo.add(
//                                                            SetAlertData(
//                                                                portInfo.latest_time,
//                                                                portInfo.model.toInt(),
//                                                                portInfo.id.toInt(),
//                                                                "가스 압력 하한 값",
//                                                                portInfo.port
//                                                            )
//                                                        )
////                                                    mainViewModel.gasStorageAlert.value = true
//                                                        mainViewModel.mExPortInfo[port]?.alert_1 =
//                                                            true
//                                                    }
//                                                } else {
//                                                    mainViewModel.mExPortInfo[port] = portInfo
//                                                    mainViewModel.mPortInfo[port] = portInfo
//                                                    mainViewModel.mPortInfo[port]?.alert_1 = false
//                                                    mainViewModel.mExPortInfo[port]?.alert_1 = false
//                                                    mainViewModel.gasStorageAlert.value = false
//                                                }
//                                            }
//                                            3 -> {
//                                                i.pressure = sensor_3
//                                                if (i.pressure_Min!! > i.pressure!!) {
//                                                    mainViewModel.gasStorageAlert.value = true
//
//                                                    i.isAlert = true
//                                                    if (!mainViewModel.mExPortInfo[port]!!.alert_1) {
//                                                        mainViewModel.alertInfo.add(
//                                                            SetAlertData(
//                                                                portInfo.latest_time,
//                                                                portInfo.model.toInt(),
//                                                                portInfo.id.toInt(),
//                                                                "가스 압력 하한 값",
//                                                                portInfo.port
//                                                            )
//                                                        )
////                                                    mainViewModel.gasStorageAlert.value = true
//                                                        mainViewModel.mExPortInfo[port]?.alert_1 =
//                                                            true
//                                                    }
//                                                } else {
//                                                    mainViewModel.mExPortInfo[port] = portInfo
//                                                    mainViewModel.mPortInfo[port] = portInfo
//                                                    mainViewModel.mPortInfo[port]?.alert_1 = false
//                                                    mainViewModel.mExPortInfo[port]?.alert_1 = false
//                                                    mainViewModel.gasStorageAlert.value = false
//                                                }
//                                            }
//                                            4 -> {
//                                                i.pressure = sensor_4
//                                                mainViewModel.gasStorageAlert.value = true
//                                                if (i.pressure_Min!! > i.pressure!!) {
//                                                    i.isAlert = true
//                                                    if (!mainViewModel.mExPortInfo[port]!!.alert_1) {
//                                                        mainViewModel.alertInfo.add(
//                                                            SetAlertData(
//                                                                portInfo.latest_time,
//                                                                portInfo.model.toInt(),
//                                                                portInfo.id.toInt(),
//                                                                "가스 압력 하한 값",
//                                                                portInfo.port
//                                                            )
//                                                        )
////                                                    mainViewModel.gasStorageAlert.value = true
//                                                        mainViewModel.mExPortInfo[port]?.alert_1 =
//                                                            true
//                                                    }
//                                                } else {
//                                                    mainViewModel.mExPortInfo[port] = portInfo
//                                                    mainViewModel.mPortInfo[port] = portInfo
//                                                    mainViewModel.mPortInfo[port]?.alert_1 = false
//                                                    mainViewModel.mExPortInfo[port]?.alert_1 = false
//                                                    mainViewModel.gasStorageAlert.value = false
//                                                }
//                                            }
//                                        }
//
//                                    }
//                                    mainViewModel.GasStorageDataLiveList.notifyChange()
//                                }
//                            }
//
//                        }
//                        "GasRoom" -> {
////                            tmp.Parser(receiveParser.mProtocol)
//
//                            val model = receiveParser.mProtocol.get(2).toInt()
//                            val pin1_data = littleEndianConversion(
//                                receiveParser.mProtocol.slice(7..8).toByteArray()
//                            ).toFloat()
//                            val pin2_data = littleEndianConversion(
//                                receiveParser.mProtocol.slice(9..10).toByteArray()
//                            ).toFloat()
//                            val pin3_data = littleEndianConversion(
//                                receiveParser.mProtocol.slice(11..12).toByteArray()
//                            ).toFloat()
//                            val pin4_data = littleEndianConversion(
//                                receiveParser.mProtocol.slice(13..14).toByteArray()
//                            ).toFloat()
//                            var sensor_1: Float
//                            var sensor_2: Float
//                            var sensor_3: Float
//                            var sensor_4: Float
//
//                            for (i in mainViewModel.GasRoomDataLiveList.value!!) {
//                                if (i.sensorType == "Sensts 142PSI") {
//                                    sensor_1 = calcPSI142(pin1_data, i.rewardValue, i.zeroPoint)
//                                    sensor_2 = calcPSI142(pin2_data, i.rewardValue, i.zeroPoint)
//                                    sensor_3 = calcPSI142(pin3_data, i.rewardValue, i.zeroPoint)
//                                    sensor_4 = calcPSI142(pin4_data, i.rewardValue, i.zeroPoint)
//
//                                } else if (i.sensorType == "Sensts 2000PSI") {
//                                    sensor_1 = calcPSI2000(pin1_data, i.rewardValue, i.zeroPoint)
//                                    sensor_2 = calcPSI2000(pin2_data, i.rewardValue, i.zeroPoint)
//                                    sensor_3 = calcPSI2000(pin3_data, i.rewardValue, i.zeroPoint)
//                                    sensor_4 = calcPSI2000(pin4_data, i.rewardValue, i.zeroPoint)
//                                } else {
//                                    sensor_1 = calcSensor(
//                                        pin1_data,
//                                        i.pressure_Max!!,
//                                        i.rewardValue,
//                                        i.zeroPoint
//                                    )
//                                    sensor_2 = calcSensor(
//                                        pin2_data,
//                                        i.pressure_Max!!,
//                                        i.rewardValue,
//                                        i.zeroPoint
//                                    )
//                                    sensor_3 = calcSensor(
//                                        pin3_data,
//                                        i.pressure_Max!!,
//                                        i.rewardValue,
//                                        i.zeroPoint
//                                    )
//                                    sensor_4 = calcSensor(
//                                        pin4_data,
//                                        i.pressure_Max!!,
//                                        i.rewardValue,
//                                        i.zeroPoint
//                                    )
//                                }
////                                val ticks = System.currentTimeMillis()
////                                val item =
////                                    TimePSI(ticks, i.pressure, model, i.id, i.port)
////                                val basetime = ticks - 1000 * 3
////                                templist.add(item)
////
////                                val tempremove = templist.filter {
////                                    it.Ticks < basetime
////                                }
////                                templist.removeAll(tempremove)
//
////                                if (i.id == receiveParser.mProtocol.get(3).toInt()) {
////                                    when (i.port) {
////                                        1 -> {
////                                            i.pressure = sensor_1
////                                            val ticks = System.currentTimeMillis()
////                                            val item =
////                                                TimePSI(ticks, i.pressure, model, i.id, i.port)
////                                            val basetime = ticks - 1000 * 3
////                                            templist_1.add(item)
////
////                                            val tempremove = templist_1.filter {
////                                                it.Ticks < basetime
////                                            }
////                                            templist_1.removeAll(tempremove)
////                                            val temp = templist_1.filter {
////                                                it.Id == i.id
////                                            }
////                                            id_slopeMap_1.put(i.id, temp)
////
////                                            for ((key, value) in id_slopeMap_1) {
////                                                val lstTicks = value.map {
////                                                    (it.Ticks / 100).toDouble()
////                                                }
////                                                val lstPsi = value.map {
////                                                    (it.Psi).toDouble()
////                                                }
////                                                val slope = AnalyticUtils.LinearRegression(
////                                                    lstTicks.toTypedArray(),
////                                                    lstPsi.toTypedArray(),
////                                                    0,
////                                                    lstPsi.size
////                                                )
////
////                                                if (slope < -10f) {
////                                                    Log.d("Test", "$slope")
////                                                    mainViewModel.roomAlert_1.put(key, 1)
//////                                                    if(key == i.id){
//////                                                        i.isAlert = true
//////                                                    }
////                                                } else {
////                                                }
////                                            }
////
////
//////                                            val lstTicks = templist.map {
//////                                                (it.Ticks / 100).toDouble()
//////                                            }
//////                                            val lstPsi = templist.map {
//////                                                (it.Psi).toDouble()
//////                                            }
//////
//////                                            if (lstTicks.count() > 1) {
//////                                                val slope = AnalyticUtils.LinearRegression(
//////                                                    lstTicks.toTypedArray(),
//////                                                    lstPsi.toTypedArray(),
//////                                                    0,
//////                                                    lstPsi.size
//////                                                )
////////                                                Log.d("test", "$slope")
//////                                                if (slope.isInfinite() && slope < -10f) {
//////
//////                                                }
//////                                            }
////
////
////                                        }
////                                        2 -> {
////                                            i.pressure = sensor_2
////                                            val ticks = System.currentTimeMillis()
////                                            val item =
////                                                TimePSI(ticks, i.pressure, model, i.id, i.port)
////                                            val basetime = ticks - 1000 * 3
////                                            templist_2.add(item)
////
////                                            val tempremove = templist_2.filter {
////                                                it.Ticks < basetime
////                                            }
////                                            templist_2.removeAll(tempremove)
////
////                                            val temp = templist_2.filter {
////                                                it.Id == i.id
////                                            }
////                                            id_slopeMap_2.put(i.id, temp)
////
////                                            for ((key, value) in id_slopeMap_2) {
////                                                val lstTicks = value.map {
////                                                    (it.Ticks / 100).toDouble()
////                                                }
////                                                val lstPsi = value.map {
////                                                    (it.Psi).toDouble()
////                                                }
////                                                val slope = AnalyticUtils.LinearRegression(
////                                                    lstTicks.toTypedArray(),
////                                                    lstPsi.toTypedArray(),
////                                                    0,
////                                                    lstPsi.size
////                                                )
////
////                                                if (slope < -10f) {
////                                                    Log.d("Test", "$slope")
////                                                    mainViewModel.roomAlert_1.put(key, 2)
//////                                                    if(key == i.id){
//////                                                        i.isAlert = true
//////                                                    }
////                                                } else {
////                                                }
////                                            }
////                                        }
////                                        3 -> {
////                                            i.pressure = sensor_3
////                                            val ticks = System.currentTimeMillis()
////                                            val item =
////                                                TimePSI(ticks, i.pressure, model, i.id, i.port)
////                                            val basetime = ticks - 1000 * 3
////                                            templist_3.add(item)
////
////                                            val tempremove = templist_3.filter {
////                                                it.Ticks < basetime
////                                            }
////                                            templist_3.removeAll(tempremove)
////
////                                            val temp = templist_3.filter {
////                                                it.Id == i.id
////                                            }
////                                            id_slopeMap_3.put(i.id, temp)
////
////                                            for ((key, value) in id_slopeMap_3) {
////                                                val lstTicks = value.map {
////                                                    (it.Ticks / 100).toDouble()
////                                                }
////                                                val lstPsi = value.map {
////                                                    (it.Psi).toDouble()
////                                                }
////                                                val slope = AnalyticUtils.LinearRegression(
////                                                    lstTicks.toTypedArray(),
////                                                    lstPsi.toTypedArray(),
////                                                    0,
////                                                    lstPsi.size
////                                                )
////
////                                                if (slope < -10f) {
////                                                    Log.d("Test", "$slope")
////                                                    mainViewModel.roomAlert_1.put(key, 3)
//////                                                    if(key == i.id){
//////                                                        i.isAlert = true
//////                                                    }
////                                                } else {
////                                                }
////                                            }
////                                        }
////                                        4 -> {
////                                            i.pressure = sensor_4
////                                            val ticks = System.currentTimeMillis()
////                                            val item =
////                                                TimePSI(ticks, i.pressure, model, i.id, i.port)
////                                            val basetime = ticks - 1000 * 3
////                                            templist_4.add(item)
////
////                                            val tempremove = templist_4.filter {
////                                                it.Ticks < basetime
////                                            }
////                                            templist_4.removeAll(tempremove)
////
////                                            val temp = templist_4.filter {
////                                                it.Id == i.id
////                                            }
////                                            id_slopeMap_4.put(i.id, temp)
////
////                                            for ((key, value) in id_slopeMap_4) {
////                                                val lstTicks = value.map {
////                                                    (it.Ticks / 100).toDouble()
////                                                }
////                                                val lstPsi = value.map {
////                                                    (it.Psi).toDouble()
////                                                }
////                                                val slope = AnalyticUtils.LinearRegression(
////                                                    lstTicks.toTypedArray(),
////                                                    lstPsi.toTypedArray(),
////                                                    0,
////                                                    lstPsi.size
////                                                )
////
////                                                if (slope < -10f) {
////                                                    Log.d("Test", "$slope")
////                                                    mainViewModel.roomAlert_1.put(key, 4)
//////                                                    if(key == i.id){
//////                                                        i.isAlert = true
//////                                                    }
////                                                } else {
////                                                }
////                                            }
////                                        }
////                                    }
////                                    for ((key, value) in mainViewModel.roomAlert_1) {
////                                        if (key == i.id) {
////                                            if (value == i.port) {
////                                                i.isAlert = true
////                                            }
////                                        }
////                                    }
////
////                                    mainViewModel.GasRoomDataLiveList.notifyChange()
////                                }
//                            }
//                        }
//                        "WasteLiquor" -> {
//                            val pin1_data = littleEndianConversion(
//                                receiveParser.mProtocol.slice(7..8).toByteArray()
//                            )
//                            val pin2_data = littleEndianConversion(
//                                receiveParser.mProtocol.slice(9..10).toByteArray()
//                            )
//                            val pin3_data = littleEndianConversion(
//                                receiveParser.mProtocol.slice(11..12).toByteArray()
//                            )
//                            val pin4_data = littleEndianConversion(
//                                receiveParser.mProtocol.slice(13..14).toByteArray()
//                            )
//
//                            for (i in mainViewModel.WasteLiquorDataLiveList.value!!) {
//                                if (i.id == receiveParser.mProtocol.get(3).toInt()) {
//                                    val port = listOf<Int>(3, i.id, i.port)
//                                    when (i.port) {
//                                        //exsensorinfo ->exprotinfo
//                                        //latestsensorinfo ->portinfo aqinfo를 port로
//
//                                        1 -> {
//                                            if (pin1_data == 0) {
//                                                i.isAlert = true
//                                                mainViewModel.wasteAlert.value = true
//                                                if (mainViewModel.exportInfo[port] != null
//                                                ) {
//                                                    if (!mainViewModel.exportInfo[port]!!.pin1_Alert) {
//                                                        val info = mainViewModel.exportInfo[port]
//                                                        mainViewModel.portInfo[port]!!.pin1_Alert =
//                                                            true
//                                                        mainViewModel.alertInfo.add(
//                                                            SetAlertData(
//                                                                info!!.getLatestTime(),
//                                                                info.getAQ_Model().toInt(),
//                                                                info.getAQ_Id().toInt(),
//                                                                "수위 초과",
//                                                                1
//                                                            )
//                                                        )
////                                                    mainViewModel.wasteAlert.value = true
//                                                        mainViewModel.exportInfo[port]!!.pin1_Alert =
//                                                            true
//                                                    }
//                                                }
//                                            } else {
//                                                i.isAlert = false
//                                                mainViewModel.exportInfo[port] = currentSensorInfo
//                                                mainViewModel.portInfo[port] = currentSensorInfo
//                                                mainViewModel.portInfo[port]?.pin1_Alert =
//                                                    false
//                                                mainViewModel.exportInfo[port]?.pin1_Alert = false
//                                                mainViewModel.wasteAlert.value = false
//
//                                            }
//                                        }
//                                        2 -> {
//                                            if (pin2_data == 0) {
//                                                i.isAlert = true
//                                                mainViewModel.wasteAlert.value = true
//                                                if (mainViewModel.exportInfo[port] != null) {
//                                                    if (!mainViewModel.exportInfo[port]!!.pin2_Alert) {
//                                                        val info = mainViewModel.exportInfo[port]
//                                                        mainViewModel.portInfo[port]!!.pin2_Alert =
//                                                            true
//                                                        mainViewModel.alertInfo.add(
//                                                            SetAlertData(
//                                                                info!!.getLatestTime(),
//                                                                info.getAQ_Model().toInt(),
//                                                                info.getAQ_Id().toInt(),
//                                                                "수위 초과",
//                                                                2
//                                                            )
//                                                        )
////                                                    mainViewModel.wasteAlert.value = true
//                                                        mainViewModel.exportInfo[port]!!.pin2_Alert =
//                                                            true
//                                                    }
//                                                }
//                                            } else {
//                                                i.isAlert = false
//                                                mainViewModel.exportInfo[port] = currentSensorInfo
//                                                mainViewModel.portInfo[port] = currentSensorInfo
//                                                mainViewModel.portInfo[port]?.pin2_Alert =
//                                                    false
//                                                mainViewModel.exportInfo[port]?.pin2_Alert = false
//                                                mainViewModel.wasteAlert.value = false
//
//                                            }
//
//                                        }
//                                        3 -> {
//                                            if (pin3_data == 0) {
//                                                i.isAlert = true
//                                                mainViewModel.wasteAlert.value = true
//                                                if (mainViewModel.exportInfo[port] != null) {
//                                                    if (!mainViewModel.exportInfo[port]!!.pin3_Alert) {
//                                                        val info = mainViewModel.exportInfo[port]
//                                                        mainViewModel.portInfo[port]!!.pin3_Alert =
//                                                            true
//                                                        mainViewModel.alertInfo.add(
//                                                            SetAlertData(
//                                                                info!!.getLatestTime(),
//                                                                info.getAQ_Model().toInt(),
//                                                                info.getAQ_Id().toInt(),
//                                                                "수위 초과",
//                                                                3
//                                                            )
//                                                        )
////                                                    mainViewModel.wasteAlert.value = true
//                                                        mainViewModel.exportInfo[port]!!.pin3_Alert =
//                                                            true
//                                                    }
//                                                }
//                                            } else {
//                                                i.isAlert = false
//                                                mainViewModel.exportInfo[port] = currentSensorInfo
//                                                mainViewModel.portInfo[port] = currentSensorInfo
//                                                mainViewModel.portInfo[port]?.pin3_Alert =
//                                                    false
//                                                mainViewModel.exportInfo[port]?.pin3_Alert = false
//                                                mainViewModel.wasteAlert.value = false
//
//                                            }
//
//                                        }
//                                        4 -> {
//                                            if (pin4_data == 0) {
//                                                i.isAlert = true
//                                                mainViewModel.wasteAlert.value = true
//                                                if (mainViewModel.exportInfo[port] != null) {
//                                                    if (!mainViewModel.exportInfo[port]!!.pin4_Alert) {
//                                                        val info = mainViewModel.exportInfo[port]
//                                                        mainViewModel.portInfo[port]!!.pin4_Alert =
//                                                            true
//                                                        mainViewModel.alertInfo.add(
//                                                            SetAlertData(
//                                                                info!!.getLatestTime(),
//                                                                info.getAQ_Model().toInt(),
//                                                                info.getAQ_Id().toInt(),
//                                                                "수위 초과",
//                                                                4
//                                                            )
//                                                        )
////                                                    mainViewModel.wasteAlert.value = true
//                                                        mainViewModel.exportInfo[port]!!.pin4_Alert =
//                                                            true
//                                                    }
//                                                }
//                                            } else {
//                                                i.isAlert = false
//                                                mainViewModel.exportInfo[port] = currentSensorInfo
//                                                mainViewModel.portInfo[port] = currentSensorInfo
//                                                mainViewModel.portInfo[port]?.pin4_Alert =
//                                                    false
//                                                mainViewModel.exportInfo[port]?.pin4_Alert = false
//                                                mainViewModel.wasteAlert.value = false
//
//                                            }
//
//                                        }
//                                    }
//                                    mainViewModel.WasteLiquorDataLiveList.notifyChange()
//
//                                }
//                            }
//
//
//                        }
//                        "Oxygen" -> {
//                            try {
//                                val tempval = littleEndianConversion(
//                                    receiveParser.mProtocol.slice(7..8).toByteArray()
//                                )
//                                Log.d(
//                                    "산소",
//                                    "protocol : ${HexDump.dumpHexString(receiveParser.mProtocol)}"
//                                )
//                                val oxygen = tempval.toInt() / 100
//                                for (i in mainViewModel.OxygenDataLiveList.value!!) {
//                                    if (i.id == receiveParser.mProtocol.get(3).toInt()) {
//                                        i.setValue = oxygen
//                                        val port = listOf<Int>(4, i.id, i.port)
//                                        val portInfo = PortInfo(4.toByte(), i.id.toByte(), i.port)
//                                        if (i.setMinValue > oxygen) {
//                                            i.isAlert = true
//                                            mainViewModel.oxyenAlert.value = true
//                                            if (!mainViewModel.mExPortInfo[port]!!.alert_1) {
//                                                mainViewModel.alertInfo.add(
//                                                    SetAlertData(
//                                                        portInfo.latest_time,
//                                                        portInfo.model.toInt(),
//                                                        portInfo.id.toInt(),
//                                                        "산소농도 하한 값",
//                                                        portInfo.port
//                                                    )
//                                                )
////                                            mainViewModel.oxyenAlert.value = true
//                                                mainViewModel.mExPortInfo[port]?.alert_1 = true
//                                            }
//                                        } else {
//                                            mainViewModel.mExPortInfo[port] = portInfo
//                                            mainViewModel.mPortInfo[port] = portInfo
//                                            mainViewModel.mPortInfo[port]?.alert_1 = false
//                                            mainViewModel.mExPortInfo[port]?.alert_1 = false
//                                            mainViewModel.oxyenAlert.value = false
//                                        }
//
//                                        mainViewModel.OxygenDataLiveList.notifyChange()
//                                    }
//                                }
//                            } catch (ex: Exception) {
//
//                            }
//
//                        }
//                        "Steamer" -> {
//                            Log.d(
//                                "태그",
//                                "Steamer // id:${receiveParser.mProtocol.get(3)} model:${
//                                    receiveParser.mProtocol.get(2)
//                                }"
//                            )
//
//                            val pin1_data = littleEndianConversion(
//                                receiveParser.mProtocol.slice(7..8).toByteArray()
//                            )
//                            val pin2_data = littleEndianConversion(
//                                receiveParser.mProtocol.slice(9..10).toByteArray()
//                            )
//                            val pin3_data = littleEndianConversion(
//                                receiveParser.mProtocol.slice(11..12).toByteArray()
//                            )
//                            val pin4_data = littleEndianConversion(
//                                receiveParser.mProtocol.slice(13..14).toByteArray()
//                            )
////                            for (i in mainViewModel.SteamerDataLiveList.value!!) {
////                                if (i.id == receiveParser.mProtocol.get(3).toInt()) {
////                                    val port = listOf<Int>(5, i.id, i.port)
////                                    val portInfo = PortInfo(5.toByte(), i.id.toByte(), i.port)
////                                    when (i.port) {
////                                        1 -> {
////                                            i.isTemp = pin1_data / 33
////                                            i.unit
////                                            if (pin3_data > 1000) {
////                                                i.isAlertLow = true
////                                                mainViewModel.steamerAlert.value = true
////
////                                                if (!mainViewModel.mExPortInfo[port]!!.alert_1) {
////                                                    mainViewModel.alertInfo.add(
////                                                        SetAlertData(
////                                                            portInfo.latest_time,
////                                                            portInfo.model.toInt(),
////                                                            portInfo.id.toInt(),
////                                                            "수위 레벨 하한 값",
////                                                            portInfo.port
////                                                        )
////                                                    )
//////                                                mainViewModel.steamerAlert.value = true
////                                                    mainViewModel.mExPortInfo[port]?.alert_1 = true
////                                                }
////                                            } else {
////                                                i.isAlertLow = false
//////                                            mainViewModel.mExPortInfo[port] = portInfo
//////                                            mainViewModel.mPortInfo[port] = portInfo
////                                                mainViewModel.mPortInfo[port]?.alert_1 = false
////                                                mainViewModel.mExPortInfo[port]?.alert_1 = false
////                                                mainViewModel.steamerAlert.value = false
////                                            }
////
////                                            if (i.isTempMin > i.isTemp) {
////                                                mainViewModel.steamerAlert.value = true
////                                                i.isAlertTemp = true
////                                                if (!mainViewModel.mExPortInfo[port]!!.alert_2) {
////                                                    mainViewModel.alertInfo.add(
////                                                        SetAlertData(
////                                                            portInfo.latest_time,
////                                                            portInfo.model.toInt(),
////                                                            portInfo.id.toInt(),
////                                                            "온도 레벨 하한 값",
////                                                            portInfo.port
////                                                        )
////                                                    )
//////                                                mainViewModel.steamerAlert.value = true
////                                                    mainViewModel.mExPortInfo[port]?.alert_2 = true
////                                                }
////                                            } else {
////                                                i.isAlertTemp = false
//////                                            mainViewModel.mExPortInfo[port] = portInfo
//////                                            mainViewModel.mPortInfo[port] = portInfo
////                                                mainViewModel.mPortInfo[port]?.alert_2 = false
////                                                mainViewModel.mExPortInfo[port]?.alert_2 = false
////                                                mainViewModel.steamerAlert.value = false
////                                            }
////
////                                        }
////                                        2 -> {
////                                            i.isTemp = pin2_data / 33
////                                            i.unit
////
////                                            if (pin4_data > 1000) {
////                                                i.isAlertLow = true
////                                                mainViewModel.steamerAlert.value = true
////                                                if (!mainViewModel.mExPortInfo[port]!!.alert_1) {
////                                                    mainViewModel.alertInfo.add(
////                                                        SetAlertData(
////                                                            portInfo.latest_time,
////                                                            portInfo.model.toInt(),
////                                                            portInfo.id.toInt(),
////                                                            "수위 레벨 하한 값",
////                                                            portInfo.port
////                                                        )
////                                                    )
//////                                                mainViewModel.steamerAlert.value = true
////                                                    mainViewModel.mExPortInfo[port]?.alert_1 = true
////                                                }
////                                            } else {
////                                                i.isAlertLow = false
//////                                            mainViewModel.mExPortInfo[port] = portInfo
//////                                            mainViewModel.mPortInfo[port] = portInfo
////                                                mainViewModel.mPortInfo[port]?.alert_1 = false
////                                                mainViewModel.mExPortInfo[port]?.alert_1 = false
////                                                mainViewModel.steamerAlert.value = false
////                                            }
////
////                                            if (i.isTempMin > i.isTemp) {
////                                                i.isAlertTemp = true
////                                                mainViewModel.steamerAlert.value = true
////                                                if (!mainViewModel.mExPortInfo[port]!!.alert_2) {
////                                                    mainViewModel.alertInfo.add(
////                                                        SetAlertData(
////                                                            portInfo.latest_time,
////                                                            portInfo.model.toInt(),
////                                                            portInfo.id.toInt(),
////                                                            "온도 레벨 상한 값",
////                                                            portInfo.port
////                                                        )
////                                                    )
//////                                                mainViewModel.steamerAlert.value = true
////                                                    mainViewModel.mExPortInfo[port]?.alert_2 = true
////                                                }
////                                            } else {
////                                                i.isAlertTemp = false
//////                                            mainViewModel.mExPortInfo[port] = portInfo
//////                                            mainViewModel.mPortInfo[port] = portInfo
////                                                mainViewModel.mPortInfo[port]?.alert_2 = false
////                                                mainViewModel.mExPortInfo[port]?.alert_2 = false
////                                                mainViewModel.oxyenAlert.value = false
////                                            }
////                                        }
////
////                                    }
////                                    mainViewModel.SteamerDataLiveList.notifyChange()
////
////                                }
////                            }
//                        }
//                        "Temp_Hum" -> {
//
//                        }
//                    }

                }
            }


            super.handleMessage(msg)
        }
    }


    override fun onResume() {
        hideNavigationBar()
        bindSerialService()
        GlobalUiTimer.getInstance().activity = this
        startModbusService(SaminModbusService::class.java, svcConnection, null)
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        GlobalUiTimer.getInstance().activity = this
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

    fun littleEndianConversion(bytes: ByteArray): Int {
        var result = 0
        for (i in bytes.indices) {
            result = result or (bytes[i].toUByte().toInt() shl 8 * i)
        }
        return result
    }

    lateinit var callbackThread: Thread
    var isSending = false
    fun callFeedback() {
        callbackThread = Thread {
            while (isSending) {
                try {
                    for ((model, ids) in mainViewModel.modelMap) {
                        for (index in ids.indices) {
                            val id = ids.get(index)
                            when (model) {
                                "GasDock" -> {
                                    val protocol = SaminProtocol()
                                    protocol.feedBack(MainViewModel.GasDockStorage, id)
                                    serialService?.sendData(protocol.mProtocol)
                                    Thread.sleep(25)
                                }
                                "GasRoom" -> {
                                    val protocol = SaminProtocol()
                                    protocol.feedBack(MainViewModel.GasRoom, id)
                                    serialService?.sendData(protocol.mProtocol)
                                    Thread.sleep(25)
                                }
                                "WasteLiquor" -> {
                                    val protocol = SaminProtocol()
                                    protocol.feedBack(MainViewModel.WasteLiquor, id)
                                    serialService?.sendData(protocol.mProtocol)
                                    Thread.sleep(25)
                                }
                                "Oxygen" -> {
                                    val protocol = SaminProtocol()
                                    protocol.feedBack(MainViewModel.Oxygen, id)
                                    serialService?.sendData(protocol.mProtocol)
                                    Thread.sleep(25)
                                    //정상 17ms 비정상 35ms
                                }
                                "Steamer" -> {
                                    val protocol = SaminProtocol()
                                    protocol.feedBack(MainViewModel.Steamer, id)
                                    serialService?.sendData(protocol.mProtocol)
                                    Thread.sleep(25)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                }
            }
        }
        callbackThread.start()

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
}