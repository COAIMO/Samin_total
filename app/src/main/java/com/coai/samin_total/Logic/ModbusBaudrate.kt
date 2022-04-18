package com.coai.samin_total.Logic

import com.coai.uikit.wheeline.EnumCodesMap

enum class ModbusBaudrate(var value: Int) {
    BPS_9600(9600),
    BPS_19200(19200),
    BPS_57600(57600),
    BPS_115200(115200),
    BPS_230400(230400),
    BPS_250000(250000),
    BPS_500000(500000),
    BPS_1000000(1000000);
    companion object : EnumCodesMap<ModbusBaudrate, Int> by EnumCodesMap({ it.value })
}