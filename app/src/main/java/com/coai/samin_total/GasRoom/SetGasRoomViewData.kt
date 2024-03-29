package com.coai.samin_total.GasRoom

import android.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
data class SetGasRoomViewData(
    val model: String,
    val id: Int,
    val port: Int,
    var usable: Boolean = true,
    var sensorType: String = "WIKAI 16BAR",
    var gasName: String = "Air",
    var gasColor: Int = Color.parseColor("#6599CD"),
    var pressure: Float = 0f,
    var pressure_Max: Float = 232.06f,
    var gasUnit: Int = 0,
    var gasIndex: Int = 0,
    var isSlopeAlert: Boolean = false,
    var zeroPoint: Float = 0f,
    var rewardValue: Float = 1f,
    var unit: Int = 0,
    var slopeValue: Float = -10f,
    val modelByte: Byte = 2,
    var heartbeatCount : UByte = 0u,
    var leakTest:Boolean = false,
    var limit_max: Float = 232.06f,
    var limit_min: Float = 0f,
    var isPressAlert: Boolean = false,
    var isAlert:Boolean = false
)
