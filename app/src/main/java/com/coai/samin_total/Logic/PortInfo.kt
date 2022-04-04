package com.coai.samin_total.Logic

import java.text.SimpleDateFormat
import java.util.*

data class PortInfo(
    var model: Byte,
    var id: Byte,
    var port: Int,
    var alert_1: Boolean = false,
    var alert_2: Boolean = false,
    var alert_3: Boolean = false,
    val dateformat: SimpleDateFormat = SimpleDateFormat("yyyy-mm-dd kk:mm:ss", Locale("ko", "KR")),
    val date: Date = Date(System.currentTimeMillis()),
    var latest_time: String = dateformat.format(date)
)
