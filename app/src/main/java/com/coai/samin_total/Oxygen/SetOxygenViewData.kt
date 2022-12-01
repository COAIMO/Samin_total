package com.coai.samin_total.Oxygen

import kotlinx.serialization.Serializable

@Serializable
data class SetOxygenViewData(
    val model: String,
    var id: Int = 0,
    val port: Int,
    var sensorType: String = "LOX-02",
    var usable: Boolean = true,
    var isAlert: Boolean = false,
    var setValue: Float = 0f,
    var setMinValue: Float = 19.2f,
    var setMaxValue: Float = 23f,
    val modelByte: Byte = 4,
    var heartbeatCount : UByte = 0u,
    var zeroPoint: Float? = 0f,
    var name:String = "산소"
)
