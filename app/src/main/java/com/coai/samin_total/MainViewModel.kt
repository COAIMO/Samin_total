package com.coai.samin_total

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coai.samin_total.Dialog.SetAlertData
import com.coai.samin_total.GasDock.SetGasStorageViewData
import com.coai.samin_total.GasRoom.SetGasRoomViewData
import com.coai.samin_total.Logic.CurrentSensorInfo
import com.coai.samin_total.Logic.MutableListLiveData
import com.coai.samin_total.Oxygen.SetOxygenViewData
import com.coai.samin_total.Steamer.SetSteamerViewData
import com.coai.samin_total.Steamer.SteamerSettingFragment
import com.coai.samin_total.WasteLiquor.SetWasteLiquorViewData

class MainViewModel : ViewModel() {
    companion object {
        const val MAINFRAGMENT = 0
        const val MAINSETTINGFRAGMENT = 1
        const val ALERTDIALOGFRAGMENT = 2
        const val SCANDIALOGFRAGMENT = 3
        const val ALERTLOGFRGAMENT = 4
        const val ADMINFRAGMENT = 5
        const val AQSETTINGFRAGMENT = 6
        const val CONTROLFRAGMENT = 7
        const val VERSIONFRAGMENT = 8
        const val PASSWORDFRAGMENT = 9
        const val GASDOCKMAINFRAGMENT = 10
        const val GASROOMMAINFRAGMENT = 11
        const val WASTELIQUORMAINFRAGMENT = 12
        const val OXYGENMAINFRAGMENT = 13
        const val STEAMERMAINFRAGMENT = 14
        const val GASSTORAGESETTINGFRAGMENT = 15
        const val LAYOUTFRAGMENT = 16
        const val CONNECTTESTFRAGEMNT = 17
        const val GASROOMSETTINGFRAGMENT = 18
        const val OXYGENSETTINGFRAGMENT = 19
        const val STEAMERSETTINGFRAGMENT = 20
        const val WASTELIQUORSETTINGFRAGMENT =21

        const val GasDockStorage = 1.toByte()
        const val GasRoom = 2.toByte()
        const val WasteLiquor = 3.toByte()
        const val Oxygen = 4.toByte()
        const val Steamer = 5.toByte()
        const val Temp_Hum = 6.toByte()

    }

    //내부에서 설정하는 자료형은 뮤터블로
    //변경가능하도록 설정
    private val _model_ID_Data = MutableLiveData<HashMap<String, Byte>>()

    val GasStorageDataLiveList = MutableListLiveData<SetGasStorageViewData>()
    val GasRoomDataLiveList = MutableListLiveData<SetGasRoomViewData>()
    val WasteLiquorDataLiveList = MutableListLiveData<SetWasteLiquorViewData>()
    val OxygenDataLiveList = MutableListLiveData<SetOxygenViewData>()
    val SteamerDataLiveList = MutableListLiveData<SetSteamerViewData>()


    val latestSensorInfo = HashMap<List<Byte>, CurrentSensorInfo>()
    val exSensorInfo = HashMap<List<Byte>, CurrentSensorInfo>()

//    val lastStateInfo = MutableLiveData<HashMap<List<Byte>, CurrentSensorInfo>>()

    val alertInfo = MutableListLiveData<SetAlertData>()

    // 변경되지 않는 데이터를 가져올때 이름을 _ 언더스코어 없이 설정
    // 공개적으로 가져오는 변수는 private 이 아닌 퍼블릭으로 외부에서도 접근가능하도록 설정
    // 하지만 값을 직접 라이브데이터에 접근하지 않고 뷰모델을 통해 가져올수 있도록 설정
    val model_ID_Data: MutableLiveData<HashMap<String, Byte>>
        get() = _model_ID_Data

    val modelMap = HashMap<String, ByteArray>()

    val gasColorMap = hashMapOf<String, Int>(
        "Air" to Color.parseColor("#6599CD"),
        "Ar" to Color.parseColor("#333333"),
        "C₂H₂" to Color.parseColor("#FECD08"),
        "CH₄" to Color.parseColor("#905501"),
        "CO₂" to Color.parseColor("#2D67B2"),
        "H₂" to Color.parseColor("#F2663A"),
        "He" to Color.parseColor("#9A679A"),
        "N₂" to Color.parseColor("#42C8F4"),
        "N₂O" to Color.parseColor("#42C8F4"),
        "O₂" to Color.parseColor("#316734")
    )

    val gasSensorType = arrayListOf<String>(
        "Sensts 142PSI",
        "Sensts 2000PSI",
        "WIKAI 10BAR",
        "WIKAI 160BAR",
        "WIKAI 16BAR",
        "Variable Sensor"
    )
    val oxygenSensorType = arrayListOf<String>(
        "LOX-02"
    )
    val tempSensorType = arrayListOf<String>(
        "SST2109"
    )
    val waterSensorType = arrayListOf<String>(
        "BS1"
    )
    val levelSensorType = arrayListOf<String>(
        "LEVEL-01"
    )
    val maxPressureMap = hashMapOf<String, Float>(
        "Sensts 142PSI" to 142f,
        "Sensts 2000PSI" to 2000f,
        "WIKAI 10BAR" to 145.038f,
        "WIKAI 160BAR" to 2320.6f,
        "WIKAI 16BAR" to 232.06f,
        "Variable Sensor" to 2000f
    )
    val gasType = arrayListOf<String>(
        "Air",
        "Ar",
        "C₂H₂",
        "CH₄",
        "CO₂",
        "H₂",
        "He",
        "N₂",
        "N₂O",
        "O₂"
    )
    val gasNameMap = hashMapOf<String, String>(
        "Air" to "Air",
        "Ar" to "Ar",
        "C2H2" to "C₂H₂",
        "CH4" to "CH₄",
        "CO2" to "CO₂",
        "H2" to "H₂",
        "He" to "He",
        "N2" to "N₂",
        "N2O" to "N₂O",
        "O2" to "O₂"
    )
    val gaugeConstMap = hashMapOf<String, Double>(
        "kPa" to 6.89476,
        "bar" to 0.0689476141537538,
        "kgf/cm2" to 0.070307,
        "psi" to 1.toDouble()
    )
    fun removeModelMap(){
        modelMap.remove("GasDock")
        modelMap.remove("GasRoom")
        modelMap.remove("WasteLiquor")
        modelMap.remove("Oxygen")
        modelMap.remove("Steamer")
    }
    init {
//        Log.d(TAG, "MainViewModel - 생성자 호출")
//        Log.d(TAG, "MainViewModel _currentValue : $_currentValue")
//        _IDsList.value = IDs


    }
}

class ListLiveData<T> : MutableLiveData<MutableList<T>>() {
    private val temp = mutableListOf<T>()

    init {
        value = temp
    }
}