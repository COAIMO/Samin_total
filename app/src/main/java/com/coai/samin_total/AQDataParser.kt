package com.coai.samin_total

import android.util.Log
import com.coai.samin_total.Dialog.SetAlertData
import com.coai.samin_total.GasDock.SetGasStorageViewData
import com.coai.samin_total.GasRoom.SetGasRoomViewData
import com.coai.samin_total.GasRoom.TimePSI
import com.coai.samin_total.Logic.AnalyticUtils
import com.coai.samin_total.Logic.PortAlertState
import com.coai.samin_total.Oxygen.SetOxygenViewData
import com.coai.samin_total.Service.HexDump
import com.coai.samin_total.Steamer.SetSteamerViewData
import com.coai.samin_total.WasteLiquor.SetWasteLiquorViewData
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Exception
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AQDataParser(viewModel: MainViewModel) {
    val hmapAQPortSettings = HashMap<Int, Any>()
    val viewModel: MainViewModel = viewModel
    val setAQport = HashMap<Int, Any>()


    // 최종 숫신시간
    val hmapLastedDate = HashMap<Int, Long>()
    val hmapPsis = HashMap<Int, ArrayList<TimePSI>>()

    val alertBase = HashMap<Int, Float>()
    val alertMap = HashMap<Int, Boolean>()
    val alertMap2 = HashMap<Int, Boolean>()

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
            hmapAQPortSettings.clear()
            setAQport.clear()
            hmapLastedDate.clear()

            for (t in hmapPsis.values)
                t.clear()

            hmapPsis.clear()
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
    private fun ProcessSingleGasStorage(id: Int, data: Int) {
        val tmp1 = hmapAQPortSettings[id] ?: return
        val tmp = (tmp1 as SetGasStorageViewData)

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
                    tmp.pressure_Max!!,
                    tmp.rewardValue,
                    tmp.zeroPoint
                )
            }
        }
        tmp.pressure = value
        if (tmp.pressure_Min!! > tmp.pressure!!) {
            tmp.isAlert = true

            if (alertMap[id] == null) {
                alertMap.put(id, true)
                viewModel.gasStorageAlert.value = true
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
            if (alertMap.containsKey(id)) {
                tmp.isAlert = false
                viewModel.gasStorageAlert.value = false
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
    }


    private fun ProcessDualGasStorage(id: Int, left_value: Int, right_value: Int) {
        val tmp1 = hmapAQPortSettings[id] ?: return
        val tmp = (tmp1 as SetGasStorageViewData)

        var left_pressure: Float = 0f
        var right_pressure: Float = 0f

        when (tmp.sensorType) {
            "Sensts 142PSI" -> {
                left_pressure = calcPSI142(left_value.toFloat(), tmp.rewardValue, tmp.zeroPoint)
                right_pressure = calcPSI142(right_value.toFloat(), tmp.rewardValue, tmp.zeroPoint)

            }
            "Sensts 2000PSI" -> {
                left_pressure = calcPSI2000(left_value.toFloat(), tmp.rewardValue, tmp.zeroPoint)
                right_pressure = calcPSI2000(right_value.toFloat(), tmp.rewardValue, tmp.zeroPoint)

            }
            else -> {
                left_pressure = calcSensor(
                    left_value.toFloat(),
                    tmp.pressure_Max!!,
                    tmp.rewardValue,
                    tmp.zeroPoint
                )
                right_pressure = calcSensor(
                    right_value.toFloat(),
                    tmp.pressure_Max!!,
                    tmp.rewardValue,
                    tmp.zeroPoint
                )
            }
        }
        tmp.pressureLeft = left_pressure
        tmp.pressureRight = right_pressure

        if (tmp.pressure_Min!! > tmp.pressureLeft!!) {
            tmp.isAlertLeft = true

            if (alertMap[id] == null) {
                alertMap.put(id, true)
                viewModel.gasStorageAlert.value = true
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
            if (alertMap.containsKey(id)) {
                tmp.isAlertLeft = false
                viewModel.gasStorageAlert.value = false
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
                viewModel.gasStorageAlert.value = true
                viewModel.addAlertInfo(
                    id + 65536,
                    SetAlertData(
                        getLatest_time(hmapLastedDate[id]!!),
                        tmp.modelByte.toInt(),
                        tmp.id + 65536,
                        "가스 압력 하한 값",
                        tmp.port + 1,
                        true
                    )
                )
            }

        } else if (tmp.pressure_Min!! < tmp.pressureRight!!) {
            if (alertMap2.containsKey(id)) {
                tmp.isAlertRight = false
                viewModel.gasStorageAlert.value = false
                viewModel.addAlertInfo(
                    id + 65536,
                    SetAlertData(
                        getLatest_time(hmapLastedDate[id]!!),
                        tmp.modelByte.toInt(),
                        tmp.id + 65536,
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
    }


    private fun ProcessGasRoom(id: Int, data: Int) {
        // 설정이 존재하는지 확인
        // val tmp = (hmapAQPortSettings[id] as SetGasRoomViewData) ?: return
        val tmp1 = hmapAQPortSettings[id] ?: return
        val tmp = (tmp1 as SetGasRoomViewData)

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
                viewModel.gasRoomAlert.value = true
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
                    viewModel.gasRoomAlert.value = false
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

        }
        val bro = setAQport[id] as SetGasRoomViewData
        bro.isAlert = tmp.isAlert
        bro.pressure = tmp.pressure
    }

    private fun ProcessWasteLiquor(id: Int, data: Int) {
        val tmp1 = hmapAQPortSettings[id] ?: return
        val tmp = (tmp1 as SetWasteLiquorViewData)
        // 수위 초과일때 0 아니면 1
        tmp.isAlert = data == 0

        val key =
            littleEndianConversion(byteArrayOf(tmp.modelByte, tmp.id.toByte())).toShort()
        Log.d(
            "ProcessWasteLiquor",
            "Model:${tmp.model} ID: ${tmp.id} Port:${tmp.port}}// data:${data}"
        )
        if (data == 0) {
            tmp.isAlert = true
            if (alertMap[id] == null) {
                alertMap.put(id, true)
                viewModel.wasteAlert.value = true
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
            if (alertMap.containsKey(id)) {
                tmp.isAlert = false
                viewModel.wasteAlert.value = false
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
    }

    private fun ProcessOxygen(id: Int, data: Int) {
        val tmp1 = hmapAQPortSettings[id] ?: return
        val tmp = (tmp1 as SetOxygenViewData)
        val oxygenValue = data / 100
        tmp.setValue = oxygenValue

        if (tmp.setMinValue > oxygenValue) {
            tmp.isAlert = true
            if (alertMap[id] == null) {
                alertMap.put(id, true)
                viewModel.oxyenAlert.value = true
                viewModel.addAlertInfo(
                    id,
                    SetAlertData(
                        getLatest_time(hmapLastedDate[id]!!),
                        tmp.modelByte.toInt(),
                        tmp.id,
                        "산소농도 하한 값",
                        tmp.port,
                        true
                    )
                )
            }
        } else {
            if (alertMap.containsKey(id)) {
                tmp.isAlert = false
                viewModel.oxyenAlert.value = false
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

        if (tmp.setMaxValue < oxygenValue) {
            tmp.isAlert = true
            if (alertMap[id] == null) {
                alertMap.put(id, true)
                viewModel.oxyenAlert.value = true
                viewModel.addAlertInfo(
                    id,
                    SetAlertData(
                        getLatest_time(hmapLastedDate[id]!!),
                        tmp.modelByte.toInt(),
                        tmp.id,
                        "산소농도 상한 값",
                        tmp.port,
                        false
                    )
                )
            }
        } else {
            if (alertMap.containsKey(id)) {
                tmp.isAlert = false
                viewModel.oxyenAlert.value = false
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
        val bro = setAQport[id] as SetOxygenViewData
        bro.setValue = tmp.setValue
        bro.isAlert = tmp.isAlert
    }

    private fun ProcessSteamer(id: Int, temp: Int, level: Int) {
        val tmp1 = hmapAQPortSettings[id] ?: return
        val tmp = (tmp1 as SetSteamerViewData)

        tmp.isTemp = temp / 33
        tmp.unit
        //설정 온도보다 현재 온도가 낮을경우 알람
        if (tmp.isTempMin > tmp.isTemp) {
            tmp.isAlertTemp = true

            if (alertMap[id] == null) {
                alertMap.put(id, true)
                viewModel.steamerAlert.value = true
                viewModel.addAlertInfo(
                    id,
                    SetAlertData(
                        getLatest_time(hmapLastedDate[id]!!),
                        tmp.modelByte.toInt(),
                        tmp.id,
                        "온도 하한 값",
                        tmp.port,
                        true
                    )
                )
            }

        } else {
            if (alertMap.containsKey(id)) {
                viewModel.steamerAlert.value = false
                tmp.isAlertTemp = false
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
                viewModel.steamerAlert.value = true
                viewModel.addAlertInfo(
                    id + 512,
                    SetAlertData(
                        getLatest_time(hmapLastedDate[id]!!),
                        tmp.modelByte.toInt(),
                        tmp.id + 512,
                        "수위 레벨 하한 값",
                        tmp.port + 2,
                        false
                    )
                )
            }

        } else {
            if (alertMap2.containsKey(id)) {
                viewModel.steamerAlert.value = false
                tmp.isAlertTemp = false
                viewModel.addAlertInfo(
                    id + 512,
                    SetAlertData(
                        getLatest_time(hmapLastedDate[id]!!),
                        tmp.modelByte.toInt(),
                        tmp.id + 512,
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

//            if (model.equals(0x02.toByte())) {
//                // 룸가스 일 경우
//                var loop = 1
//                for (tmp in datas) {
//                    //아이디 1개당 포트 4개 추가
//                    val port = loop++.toByte()
//                    //키는 아이디 포트
//                    val key = littleEndianConversion(byteArrayOf(id.toByte(), port))
//                    hmapLastedDate[key] = time
//                    ProcessGasRoom(key, tmp)
//                }
//            }

                when (model) {
                    0x01.toByte() -> {
                        if (hmapAQPortSettings.size > 0) {
//                        try {
//                            dockSetting = hmapAQPortSettings.filter {
//                                (it.value as SetGasStorageViewData).modelByte == 1.toByte()
//                            }
//                        } catch (e: Exception) {
//                        }

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
            SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale("ko", "KR"))
        val date: Date = Date(time)
        return dateformat.format(date)
    }

    val alphavalue = 0.25
    //들어온값이 이상하게 들어오는거 방지
    fun getLPF(x: Int, key: Int): Int {

        if (!exSensorData.containsKey(key)) {
            exSensorData.put(key, x)
            return x
        }

        val prev = exSensorData[key]
        val ret = alphavalue * prev!! + (1 - alphavalue) * x
        return ret.toInt()
    }

    init {
        LoadSetting()
    }
}