package com.coai.samin_total.Logic

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.coai.samin_total.GasDock.SetGasStorageViewData
import com.coai.samin_total.GasRoom.GasRoomMainFragment
import com.coai.samin_total.GasRoom.SetGasRoomViewData
import com.coai.samin_total.Oxygen.SetOxygenViewData
import com.coai.samin_total.Service.HexDump
import com.coai.samin_total.Steamer.SetSteamerViewData
import com.coai.samin_total.TempHum.SetTempHumViewData
import com.coai.samin_total.WasteLiquor.SetWasteLiquorViewData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject

class SaminSharedPreference(context: Context) {
    companion object {
        const val ENABLE_PASSLOCK = 1
        const val DISABLE_PASSLOCK = 2
        const val CHANGE_PASSWORD = 3
        const val UNLOCK_PASSWORD = 4
        const val GASSTORAGE = "GasStorage"
        const val GASROOM = "GasRoom"
        const val WASTELIQUOR = "WasteLiquor"
        const val OXYGEN = "Oxygen"
        const val STEAMER = "Steamer"
        const val CONTROL = "Control"
        const val LABNAME = "LAB"
        const val MASTEROXYGEN = "MasterOxygen"
        const val TIMEOUTSTATE = "TimeOut"
        const val FEEDBACKTIMING = "FeedbackTiming"
        const val TEMPHUM = "TempHum"
        const val BAUDRATE = "Baudrate"
    }

    val labName = "Lab - 015"
    private val boardSetDataSharedPreference =
        context.getSharedPreferences("aa", Context.MODE_PRIVATE)

    fun saveHashMap(hashMap: HashMap<String, ByteArray>) {
        val mapString: String = Gson().toJson(hashMap)
        boardSetDataSharedPreference.edit().apply {
            putString("model_IDs", mapString)
            apply()
        }
    }

    fun loadHashMap(): HashMap<String, ByteArray> {
        val value = boardSetDataSharedPreference.getString("model_IDs", "")
        val token = object : TypeToken<HashMap<String, ByteArray>>() {}.type
        var map: HashMap<String, ByteArray> = hashMapOf()
        if (!value.isNullOrBlank()) {
            map = Gson().fromJson(value, token)
        }
        return map
    }

    fun saveGasStorageSetData(data: MutableList<SetGasStorageViewData>) {
        val listString = Gson().toJson(data)
        boardSetDataSharedPreference.edit().apply {
            putString("GasStorage", listString)
            apply()
        }
    }

    fun loadGasStorageSetData(): MutableList<SetGasStorageViewData> {
        val data = boardSetDataSharedPreference.getString("GasStorage", "")!!
        var setDataList = ArrayList<SetGasStorageViewData>()
        val token = object : TypeToken<MutableList<SetGasStorageViewData>>() {}.type
        if (data.isNotEmpty()) {
            setDataList = Gson().fromJson(data, token)
        }
        return setDataList
    }

    fun saveBoardSetData(key: String, data: Any) {
        when (key) {
            "GasStorage" -> {
                val listString = Gson().toJson(data)
                boardSetDataSharedPreference.edit().apply {
                    putString(key, listString)
                    apply()
                }
            }
            "GasRoom" -> {
                val listString = Gson().toJson(data)
                boardSetDataSharedPreference.edit().apply {
                    putString(key, listString)
                    apply()
                }
            }
            "WasteLiquor" -> {
                val listString = Gson().toJson(data)
                boardSetDataSharedPreference.edit().apply {
                    putString(key, listString)
                    apply()
                }
            }
            "Oxygen" -> {
                val listString = Gson().toJson(data)
                boardSetDataSharedPreference.edit().apply {
                    putString(key, listString)
                    apply()
                }
            }
            "Steamer" -> {
                val listString = Gson().toJson(data)
                boardSetDataSharedPreference.edit().apply {
                    putString(key, listString)
                    apply()
                }
            }
            "Control" -> {
                val listString = Gson().toJson(data)
                boardSetDataSharedPreference.edit().apply {
                    putString(key, listString)
                    apply()
                }
            }
            "MasterOxygen" -> {
                val listString = Gson().toJson(data)
                boardSetDataSharedPreference.edit().apply {
                    putString(key, listString)
                    apply()
                }
            }
            "TempHum" -> {
                val listString = Gson().toJson(data)
                boardSetDataSharedPreference.edit().apply {
                    putString(key, listString)
                    apply()
                }
            }
            "Baudrate" -> {
                val listString = Gson().toJson(data)
                boardSetDataSharedPreference.edit().apply {
                    putString(key, listString)
                    apply()
                }
            }
        }
    }

