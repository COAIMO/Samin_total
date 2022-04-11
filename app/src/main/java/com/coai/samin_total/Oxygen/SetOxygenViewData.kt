package com.coai.samin_total.Oxygen

data class SetOxygenViewData(
    val model: String,
    var id: Int = 0,
    val port: Int,
    var sensorType: String = "LOX-02",
    var usable: Boolean = true,
    var isAlert: Boolean = false,
    var setValue: Int = 0,
    var setMinValue: Int = 20,
    var setMaxValue: Int = 23,
    val modelByte: Byte = 4,
    var heartbeatCount : UByte = 0u

)
