package com.coai.samin_total

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SerialDataInfo (
val id: Byte,
val model: Byte,
val time: Long,
val data1: Int? = null,
val data2: Int? = null,
val data3: Int? = null,
val data4: Int? = null,
  ): Parcelable

