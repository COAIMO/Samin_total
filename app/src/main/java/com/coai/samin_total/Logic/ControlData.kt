package com.coai.samin_total.Logic

/**
 * 관제
 */
data class ControlData(
    var useSettingShare: Boolean = false,
    var isMirrorMode: Boolean = false,
    var useModbusRTU: Boolean = false,
    var modbusBaudrate: ModbusBaudrate = ModbusBaudrate.BPS_9600,
    var modbusRTUID: Int = 1,
//    val useModbusTCP: Boolean = false,
//    val useModbusTCPPORT: Int = 1502,
//    val useModbusUDP: Boolean = false,
//    val useModbusUDPPORT: Int = 5502
)
