package com.coai.samin_total

import android.util.Log
import com.coai.samin_total.GasDock.SetGasStorageViewData
import com.coai.samin_total.GasRoom.SetGasRoomViewData
import com.coai.samin_total.GasRoom.TimePSI
import com.coai.samin_total.Logic.AnalyticUtils
import com.coai.samin_total.Oxygen.SetOxygenViewData
import com.coai.samin_total.Steamer.SetSteamerViewData
import com.coai.samin_total.WasteLiquor.SetWasteLiquorViewData

class AQDataParser(viewModel: MainViewModel) {
    val hmapAQPortSettings = HashMap<Int, Any>()
    val viewModel: MainViewModel = viewModel


    // 최종 숫신시간
    val hmapLastedDate = HashMap<Int, Long>()
    val hmapPsis = HashMap<Int, ArrayList<TimePSI>>()

    val alertBase = HashMap<Int, Float>()

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
        Log.d("ProcessGasRoom", "id: ${tmp.id}, port:${tmp.port}value : $value")
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
        }else{
            if (alertBase.containsKey(id)){

                if (alertBase[id]!! + 2 > value) {
                    tmp.isAlert = false
                }
            }

        }


    }

    private fun ProcessGasStorage(id: Int, data: Int) {
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
        if (tmp.ViewType == 1 || tmp.ViewType ==2){
            if (tmp.port ==1) tmp.pressureLeft = value
        }
    }

    private fun ProcessWasteLiquor(id: Int, data: Int) {
        val tmp1 = hmapAQPortSettings[id] ?: return
        val tmp = (tmp1 as SetWasteLiquorViewData)
        // 수위 초과일때 0 아니면 1
        tmp.isAlert = data == 0

        Log.d("ProcessWasteLiquor", "value : $")

    }

    private fun ProcessOxygen(id: Int, data: Int) {
        val tmp1 = hmapAQPortSettings[id] ?: return
        val tmp = (tmp1 as SetOxygenViewData)
        val oxygenValue = data / 100
        tmp.setValue = oxygenValue

        if (tmp.setMinValue > oxygenValue) {
            tmp.isAlert = true
        } else if (tmp.setMaxValue < oxygenValue) {
            tmp.isAlert = false
        }

    }

    private fun ProcessSteamer(id: Int, temp: Int, level:Int) {
        val tmp1 = hmapAQPortSettings[id] ?: return
        val tmp = (tmp1 as SetSteamerViewData)

        tmp.isTemp = temp/33
        tmp.unit
        //설정 온도보다 현재 온도가 낮을경우 알람
        tmp.isAlertTemp = tmp.isTempMin > tmp.isTemp

        //센서가 물에 담겨져있지 않다면(1000보다 클 경우) 알람
        tmp.isAlertLow = level > 1000


    }

    /**
     * 수신 데이터 처리
     */
    fun Parser(arg: ByteArray) {
        synchronized(this) {
            val id = arg[3]
            val model = arg[2]

            val time = System.currentTimeMillis()
            val datas = ArrayList<Int>()

            datas.add(littleEndianConversion(arg.slice(7..8).toByteArray()))
            datas.add(littleEndianConversion(arg.slice(9..10).toByteArray()))
            datas.add(littleEndianConversion(arg.slice(11..12).toByteArray()))
            datas.add(littleEndianConversion(arg.slice(13..14).toByteArray()))

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

                    for (m in viewModel.GasStorageDataLiveList.value!!){

                    }

                    var loop = 1
                    for (tmp in datas) {
                        //아이디 1개당 포트 4개 추가
                        val port = loop++.toByte()
                        //키는 아이디 포트
                        val key = littleEndianConversion(byteArrayOf(model, id.toByte(), port))
                        hmapLastedDate[key] = time
                        ProcessGasStorage(key, tmp)
                    }
                }
                0x02.toByte() -> {
                    var loop = 1
                    for (tmp in datas) {
                        //아이디 1개당 포트 4개 추가
                        val port = loop++.toByte()
                        //키는 아이디 포트
                        val key = littleEndianConversion(byteArrayOf(model, id.toByte(), port))
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
                        val key = littleEndianConversion(byteArrayOf(model, id.toByte(), port))
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
                        val level_data = datas[loop + 2]
                        val key = littleEndianConversion(byteArrayOf(model, id.toByte(), loop.toByte()))
                        hmapLastedDate[key] = time
                        ProcessSteamer(key, temp_data, level_data)

                    }
                }
            }
        }
    }

    init {
        LoadSetting()
    }
}