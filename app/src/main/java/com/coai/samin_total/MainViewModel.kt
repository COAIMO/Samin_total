package com.coai.samin_total

import android.app.Application
import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.coai.libmodbus.service.SaminModbusService
import com.coai.libsaminmodbus.model.KeyUtils
import com.coai.libsaminmodbus.model.ModelMonitorValues
import com.coai.libsaminmodbus.model.ObserveModelMonitorValues
import com.coai.samin_total.Dialog.AlertDialogFragment
import com.coai.samin_total.Dialog.SetAlertData
import com.coai.samin_total.GasDock.SetGasStorageViewData
import com.coai.samin_total.GasRoom.SetGasRoomViewData
import com.coai.samin_total.GasRoom.TimePSI
import com.coai.samin_total.Logic.*
import com.coai.samin_total.Oxygen.SetOxygenViewData
import com.coai.samin_total.Steamer.SetSteamerViewData
import com.coai.samin_total.Steamer.SteamerSettingFragment
import com.coai.samin_total.WasteLiquor.SetWasteLiquorViewData
import com.coai.samin_total.database.AlertData
import com.coai.samin_total.database.AlertDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

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
        const val WASTELIQUORSETTINGFRAGMENT = 21
        const val GASROOMLEAKTESTFRAGMENT = 22

        const val GasDockStorage = 1.toByte()
        const val GasRoom = 2.toByte()
        const val WasteLiquor = 3.toByte()
        const val Oxygen = 4.toByte()
        const val Steamer = 5.toByte()
        const val Temp_Hum = 6.toByte()

    }

    val GasStorageDataLiveList = MutableListLiveData<SetGasStorageViewData>()
    val GasRoomDataLiveList = MutableListLiveData<SetGasRoomViewData>()
    val WasteLiquorDataLiveList = MutableListLiveData<SetWasteLiquorViewData>()
    val OxygenDataLiveList = MutableListLiveData<SetOxygenViewData>()
    val SteamerDataLiveList = MutableListLiveData<SetSteamerViewData>()

    //    var oxygenMasterData = SetOxygenViewData("0",0,0)
