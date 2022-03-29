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
import androidx.core.content.getSystemService
import androidx.lifecycle.ViewModelProvider
import com.coai.samin_total.Dialog.AlertDialogFragment
import com.coai.samin_total.Dialog.ScanDialogFragment
import com.coai.samin_total.GasDock.GasDockMainFragment
import com.coai.samin_total.GasDock.GasStorageSettingFragment
import com.coai.samin_total.GasRoom.GasRoomMainFragment
import com.coai.samin_total.Logic.SaminProtocol
import com.coai.samin_total.Oxygen.OxygenMainFragment
import com.coai.samin_total.Service.HexDump
import com.coai.samin_total.Service.SerialService
import com.coai.samin_total.Steamer.SetSteamerViewData
import com.coai.samin_total.Steamer.SteamerMainFragment
import com.coai.samin_total.WasteLiquor.WasteLiquorMainFragment
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
        override fun handleMessage(msg: Message) {
            Log.d(mainTAG, "datahandler : ${HexDump.dumpHexString(msg.obj as ByteArray)}")
            val receiveParser = SaminProtocol()
            receiveParser.parse(msg.obj as ByteArray)
            if (receiveParser.packetName == "CheckProductPing") {
                val model = receiveParser.modelName
                val ids = receiveParser.mProtocol.get(3)
                Log.d(
                    "체크1",
                    "model: ${model} // id:${ids}"
                )
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
                        Log.d(
                            "태그", "pin1_data : ${pin1_data} " +
                                    "pin2_data:${pin2_data}" + "pin3_data:${pin3_data}" + "pin4_data:${pin4_data}"
                        )

                        val rewardvalue: Float = 1f
                        var sensor_Data_1 = 0f
                        if (pin2_data < 204.8f) {
                            mainViewModel.GasStorageData.value = 0f
                        } else {
                            sensor_Data_1 =
                                rewardvalue * ((pin2_data - 204.8f) * (2320.6f / (1024f - 204.8f)))
                            mainViewModel.GasStorageData.value = sensor_Data_1
                        }
                        Log.d("태그", "sensor_Data_1:${sensor_Data_1} ")


                        for (i in mainViewModel.GasStorageDataLiveList.value!!) {
                            if (i.id == receiveParser.mProtocol.get(3).toInt()) {
//                                if(i.ViewType == )

                                when (i.port) {
                                    1 -> {
                                        i.pressure = pin1_data
                                        Log.d(
                                            "체크",
                                            " id : ${i.id} // sensor : ${i.pressure} //sensor1 : ${i.pressure} "
                                        )
                                    }
                                    2 -> {
                                        i.pressure = pin2_data
                                        Log.d(
                                            "체크",
                                            " id : ${i.id} // sensor : ${i.pressure} //sensor2 : ${i.pressure} "
                                        )
                                    }
                                    3 -> {
                                        i.pressure = pin3_data
                                        Log.d(
                                            "체크",
                                            " id : ${i.id} // sensor : ${i.pressure} //sensor3 : ${i.pressure} "
                                        )
                                    }
                                    4 -> {
                                        i.pressure = pin4_data
                                        Log.d(
                                            "체크",
                                            " id : ${i.id} // sensor : ${i.pressure} //sensor4 : ${i.pressure} "
                                        )
                                    }
                                }
                                mainViewModel.GasStorageDataLiveList.notifyChange()

                            }
                        }

                    }
                    "GasRoom" -> {
                        Log.d(
                            "태그",
                            "GasRoom // id:${receiveParser.mProtocol.get(3)} model:${
                                receiveParser.mProtocol.get(2)
                            }"
                        )
                        //WIKAI 16BAR

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

                        val rewardvalue: Float = 1f
                        var sensor_Data_1 = 0f
                        if (pin2_data < 204.8f) {
                            mainViewModel.GasRoomData.value = 0f
                        } else {
                            sensor_Data_1 =
                                rewardvalue * ((pin2_data - 204.8f) * (232.06f / (1024f - 204.8f)))
                            mainViewModel.GasRoomData.value = sensor_Data_1
                        }

                        Log.d("태그", "sensor_Data_1:${sensor_Data_1} ")
                        for (i in mainViewModel.GasRoomDataLiveList.value!!) {
                            if (i.id == receiveParser.mProtocol.get(3).toInt()) {
                                when (i.port) {
                                    1 -> {
                                        i.pressure = pin1_data
                                        Log.d(
                                            "체크",
                                            " id : ${i.id} // sensor : ${i.pressure} //sensor1 : ${i.pressure} "
                                        )
                                    }
                                    2 -> {
                                        i.pressure = pin2_data
                                        Log.d(
                                            "체크",
                                            " id : ${i.id} // sensor : ${i.pressure} //sensor2 : ${i.pressure} "
                                        )
                                    }
                                    3 -> {
                                        i.pressure = pin3_data
                                        Log.d(
                                            "체크",
                                            " id : ${i.id} // sensor : ${i.pressure} //sensor3 : ${i.pressure} "
                                        )
                                    }
                                    4 -> {
                                        i.pressure = pin4_data
                                        Log.d(
                                            "체크",
                                            " id : ${i.id} // sensor : ${i.pressure} //sensor4 : ${i.pressure} "
                                        )
                                    }
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

                                    }
                                    2 -> {
                                        i.isTemp = pin2_data / 33
                                        i.isAlertLow = if (pin4_data < 1000) true else false
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

    fun callFeedback() {
        callbackThread = Thread {
            while (true) {
                try {
                    for ((model, ids) in mainViewModel.modelMap) {
                        for (index in ids.indices) {
                            val id = ids.get(index)
                            when (model) {
                                "GasDock" -> {
                                    val protocol = SaminProtocol()
                                    protocol.feedBack(MainViewModel.GasDockStorage, id)
                                    serialService?.sendData(protocol.mProtocol)
                                    Thread.sleep(20)
                                }
                                "GasRoom" -> {
                                    val protocol = SaminProtocol()
                                    protocol.feedBack(MainViewModel.GasRoom, id)
                                    serialService?.sendData(protocol.mProtocol)
                                    Thread.sleep(20)
                                }
                                "WasteLiquor" -> {
                                    val protocol = SaminProtocol()
                                    protocol.feedBack(MainViewModel.WasteLiquor, id)
                                    serialService?.sendData(protocol.mProtocol)
                                    Thread.sleep(20)
                                }
                                "Oxygen" -> {
                                    val protocol = SaminProtocol()
                                    protocol.feedBack(MainViewModel.Oxygen, id)
                                    serialService?.sendData(protocol.mProtocol)
                                    Thread.sleep(20)
                                    //정상 17ms 비정상 35ms
                                }
                                "Steamer" -> {
                                    val protocol = SaminProtocol()
                                    protocol.feedBack(MainViewModel.Steamer, id)
                                    serialService?.sendData(protocol.mProtocol)
                                    Thread.sleep(20)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                }
            }
        }
//        callbackThread = Thread {
//            while (true) {
//                try {
//                    for ((model, ids) in mainViewModel.modelMap) {
//
//                        if (model == "Oxygen"){
//                            for (index in ids.indices){
//                                val id = ids.get(index)
//                                val protocol = SaminProtocol()
//                                protocol.feedBack(MainViewModel.Oxygen, id)
//                                serialService?.sendData(protocol.mProtocol)
//                                Thread.sleep(200)
//                            }
//
//                        }else{
//                            for (index in ids.indices){
//                                val id = ids.get(index)
//                                when(model){
//                                    "GasDock" -> {
//                                        val protocol = SaminProtocol()
//                                        protocol.feedBack(MainViewModel.GasDockStorage, id)
//                                        serialService?.sendData(protocol.mProtocol)
//                                        Thread.sleep(50)
//                                    }
//                                    "GasRoom" -> {
//                                        val protocol = SaminProtocol()
//                                        protocol.feedBack(MainViewModel.GasRoom, id)
//                                        serialService?.sendData(protocol.mProtocol)
//                                        Thread.sleep(50)
//                                    }
//                                    "WasteLiquor" -> {
//                                        val protocol = SaminProtocol()
//                                        protocol.feedBack(MainViewModel.WasteLiquor, id)
//                                        serialService?.sendData(protocol.mProtocol)
//                                        Thread.sleep(50)
//                                    }
//                                    "Oxygen" -> {
//                                        val protocol = SaminProtocol()
//                                        protocol.feedBack(MainViewModel.Oxygen, id)
//                                        serialService?.sendData(protocol.mProtocol)
//                                        Thread.sleep(200)
//                                    }
//                                    "Steamer" -> {
//                                        val protocol = SaminProtocol()
//                                        protocol.feedBack(MainViewModel.Steamer, id)
//                                        serialService?.sendData(protocol.mProtocol)
//                                        Thread.sleep(50)
//                                    }
//                                }
//                            }
//
//                        }
//                    }
//                } catch (e: Exception) {
//                }
//            }
//        }
        callbackThread.start()
    }



}