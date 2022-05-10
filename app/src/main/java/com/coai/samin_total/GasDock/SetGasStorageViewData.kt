package com.coai.samin_total.GasDock

import android.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
data class SetGasStorageViewData(
    val model: String,
    val id: Int,
    var port: Int,
    var usable: Boolean = true,
    var ViewType: Int = 0,
    var sensorType: String = "WIKAI 160BAR",
    var gasName: String = "Air",
    var gasColor: Int? = Color.parseColor("#6599CD"),
    var pressure_Min: Float? = 232f,
    var pressure_Max: Float? = 2320.6f,
    var gasIndex: Int? = null,
    var isAlert: Boolean? = false,
    var isAlertLeft: Boolean? = false,
    var isAlertRight: Boolean? = false,
    var pressure: Float? = null,
    var pressureLeft: Float? = null,
    var pressureRight: Float? = null,
    var zeroPoint: Float = 0f,
    var rewardValue: Float = 1f,
    var unit:Int = 0,
    val modelByte: Byte = 1,
    var heartbeatCount : UByte = 0u
)