//    var oxygenMasterData: SetOxygenViewData? = null
    val oxygensData = HashMap<Int, SetOxygenViewData>()

    val alertInfo = MutableListLiveData<SetAlertData>()
    val alertMap = ConcurrentHashMap<Int, SetAlertData>()
    val portAlertMapLed = ConcurrentHashMap<Short, Byte>()

    val wasteAlert: MutableLiveData<Boolean> = MutableLiveData()
    val oxyenAlert: MutableLiveData<Boolean> = MutableLiveData()
    val gasStorageAlert: MutableLiveData<Boolean> = MutableLiveData()
    val steamerAlert: MutableLiveData<Boolean> = MutableLiveData()
    val gasRoomAlert: MutableLiveData<Boolean> = MutableLiveData()

    var isSoundAlert = true

    val modelMap = HashMap<String, ByteArray>()
    val modelMapInt = HashMap<Int, ByteArray>()
    val hasKey = ConcurrentHashMap<Int, Int>()
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
    val gasColor = arrayListOf(
        "#6599CD(Air)",
        "#333333(Ar)",
        "#FECD08(C₂H₂)",
        "#905501(CH₄)",
        "#2D67B2(CO₂)",
        "#F2663A(H₂)",
        "#9A679A(He)",
        "#42C8F4(N₂)",
        "#42C8F4(N₂O)",
        "#316734(O₂)"
    )
    val gasColorValue = arrayListOf(
        Color.parseColor("#6599CD"),
        Color.parseColor("#333333"),
        Color.parseColor("#FECD08"),
        Color.parseColor("#905501"),
        Color.parseColor("#2D67B2"),
        Color.parseColor("#F2663A"),
        Color.parseColor("#9A679A"),
        Color.parseColor("#42C8F4"),
        Color.parseColor("#42C8F4"),
        Color.parseColor("#316734")
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

    fun removeModelMap() {
        modelMap.remove("GasDock")
        modelMap.remove("GasRoom")
        modelMap.remove("WasteLiquor")
        modelMap.remove("Oxygen")
        modelMap.remove("Steamer")

        modelMapInt.remove(1)
        modelMapInt.remove(2)
        modelMapInt.remove(3)
        modelMapInt.remove(4)
        modelMapInt.remove(5)

        alertMap.clear()
    }


    /**
     * 경고 상태 전달
     */
    fun addAlertInfo(id: Int, arg: SetAlertData) {
        try {
            alertInfo.add(arg)
        } catch (e: Exception) {
        }
        try {
            alertMap[id] = arg
        } catch (e: Exception) {
        }
    }

    /**
     * 경고 상태 전달
     * AQ에 LED/Beep음 제어는 제한한다.
     */
    fun addAlertInfoNoNoti(id: Int, arg: SetAlertData) {
        try {
            alertInfo.add(arg)
        } catch (e: Exception) {
        }
    }

    var storageViewZoomState = false
    var roomViewZoomState = false
    var wasteViewZoomState = false
    var oxygenViewZoomState = false
    var steamerViewZoomState = false

    /**
     *  관제 설정 데이터
     */
    var controlData: ControlData = ControlData()

    /**
     * 모드버스 플래그 객체
     */
    var mModelMonitorValues: ModelMonitorValues = ModelMonitorValues()
    var mObserveModelMonitorValues: ObserveModelMonitorValues? = null
    var modbusService: SaminModbusService? = null

    /**
     *  모드 버스 모니터링 여부
     */
    var isProcessingMonitor: Boolean = false

    // 모드버스용 가스 구분 코드 변환
    private fun getModbusGASNum(arg: String): Short {
        var ret: Short = 0

        ret = when (arg) {
            gasType[0] -> 1
            gasType[1] -> 2
            gasType[2] -> 3

            gasType[3] -> 4
            gasType[4] -> 5
            gasType[5] -> 6
            gasType[6] -> 7
            gasType[7] -> 8
            gasType[8] -> 9
            gasType[9] -> 10
            else -> 11
        }

        return ret
    }

    /**
     * 모드버스 초기화
     * 설정 변경 시 호출 필요
     */
    fun refreshModbusModels() {
        isProcessingMonitor = false
        // 미처리 데이터 대기
        Thread.sleep(100)
        mModelMonitorValues = ModelMonitorValues()

        modbusService?.let {
            mObserveModelMonitorValues =
                ObserveModelMonitorValues(it, mModelMonitorValues)
        }


        var cntGasStorage = 0
        GasStorageDataLiveList.value?.let {
            for (tmp in it) {
                if (tmp.usable) {
                    if (tmp.ViewType == 0) {
                        // 싱글
                        KeyUtils.setIndex(
                            tmp.modelByte.toInt(),
                            tmp.id.toByte(),
                            tmp.port.toByte()
                        )
                        val idx = KeyUtils.getIndex(
                            tmp.modelByte.toInt(),
                            tmp.id.toByte(),
                            tmp.port.toByte()
                        )
                        cntGasStorage++
                        mModelMonitorValues.setStorageKinds(idx, getModbusGASNum(tmp.gasName))
                    } else {
                        // 듀얼, 오토체인저
                        KeyUtils.setIndex(
                            tmp.modelByte.toInt(),
                            tmp.id.toByte(),
                            tmp.port.toByte()
                        )
                        KeyUtils.setIndex(
                            tmp.modelByte.toInt(),
                            tmp.id.toByte(),
                            (tmp.port + 1).toByte()
                        )
                        val idx1 = KeyUtils.getIndex(
                            tmp.modelByte.toInt(),
                            tmp.id.toByte(),
                            tmp.port.toByte()
                        )
                        val idx2 = KeyUtils.getIndex(
                            tmp.modelByte.toInt(),
                            tmp.id.toByte(),
                            (tmp.port + 1).toByte()
                        )
                        cntGasStorage++
                        mModelMonitorValues.setStorageKinds(idx1, getModbusGASNum(tmp.gasName))
                        cntGasStorage++
                        mModelMonitorValues.setStorageKinds(idx2, getModbusGASNum(tmp.gasName))
                    }
                }
            }
        }
        mModelMonitorValues.setCountStorage(cntGasStorage.toShort())

        var cntGasRoom = 0
        GasRoomDataLiveList.value?.let {
            for (tmp in it) {
                if (tmp.usable) {
                    KeyUtils.setIndex(
                        tmp.modelByte.toInt(),
                        tmp.id.toByte(),
                        tmp.port.toByte()
                    )
                    val idx = KeyUtils.getIndex(
                        tmp.modelByte.toInt(),
                        tmp.id.toByte(),
                        tmp.port.toByte()
                    )
                    cntGasRoom++
                    mModelMonitorValues.setRoomKinds(idx, getModbusGASNum(tmp.gasName))
                }
            }
        }
        mModelMonitorValues.setCountRoom(cntGasRoom.toShort())

        var cntWaste = 0
        WasteLiquorDataLiveList.value?.let {
            for (tmp in it) {
                if (tmp.usable) {
                    KeyUtils.setIndex(
                        tmp.modelByte.toInt(),
                        tmp.id.toByte(),
                        tmp.port.toByte()
                    )
                    val idx = KeyUtils.getIndex(
                        tmp.modelByte.toInt(),
                        tmp.id.toByte(),
                        tmp.port.toByte()
                    )
                    cntWaste++
                }
            }
        }
        mModelMonitorValues.setCountWaste(cntWaste.toShort())

        var cntOxygen = 0
        OxygenDataLiveList.value?.let {
            for (tmp in it) {
                if (tmp.usable) {
                    KeyUtils.setIndex(
                        tmp.modelByte.toInt(),
                        tmp.id.toByte(),
                        tmp.port.toByte()
                    )
                    val idx = KeyUtils.getIndex(
                        tmp.modelByte.toInt(),
                        tmp.id.toByte(),
                        tmp.port.toByte()
                    )
                    cntOxygen++
                }
            }
        }
        mModelMonitorValues.setCountOxygen(cntOxygen.toShort())

        var cntSteamer = 0
        SteamerDataLiveList.value?.let {
            for (tmp in it) {
                if (tmp.usable) {
                    KeyUtils.setIndex(
                        tmp.modelByte.toInt(),
                        tmp.id.toByte(),
                        tmp.port.toByte()
                    )
                    val idx = KeyUtils.getIndex(
                        tmp.modelByte.toInt(),
                        tmp.id.toByte(),
                        tmp.port.toByte()
                    )
                    cntSteamer++
                }
            }
        }
        mModelMonitorValues.setCountSteam(cntSteamer.toShort())

        isProcessingMonitor = true
    }

    /**
     * 스캔 모드 여부
     */
    var isScanmode: Boolean = false

    var isLeakTestTime: Int = -1
    var isSaveLeakTestData: Boolean = false
    var isCheckTimeOut: Boolean = true
    val isPopUp: MutableLiveData<Boolean> = MutableLiveData()

//    val popUpHashMap: LiveData<HashMap<Int, SetAlertData>> = _popUpHashMap
    val popUpHashMap = HashMap<Int, SetAlertData>()
    val _popUpList = MutableLiveData<MutableList<SetAlertData>>()

    init {
        _popUpList.value = mutableListOf<SetAlertData>()
    }
    fun addPopupMap(key:Int, value:SetAlertData){
        try {
            popUpHashMap.set(key,value)
            for (i in popUpHashMap){
                _popUpList.value?.add(i.value)
            }
            Log.d("테스트1","map = ${popUpHashMap[key]}")
            Log.d("테스트1","list = ${_popUpList.value}")
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
    fun removePopupMap(key: Int,value: SetAlertData){
        try {
            popUpHashMap.remove(key)
            _popUpList.value!!.remove(value)

        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    val popUpDataLiveList = MutableListLiveData<SetAlertData>()

    fun clearPopUP(){
        popUpHashMap.clear()
        _popUpList.value?.clear()
        popUpDataLiveList.clear(true)
    }

    val alertDialogLiveData = MutableListLiveData<SetAlertData>()
    val alertDialogFragment = AlertDialogFragment()
    val saveConetMap = ConcurrentHashMap<Int, SetAlertData>()

}

