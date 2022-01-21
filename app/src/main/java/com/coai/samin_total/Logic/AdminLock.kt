package com.coai.samin_total.Logic

import android.content.Context
import android.os.Binder

class AdminLock(context: Context) {

    companion object{
        const val ENABLE_PASSLOCK = 1
        const val DISABLE_PASSLOCK = 2
        const val CHANGE_PASSWORD = 3
        const val UNLOCK_PASSWORD = 4
    }
    private val initPassword = "8940"
    private var sharedPref = context.getSharedPreferences("AdminLock", Context.MODE_PRIVATE)

    //
    fun setPassLock(password: String) {
        sharedPref.edit().apply {
            putString("AdminLock", password)
            apply()
        }
    }

    fun removePassLock() {
        sharedPref.edit().apply {
            remove("AdminLock")
            apply()
        }
    }

    fun checkPassLockSet(password: String): Boolean {
        return sharedPref.getString("AdminLock", initPassword) == password
    }

    fun isPassLockSet(): Boolean {
        if (sharedPref.contains("AdminLock")) {
            return true
        }
        return false
    }

}