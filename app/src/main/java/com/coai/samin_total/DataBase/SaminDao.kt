package com.coai.samin_total.DataBase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import org.jetbrains.annotations.NotNull

@Dao
interface SaminDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun inert(data :AQmdel_IDs)

    @Update
    fun update(data: AQmdel_IDs)

    @Delete
    fun delete(data: AQmdel_IDs)

    @Query("SELECT * FROM aqmdel_ids")
    fun getAll(): List<AQmdel_IDs>

    @Query("DELETE FROM aqmdel_ids")
    fun deleteAll()

}