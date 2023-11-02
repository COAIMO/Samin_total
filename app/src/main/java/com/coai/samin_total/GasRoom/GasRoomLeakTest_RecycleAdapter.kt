package com.coai.samin_total.GasRoom

import android.graphics.Color
import android.util.Log
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
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class GasRoomLeakTest_RecycleAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    AutoUpdatableAdapter {

    var setGasRoomViewData = listOf<SetGasRoomViewData>()
    var testTime: Int = 1
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

    fun setLeakTestTime(mins: Int) {
        testTime = mins
    }

    fun setEntry(entries: CopyOnWriteArrayList<ChartDatas>) {
        setGraphData = entries
    }

    var setGraphData = CopyOnWriteArrayList<ChartDatas>()
    var beforepos: Int = -1


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        var curr = (holder as gasRoomLeakTestViewHodler)
        curr.bind(setGasRoomViewData[position])
        curr.graphBind(setGraphData[position])

//        val currentTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(Date().time)
//        val collectTime: Int = Math.max(1000f, testTime * 60 * 1000 / 600f).toInt()
//        val maxcount = (testTime * 60 * 1000) / collectTime
//
//        if (setGraphData[position].data.size < 10) {
//            curr.graphBind(setGraphData[position])
//        }
//        else if (setGraphData[position].data.size < maxcount) {
////            if (((currentTimeSeconds / 2) % 3).toInt() == (position % 2).toInt()) {
////                if (beforepos != position) {
////                    curr.graphBind(setGraphData[position])
//////                    Log.d("onBindViewHolder", "${position}")
////                    beforepos = position
////                }
////            }
//            if (beforepos != position) {
//                curr.graphBind(setGraphData[position])
////                    Log.d("onBindViewHolder", "${position}")
//                beforepos = position
//            }
//        }
//        else {
//            if (curr.count !== maxcount) {
//                curr.graphBind(setGraphData[position])
////                Log.d("onBindViewHolder", "Done")
//            }
//        }
//        Log.d("datas", "${setGraphData[position].data.count()}")
        holder.setIsRecyclable(false)
    }


    inner class gasRoomLeakTestViewHodler(view: View) : RecyclerView.ViewHolder(view) {
        private val gasRoomView = view.findViewById<GasRoomView>(R.id.gas_room_data_view)
        private val gasRoomGraphView = view.findViewById<LineChart>(R.id.gas_room_graph_view)

        var count = 0
        fun bind(setGasRoomViewData: SetGasRoomViewData) {
            gasRoomView.setGasName(setGasRoomViewData.gasName)
            gasRoomView.setGasColor(setGasRoomViewData.gasColor)
            gasRoomView.setPressure(setGasRoomViewData.pressure)
            gasRoomView.setPressureMax(setGasRoomViewData.pressure_Max)
            gasRoomView.setGasUnit(setGasRoomViewData.unit)
            gasRoomView.setGasIndex(setGasRoomViewData.gasIndex)
            gasRoomView.setAlert(setGasRoomViewData.isAlert)
            gasRoomView.setLimitMax(setGasRoomViewData.limit_max)
            gasRoomView.setLimitMin(setGasRoomViewData.limit_min)
            gasRoomView.setSlopeAlert(setGasRoomViewData.isSlopeAlert)
            gasRoomView.setPressureAlert(setGasRoomViewData.isPressAlert)
            gasRoomView.heartBeat(setGasRoomViewData.heartbeatCount)
        }

        fun graphBind(arg: ChartDatas) {
            val tmpdatas = arg.data.toList()
            val lineDataSet = LineDataSet(tmpdatas, "Data Set")
            lineDataSet.apply {
                axisDependency = YAxis.AxisDependency.LEFT
                setDrawValues(false)
                setDrawCircleHole(false)
                setDrawCircles(false)
                setColor(Color.parseColor("#ff9800"))
                highLightColor = Color.parseColor("#ff9800")
                lineWidth = 2f// 라인 두께
                fillAlpha = 0//라인색 투명도
                fillColor = Color.parseColor("#ff9800")
                mode = LineDataSet.Mode.LINEAR
            }
            val lineData = LineData(lineDataSet)
            gasRoomGraphView.apply {
                axisLeft.apply {
                    setDrawGridLines(false)
                    axisMinimum = 0f
                    axisMaximum = 240f
                }
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    textSize = 10f
                    axisMinimum = 0f
                    axisMaximum = 60f * testTime
                    setDrawGridLines(false)
                }
                isHighlightPerTapEnabled = false
                isHighlightPerDragEnabled = false
                axisRight.isEnabled = false
                legend.isEnabled = false
                setPinchZoom(true)
                description.text = "시간(s)"
                description.textSize = 10f
                setBackgroundColor(Color.parseColor("#ffffff"))
                setExtraOffsets(8f, 8f, 8f, 8f)
                data = lineData
                count = tmpdatas.size
                invalidate()
            }
        }
    }


}