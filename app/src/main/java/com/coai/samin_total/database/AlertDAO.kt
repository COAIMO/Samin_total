package com.coai.samin_total.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface  AlertDAO {
    @Query("select * from AlertData order by id DESC LIMIT 10 OFFSET (:index * 10)")
    fun getPage(index : Int) : List<AlertData>

    @Insert
    fun insertData(data : AlertData)

    @Query("DELETE FROM AlertData")
    fun deleteAllData()

    @Query("SELECT * from AlertData")
    fun getAll():List<AlertData>
}