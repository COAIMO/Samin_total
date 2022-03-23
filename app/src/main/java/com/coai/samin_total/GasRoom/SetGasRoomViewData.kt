package com.coai.samin_total.GasRoom

data class SetGasRoomViewData(
    val id: Int, val port:Int,
    var gasName: String, var gasColor: Int, var pressure: Float = 0f, var pressureMax: Float = 142f,
    var gasUnit: Int = 0, var gasIndex: Int = 0, var isAlert: Boolean = false
)
