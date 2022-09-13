package com.coai.samin_total

import android.util.Log
import com.coai.libsaminmodbus.model.KeyUtils
import com.coai.samin_total.Dialog.SetAlertData
import com.coai.samin_total.GasDock.SetGasStorageViewData
import com.coai.samin_total.GasRoom.SetGasRoomViewData
import com.coai.samin_total.GasRoom.TimePSI
import com.coai.samin_total.Logic.AnalyticUtils
import com.coai.samin_total.Oxygen.SetOxygenViewData
import com.coai.samin_total.Service.HexDump
import com.coai.samin_total.Steamer.SetSteamerViewData
import com.coai.samin_total.WasteLiquor.SetWasteLiquorViewData
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.Exception
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AQDataParser(viewModel: MainViewModel) {
    val hmapErrorOxygen = HashMap<Int, Boolean>()
    val hmapAQPortSettings = HashMap<Int, Any>()
    val viewModel: MainViewModel = viewModel
    val setAQport = HashMap<Int, Any>()


    // 최종 숫신시간
    val hmapLastedDate = ConcurrentHashMap<Int, Long>()

    //    var hmapIDLastedDate = HashMap<Short, Long>()
    val hmapPsis = HashMap<Int, ArrayList<TimePSI>>()

    val alertBase = HashMap<Int, Float>()
    val alertMap = HashMap<Int, Boolean>()
    val alertMap2 = HashMap<Int, Boolean>()

    private fun alertMapClear() {
        alertBase.clear()
        alertMap.clear()
        alertMap2.clear()
    }

    /**
     * 바이트배열 Int 변환
     */
    private fun littleEndianConversion(bytes: ByteArray): Int {
        var result = 0
        for (i in bytes.indices) {
            result = result or (bytes[i].toUByte().toInt() shl 8 * i)
        }
        return result
    }

    /**
     * 설정을 해제한다.
     */
    fun Clear() {
        synchronized(this) {
            hmapErrorOxygen.clear()
            hmapAQPortSettings.clear()
            setAQport.clear()
            hmapLastedDate.clear()
            alertBase.clear()
            alertMap.clear()
            alertMap2.clear()

            for (t in hmapPsis.values)
                t.clear()

            hmapPsis.clear()
//            hmapIDLastedDate.clear()
            lostConnectAQs.clear()
        }
    }

    /**
     * 설정을 가져온다.
     */
    fun LoadSetting() {
        Clear()
        // 각 설정별 처리
        for (tmp in viewModel.GasStorageDataLiveList.value!!) {
            val key = littleEndianConversion(
                byteArrayOf(
                    tmp.modelByte,
                    tmp.id.toByte(),
                    tmp.port.toByte()
                )
            )
            hmapAQPortSettings[key] = tmp.copy()
            setAQport[key] = tmp
            hmapLastedDate[key] = System.currentTimeMillis()
        }
        // 룸 센서에 대해서만 처리
        for (tmp in viewModel.GasRoomDataLiveList.value!!) {
            val key = littleEndianConversion(
                byteArrayOf(
                    tmp.modelByte,
                    tmp.id.toByte(),
                    tmp.port.toByte()
                )
            )
            hmapAQPortSettings[key] = tmp.copy()
            setAQport[key] = tmp
            hmapLastedDate[key] = System.currentTimeMillis()
        }

        for (tmp in viewModel.WasteLiquorDataLiveList.value!!) {
            val key = littleEndianConversion(
                byteArrayOf(
                    tmp.modelByte,
                    tmp.id.toByte(),
                    tmp.port.toByte()
                )
            )
            hmapAQPortSettings[key] = tmp.copy()
            setAQport[key] = tmp
            hmapLastedDate[key] = System.currentTimeMillis()
        }

        for (tmp in viewModel.OxygenDataLiveList.value!!) {
            val key = littleEndianConversion(
                byteArrayOf(
                    tmp.modelByte,
                    tmp.id.toByte(),
                    tmp.port.toByte()
                )
            )
            hmapAQPortSettings[key] = tmp.copy()
            setAQport[key] = tmp
            hmapLastedDate[key] = System.currentTimeMillis()
        }

        for (tmp in viewModel.SteamerDataLiveList.value!!) {
            val key = littleEndianConversion(
                byteArrayOf(
                    tmp.modelByte,
                    tmp.id.toByte(),
                    tmp.port.toByte()
                )
            )
            hmapAQPortSettings[key] = tmp.copy()
            setAQport[key] = tmp
            hmapLastedDate[key] = System.currentTimeMillis()
        }

//        val tmp = viewModel.oxygenMasterData
//        val key = littleEndianConversion(
//            byteArrayOf(
//                tmp.modelByte,
//                tmp.id.toByte(),
//                tmp.port.toByte()
//            )
//        )
//        hmapAQPortSettings[key] = tmp.copy()
//        setAQport[key] = tmp
//        hmapLastedDate[key] = System.currentTimeMillis()
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

    /**
     * 가스 룸 로직
     * Todo: 에러 체크 기능 필요. 정상화 체크 필요.
     */

//    private fun ProcessGasStorage(id: Int, data: Int) {
//        val tmp1 = hmapAQPortSettings[id] ?: return
//        val tmp = (tmp1 as SetGasStorageViewData)
//        if (!tmp.usable) {
//            return
//        }
//        var value: Float = 0f
//        when (tmp.sensorType) {
//            "Sensts 142PSI" -> {
//                value = calcPSI142(data.toFloat(), tmp.rewardValue, tmp.zeroPoint)
//            }
//            "Sensts 2000PSI" -> {
//                value = calcPSI2000(data.toFloat(), tmp.rewardValue, tmp.zeroPoint)
//            }
//            else -> {
//                value = calcSensor(
//                    data.toFloat(),
//                    tmp.pressure_Max!!,
//                    tmp.rewardValue,
//                    tmp.zeroPoint
//                )
//            }
//        }
//
//        if (tmp.ViewType == 0) {
//            tmp.pressure = value
//            if (tmp.pressure_Min!! > tmp.pressure!!) {
//                tmp.isAlert = true
//
//                if (alertMap[id] == null) {
//                    alertMap.put(id, true)
////                    viewModel.gasStorageAlert.value = true
//                    viewModel.addAlertInfo(
//                        id,
//                        SetAlertData(
//                            getLatest_time(hmapLastedDate[id]!!),
//                            tmp.modelByte.toInt(),
//                            tmp.id,
//                            "가스 압력 하한 값",
//                            tmp.port,
//                            true
//                        )
//                    )
//                }
//
//            } else {
//                if (alertMap.containsKey(id)) {
//                    tmp.isAlert = false
////                    viewModel.gasStorageAlert.value = false
//                    viewModel.addAlertInfo(
//                        id,
//                        SetAlertData(
//                            getLatest_time(hmapLastedDate[id]!!),
//                            tmp.modelByte.toInt(),
//                            tmp.id,
//                            "가스 압력 정상",
//                            tmp.port,
//                            false
//                        )
//                    )
//                    if (alertMap.containsKey(id)) {
//                        alertMap.remove(id)
//                    }
//                }
//            }
//        } else if (tmp.ViewType == 1 || tmp.ViewType == 2) {
//            if (tmp.port == 1 || tmp.port == 3) {
//                tmp.pressureLeft = value
//            } else {
//                tmp.pressureRight = value
//            }
//
//            if (tmp.pressure_Min!! > tmp.pressureLeft!!) {
//                tmp.isAlertLeft = true
//
//                if (alertMap[id] == null) {
//                    alertMap.put(id, true)
////                    viewModel.gasStorageAlert.value = true
//                    viewModel.addAlertInfo(
//                        id,
//                        SetAlertData(
//                            getLatest_time(hmapLastedDate[id]!!),
//                            tmp.modelByte.toInt(),
//                            tmp.id,
//                            "가스 압력 하한 값",
//                            tmp.port,
//                            true
//                        )
//                    )
//                }
//
//            } else if (tmp.pressure_Min!! < tmp.pressureLeft!!) {
//                if (alertMap.containsKey(id)) {
//                    tmp.isAlertLeft = false
////                    viewModel.gasStorageAlert.value = false
//                    viewModel.addAlertInfo(
//                        id,
//                        SetAlertData(
//                            getLatest_time(hmapLastedDate[id]!!),
//                            tmp.modelByte.toInt(),
//                            tmp.id,
//                            "가스 압력 정상",
//                            tmp.port,
//                            false
//                        )
//                    )
//
//                    if (alertMap.containsKey(id)) {
//                        alertMap.remove(id)
//                    }
//                }
//                if (tmp.pressure_Min!! > tmp.pressureRight!!) {
//                    tmp.isAlertRight = true
//
//                    if (alertMap2[id] == null) {
//                        alertMap2.put(id, true)
////                        viewModel.gasStorageAlert.value = true
//                        viewModel.addAlertInfo(
//                            id + 65536,
//                            SetAlertData(
//                                getLatest_time(hmapLastedDate[id]!!),
//                                tmp.modelByte.toInt(),
//                                tmp.id,
//                                "가스 압력 하한 값",
//                                tmp.port + 1,
//                                true
//                            )
//                        )
//                    }
//
//                } else if (tmp.pressure_Min!! < tmp.pressureRight!!) {
//                    if (alertMap2.containsKey(id)) {
//                        tmp.isAlertRight = false
////                        viewModel.gasStorageAlert.value = false
//                        viewModel.addAlertInfo(
//                            id + 65536,
//                            SetAlertData(
//                                getLatest_time(hmapLastedDate[id]!!),
//                                tmp.modelByte.toInt(),
//                                tmp.id,
//                                "가스 압력 정상",
//                                tmp.port + 1,
//                                false
//                            )
//                        )
//
//                        if (alertMap2.containsKey(id)) {
//                            alertMap2.remove(id)
//                        }
//                    }
//                }
//
//            }
//        }
//
//        val bro = setAQport[id] as SetGasStorageViewData
//        bro.pressure = tmp.pressure
//        bro.isAlert = tmp.isAlert
//        bro.pressureLeft = tmp.pressureLeft
//        bro.pressureRight = tmp.pressureRight
//        bro.isAlertLeft = tmp.isAlertLeft
//        bro.isAlertRight = tmp.isAlertRight
//    }

    fun ProcessSingleGasStorage(id: Int, data: Int) {
        val tmp1 = hmapAQPortSettings[id] ?: return
        val tmp = (tmp1 as SetGasStorageViewData)
        if (!tmp.usable) {
            return
        }
        var value: Float = 0f
        when (tmp.sensorType) {
            "Sensts 142PSI" -> {
                value = calcPSI142(data.toFloat(), tmp.left_rewardValue, tmp.left_zeroPoint)
            }
            "Sensts 2000PSI" -> {
                value = calcPSI2000(data.toFloat(), tmp.left_rewardValue, tmp.left_zeroPoint)
            }
            else -> {
                value = calcSensor(
                    data.toFloat(),
                    tmp.pressure_Max!!,
                    tmp.left_rewardValue,
                    tmp.left_zeroPoint
                )
            }
        }
        tmp.pressure = value
        if (tmp.pressure_Min!! > tmp.pressure!!) {
            tmp.isAlert = true

            if (alertMap[id] == null) {
                alertMap.put(id, true)
//                viewModel.gasStorageAlert.value = true
                viewModel.addAlertInfo(
                    id,
                    SetAlertData(
                        getLatest_time(hmapLastedDate[id]!!),
                        tmp.modelByte.toInt(),
                        tmp.id,
                        "가스 압력 하한 값",
                        tmp.port,
                        true
                    )
                )
            }

        } else {
            tmp.isAlert = false
            if (alertMap.containsKey(id)) {
//                viewModel.gasStorageAlert.value = false
                viewModel.addAlertInfo(
                    id,
                    SetAlertData(
                        getLatest_time(hmapLastedDate[id]!!),
                        tmp.modelByte.toInt(),
                        tmp.id,
                        "가스 압력 정상",
                        tmp.port,
                        false
                    )
                )
                if (alertMap.containsKey(id)) {
                    alertMap.remove(id)
                }
            }
        }
        val bro = setAQport[id] as SetGasStorageViewData
        bro.isAlert = tmp.isAlert
        bro.pressure = tmp.pressure

        val idx = KeyUtils.getIndex(
            tmp.modelByte.toInt(),
            tmp.id.toByte(),
            tmp.port.toByte()
        )

        viewModel.mModelMonitorValues.setStoragePressurePSI(
            idx,
            (tmp.pressure ?: 0f).toInt().toShort()
        )
        viewModel.mModelMonitorValues.setWarningsStorage(
            idx,
            tmp.isAlert!!
        )
        viewModel.mModelMonitorValues.setErrorsStorage(idx, false)
    }


    fun ProcessDualGasStorage(id: Int, left_value: Int, right_value: Int) {
        val tmp1 = hmapAQPortSettings[id] ?: return
        val tmp = (tmp1 as SetGasStorageViewData)
        if (!tmp.usable) {
            return
        }
        var left_pressure: Float = 0f
        var right_pressure: Float = 0f

        when (tmp.sensorType) {
            "Sensts 142PSI" -> {
                left_pressure = calcPSI142(left_value.toFloat(), tmp.left_rewardValue, tmp.left_zeroPoint)
                right_pressure = calcPSI142(right_value.toFloat(), tmp.right_rewardValue, tmp.right_zeroPoint)

            }
            "Sensts 2000PSI" -> {
                left_pressure = calcPSI2000(left_value.toFloat(), tmp.left_rewardValue, tmp.left_zeroPoint)
                right_pressure = calcPSI2000(right_value.toFloat(), tmp.right_rewardValue, tmp.right_zeroPoint)

            }
            else -> {
                left_pressure = calcSensor(
                    left_value.toFloat(),
                    tmp.pressure_Max!!,
                    tmp.left_rewardValue,
                    tmp.left_zeroPoint
                )
                right_pressure = calcSensor(
                    right_value.toFloat(),
                    tmp.pressure_Max!!,
                    tmp.right_rewardValue,
                    tmp.right_zeroPoint
                )
            }
        }
        tmp.pressureLeft = left_pressure
        tmp.pressureRight = right_pressure

        if (tmp.pressure_Min!! > tmp.pressureLeft!!) {
            tmp.isAlertLeft = true

            if (alertMap[id] == null) {
                alertMap.put(id, true)
//                viewModel.gasStorageAlert.value = true
                viewModel.addAlertInfo(
                    id,
                    SetAlertData(
                        getLatest_time(hmapLastedDate[id]!!),
                        tmp.modelByte.toInt(),
                        tmp.id,
                        "가스 압력 하한 값",
                        tmp.port,
                        true
                    )
                )
            }

        } else if (tmp.pressure_Min!! < tmp.pressureLeft!!) {
            tmp.isAlertLeft = false
            if (alertMap.containsKey(id)) {
//                viewModel.gasStorageAlert.value = false
                viewModel.addAlertInfo(
                    id,
                    SetAlertData(
                        getLatest_time(hmapLastedDate[id]!!),
                        tmp.modelByte.toInt(),
                        tmp.id,
                        "가스 압력 정상",
                        tmp.port,
                        false
                    )
                )

                if (alertMap.containsKey(id)) {
                    alertMap.remove(id)
                }
            }
        }

        if (tmp.pressure_Min!! > tmp.pressureRight!!) {
            tmp.isAlertRight = true
            if (alertMap2[id] == null) {
                alertMap2.put(id, true)
//                viewModel.gasStorageAlert.value = true
                viewModel.addAlertInfo(
                    id + 65536,
                    SetAlertData(
                        getLatest_time(hmapLastedDate[id]!!),
                        tmp.modelByte.toInt(),
                        tmp.id,
                        "가스 압력 하한 값",
                        tmp.port + 1,
                        true
                    )
                )
            }

        } else if (tmp.pressure_Min!! < tmp.pressureRight!!) {
            tmp.isAlertRight = false
            if (alertMap2.containsKey(id)) {
//                viewModel.gasStorageAlert.value = false
                viewModel.addAlertInfo(
                    id + 65536,
                    SetAlertData(
                        getLatest_time(hmapLastedDate[id]!!),
                        tmp.modelByte.toInt(),
                        tmp.id,
                        "가스 압력 정상",
                        tmp.port + 1,
                        false
                    )
                )

                if (alertMap2.containsKey(id)) {
                    alertMap2.remove(id)
                }
            }
        }

        val bro = setAQport[id] as SetGasStorageViewData
        bro.pressureLeft = tmp.pressureLeft
        bro.pressureRight = tmp.pressureRight
        bro.isAlertLeft = tmp.isAlertLeft
        bro.isAlertRight = tmp.isAlertRight

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

        viewModel.mModelMonitorValues.setStoragePressurePSI(
            idx1,
            (tmp.pressureLeft ?: 0f).toInt().toShort()
        )
        viewModel.mModelMonitorValues.setWarningsStorage(
            idx1,
            tmp.isAlertLeft!!
        )
        viewModel.mModelMonitorValues.setErrorsStorage(idx1, false)

        viewModel.mModelMonitorValues.setStoragePressurePSI(
            idx2,
            (tmp.pressureRight ?: 0f).toInt().toShort()
        )
        viewModel.mModelMonitorValues.setWarningsStorage(
            idx2,
            tmp.isAlertRight!!
        )
        viewModel.mModelMonitorValues.setErrorsStorage(idx2, false)
    }


    fun ProcessGasRoom(id: Int, data: Int) {
        // 설정이 존재하는지 확인
        // val tmp = (hmapAQPortSettings[id] as SetGasRoomViewData) ?: return
        val tmp1 = hmapAQPortSettings[id] ?: return
        val tmp = (tmp1 as SetGasRoomViewData)

        if (!tmp.usable) {
            return
        }

        // 대상 센서에 맞는 데이터 변환 함수 호출
        var value: Float = 0f
        when (tmp.sensorType) {
            "Sensts 142PSI" -> {
                value = calcPSI142(data.toFloat(), tmp.rewardValue, tmp.zeroPoint)
            }
            "Sensts 2000PSI" -> {
                value = calcPSI2000(data.toFloat(), tmp.rewardValue, tmp.zeroPoint)
            }
            else -> {
                value = calcSensor(
                    data.toFloat(),
                    tmp.pressure_Max,
                    tmp.rewardValue,
                    tmp.zeroPoint
                )
            }
        }
        tmp.pressure = value

        // 기울기 데이터 값 수집
        val item = TimePSI(hmapLastedDate[id]!!, tmp.pressure, 0x02, tmp.id, tmp.port)
        val basetime = hmapLastedDate[id]!! - 1000 * 2

        var tmppsis = hmapPsis[id]

        if (tmppsis.isNullOrEmpty()) {
            tmppsis = ArrayList<TimePSI>()
            hmapPsis[id] = tmppsis
        } // 대상 없으면

        tmppsis.add(item)
        val tempremove = tmppsis!!.filter {
            it.Ticks < basetime
        }
        tmppsis.removeAll(tempremove)

        val lstTicks = tmppsis!!.map {
            (it.Ticks / 100).toDouble()
        }
        val lstPsi = tmppsis!!.map {
            (it.Psi).toDouble()
        }

        // 기울기 산출
        val slope = AnalyticUtils.LinearRegression(
            lstTicks.toTypedArray(),
            lstPsi.toTypedArray(),
            0,
            lstPsi.size
        )
        // 기울기에 따른 에러 설정
        if (slope < tmp.slopeValue) {
            // 경고
            alertBase.put(id, value)
            tmp.isAlert = true
            tmp.pressure

            if (alertMap[id] == null) {
                alertMap.put(id, true)
//                viewModel.gasRoomAlert.value = true
                viewModel.addAlertInfo(
                    id,
                    SetAlertData(
                        getLatest_time(hmapLastedDate[id]!!),
                        tmp.modelByte.toInt(),
                        tmp.id,
                        "MASSIVE_LEAK 발생",
                        tmp.port,
                        true
                    )
                )
            }
        } else {
            if (alertMap.containsKey(id)) {
                if (alertBase[id]!! + 2 < value) {
                    tmp.isAlert = false
//                    viewModel.gasRoomAlert.value = false
                    viewModel.addAlertInfo(
                        id,
                        SetAlertData(
                            getLatest_time(hmapLastedDate[id]!!),
                            tmp.modelByte.toInt(),
                            tmp.id,
                            "MASSIVE_LEAK 문제해결",
                            tmp.port,
                            false
                        )
                    )

                    if (alertMap.containsKey(id)) {
                        alertMap.remove(id)
                    }
                }
            }
            else {
                tmp.isAlert = false
            }

        }
        val bro = setAQport[id] as SetGasRoomViewData
        bro.isAlert = tmp.isAlert
        bro.pressure = tmp.pressure

        val idx = KeyUtils.getIndex(
            tmp.modelByte.toInt(),
            tmp.id.toByte(),
            tmp.port.toByte()
        )

        viewModel.mModelMonitorValues.setRoomPressurePSI(
            idx,
            (tmp.pressure ?: 0f).toInt().toShort()
        )
        viewModel.mModelMonitorValues.setWarningsRoom(
            idx,
            tmp.isAlert!!
        )
        viewModel.mModelMonitorValues.setErrorsRoom(idx, false)
    }

    fun ProcessWasteLiquor(id: Int, data: Int) {
        val tmp1 = hmapAQPortSettings[id] ?: return
        val tmp = (tmp1 as SetWasteLiquorViewData)
        // 수위 초과일때 0 아니면 1
        tmp.isAlert = data == 0

        if (!tmp.usable) {
            return
        }
        val key =
            littleEndianConversion(byteArrayOf(tmp.modelByte, tmp.id.toByte())).toShort()
//        Log.d(
//            "ProcessWasteLiquor",
//            "Model:${tmp.model} ID: ${tmp.id} Port:${tmp.port}}// data:${data}"
//        )

        if (data == 0) {
            tmp.isAlert = true
            if (alertMap[id] == null) {
                alertMap.put(id, true)
//                viewModel.wasteAlert.value = true
                viewModel.addAlertInfo(
                    id,
                    SetAlertData(
                        getLatest_time(hmapLastedDate[id]!!),
                        tmp.modelByte.toInt(),
                        tmp.id,
                        "수위 초과",
                        tmp.port,
                        true
                    )
                )
            }
        } else {
            tmp.isAlert = false
            if (alertMap.containsKey(id)) {
//                viewModel.wasteAlert.value = false
                viewModel.addAlertInfo(
                    id,
                    SetAlertData(
                        getLatest_time(hmapLastedDate[id]!!),
                        tmp.modelByte.toInt(),
                        tmp.id,
                        "수위 정상",
                        tmp.port,
                        false
                    )
                )
                if (alertMap.containsKey(id)) {
                    alertMap.remove(id)
                }
            }
        }
        val bro = setAQport[id] as SetWasteLiquorViewData
        bro.isAlert = tmp.isAlert

        val idx = KeyUtils.getIndex(
            tmp.modelByte.toInt(),
            tmp.id.toByte(),
            tmp.port.toByte()
        )

        viewModel.mModelMonitorValues.setWarningsWaste(
            idx,
            tmp.isAlert!!
        )
        viewModel.mModelMonitorValues.setErrorsWaste(idx, false)
    }


    fun ProcessOxygen(id: Int, data: Int) {
        val tmp1 = hmapAQPortSettings[id] ?: return
        val preError = hmapErrorOxygen[id] ?: false
        val tmp = (tmp1 as SetOxygenViewData)
        var oxygenValue = data / 100f
        if (oxygenValue <= 0f || oxygenValue > 100f) {
            return
        }
        oxygenValue += (tmp.zeroPoint ?: 0f)
        tmp.setValue = oxygenValue

        if (!tmp.usable) {
            return
        }

        if (tmp.setMinValue > oxygenValue) {
            if (!preError) {
                hmapErrorOxygen[id] = true
                return
            }

            tmp.isAlert = true
            if (alertMap[id] == null) {
                alertMap.put(id, true)
                viewModel.addAlertInfo(
                    id,
                    SetAlertData(
                        getLatest_time(hmapLastedDate[id]!!),
                        tmp.modelByte.toInt(),
                        tmp.id,
                        "산소농도 하한 값 ($oxygenValue)",
                        tmp.port,
                        true
                    )
                )
            }
        } else if(tmp.setMaxValue < oxygenValue){
            if (!preError) {
                hmapErrorOxygen[id] = true
                return
            }

            tmp.isAlert = true
            if (alertMap[id] == null) {
                alertMap.put(id, true)
                viewModel.addAlertInfo(
                    id,
                    SetAlertData(
                        getLatest_time(hmapLastedDate[id]!!),
                        tmp.modelByte.toInt(),
                        tmp.id,
                        "산소농도 상한 값 ($oxygenValue)",
                        tmp.port,
                        true
                    )
                )
            }
        }else{
            tmp.isAlert = false
            hmapErrorOxygen.remove(id)

            if (alertMap.containsKey(id)) {
                viewModel.addAlertInfo(
                    id,
                    SetAlertData(
                        getLatest_time(hmapLastedDate[id]!!),
                        tmp.modelByte.toInt(),
                        tmp.id,
                        "산소농도 정상",
                        tmp.port,
                        false
                    )
                )
                if (alertMap.containsKey(id)) {
                    alertMap.remove(id)
                }
            }
        }

//        else {
//            if (tmp.setMaxValue < oxygenValue) {
//                if (!preError) {
//                    hmapErrorOxygen[id] = true
//                    return
//                }
//
//                tmp.isAlert = true
//                if (alertMap[id] == null) {
//                    alertMap.put(id, true)
//                    viewModel.addAlertInfo(
//                        id,
//                        SetAlertData(
//                            getLatest_time(hmapLastedDate[id]!!),
//                            tmp.modelByte.toInt(),
//                            tmp.id,
//                            "산소농도 상한 값 ($oxygenValue)",
//                            tmp.port,
//                            true
//                        )
//                    )
//                }
//            } else {
//                tmp.isAlert = false
//                hmapErrorOxygen.remove(id)
//
//                if (alertMap.containsKey(id)) {
//                    viewModel.addAlertInfo(
//                        id,
//                        SetAlertData(
//                            getLatest_time(hmapLastedDate[id]!!),
//                            tmp.modelByte.toInt(),
//                            tmp.id,
//                            "산소농도 정상",
//                            tmp.port,
//                            false
//                        )
//                    )
//                    if (alertMap.containsKey(id)) {
//                        alertMap.remove(id)
//                    }
//                }
//            }
//        }

        val bro = setAQport[id] as SetOxygenViewData
        bro.setValue = tmp.setValue
        bro.isAlert = tmp.isAlert

        val idx = KeyUtils.getIndex(
            tmp.modelByte.toInt(),
            tmp.id.toByte(),
            tmp.port.toByte()
        )

        viewModel.mModelMonitorValues.setWarningsOxygen(
            idx,
            tmp.isAlert!!
        )

        viewModel.mModelMonitorValues.setOxygen(
            idx,
            tmp.setValue.toInt().toShort()
        )
        viewModel.mModelMonitorValues.setErrorsOxygen(idx, false)

        val oxygenAQ = setAQport.filter {
            HexDump.toByteArray(it.key).get(3) == 4.toByte()
        }

//        val masterKey = littleEndianConversion(
//            byteArrayOf(
//                4.toByte(),
//                11.toByte(),
//                11.toByte()
//            )
//        )
//        val oxygenLastValueList = mutableListOf<Float>()
//        for ((key, value) in oxygenAQ) {
//            val oxydata = (value as SetOxygenViewData)
//
//            viewModel.oxygensData.put(key, oxydata)
//            if (oxydata.setValue == 0f) {
//                continue
//            } else {
//                oxygenLastValueList.add(oxydata.setValue)
//            }
//        }

//        var tmpavg = oxygenLastValueList.average()
//        if(tmpavg.isNaN()) tmpavg = 0.0
//        if (viewModel.oxygenMasterData == null)
//            return
//
//        viewModel.oxygenMasterData!!.setValue = tmpavg.toFloat()
//        if (viewModel.oxygenMasterData!!.setMinValue > viewModel.oxygenMasterData!!.setValue) {
//            viewModel.oxygenMasterData!!.isAlert = true
//            if (alertMap[masterKey] == null) {
//                alertMap.put(masterKey, true)
//                viewModel.addAlertInfo(
//                    masterKey,
//                    SetAlertData(
//                        getLatest_time(hmapLastedDate[id]!!),
//                        4,
//                        8,
//                        "[평균]산소농도 하한 값 (${viewModel.oxygenMasterData!!.setValue})",
//                        8,
//                        true
//                    )
//                )
//            }
//        } else {
//            if (viewModel.oxygenMasterData!!.setMaxValue < viewModel.oxygenMasterData!!.setValue) {
//                viewModel.oxygenMasterData!!.isAlert = true
//                if (alertMap[masterKey] == null) {
//                    alertMap.put(masterKey, true)
//                    viewModel.addAlertInfo(
//                        masterKey,
//                        SetAlertData(
//                            getLatest_time(hmapLastedDate[id]!!),
//                            4,
//                            8,
//                            "[평균]산소농도 상한 값(${viewModel.oxygenMasterData!!.setValue})",
//                            8,
//                            true
//                        )
//                    )
//                }
//            } else {
//                viewModel.oxygenMasterData!!.isAlert = false
//                if (alertMap.containsKey(masterKey)) {
//                    viewModel.addAlertInfo(
//                        masterKey,
//                        SetAlertData(
//                            getLatest_time(hmapLastedDate[id]!!),
//                            4,
//                            8,
//                            "[평균]산소농도 정상",
//                            8,
//                            false
//                        )
//                    )
//                    if (alertMap.containsKey(masterKey)) {
//                        alertMap.remove(masterKey)
//                    }
//                }
//            }
//        }
    }

    fun ProcessSteamer(id: Int, temp: Int, level: Int) {
        val tmp1 = hmapAQPortSettings[id] ?: return
        val tmp = (tmp1 as SetSteamerViewData)
//        tmp.isTemp = ((0.0019 * temp * temp) + (0.0796 * temp - 186.76)).toInt()
        tmp.isTemp = ((-1.3462 * temp) + 972.12).toInt()
        tmp.unit

        if (!tmp.usable) {
            return
        }
        //설정 온도보다 현재 온도가 낮을경우 알람
        if (tmp.tempMax < tmp.isTemp) {
            tmp.isAlertTemp = true

            if (alertMap[id] == null) {
                alertMap.put(id, true)
//                viewModel.steamerAlert.value = true
                viewModel.addAlertInfo(
                    id,
                    SetAlertData(
                        getLatest_time(hmapLastedDate[id]!!),
                        tmp.modelByte.toInt(),
                        tmp.id,
                        "온도 상한 값",
                        tmp.port,
                        true
                    )
                )
            }

        } else {
            tmp.isAlertTemp = false
            if (alertMap.containsKey(id)) {
//                viewModel.steamerAlert.value = false
                viewModel.addAlertInfo(
                    id,
                    SetAlertData(
                        getLatest_time(hmapLastedDate[id]!!),
                        tmp.modelByte.toInt(),
                        tmp.id,
                        "온도 정상",
                        tmp.port,
                        false
                    )
                )

                if (alertMap.containsKey(id)) {
                    alertMap.remove(id)
                }
            }
        }

        //센서가 물에 담겨져있지 않다면(1000보다 클 경우) 알람
        tmp.isAlertLow = level > 1000

        if (level > 1000) {
            tmp.isAlertLow = true
            if (alertMap2[id] == null) {
                alertMap2.put(id, true)
                //viewModel.steamerAlert.value = true
                viewModel.addAlertInfo(
                    id + 65536 * 2,
                    SetAlertData(
                        getLatest_time(hmapLastedDate[id]!!),
                        tmp.modelByte.toInt(),
                        tmp.id,
                        "수위 레벨 하한 값",
                        tmp.port + 2,
                        true
                    )
                )
            }

        } else {
            tmp.isAlertLow = false
            if (alertMap2.containsKey(id)) {
//                viewModel.steamerAlert.value = false
                viewModel.addAlertInfo(
                    id + 65536 * 2,
                    SetAlertData(
                        getLatest_time(hmapLastedDate[id]!!),
                        tmp.modelByte.toInt(),
                        tmp.id,
                        "수위 레벨 정상",
                        tmp.port + 2,
                        false
                    )
                )

                if (alertMap2.containsKey(id)) {
                    alertMap2.remove(id)
                }
            }
        }

        val bro = setAQport[id] as SetSteamerViewData
        bro.isAlertTemp = tmp.isAlertTemp
        bro.isAlertLow = tmp.isAlertLow
        bro.isTemp = tmp.isTemp

        val idx = KeyUtils.getIndex(
            tmp.modelByte.toInt(),
            tmp.id.toByte(),
            tmp.port.toByte()
        )

        viewModel.mModelMonitorValues.setWarningsSteam(
            idx,
            tmp.isAlertTemp || tmp.isAlertLow
        )

        viewModel.mModelMonitorValues.setSteamTemps(
            idx,
            tmp.isTemp.toInt().toShort()
        )
        viewModel.mModelMonitorValues.setErrorsSteam(idx, false)
    }

    fun ParserGas(key: Byte, datas: ArrayList<Int>, time : Long) {
        for (t in 1..4) {
            val key = littleEndianConversion(
                byteArrayOf(
                    1,
                    key.toByte(),
                    t.toByte()
                )
            )

//            datas[t-1]

            val tmp1 = hmapAQPortSettings[key] ?: continue
            val tmp = (tmp1 as SetGasStorageViewData)

            if (!tmp.usable)
                continue

            if (tmp.ViewType in 1..2) {
                hmapLastedDate[key] = time
                ProcessDualGasStorage(key,  datas[t-1],  datas[t])
            } else {
                hmapLastedDate[key] = time
                ProcessSingleGasStorage(key, datas[t-1])
            }
        }
    }
    /**
     * 수신 데이터 처리
     */
    val exSensorData = HashMap<Int, Int>()
    fun Parser(arg: ByteArray) {
        synchronized(this) {
            try {
                val id = arg[3]
                val model = arg[2]
                val time = System.currentTimeMillis()
                val datas = ArrayList<Int>()

                datas.add(littleEndianConversion(arg.slice(7..8).toByteArray()))
                datas.add(littleEndianConversion(arg.slice(9..10).toByteArray()))
                datas.add(littleEndianConversion(arg.slice(11..12).toByteArray()))
                datas.add(littleEndianConversion(arg.slice(13..14).toByteArray()))

                if (model == 1.toByte() || model == 2.toByte() || model == 5.toByte()) {
                    for (t in 0..3) {
                        datas[t] = getLPF(
                            datas[t], littleEndianConversion(
                                byteArrayOf(
                                    model,
                                    id.toByte(),
                                    (t + 1).toByte()
                                )
                            )
                        )
                    }
                } else if (model == 3.toByte()) {
                    for (t in 0..3) {
                        1
                        if (datas[t] != 0 && datas[t] != 1) {
                            return
                        }
                    }
                }

                when (model) {
                    0x01.toByte() -> {
                        if (hmapAQPortSettings.size > 0) {
                            for (i in viewModel.GasStorageDataLiveList.value!!)
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
                                        hmapLastedDate[key] = time
                                        ProcessDualGasStorage(key, left_value, right_value)
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
                                        hmapLastedDate[key] = time
                                        ProcessDualGasStorage(key, left_value, right_value)
                                    }


                                } else {
                                    var loop = 1
                                    for (tmp in datas) {
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
                                        hmapLastedDate[key] = time
                                        ProcessSingleGasStorage(key, tmp)
                                    }
                                }
                        }
                    }
                    0x02.toByte() -> {
                        var loop = 1
                        for (tmp in datas) {
                            //아이디 1개당 포트 4개 추가
                            val port = loop++.toByte()
                            //키는 아이디 포트
                            val key =
                                littleEndianConversion(byteArrayOf(model, id.toByte(), port))
                            hmapLastedDate[key] = time
                            ProcessGasRoom(key, tmp)
                        }
                    }
                    0x03.toByte() -> {
                        var loop = 1
                        for (tmp in datas) {
                            //아이디 1개당 포트 4개 추가
                            val port = loop++.toByte()
                            //키는 아이디 포트
                            val key =
                                littleEndianConversion(byteArrayOf(model, id.toByte(), port))
                            hmapLastedDate[key] = time
                            ProcessWasteLiquor(key, tmp)
                        }
                    }
                    0x04.toByte() -> {
                        val port = 1.toByte()
                        val key = littleEndianConversion(byteArrayOf(model, id.toByte(), port))
                        hmapLastedDate[key] = time
                        ProcessOxygen(key, datas[0])
                    }
                    0x05.toByte() -> {
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
                            hmapLastedDate[key] = time
                            ProcessSteamer(key, temp_data, level_data)

                        }
                    }
                }
            } catch (e: Exception) {
            }
        }

    }

    fun getLatest_time(time: Long): String {
        val dateformat: SimpleDateFormat =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("ko", "KR"))
        val date: Date = Date(time)
        return dateformat.format(date)
    }

    val alphavalue = 0.25

    //들어온값이 이상하게 들어오는거 방지
    fun getLPF(x: Int, key: Int): Int {
        var tmpx = x
        if (x > 1023) {
            Log.d("ERROR", "이상한 데이터 : $x")
            tmpx = exSensorData[key] ?: 0
        }
        if (!exSensorData.containsKey(key)) {
            exSensorData.put(key, tmpx)
            return tmpx
        }

        val prev = exSensorData[key]
        val ret = alphavalue * prev!! + (1 - alphavalue) * tmpx
        exSensorData[key] = ret.toInt()
        return ret.toInt()
    }

    val lostConnectAQs = HashMap<Int, Boolean>()


    fun timeoutAQCheckStep() {
        val baseTime = System.currentTimeMillis() - 1000 * 10
        //기존의 가지고있던 키와 다른 키가 들어올경우 삭제(가비지 데이터땜에)
        for (i in hmapLastedDate) {
            if (!viewModel.hasKey.containsKey(i.key)) {
                hmapLastedDate.remove(i.key)
            }
        }
//        val starttime = System.currentTimeMillis()
        val oldDatas = hmapLastedDate.filter { it.value < baseTime }
//        val measuretime =  System.currentTimeMillis() - starttime
//        Log.d("hmapLastedDate", "hmapLastedDate filter : $measuretime")

//        val starttime1 = System.currentTimeMillis()
////        val lsttmp = ArrayList<Map.Entry<Int, Long>>()
////        val oldDatas2 = hmapLastedDate.filter { it.value < baseTime }
//        val tmps = hmapLastedDate.forEach{
//            if (it.value < baseTime) it
//        }
//        val measuretime1 =  System.currentTimeMillis() - starttime1
//        Log.d("hmapLastedDate", "hmapLastedDate foreach : $measuretime1")
//

        val lastaqs = lostConnectAQs.keys.toMutableList()
        for (tmp in oldDatas) {
            if (lostConnectAQs.containsKey(tmp.key)) {
                lastaqs.remove(tmp.key)
                continue
            }

            // 경고 전달
            val current = setAQport[tmp.key]

            val aqInfo = HexDump.toByteArray(tmp.key)
            val model = aqInfo[3].toInt()
            val id = aqInfo[2].toInt()
            val port = aqInfo[1].toInt()


            val idx = KeyUtils.getIndex(
                model.toInt(),
                id.toByte(),
                port.toByte()
            )
            if (current is SetGasStorageViewData) {
                (current as SetGasStorageViewData).isAlert = true
                current.isAlertRight = true
                current.isAlertLeft = true

                if (current.usable) {
                    if (current.ViewType == 0)
                        viewModel.mModelMonitorValues.setErrorsStorage(idx, true)
                    else {
                        viewModel.mModelMonitorValues.setErrorsStorage(idx, true)
                        viewModel.mModelMonitorValues.setErrorsStorage(idx + 1, true)
                    }
                }
                else
                    continue
            } else if (current is SetGasRoomViewData) {
                (current as SetGasRoomViewData).isAlert = true
                if (current.usable)
                    viewModel.mModelMonitorValues.setErrorsRoom(idx, true)
                else
                    continue
            } else if (current is SetWasteLiquorViewData) {
                (current as SetWasteLiquorViewData).isAlert = true
                if (current.usable)
                    viewModel.mModelMonitorValues.setErrorsWaste(idx, true)
                else
                    continue
            } else if (current is SetOxygenViewData) {
                (current as SetOxygenViewData).isAlert = true
//                viewModel.oxygenMasterData!!.isAlert = true
                if (current.usable)
                    viewModel.mModelMonitorValues.setErrorsOxygen(idx, true)
                else
                    continue
            } else if (current is SetSteamerViewData) {
                (current as SetSteamerViewData).isAlertLow = true
                (current as SetSteamerViewData).isAlertTemp = true
                if (current.usable)
                    viewModel.mModelMonitorValues.setErrorsSteam(idx, true)
                else
                    continue
            }

            viewModel.addAlertInfo(
                tmp.key,
                SetAlertData(
                    getLatest_time(System.currentTimeMillis()),
                    model,
                    id,
                    "AQ 신호 이상(통신불가)",
                    port,
                    true
                )
            )
            lostConnectAQs[tmp.key] = true
        }

        for (tmp in lastaqs) {
            lostConnectAQs.remove(tmp)
            // 경고 해제 전달

            val current = setAQport[tmp]

            val alertinfo = viewModel.alertMap[tmp]
            if (alertinfo == alertinfo || !alertinfo!!.isAlert) {
                if (current is SetGasStorageViewData) {
                    (current as SetGasStorageViewData).isAlert = false
                    current.isAlertRight = false
                    current.isAlertLeft = false
                } else if (current is SetGasRoomViewData)
                    (current as SetGasRoomViewData).isAlert = false
                else if (current is SetWasteLiquorViewData)
                    (current as SetWasteLiquorViewData).isAlert = false
                else if (current is SetOxygenViewData) {
                    (current as SetOxygenViewData).isAlert = false
//                    viewModel.oxygenMasterData!!.isAlert = false
                } else if (current is SetSteamerViewData) {
                    (current as SetSteamerViewData).isAlertLow = false
                    (current as SetSteamerViewData).isAlertTemp = false
                }
            }
//            alertMapClear()
            alertMap.remove(tmp)
            alertMap2.remove(tmp)
            alertBase.remove(tmp)

            val aqInfo = HexDump.toByteArray(tmp)
            val model = aqInfo[3].toInt()
            val id = aqInfo[2].toInt()
            val port = aqInfo[1].toInt()

            viewModel.addAlertInfo(
                tmp,
                SetAlertData(
                    getLatest_time(System.currentTimeMillis()),
                    model,
                    id,
                    "AQ 신호 정상",
                    port,
                    false
                )
            )
        }
    }

    init {
        LoadSetting()
    }
}