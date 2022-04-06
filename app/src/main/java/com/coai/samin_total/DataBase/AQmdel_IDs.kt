package com.coai.samin_total.DataBase

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AQmdel_IDs(
    @ColumnInfo var model: String,
    @ColumnInfo var ids: ByteArray
){
    @PrimaryKey(autoGenerate = true)var id: Int =0

}
