package com.coai.samin_total.Dialog

data class SetAlertData(
    val time: String,
    val model:Int,
    val id:Int,
    val content : String,
    val port:Int,
    var isAlert:Boolean,
    var alertState: Int = 0,
    var humtempAlertBit :Byte = 0.toByte()
)
