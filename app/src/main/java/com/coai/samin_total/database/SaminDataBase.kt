package com.coai.samin_total.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AQmdel_IDs::class], version = 1)
abstract class SaminDataBase:RoomDatabase() {
    abstract fun saminDao(): SaminDao

    companion object{
        private var instance : SaminDataBase? = null

        @Synchronized
        fun getIstance(context: Context) : SaminDataBase?{
            if (instance == null){
                synchronized(SaminDataBase::class){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        SaminDataBase::class.java,
                        "Samin.db"
                    ).build()
                }
            }
            return instance
        }

    }
}