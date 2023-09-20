package com.coai.samin_total.GasRoom

data class Candle(    val createdAt: Long,
                      val open: Float,
                      val close: Float,
                      val shadowHigh: Float,
                      val shadowLow: Float,
                      val arrayPSI: ArrayList<PSIData>
)

data class PSIData(val createdAt: Float, val psi: Float)