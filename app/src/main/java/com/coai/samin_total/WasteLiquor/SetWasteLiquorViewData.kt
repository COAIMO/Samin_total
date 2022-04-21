package com.coai.samin_total.WasteLiquor

import kotlinx.serialization.Serializable

@Serializable
data class SetWasteLiquorViewData(
    val model: String,
    val id:Int,
    val port:Int,
    var usable: Boolean = true,
    var liquidName: String = "폐액",
    var isAlert: Boolean = false,
    var level_SensorType: String = "LEVEL-01",
    val modelByte: Byte = 3,
    var heartbeatCount : UByte = 0u

)
