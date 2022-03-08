package com.coai.samin_total

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.coai.samin_total.Dialog.AlertDialogFragment
import com.coai.samin_total.Dialog.ScanDialogFragment
import com.coai.samin_total.GasDock.GasDockMainFragment
import com.coai.samin_total.GasDock.GasStorageSettingFragment
import com.coai.samin_total.GasRoom.GasRoomMainFragment
import com.coai.samin_total.Logic.SaminProtocol
import com.coai.samin_total.Oxygen.OxygenMainFragment
import com.coai.samin_total.Oxygen.OxygenViewModel
import com.coai.samin_total.Service.HexDump
import com.coai.samin_total.Service.SerialService
import com.coai.samin_total.Steamer.SetSteamerViewData
import com.coai.samin_total.Steamer.SteamerMainFragment
import com.coai.samin_total.WasteLiquor.WasteLiquorMainFragment
import com.coai.samin_total.databinding.ActivityMainBinding
import com.coai.uikit.GlobalUiTimer
import java.text.DecimalFormat

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
    private lateinit var oxygenViewModel: OxygenViewModel
    private lateinit var mainViewModel: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        oxygenViewModel = ViewModelProvider(this).get(OxygenViewModel::class.java)
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

    @ExperimentalUnsignedTypes
    val datahandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            Log.d(mainTAG, "datahandler : ${HexDump.dumpHexString(msg.obj as ByteArray)}")
            val receiveParser = SaminProtocol()
            receiveParser.parse(msg.obj as ByteArray)
            when (receiveParser.modelName) {
                "GasDock" -> {
                    Log.d(
                        "태그",
                        "GasDock // id:${receiveParser.mProtocol.get(3)} model:${
                            receiveParser.mProtocol.get(2)
                        }"
                    )
                    mainViewModel.model_ID_Data.value?.put(
                        "GasDock",
                        receiveParser.mProtocol.get(3)
                    )

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

                    mainViewModel.LevelValue.value = pin1_data

                }
                "Oxygen" -> {

                    try {
                        oxygenViewModel.OxygenSensorID.value =
                            receiveParser.mProtocol.get(3).toInt()
                        oxygenViewModel.OxygenSensorIDs.value?.add(
                            receiveParser.mProtocol.get(3).toInt()
                        )
                        val tempval = littleEndianConversion(
                            receiveParser.mProtocol.slice(7..8).toByteArray()
                        ).toUShort()

                        val oxygen = tempval.toInt() / 100

                        oxygenViewModel.OxygenValue.value = oxygen
                        //todo  에러 데이터 포함시킬것

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

                    mainViewModel.TempValue.value = pin1_data / 33
                    Log.d(
                        "태그", "pin1_data : ${pin1_data} " +
                                "pin2_data:${pin2_data}" + "pin3_data:${pin3_data}" + "pin4_data:${pin4_data}"
                    )

                    mainViewModel.WaterGauge.value = pin3_data < 1000

                    mainViewModel.SteamerData.value =
                        SetSteamerViewData(pin3_data < 1000, isTemp = pin1_data / 33)

                    Log.d(
                        "태그", "TempValue : ${mainViewModel.TempValue.value}" +
                                "WaterGauge : ${mainViewModel.WaterGauge.value}"
                    )
                }
                "Temp_Hum" -> {

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

}