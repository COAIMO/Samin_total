package com.coai.samin_total.Steamer

import kotlinx.serialization.Serializable

@Serializable
data class SetSteamerViewData(
    val model: String,
    val id: Int,
    val port: Int,
    var usable: Boolean = true,
    // 수위 알람
    var isAlertLow: Boolean = false,
    var isAlertTemp: Boolean =false,
    // min 온도 설정
    var tempMax: Int = 0,
    //현재 온도
    var isTemp: Int = 0,
    var temp_SensorType: String = "SST2109",
    var level_SensorType: String = "BS1",
    var unit:Int = 0,
    val modelByte: Byte = 5,
    var heartbeatCount : UByte = 0u

)
