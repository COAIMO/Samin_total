package com.coai.samin_total

import android.util.Log
import com.coai.samin_total.GasRoom.SetGasRoomViewData
import com.coai.samin_total.GasRoom.TimePSI
import com.coai.samin_total.Logic.AnalyticUtils

class AQDataParser(viewModel: MainViewModel) {
    val hmapAQPortSettings = HashMap<Int, Any>()
    val viewModel: MainViewModel = viewModel


    // 최종 숫신시간
    val hmapLastedDate = HashMap<Int, Long>()
    val hmapPsis = HashMap<Int, ArrayList<TimePSI>>()

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
            val key = littleEndianConversion(byteArrayOf(tmp.id.toByte(), tmp.port.toByte()))
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

    /**
     * 가스 룸 로직
     * Todo: 에러 체크 기능 필요. 정상화 체크 필요.
     */
    private fun ProcessGasRoom(id: Int, data: Int) {
        // 설정이 존재하는지 확인
        // val tmp = (hmapAQPortSettings[id] as SetGasRoomViewData) ?: return
        val tmp1 = hmapAQPortSettings[id] ?: return
        val tmp = (tmp1 as SetGasRoomViewData)
        Log.d("ProcessGasRoom", "설정 존재")

        Log.d("ProcessGasRoom", "${tmp?.gasName}")
        // 대상 센서에 맞는 데이터 변환 함수 호출
        var value : Float = 0f
        when (tmp.sensorType) {
            else -> {
                value = calcSensor(
                    data.toFloat(),
                    tmp.pressure_Max,
                    tmp.rewardValue,
                    tmp.zeroPoint
                )
            }
        }
        Log.d("ProcessGasRoom", "value : $value")
        tmp.pressure = value

        // 기울기 데이터 값 수집
        val item = TimePSI(hmapLastedDate[id]!!, tmp.pressure, 0x02, tmp.id, tmp.port)
        val basetime = hmapLastedDate[id]!! - 1000 * 2

        var tmppsis = hmapPsis[id]

        if (tmppsis.isNullOrEmpty()) {
            tmppsis = ArrayList<TimePSI>()
            hmapPsis[id] = tmppsis
        } // 대상 없으면

        tmppsis!!.add(item)
        val tempremove = tmppsis!!.filter {
            it.Ticks < basetime
        }
        tmppsis!!.removeAll(tempremove)

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
        Log.d("test", "$slope")
        // 기울기에 따른 에러 설정
        if (slope < tmp.slopeValue) {
            // 경고
        }
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

            if (model.equals(0x02.toByte())) {
                // 룸가스 일 경우
                var loop = 1
                for (tmp in datas) {
                    val port = loop++.toByte()
                    val key = littleEndianConversion(byteArrayOf(id.toByte(), port))
                    hmapLastedDate[key] = time
                    ProcessGasRoom(key, tmp)
                }
            }
        }
    }

    init {
        LoadSetting()
    }
}