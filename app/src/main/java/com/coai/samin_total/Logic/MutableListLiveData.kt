package com.coai.samin_total.Logic

import androidx.lifecycle.MutableLiveData

class MutableListLiveData<T> : MutableLiveData<MutableList<T>>() {
    fun add(item: T) {
        val items: MutableList<T>? = value
        items!!.add(item)
        setValue(items)
    }

    fun addAll(list: List<T>?) {
        val items: MutableList<T>? = value
        items!!.addAll(list!!)
        setValue(items)
    }

    fun clear(notify: Boolean) {
        val items: MutableList<T>? = value
        items!!.clear()
        if (notify) {
            setValue(items)
        }
    }

    fun remove(item: T) {
        val items: MutableList<T>? = value
        items!!.remove(item)
        setValue(items)
    }

    fun notifyChange() {
        val items: MutableList<T>? = value
        setValue(items)
    }

    init {
        value = ArrayList()
    }
}