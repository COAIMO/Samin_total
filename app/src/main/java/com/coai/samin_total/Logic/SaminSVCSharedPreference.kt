package com.coai.samin_total.Logic

import android.content.Context
import com.coai.samin_total.GasDock.SetGasStorageViewData
import com.coai.samin_total.GasRoom.SetGasRoomViewData
import com.coai.samin_total.Oxygen.SetOxygenViewData
import com.coai.samin_total.Steamer.SetSteamerViewData
import com.coai.samin_total.TempHum.SetTempHumViewData
import com.coai.samin_total.WasteLiquor.SetWasteLiquorViewData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SaminSVCSharedPreference(context: Context)  {
    companion object {
        const val BAUDRATE = "Baudrate"
    }
    private val boardSetDataSharedPreference =
        context.getSharedPreferences("bb", Context.MODE_PRIVATE)

    fun saveBoardSetData(key: String, data: Any) {
        when (key) {
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
}