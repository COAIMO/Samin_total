package com.coai.samin_total.Logic

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.room.Room
import com.coai.samin_total.DataBase.SaminDataBase

class PageViewModel(private var app: Application) : ViewModel() {
    val dao = Room.databaseBuilder(app, SaminDataBase::class.java, "SaminDb").build().saminDao()

//    val data = Pager(
//        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
//        pagingSourceFactory = { PagingSource(dao) }).flow

}