package com.coai.samin_total.WasteLiquor

data class SetWasteLiquorViewData(
    val id:Int,
    val port:Int,
    var liquidName: String = "폐액",
    var isAlert: Boolean = false
)
