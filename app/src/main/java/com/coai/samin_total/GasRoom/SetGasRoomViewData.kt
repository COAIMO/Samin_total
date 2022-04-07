package com.coai.samin_total.GasRoom

import android.graphics.Color

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
    var isAlert: Boolean = false,
    var zeroPoint: Float = 0f,
    var rewardValue: Float = 1f,
    var unit:Int = 0,
    var slopeValue: Float = 1f
)
