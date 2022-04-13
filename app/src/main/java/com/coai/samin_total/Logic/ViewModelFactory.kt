package com.coai.samin_total.Logic

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.coai.samin_total.MainViewModel

class ViewModelFactory(private var app: Application?) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PageViewModel::class.java)) {
            return PageViewModel(app!!) as T
        } else if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel() as T
        }else throw IllegalAccessException("unknow view model class")
    }
}

