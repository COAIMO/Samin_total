package com.coai.samin_total.Logic

enum class SaminProtocolMode(val byte: Byte) {
    CheckProduct(0xA0.toByte()),
    RequestFeedBack(0xA1.toByte()),
    SetBuzzer(0xA2.toByte()),
    SetLED(0xA2.toByte()),
    CheckProductPing(0x00.toByte()),
    RequestFeedBackPing(0x01.toByte()),
    SettingShare(0xB0.toByte());
    companion object : EnumCodesMap<SaminProtocolMode, Byte> by EnumCodesMap({ it.byte })

}
