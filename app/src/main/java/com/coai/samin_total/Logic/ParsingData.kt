package com.coai.samin_total.Logic

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/*
import java.io.Serializable

*/
/**
 * 파싱 데이터 메시지 전달용 객체
 *//*

@kotlinx.serialization.Serializable
data class ParsingData(
    val id: Byte,
    val model: Byte,
    val time: Long,
    val datas: ArrayList<Int>
): Serializable*/
@Parcelize
data class ParsingData(
    val id: Byte,
    val model: Byte,
    val time: Long,
    val datas: ArrayList<Int>
): Parcelable
