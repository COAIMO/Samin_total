package com.coai.samin_total.Logic

import android.util.Log
import kotlin.experimental.inv

class SaminProtocol {
    var mProtocol: ByteArray = ByteArray(20)
    /**
     * model (저장고, 룸, 폐액통...)
     * id 대상 ID
     * data 전달 데이터
     */
    fun buildProtocol(model: Byte, id: Byte, mode: Byte, data: Byte?) {
        val length: Byte
        if (data == null) {
            length = 0x02.toByte()
            mProtocol[4] = length
        } else {
            //Length
            length = 0x03.toByte()
            mProtocol[4] = length
            mProtocol[7] = data
        }
        //Header
        mProtocol[0] = 0xFF.toByte()
        mProtocol[1] = 0xFE.toByte()
        mProtocol[2] = model
        mProtocol[3] = id
        mProtocol[6] = mode

        mProtocol[5] = checkSum()
    }

    fun checkSum(): Byte {
        //if()
        // mProtocol[4]
        // ===============================
        //                                   4 + 1
        //                                   ========================================
        // Checksum(1) + Mode(1) + Data(?) + Header(2) + Model(1) + ID(1) + Length(1)
        val datacount = mProtocol[4] + 4 + 1
        return (mProtocol.toUByteArray().take(datacount).sum()
                - mProtocol[0].toUByte()
                - mProtocol[1].toUByte()
                - mProtocol[5].toUByte()
                )
            .toUByte()
            .inv()
            .toByte()

    }

    /**
     * Tab.송신 - 제품 확인
     */
    fun checkModel(model: Byte, id: Byte) {
        buildProtocol(model, id, 0xA0.toByte(), null)
    }

    /**
     * Tab.송신 - 피드백 요청
     */
    fun feedBack(model: Byte, id: Byte){
        buildProtocol(model, id, 0xA1.toByte(), null)
    }

    /**
     * Tab.송신 - 부저 on
     */
    fun buzzer_On(model: Byte, id: Byte){
        buildProtocol(model, id, 0xA2.toByte(), 0x01)
    }

    /**
     * Tab.송신 - 부저 off
     */
    fun buzzer_Off(model: Byte, id: Byte){
        buildProtocol(model, id, 0xA2.toByte(), 0x00)
    }

    /**
     * Tab.송신 - LED Normal State
     */
    fun led_NormalState(model: Byte, id: Byte){
        buildProtocol(model, id, 0xA3.toByte(), 0x1F)
    }

    /**
     * Tab.송신 - LED Alert State
     */
    fun led_AlertState(model: Byte, id: Byte){
        buildProtocol(model, id, 0xA3.toByte(), 0x00)
    }
    var modelName: String? = null

    @ExperimentalUnsignedTypes
    fun parse(data: ByteArray): Boolean {
        var ret = false
        try {
            mProtocol = ByteArray(data.size)
            data.copyInto(mProtocol!!, endIndex = mProtocol!!.size)

            if (mProtocol[4] + 4 + 1 != data.size)
                return ret

            val chksum = checkSum()
            if (mProtocol[5] == chksum) {
                try {
                    modelName = SaminModel.codesMap[mProtocol!![2].toUByte()].toString()
//                    PacketName = Data!![5].toString().format("%g")
                    ret = true
                } catch (e: Exception) {
                    ret = false
                }
            } else ret = false

        } catch (e: Exception) {
            Log.e("TAG", "Error", e)
        }
        return ret
    }
}