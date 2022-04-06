package com.coai.samin_total.Logic

import android.annotation.SuppressLint
import android.content.Context
import com.coai.samin_total.GasDock.SetGasStorageViewData
import org.json.JSONObject

class SaminSharedPreference(context: Context) {
    companion object {
        const val ENABLE_PASSLOCK = 1
        const val DISABLE_PASSLOCK = 2
        const val CHANGE_PASSWORD = 3
        const val UNLOCK_PASSWORD = 4

    }

    private val boardSetDataSharedPreference =
        context.getSharedPreferences("aa", Context.MODE_PRIVATE)


    fun saveBoardSetData(key: String, data: String) {
        when (key) {
            "GasStorage" -> {
                boardSetDataSharedPreference.edit().apply {
                    putString("GasStorage", data)
                    apply()
                }
            }
            "GasRoom" -> {
                boardSetDataSharedPreference.edit().apply {
                    putString("GasRoom", data)
                    apply()
                }
            }
            "WasteLiquor" -> {
                boardSetDataSharedPreference.edit().apply {
                    putString("WasteLiquor", data)
                    apply()
                }
            }
            "Oxygen" -> {
                boardSetDataSharedPreference.edit().apply {
                    putString("Oxygen", data)
                    apply()
                }
            }
            "Steamer" -> {
                boardSetDataSharedPreference.edit().apply {
                    putString("Steamer", data)
                    apply()
                }
            }
        }
    }

    fun requestData(key: String): String? {
        return boardSetDataSharedPreference.getString(key, "")
    }

    fun saveHashMap(key: String, data:String){
        boardSetDataSharedPreference.edit().apply {
            putString(key, data)
            apply()
        }
    }

    fun saveHa(hashMap: HashMap<String, ByteArray>){
        val ss = JSONObject(hashMap as Map<*, *>?)
        val aa = ss.toString()
        boardSetDataSharedPreference.edit().apply {
            putString("map", aa)
            apply()
        }
    }

    fun loadhashmap():HashMap<String, ByteArray>{
        val outMap = HashMap<String, ByteArray>()
        val jsonString = boardSetDataSharedPreference.getString("map",JSONObject().toString())
        val jsonObject = JSONObject(jsonString)

        val keysltr: MutableIterator<String> = jsonObject.keys()
        while (keysltr.hasNext()){
            val key = keysltr.next()
            val value = jsonObject.get(key).toString().toByteArray()
            outMap.put(key, value)
        }
        return outMap
    }
//    fun setPassLock(password: String) {
//        sharedPref.edit().apply {
//            putString("AdminLock", password)
//            apply()
//        }
//    }
//
//    fun removePassLock() {
//        sharedPref.edit().apply {
//            remove("AdminLock")
//            apply()
//        }
//    }
//
//    fun checkPassLockSet(password: String): Boolean {
//        return sharedPref.getString("AdminLock", initPassword) == password
//    }
//
//    fun isPassLockSet(): Boolean {
//        if (sharedPref.contains("AdminLock")) {
//            return true
//        }
//        return false
//    }
}