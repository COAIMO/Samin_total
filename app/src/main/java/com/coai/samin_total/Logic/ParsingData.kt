package com.coai.samin_total.Logic

import java.io.Serializable

/**
 * 파싱 데이터 메시지 전달용 객체
 */
data class ParsingData(
    val id: Byte,
    val model: Byte,
    val time: Long,
    val datas: ArrayList<Int>
) : Serializable
