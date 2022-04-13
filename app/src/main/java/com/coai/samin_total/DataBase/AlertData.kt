package com.coai.samin_total.DataBase

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity
data class AlertData(
    @ColumnInfo val time: String,
    @ColumnInfo val model: Int,
    @ColumnInfo val apId: Int,
    @ColumnInfo val content: String,
    @ColumnInfo val port: Int,
    @ColumnInfo var isAlert: Boolean
){
    @PrimaryKey(autoGenerate = true)var id: Int =0
}