    fun loadBoardSetData(key: String): Any {
        var setdata = Any()
        when (key) {
            "GasStorage" -> {
                val data = boardSetDataSharedPreference.getString(key, "")!!
                var setDataList = ArrayList<SetGasStorageViewData>()
                val token = object : TypeToken<MutableList<SetGasStorageViewData>>() {}.type
                if (data.isNotEmpty()) {
                    setDataList = Gson().fromJson(data, token)
                }
                setdata = setDataList
            }
            "GasRoom" -> {
                val data = boardSetDataSharedPreference.getString(key, "")!!
                var setDataList = ArrayList<SetGasRoomViewData>()
                val token = object : TypeToken<MutableList<SetGasRoomViewData>>() {}.type
                if (data.isNotEmpty()) {
                    setDataList = Gson().fromJson(data, token)
                }
                setdata = setDataList
            }
            "WasteLiquor" -> {
                val data = boardSetDataSharedPreference.getString(key, "")!!
                var setDataList = ArrayList<SetWasteLiquorViewData>()
                val token = object : TypeToken<MutableList<SetWasteLiquorViewData>>() {}.type
                if (data.isNotEmpty()) {
                    setDataList = Gson().fromJson(data, token)
                }
                setdata = setDataList
            }
            "Oxygen" -> {
                val data = boardSetDataSharedPreference.getString(key, "")!!
                var setDataList = ArrayList<SetOxygenViewData>()
                val token = object : TypeToken<MutableList<SetOxygenViewData>>() {}.type
                if (data.isNotEmpty()) {
                    setDataList = Gson().fromJson(data, token)
                }
                setdata = setDataList
            }
            "Steamer" -> {
                val data = boardSetDataSharedPreference.getString(key, "")!!
                var setDataList = ArrayList<SetSteamerViewData>()
                val token = object : TypeToken<MutableList<SetSteamerViewData>>() {}.type
                if (data.isNotEmpty()) {
                    setDataList = Gson().fromJson(data, token)
                }
                setdata = setDataList
            }
            "Control" -> {
                val data = boardSetDataSharedPreference.getString(key, "")!!
                var setDataList = ControlData()
                val token = object : TypeToken<ControlData>() {}.type
                if (data.isNotEmpty()) {
                    setDataList = Gson().fromJson(data, token)
                }
                setdata = setDataList
            }
            "MasterOxygen" -> {
                val data = boardSetDataSharedPreference.getString(key, "")!!
                val token = object : TypeToken<SetOxygenViewData>() {}.type
                if (data.isNotEmpty()) {
                    setdata = Gson().fromJson(data, token)
                }
            }
            "TempHum" ->{
                val data = boardSetDataSharedPreference.getString(key, "")!!
                var setDataList = ArrayList<SetTempHumViewData>()
                val token = object : TypeToken<MutableList<SetTempHumViewData>>() {}.type
                if (data.isNotEmpty()) {
                    setDataList = Gson().fromJson(data, token)
                }
                setdata = setDataList
            }
            "Baudrate" -> {
                val data = boardSetDataSharedPreference.getString(key, "")!!
                val token = object : TypeToken<Int>() {}.type
                if (data.isNotEmpty()) {
                    setdata = Gson().fromJson(data, token)
                } else {
                    setdata = 1000000
                }
            }
        }
        return setdata
    }

    fun removeBoardSetData(key: String) {
        boardSetDataSharedPreference.edit().apply {
            remove(key)
            apply()
        }
    }

    fun labNameSave(key: String, data: String) {
        val listString = Gson().toJson(data)
        boardSetDataSharedPreference.edit().apply {
            putString(key, listString)
            apply()
        }
    }

    fun loadLabNameData(): String {
        val data = boardSetDataSharedPreference.getString(LABNAME, "")!!
        val token = object : TypeToken<String>() {}.type
        var name = String()
        if (data.isNotEmpty()) {
            name = Gson().fromJson(data, token)
        }
        return name
    }

    fun SavecheckTimeOutState(value: Boolean) {
        boardSetDataSharedPreference.edit().apply {
            putBoolean(TIMEOUTSTATE, value)
            apply()
        }
    }

    fun getTimeOutState(): Boolean {
        return boardSetDataSharedPreference.getBoolean(TIMEOUTSTATE, true)
    }

    fun SaveFeedbackTiming(time: Long) {
        boardSetDataSharedPreference.edit().apply {
            putLong(FEEDBACKTIMING, time)
            apply()
        }
    }

    fun getFeedbackTiming(): Long {
        return boardSetDataSharedPreference.getLong(FEEDBACKTIMING, 50)
    }
}