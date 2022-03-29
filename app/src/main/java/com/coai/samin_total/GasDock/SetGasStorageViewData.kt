package com.coai.samin_total.GasDock

import android.graphics.Color

data class SetGasStorageViewData(
    val id: Int,
    val port: Int,
    var ViewType: Int,
    var gasName: String,
    var gasColor: Int,
    var pressure_Min: Float? = null,
    var pressure_Max: Float? = null,
    var gasIndex: Int? = null,
    var isAlert: Boolean? = false,
    var isAlertLeft: Boolean? = false,
    var isAlertRight: Boolean? = false,
    var pressure: Float? = null,
    var pressureLeft: Float? = null,
    var pressureRight: Float? = null
//    val model: Int,
//    val id: Int,
//    val port: Int,
//    var ViewType: Int = 0,
//    var gasName: String = "Air",
//    var gasColor: Int = Color.parseColor("#6599CD"),
//    var pressure_Min: Float? = null,
//    var pressure_Max: Float? = null,
//    var gasIndex: Int? = null,
//    var isAlert: Boolean? = false,
//    var isAlertLeft: Boolean? = false,
//    var isAlertRight: Boolean? = false,
//    var pressure: Float? = null,
//    var pressureLeft: Float? = null,
//    var pressureRight: Float? = null

)
