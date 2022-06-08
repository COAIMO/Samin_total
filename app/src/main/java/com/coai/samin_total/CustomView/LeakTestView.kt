package com.coai.samin_total.CustomView

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.coai.samin_total.GasRoom.SetGasRoomViewData
import com.coai.samin_total.R
import com.coai.uikit.samin.status.GasRoomView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet

class LeakTestView(context: Context, attrs: AttributeSet? = null) :
    ConstraintLayout(context, attrs) {
    var gasRoomView: GasRoomView
    var lineChart: LineChart
    lateinit var graphData: LineData
    val graphMap = hashMapOf<Float,Float>()
    init {
        LayoutInflater.from(context).inflate(R.layout.gas_room_leaktest_view, this, true)
        gasRoomView = findViewById(R.id.gas_room_data_view)
        lineChart = findViewById(R.id.gas_room_graph_view)
        setChart()
    }

    private fun onRefresh() {
        invalidate()
        requestLayout()
    }

    fun bind(setGasRoomViewData: SetGasRoomViewData) {
        gasRoomView.setGasName(setGasRoomViewData.gasName)
        gasRoomView.setGasColor(setGasRoomViewData.gasColor)
        gasRoomView.setPressure(setGasRoomViewData.pressure)
        gasRoomView.setPressureMax(setGasRoomViewData.pressure_Max)
        gasRoomView.setGasUnit(setGasRoomViewData.unit)
        gasRoomView.setGasIndex(setGasRoomViewData.gasIndex)
        gasRoomView.setAlert(setGasRoomViewData.isAlert)
        gasRoomView.heartBeat(setGasRoomViewData.heartbeatCount)
    }

    private fun setChart() {
        val xAxis: XAxis = lineChart.xAxis
        val yAxis: YAxis = lineChart.axisLeft

        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            textSize = 10f
            setDrawGridLines(false)//배경 그리드 라인 세팅
            setDrawAxisLine(true)
//            granularity = 1f // x축 데이터 표시 간격
//            axisMinimum = 2f// x축 데이터의 최소 표시값
//            isGranularityEnabled = true // x축 간격을 제한하는 세분화 기능
        }
        yAxis.apply {
            setDrawGridLines(false)
//            axisMinimum = 0f
//            axisMaximum = 20f
        }
        lineChart.apply {
            axisRight.isEnabled = false //y축의 오른쪽 데이터 비활성화
//            legend.apply { //범례 세팅
//                textSize = 15f
//                verticalAlignment = Legend.LegendVerticalAlignment.TOP // 수직 조정
//                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER // 수평 조정
//                orientation = Legend.LegendOrientation.HORIZONTAL// 범례와 차트 정렬
//                setDrawInside(false)// 차트안에 그릴것인가?
//            }
            legend.isEnabled = false

            setPinchZoom(true)// 확대 설정
            isDoubleTapToZoomEnabled = false// 더블탭 확대 설정
            description.text = "시간(s)"
            setBackgroundColor(Color.parseColor("#ffffff"))
            description.textSize = 10f
            setExtraOffsets(8f, 15f, 8f, 15f)
        }
        val lineData = LineData()
        lineChart.data = lineData// 라인 차트 데이터 지정
    }

    private fun createSet(): LineDataSet {
        val set = LineDataSet(null, "psi")
        set.apply {
            axisDependency = YAxis.AxisDependency.RIGHT//y값 데이터 왼쪽으로
//            color = Color.parseColor("#ff9800")
//            setCircleColor(Color.parseColor("#ff9800"))// 데이터 원형 색 지정
//            circleRadius = 3f
//            valueTextSize = 10f //값 글자 크기
            setDrawValues(false)
            setDrawCircleHole(false)
            setDrawCircles(false)
            setColor(Color.parseColor("#ff9800"))
            mode = LineDataSet.Mode.LINEAR
            highLightColor = Color.parseColor("#ff9800")
            lineWidth = 2f// 라인 두께
            fillAlpha = 0//라인색 투명도
            fillColor = Color.parseColor("#ff9800") // 라인 색 지정
//            setDrawValues(true)//값을 그리기
        }
        return set
    }


//    fun addEntry(psi: Float) {
//        graphData = lineChart.data
//
//        graphData.let {
//            var set: ILineDataSet? = graphData.getDataSetByIndex(0)
//            //임의의 데이터 셋 (0번부터 시작)
//            if (set == null) {
//                set = createSet()
//                graphData.addDataSet(set)
//            }
//            //데이터 엔트리 추가
//            graphData.addEntry(Entry(set.entryCount.toFloat(), psi), 0)
//            graphData.notifyDataChanged()//데이터 변경알림
//
//            lineChart.apply {
//                notifyDataSetChanged()//라인 차트변경 알림.
//                moveViewToX(data.entryCount.toFloat())
////                setVisibleXRangeMaximum(4f)// x축 데이터 최대 표현 개수
//                setPinchZoom(true)// 확대 설정
//                isDoubleTapToZoomEnabled = false// 더블탭 확대 설정
//                description.text = "시간"
//                setBackgroundColor(Color.parseColor("#ffffff"))
//                description.textSize = 15f
//                setExtraOffsets(8f, 15f, 8f, 15f)
//            }
//
//        }
//    }
    fun addEntry(testTime:Float, psi: Float) {
        graphData = lineChart.data

        graphData.let {
            var set: ILineDataSet? = graphData.getDataSetByIndex(0)
            //임의의 데이터 셋 (0번부터 시작)
            if (set == null) {
                set = createSet()
                graphData.addDataSet(set)
            }
            //데이터 엔트리 추가
//            graphData.addEntry(Entry(set.entryCount.toFloat(), psi), 0)
            graphData.addEntry(Entry(testTime, psi), 0)
            graphMap.put(testTime, psi)
            graphData.notifyDataChanged()//데이터 변경알림

            lineChart.apply {
                notifyDataSetChanged()//라인 차트변경 알림.
                moveViewToX(data.entryCount.toFloat())
            }
        }
    }
}