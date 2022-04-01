package com.coai.samin_total

import android.app.Activity
import android.app.PendingIntent.getActivity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import androidx.lifecycle.ViewModelProvider
import com.coai.samin_total.Dialog.AlertDialogFragment
import com.coai.samin_total.Dialog.ScanDialogFragment
import com.coai.samin_total.GasDock.GasDockMainFragment
import com.coai.samin_total.GasDock.GasStorageSettingFragment
import com.coai.samin_total.GasRoom.GasRoomMainFragment
import com.coai.samin_total.GasRoom.GasRoomSettingFragment
import com.coai.samin_total.Logic.CurrentSensorInfo
import com.coai.samin_total.Logic.SaminProtocol
import com.coai.samin_total.Oxygen.OxygenMainFragment
import com.coai.samin_total.Oxygen.OxygenSettingFragment
import com.coai.samin_total.Service.HexDump
import com.coai.samin_total.Service.SerialService
import com.coai.samin_total.Steamer.SetSteamerViewData
import com.coai.samin_total.Steamer.SteamerMainFragment
import com.coai.samin_total.Steamer.SteamerSettingFragment
import com.coai.samin_total.WasteLiquor.WasteLiquorMainFragment
import com.coai.samin_total.WasteLiquor.WasteWaterSettingFragment
import com.coai.samin_total.databinding.ActivityMainBinding
import com.coai.uikit.GlobalUiTimer

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        setFragment()

