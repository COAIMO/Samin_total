package com.coai.samin_total.Logic

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.google.gson.Gson

abstract class AppDatabase : RoomDatabase() {
//    abstract fun appDao(): AppDao
}


object DatabaseModule {
    private const val DB_NAME ="Sample.db"

    fun providerGson(): Gson {
        return Gson()
    }

    fun provideDatabase(context: Context, gson:Gson):AppDatabase{
        return Room
            .databaseBuilder(context, AppDatabase::class.java, DB_NAME)
            .build()
    }

//    fun providerAppDao(database: AppDatabase): AppDao{
//        return database.appDao()
//    }
}