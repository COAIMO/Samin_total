package com.coai.samin_total.Logic

import android.util.Log
import com.coai.samin_total.Service.HexDump
import com.hoho.android.usbserial.driver.Ch34xSerialDriver
import kotlin.experimental.inv

class SaminProtocol {
//    var mProtocol: ByteArray = ByteArray(20)
    lateinit var mProtocol: ByteArray
    var modelName: String? = null
    var packetName: String? = null
    var packet : Byte = 0
    /**
     * model (저장고, 룸, 폐액통...)
     * id 대상 ID
     * data 전달 데이터
     */
    fun buildProtocol(model: Byte, id: Byte, mode: Byte, data: Byte?) {
        val length: Byte
        if (data == null) {
            length = 2
            mProtocol = ByteArray(length + 5)
            mProtocol[4] = length
        } else {
            //Length
            length = 0x03.toByte()
            mProtocol = ByteArray(length + 5)
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

    fun BuildProtocoOld(model: Byte, id: Byte, mode: Byte, data: ByteArray) {
        val protocolSize: Int = 7 + data.size
        mProtocol = ByteArray(protocolSize)
        mProtocol[0] = 0xFF.toByte()
        mProtocol[1] = 0xFE.toByte()
        mProtocol[2] = model
        mProtocol[3] = id
        mProtocol[4] = (2 + data.size).toByte()
        mProtocol[6] = mode

        data.copyInto(mProtocol!!, 7, 0, data.size)
        mProtocol!![5] = checkSum()
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun checkSum(): Byte {
        //if()
        // mProtocol[4]
        // ===============================
        //                                   4 + 1
        //                                   ========================================
        // Checksum(1) + Mode(1) + Data(?) + Header(2) + Model(1) + ID(1) + Length(1)
        var result:Byte = 0.toByte()
        try {
            val datacount = mProtocol[4] + 4 + 1
            result = (mProtocol.toUByteArray().take(datacount).sum()
                    - mProtocol[0].toUByte()
                    - mProtocol[1].toUByte()
                    - mProtocol[5].toUByte()
                    )
                .toUByte()
                .inv()
                .toByte()

        }catch (e:Exception){
            Log.e("Error","$e")
        }
        return result
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
    fun feedBack(model: Byte, id: Byte) {
        buildProtocol(model, id, 0xA1.toByte(), null)
    }

    /**
     * Tab.송신 - 부저 on
     */
    fun buzzer_On(model: Byte, id: Byte) {
        buildProtocol(model, id, 0xA2.toByte(), 0x01)
    }

    /**
     * Tab.송신 - 부저 off
     */
    fun buzzer_Off(model: Byte, id: Byte) {
        buildProtocol(model, id, 0xA2.toByte(), 0x00)
    }

    /**
     * Tab.송신 - LED Normal State
     */
    fun led_NormalState(model: Byte, id: Byte) {
        buildProtocol(model, id, 0xA3.toByte(), 0x00)
    }

    /**
     * Tab.송신 - LED Alert State
     */
    fun led_AlertState(model: Byte, id: Byte, port1 : Boolean, port2: Boolean, port3:Boolean, port4: Boolean) {
        var alertData = 0x00.toByte()
        if (port1 || port2 || port3 || port4) {
            alertData = alertData.plus(16).toByte()

            if (port1)
                alertData = alertData.plus(1).toByte()
            if (port2)
                alertData = alertData.plus(2).toByte()
            if (port3)
                alertData = alertData.plus(4).toByte()
            if (port4)
                alertData = alertData.plus(8).toByte()



        }
        buildProtocol(model, id, 0xA3.toByte(), alertData)
    }

    fun led_AlertStateByte(model: Byte, id: Byte, alertData: Byte) {
        buildProtocol(model, id, 0xA3.toByte(), alertData)
    }

    /**
     * Tab.송신 - 펌웨어 버전 확인
     */
    fun checkVersoin(model: Byte, id: Byte){
        buildProtocol(model, id, 0xCD.toByte(),null)
    }

    @ExperimentalUnsignedTypes
    fun parse(data: ByteArray): Boolean {
        var ret = false
        try {
            mProtocol = ByteArray(data.size)
//            Log.d("로그", "parse : ${HexDump.dumpHexString(mProtocol)}")

            data.copyInto(mProtocol, endIndex = mProtocol.size)

            if (mProtocol[4] + 4 + 1 > data.size)
                return ret

            packet = mProtocol[6]
            packetName = SaminProtocolMode.codesMap[mProtocol[6]].toString()
            val chksum = checkSum()
            //이후 아래 2줄삭제
//            modelName = SaminModel.codesMap[mProtocol!![2].toUByte()].toString()
//            ret = true

            if (mProtocol[5] == chksum) {
                try {
                    modelName = SaminModel.codesMap[mProtocol[2].toUByte()].toString()
                    ret = true
                } catch (e: Exception) {
                    ret = false
                }
            } else ret = false

            ret = true
        } catch (e: Exception) {
            Log.e("TAG", "Error", e)
        }
        return ret
    }
}