package com.coai.samin_total.DataBase

import android.provider.ContactsContract
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import org.jetbrains.annotations.NotNull

@Dao
interface SaminDao {

//    @Query("select * from AlertData order by id DESC LIMIT :loadSize OFFSET :index * :loadSize")
//    fun getPage(index: Int, loadSize: Int): List<AlertData>
//    @Insert
//    fun insertData(data: AlertData)
//    @Query("SELECT * FROM alertdata")
//    fun getAll(): List<AlertData>
//
//    @Insert
//    fun insertAll(vararg contacts: ContactsContract.Contacts)
//
//    @Delete
//    fun delete(contacts: ContactsContract.Contacts)
}