//        mainViewModel.model_ID_Data.observe(this, Observer {
//            Log.d("태그", "model_ID_Data: $it")
//        })

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
        @RequiresApi(Build.VERSION_CODES.O)
        override fun handleMessage(msg: Message) {
            Log.d(mainTAG, "datahandler : ${HexDump.dumpHexString(msg.obj as ByteArray)}")
            val receiveParser = SaminProtocol()
            receiveParser.parse(msg.obj as ByteArray)

            if (receiveParser.packetName == "CheckProductPing") {
//                val aqInfo = receiveParser.mProtocol.slice(2..3).plus(receiveParser.mProtocol.get(6)).toByteArray()
//                val currentSensorInfo = CurrentSensorInfo(aqInfo)
//                mainViewModel.latestSensorInfo.put(aqInfo, currentSensorInfo)

                val model = receiveParser.modelName
                val ids = receiveParser.mProtocol.get(3)
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
                for ((key, value) in mainViewModel.modelMap) {
                    Log.d(
                        "체크",
                        "modelMap(key): ${key} // modelMap(value):${
                            HexDump.dumpHexString(
                                mainViewModel.modelMap[key]
                            )
                        } "
                    )

                }
            } else if (receiveParser.packetName == "RequestFeedBackPing") {
                val aqInfo = receiveParser.mProtocol.slice(2..3)
                val sensorData = receiveParser.mProtocol.slice(7..14).toByteArray()
                val currentSensorInfo = CurrentSensorInfo(aqInfo, sensorData)
//                mainViewModel.latestSensorInfo.put(aqInfo, currentSensorInfo)
                mainViewModel.latestSensorInfo[aqInfo] = currentSensorInfo

                when (receiveParser.modelName) {
                    "GasDock" -> {
                        val pin1_data = littleEndianConversion(
                            receiveParser.mProtocol.slice(7..8).toByteArray()
                        ).toFloat()
                        val pin2_data = littleEndianConversion(
                            receiveParser.mProtocol.slice(9..10).toByteArray()
                        ).toFloat()
                        val pin3_data = littleEndianConversion(
                            receiveParser.mProtocol.slice(11..12).toByteArray()
                        ).toFloat()
                        val pin4_data = littleEndianConversion(
                            receiveParser.mProtocol.slice(13..14).toByteArray()
                        ).toFloat()
                        var sensor_1: Float
                        var sensor_2: Float
                        var sensor_3: Float
                        var sensor_4: Float

                        for (i in mainViewModel.GasStorageDataLiveList.value!!) {
                            if (i.sensorType == "Sensts 142PSI") {
                                sensor_1 = calcPSI142(pin1_data, i.rewardValue, i.zeroPoint)
                                sensor_2 = calcPSI142(pin2_data, i.rewardValue, i.zeroPoint)
                                sensor_3 = calcPSI142(pin3_data, i.rewardValue, i.zeroPoint)
                                sensor_4 = calcPSI142(pin4_data, i.rewardValue, i.zeroPoint)

                            } else if (i.sensorType == "Sensts 2000PSI") {
                                sensor_1 = calcPSI2000(pin1_data, i.rewardValue, i.zeroPoint)
                                sensor_2 = calcPSI2000(pin2_data, i.rewardValue, i.zeroPoint)
                                sensor_3 = calcPSI2000(pin3_data, i.rewardValue, i.zeroPoint)
                                sensor_4 = calcPSI2000(pin4_data, i.rewardValue, i.zeroPoint)
                            } else {
                                sensor_1 = calcSensor(
                                    pin1_data,
                                    i.pressure_Max!!,
                                    i.rewardValue,
                                    i.zeroPoint
                                )
                                sensor_2 = calcSensor(
                                    pin2_data,
                                    i.pressure_Max!!,
                                    i.rewardValue,
                                    i.zeroPoint
                                )
                                sensor_3 = calcSensor(
                                    pin3_data,
                                    i.pressure_Max!!,
                                    i.rewardValue,
                                    i.zeroPoint
                                )
                                sensor_4 = calcSensor(
                                    pin4_data,
                                    i.pressure_Max!!,
                                    i.rewardValue,
                                    i.zeroPoint
                                )
                            }

                            //받은 데이터 아이디와 데이터리스트의 아디가 동일한 경우
                            if (i.id == receiveParser.mProtocol.get(3).toInt()) {
                                // 데이터 리스트의 데이터의 뷰타입이 듀얼 또는 오토체인처일 경우
                                if (i.ViewType == 1 || i.ViewType == 2) {
                                    //2,4port 삭제되서 날라옴
                                    if (i.port == 1) {
                                        i.pressureLeft = sensor_1
                                        i.pressureRight = sensor_2
                                    } else {
                                        i.pressureLeft = sensor_3
                                        i.pressureRight = sensor_4
                                    }

                                } else {
                                    when (i.port) {
                                        1 -> {
                                            i.pressure = sensor_1
                                        }
                                        2 -> {
                                            i.pressure = sensor_2
                                        }
                                        3 -> {
                                            i.pressure = sensor_3
                                        }
                                        4 -> {
                                            i.pressure = sensor_4
                                        }
                                    }

                                }
                                mainViewModel.GasStorageDataLiveList.notifyChange()
                            }
                        }

                    }
                    "GasRoom" -> {
                        val pin1_data = littleEndianConversion(
                            receiveParser.mProtocol.slice(7..8).toByteArray()
                        ).toFloat()
                        val pin2_data = littleEndianConversion(
                            receiveParser.mProtocol.slice(9..10).toByteArray()
                        ).toFloat()
                        val pin3_data = littleEndianConversion(
                            receiveParser.mProtocol.slice(11..12).toByteArray()
                        ).toFloat()
                        val pin4_data = littleEndianConversion(
                            receiveParser.mProtocol.slice(13..14).toByteArray()
                        ).toFloat()
                        var sensor_1: Float
                        var sensor_2: Float
                        var sensor_3: Float
                        var sensor_4: Float
                        for (i in mainViewModel.GasRoomDataLiveList.value!!) {
                            if (i.sensorType == "Sensts 142PSI") {
                                sensor_1 = calcPSI142(pin1_data, i.rewardValue, i.zeroPoint)
                                sensor_2 = calcPSI142(pin2_data, i.rewardValue, i.zeroPoint)
                                sensor_3 = calcPSI142(pin3_data, i.rewardValue, i.zeroPoint)
                                sensor_4 = calcPSI142(pin4_data, i.rewardValue, i.zeroPoint)

                            } else if (i.sensorType == "Sensts 2000PSI") {
                                sensor_1 = calcPSI2000(pin1_data, i.rewardValue, i.zeroPoint)
                                sensor_2 = calcPSI2000(pin2_data, i.rewardValue, i.zeroPoint)
                                sensor_3 = calcPSI2000(pin3_data, i.rewardValue, i.zeroPoint)
                                sensor_4 = calcPSI2000(pin4_data, i.rewardValue, i.zeroPoint)
                            } else {
                                sensor_1 = calcSensor(
                                    pin1_data,
                                    i.pressure_Max!!,
                                    i.rewardValue,
                                    i.zeroPoint
                                )
                                sensor_2 = calcSensor(
                                    pin2_data,
                                    i.pressure_Max!!,
                                    i.rewardValue,
                                    i.zeroPoint
                                )
                                sensor_3 = calcSensor(
                                    pin3_data,
                                    i.pressure_Max!!,
                                    i.rewardValue,
                                    i.zeroPoint
                                )
                                sensor_4 = calcSensor(
                                    pin4_data,
                                    i.pressure_Max!!,
                                    i.rewardValue,
                                    i.zeroPoint
                                )
                            }

                            if (i.id == receiveParser.mProtocol.get(3).toInt()) {
                                when (i.port) {
                                    1 -> i.pressure = sensor_1
                                    2 -> i.pressure = sensor_2
                                    3 -> i.pressure = sensor_3
                                    4 -> i.pressure = sensor_4
                                }
                                mainViewModel.GasRoomDataLiveList.notifyChange()
                            }
                        }
                    }
                    "WasteLiquor" -> {
                        Log.d(
                            "태그",
                            "WasteLiquor // id:${receiveParser.mProtocol.get(3)} model:${
                                receiveParser.mProtocol.get(2)
                            }"
                        )
                        mainViewModel.model_ID_Data.value?.put(
                            "WasteLiquor",
                            receiveParser.mProtocol.get(3)
                        )

                        val pin1_data = littleEndianConversion(
                            receiveParser.mProtocol.slice(7..8).toByteArray()
                        )
                        val pin2_data = littleEndianConversion(
                            receiveParser.mProtocol.slice(9..10).toByteArray()
                        )
                        val pin3_data = littleEndianConversion(
                            receiveParser.mProtocol.slice(11..12).toByteArray()
                        )
                        val pin4_data = littleEndianConversion(
                            receiveParser.mProtocol.slice(13..14).toByteArray()
                        )

                        for (i in mainViewModel.WasteLiquorDataLiveList.value!!) {
                            if (i.id == receiveParser.mProtocol.get(3).toInt()) {
                                when (i.port) {
                                    1 -> {
                                        i.isAlert = if (pin1_data == 0) true else false

                                    }
                                    2 -> {
                                        i.isAlert = if (pin2_data == 0) true else false

                                    }
                                    3 -> {
                                        i.isAlert = if (pin3_data == 0) true else false

                                    }
                                    4 -> {
                                        i.isAlert = if (pin4_data == 0) true else false

                                    }
                                }
                                mainViewModel.WasteLiquorDataLiveList.notifyChange()

                            }
                        }


                    }
                    "Oxygen" -> {
                        try {

                            val tempval = littleEndianConversion(
                                receiveParser.mProtocol.slice(7..8).toByteArray()
                            ).toUShort()
                            Log.d(
                                "산소",
                                "protocol : ${HexDump.dumpHexString(receiveParser.mProtocol)}"
                            )
                            val oxygen = tempval.toInt() / 100

                            //todo  에러 데이터 포함시킬것

//                            for (i in mainViewModel.OxygenDataLiveList.value!!) {
//                                Log.d(
//                                    "cpcp",
//                                    "id:${i.id} //minvalue:${i.setMinValue}// setvalue:${i.setValue}"
//                                )
//                                if (i.id == receiveParser.mProtocol.get(3).toInt()) {
//                                    i.setValue = oxygen
//                                }
//
//
//                            }

                            for (i in mainViewModel.OxygenDataLiveList.value!!) {
                                if (i.id == receiveParser.mProtocol.get(3).toInt()) {
                                    i.setValue = oxygen
                                    mainViewModel.OxygenDataLiveList.notifyChange()
                                }
                            }
                        } catch (ex: Exception) {

                        }

                    }

                    "Steamer" -> {
                        Log.d(
                            "태그",
                            "Steamer // id:${receiveParser.mProtocol.get(3)} model:${
                                receiveParser.mProtocol.get(2)
                            }"
                        )

                        val pin1_data = littleEndianConversion(
                            receiveParser.mProtocol.slice(7..8).toByteArray()
                        )
                        val pin2_data = littleEndianConversion(
                            receiveParser.mProtocol.slice(9..10).toByteArray()
                        )
                        val pin3_data = littleEndianConversion(
                            receiveParser.mProtocol.slice(11..12).toByteArray()
                        )
                        val pin4_data = littleEndianConversion(
                            receiveParser.mProtocol.slice(13..14).toByteArray()
                        )

//                        mainViewModel.TempValue.value = pin1_data / 33
//                        Log.d(
//                            "태그", "pin1_data : ${pin1_data} " +
//                                    "pin2_data:${pin2_data}" + "pin3_data:${pin3_data}" + "pin4_data:${pin4_data}"
//                        )
//
//                        mainViewModel.WaterGauge.value = pin3_data < 1000
//
//                        mainViewModel.SteamerData.value =
//                            SetSteamerViewData(pin3_data < 1000, isTemp = pin1_data / 33)
//
//                        Log.d(
//                            "태그", "TempValue : ${mainViewModel.TempValue.value}" +
//                                    "WaterGauge : ${mainViewModel.WaterGauge.value}"
//                        )

                        for (i in mainViewModel.SteamerDataLiveList.value!!) {
                            if (i.id == receiveParser.mProtocol.get(3).toInt()) {
                                when (i.port) {
                                    1 -> {
                                        i.isTemp = pin1_data / 33
                                        i.isAlertLow = if (pin3_data < 1000) true else false
                                        i.unit

                                    }
                                    2 -> {
                                        i.isTemp = pin2_data / 33
                                        i.isAlertLow = if (pin4_data < 1000) true else false
                                        i.unit
                                    }

                                }
                                mainViewModel.SteamerDataLiveList.notifyChange()

                            }
                        }
                    }
                    "Temp_Hum" -> {

                    }
                }
            }


            super.handleMessage(msg)
        }
    }


    override fun onResume() {
        hideNavigationBar()
        bindSerialService()
        GlobalUiTimer.getInstance().activity = this
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

    private lateinit var callbackThread: Thread
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

    private fun calcSensor(
        analog: Float,
        maxvalue: Float,
        rewardvalue: Float,
        zeroPoint: Float
    ): Float {
        if (analog < 204.8f) {
            return 0f
        } else {
            return (rewardvalue * ((analog - 204.8f) * (maxvalue / (1024f - 204.8f))) + zeroPoint)
        }
    }

    private fun calcPSI142(analog: Float, rewardvalue: Float, zeroPoint: Float): Float {
        return (rewardvalue * (analog * 0.1734 - 17.842)).toFloat() + zeroPoint
    }

    private fun calcPSI2000(analog: Float, rewardvalue: Float, zeroPoint: Float): Float {
        return (rewardvalue * (analog * 2.4414 - 249.66)).toFloat() + zeroPoint
    }

    private fun checkCommunication(){
        mainViewModel.latestSensorInfo.forEach {
            val currenttime = System.currentTimeMillis()
            if (it.value.getLatestTime().toInt() + 5000 > currenttime){

            }
        }
    }
}