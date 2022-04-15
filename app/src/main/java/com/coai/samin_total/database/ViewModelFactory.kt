package com.coai.samin_total.database

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory(private var app : Application?) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PageViewModel::class.java)) {
            return PageViewModel(app!!) as T
        } else if (modelClass.isAssignableFrom(ListItemViewModel::class.java)) {
            return ListItemViewModel() as T
        }

        throw IllegalAccessException("unknow view model class")
    }
}