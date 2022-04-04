package com.coai.samin_total.Logic

class AnalyticUtils {

    fun LinearRegression(
        xValus: Array<Double>,
        yValus: Array<Double>,
        inclusiveStart: Int,
        exclusiveEnd: Int,
        rsquared: Double = 0.0,
        yintercept: Double = 0.0,
        slope: Double = 0.0
    ) {
        var sumOfx: Double = 0.0
        var sumOfy: Double = 0.0
        val count: Double = (exclusiveEnd - inclusiveStart).toDouble()

        for (ctr:Int in inclusiveStart..exclusiveEnd){
            sumOfx += xValus[ctr]
            sumOfy += yValus[ctr]
        }

        val avgX = sumOfx /count
        val avgY = sumOfy /count

        var sumxy:Double = 0.0
        var sumxsq:Double =0.0
        for (ctr:Int in inclusiveStart..exclusiveEnd){
            val x = xValus[ctr]
            val y = yValus[ctr]
            sumxy += (x - avgX) * (y - avgY)
            sumxsq += Math.pow(x - avgX, 2.0)
        }
//        slope = sumxy / sumxsq

    }
}