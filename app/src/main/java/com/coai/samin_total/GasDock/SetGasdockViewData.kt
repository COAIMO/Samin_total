package com.coai.samin_total.GasDock

data class SetGasdockViewData(
    var ViewType: Int,
    var gasName: String,
    var gasColor: Int,
    var pressure_Min: Float? = null,
    var pressure_Max: Float? = null,
    var gasIndex: Int? = null,
    var isAlert: Boolean? = false,
    var isAlertLeft: Boolean? = false,
    var isAlertRight: Boolean? = false,
    var pressure: Float? = null
)
