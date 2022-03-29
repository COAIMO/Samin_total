package com.coai.samin_total.GasDock

import android.graphics.Color
import java.sql.Struct

data class SetGasStorageViewData(
//    val id: Int,
//    val port: Int,
//    var ViewType: Int,
//    var gasName: String,
//    var gasColor: Int,
//    var pressure_Min: Float? = null,
//    var pressure_Max: Float? = null,
//    var gasIndex: Int? = null,
//    var isAlert: Boolean? = false,
//    var isAlertLeft: Boolean? = false,
//    var isAlertRight: Boolean? = false,
//    var pressure: Float? = null,
//    var pressureLeft: Float? = null,
//    var pressureRight: Float? = null

    val model: String,
    val id: Int,
    val port: Int,
    var usable: Boolean = true,
    var ViewType: Int = 0,
    var sensorType: String = "Sensts 142PSI",
    var gasName: String = "Air",
    var gasColor: Int? = null,
    var pressure_Min: Float? = 0f,
    var pressure_Max: Float? = 2000f,
    var gasIndex: Int? = null,
    var isAlert: Boolean? = false,
    var isAlertLeft: Boolean? = false,
    var isAlertRight: Boolean? = false,
    var pressure: Float? = null,
    var pressureLeft: Float? = null,
    var pressureRight: Float? = null,
    var zeroPoint: Float = 0f,
    var rewardValue: Float = 1f

)
