package com.coai.samin_total.Steamer

data class SetSteamerViewData(
    val id:Int,
    val port:Int,
    // 수위 알람
    var isAlertLow:Boolean = false,
    // min 온도 설정
    var isTempMin:Int = 0,
    //현재 온도
    var isTemp:Int = 0
)
//var isAlertTemp:Boolean = false,
