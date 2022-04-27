package com.coai.samin_total.Logic

import android.content.Context
import android.os.Binder

class AdminLock(context: Context) {

    companion object {
        const val MASTER_KEY = "1"
        const val PERSONAL_KEY = "2"
    }

    private val masterPassword = "8940"
    private var sharedPref = context.getSharedPreferences("AdminLock", Context.MODE_PRIVATE)

    fun setPassLock(key: String, password: String) {
        sharedPref.edit().apply {
            putString(key, password)
            apply()
        }
    }


    fun removePassLock() {
        sharedPref.edit().apply {
            remove("AdminLock")
            apply()
        }
    }

    fun checkPassLockSet(key: String, password: String): Boolean {
        return sharedPref.getString(key, masterPassword) == password
    }

    fun isPassLockSet(): Boolean {
        if (sharedPref.contains("AdminLock")) {
            return true
        }
        return false
    }
    init {
        setPassLock(AdminLock.MASTER_KEY, masterPassword)
    }

}