package com.coai.samin_total.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(AlertData::class), version = 1)
abstract class AlertDatabase : RoomDatabase(){
    abstract fun alertDAO() : AlertDAO
}