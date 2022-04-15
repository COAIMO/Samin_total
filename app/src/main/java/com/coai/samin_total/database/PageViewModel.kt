package com.coai.samin_total.database

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.room.Room

class PageViewModel(private var app : Application) : ViewModel() {
    val dao = Room.databaseBuilder(app, AlertDatabase::class.java, "alertLogs")
        .build()
        .alertDAO()

    val data = Pager(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            PagingSourc(dao)
        }
    ).flow
}
