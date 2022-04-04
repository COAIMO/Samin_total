package com.coai.samin_total.Logic

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.coai.samin_total.Dialog.SetAlertData
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class CurrentSensorInfo(sensorInfo: List<Byte>) {

    private var model: Byte
    private var id: Byte
    private var mSensorData: ByteArray = byteArrayOf()
//    private var latest_date: String
    private var latest_time: String
    private lateinit var sensorData: ByteArray
    var pin1_Alert = false
    var pin2_Alert = false
    var pin3_Alert = false
    var pin4_Alert = false


    constructor(sensorInfo: List<Byte>, data: ByteArray) : this(sensorInfo) {
        sensorData = data
        val pin1_data = littleEndianConversion(
            sensorData.slice(0..1).toByteArray()
        )
        val pin2_data = littleEndianConversion(
            sensorData.slice(2..3).toByteArray()
        )
        val pin3_data = littleEndianConversion(
            sensorData.slice(4..5).toByteArray()
        )
        val pin4_data = littleEndianConversion(
            sensorData.slice(6..7).toByteArray()
        )
        when (model.toInt()) {
            1 -> {

            }
            2 -> {

            }
            3 -> {
                if (pin1_data == 0) {
//                    i.isAlert = true
//                    val currentTime = System.currentTimeMillis()
//                    val date = Date(currentTime)
//                    val dateformat = SimpleDateFormat("yyyy-mm-dd kk:mm:ss", Locale("ko", "KR"))
//                    val str_date = dateformat.format(date)
//                    mainViewModel.alertInfo.add(
//                        SetAlertData(
//                        str_date,
//                        receiveParser.mProtocol.get(2).toInt(),
//                        receiveParser.mProtocol.get(3).toInt(),
//                        "고수위 알람"
//                    )
//                    )

                } else {
//                    i.isAlert = false
                }
            }
            4 -> {

            }
            5 -> {

            }
        }
    }

    fun getAQ_Model() = model
    fun getAQ_Id() = id
    fun getSensorData() = mSensorData
//    fun getLatestData() = latest_date
    fun getLatestTime() = latest_time

    fun littleEndianConversion(bytes: ByteArray): Int {
        var result = 0
        for (i in bytes.indices) {
            result = result or (bytes[i].toUByte().toInt() shl 8 * i)
        }
        return result
    }

    init {
//        val current = LocalDateTime.now()
//        latest_date = current.format(DateTimeFormatter.ISO_LOCAL_DATE)
        model = sensorInfo.get(0)
        id = sensorInfo.get(1)
        val currentTime = System.currentTimeMillis()
        val date = Date(currentTime)
        val dateformat = SimpleDateFormat("yyyy-mm-dd kk:mm:ss", Locale("ko", "KR"))
        latest_time = dateformat.format(date)
    }
}