package com.coai.samin_total.database

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory(private var app : Application?) : ViewModelProvider.Factory {
//    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(PageViewModel::class.java)) {
//            return PageViewModel(app!!) as T
//        } else if (modelClass.isAssignableFrom(ListItemViewModel::class.java)) {
//            return ListItemViewModel() as T
//        }
//
//        throw IllegalAccessException("unknow view model class")
//    }
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(PageViewModel::class.java) -> PageViewModel(app!!) as? T
                ?: throw IllegalArgumentException("Unable to cast to PageViewModel")
            modelClass.isAssignableFrom(ListItemViewModel::class.java) -> ListItemViewModel() as? T
                ?: throw IllegalArgumentException("Unable to cast to ListItemViewModel")
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}