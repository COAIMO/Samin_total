package com.coai.samin_total.GasRoom

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coai.samin_total.Logic.AutoUpdatableAdapter
import com.coai.samin_total.R
import com.coai.uikit.samin.status.GasRoomView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet

class GasRoomLeakTest_RecycleAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    AutoUpdatableAdapter {

    var setGasRoomViewData = listOf<SetGasRoomViewData>()
    var testTime: Int? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return gasRoomLeakTestViewHodler(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.gas_room_leaktest_view, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return setGasRoomViewData.size
    }


    fun submitList(viewData: List<SetGasRoomViewData>) {
        val tmp = viewData.filter {
            it.usable
        }
        this.setGasRoomViewData = tmp
    }

    fun setLeakTestTime(mins:Int){
        testTime = mins
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as gasRoomLeakTestViewHodler).bind(setGasRoomViewData[position])

        holder.setIsRecyclable(false)
    }

    inner class gasRoomLeakTestViewHodler(view: View) : RecyclerView.ViewHolder(view) {
        private val gasRoomView =
            view.findViewById<GasRoomView>(R.id.gas_room_data_view)
        private val gasRoomGraphView = view.findViewById<LineChart>(R.id.gas_room_graph_view)

        fun bind(setGasRoomViewData: SetGasRoomViewData) {
            gasRoomView.setGasName(setGasRoomViewData.gasName)
            gasRoomView.setGasColor(setGasRoomViewData.gasColor)
            gasRoomView.setPressure(setGasRoomViewData.pressure)
            gasRoomView.setPressureMax(setGasRoomViewData.pressure_Max)
            gasRoomView.setGasUnit(setGasRoomViewData.unit)
            gasRoomView.setGasIndex(setGasRoomViewData.gasIndex)
            gasRoomView.setAlert(setGasRoomViewData.isAlert)
            gasRoomView.heartBeat(setGasRoomViewData.heartbeatCount)
            setLineChart()
        }

        fun setLineChart(){
            val xAxis: XAxis = gasRoomGraphView.xAxis
            val yAxis: YAxis = gasRoomGraphView.axisLeft

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textSize = 10f
                setDrawGridLines(false)//배경 그리드 라인 세팅
                granularity = 1f // x축 데이터 표시 간격
                axisMinimum = 2f// x축 데이터의 최소 표시값
                isGranularityEnabled = true // x축 간격을 제한하는 세분화 기능
            }
            yAxis.apply {
                setDrawGridLines(false)
                axisMinimum = 0f
                axisMaximum = 20f

            }
            gasRoomGraphView.apply {
                axisRight.isEnabled = false //y축의 오른쪽 데이터 비활성화
//                legend.apply { //범례 세팅
//                    textSize = 15f
//                    verticalAlignment = Legend.LegendVerticalAlignment.TOP // 수직 조정
//                    horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER // 수평 조정
//                    orientation = Legend.LegendOrientation.HORIZONTAL// 범례와 차트 정렬
//                    setDrawInside(false)// 차트안에 그릴것인가?
//                }
                legend.isEnabled = false
                description.text = "${testTime}"+"(m)T"
                setVisibleXRangeMaximum(4f)// x축 데이터 최대 표현 개수
                setPinchZoom(false)// 확대 설정
                isDoubleTapToZoomEnabled = false// 더블탭 확대 설정
                setBackgroundColor(Color.parseColor("#ffffff"))
                description.textSize = 15f
                setExtraOffsets(8f,15f,8f,15f)
            }
            val lineData = LineData()
            gasRoomGraphView.data = lineData// 라인 차트 데이터 지정
        }

    }
}