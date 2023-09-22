package com.coai.samin_total.Logic

enum class Baudrate(var value: Int) {
    BPS_2400(2400),
    BPS_4800(4800),
    BPS_9600(9600),
    BPS_14400(14400),
    BPS_19200(19200),
    BPS_28800(28800),
    BPS_38400(38400),
    BPS_57600(57600),
    BPS_76800(76800),
    BPS_115200(115200),
    BPS_230400(230400),
    BPS_250000(250000),
    BPS_500000(500000),
    BPS_1000000(1000000);
    companion object : EnumCodesMap<Baudrate, Int> by EnumCodesMap({it.value})
}