package com.coai.samin_total.TempHum

import kotlinx.serialization.Serializable

@Serializable
data class SetTempHumViewData(
    val model: String,
    val id: Int,
    val port: Int,
    var usable: Boolean = true,
    var temphumName: String = "온습도",
    var unit:Int = 0,
    var setTempMax: Float = 32f,
    var setTempMin: Float = 18f,
    var setHumMax: Float = 55f,
    var setHumMin: Float = 35f,
    var temp: Float = 0f,
    var hum: Float = 0f,
    var isAlert: Boolean = false,
    var isTempAlert: Boolean = false,
    var isHumAlert: Boolean = false,
    val modelByte: Byte = 6,
    var setTempZeroPoint: Float = 0f,
    var setHumZeroPoint: Float = 0f,
    var heartbeatCount: UByte = 0u
)
