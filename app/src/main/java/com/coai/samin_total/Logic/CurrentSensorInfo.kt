package com.coai.samin_total.Logic

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class CurrentSensorInfo(sensorInfo:List<Byte>) {

    private var model:Byte
    private var id:Byte
    private var mSensorData:ByteArray = byteArrayOf()
    private var latest_date:String
    private var latest_time:String
    private lateinit var sensorData:ByteArray


    constructor(sensorInfo:List<Byte>, data:ByteArray) : this(sensorInfo) {
        sensorData = data
    }

//    fun getAQ_Model() = model
//    fun getAQ_Id() = id
    fun getSensorData() = mSensorData
    fun getLatestData() = latest_date
    fun getLatestTime() = latest_time

    init {
        val current = LocalDateTime.now()
        model = sensorInfo.get(0)
        id = sensorInfo.get(1)
        latest_date = current.format(DateTimeFormatter.ISO_LOCAL_DATE)
        latest_time = current.format(DateTimeFormatter.ISO_LOCAL_TIME)
    }
}