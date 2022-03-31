package com.coai.samin_total.WasteLiquor

data class SetWasteLiquorViewData(
    val model: String,
    val id:Int,
    val port:Int,
    var usable: Boolean = true,
    var liquidName: String = "폐액",
    var isAlert: Boolean = false,
    var level_SensorType: String = "LEVEL-01"
)
