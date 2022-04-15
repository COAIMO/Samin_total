package com.coai.samin_total.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ListItemViewModel() : ViewModel() {
    private var _model = MutableLiveData<String>()
    private var _id = MutableLiveData<String>()
    private var _error_content = MutableLiveData<String>()
    private var _time = MutableLiveData<String>()
    private var _port = MutableLiveData<String>()

    val modelText : LiveData<String> get() = _model
    val idText : LiveData<String> get() = _id
    val errorText : LiveData<String> get() = _error_content
    val timeText : LiveData<String> get() = _time
    val portText : LiveData<String> get() = _port

    fun setModelText(value: String) {
        _model.value = value
    }

    fun setIdText(value: String) {
        _id.value = value
    }

    fun setErrorText(value: String) {
        _error_content.value = value
    }

    fun setTimeText(value: String) {
        _time.value = value
    }

    fun setPortTExt(value: String) {
        _port.value = value
    }
}