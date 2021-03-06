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

    fun setLeakTestTime(mins: Int) {
        testTime = mins
    }

    fun setEntry(entries: List<Entry>) {
        setGraphData = entries
    }

    var setGraphData = listOf<Entry>()


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as gasRoomLeakTestViewHodler).bind(setGasRoomViewData[position])
        (holder as gasRoomLeakTestViewHodler).graphBind(setGraphData[position])
        holder.setIsRecyclable(false)
    }

    inner class gasRoomLeakTestViewHodler(view: View) : RecyclerView.ViewHolder(view) {
        private val gasRoomView =
            view.findViewById<GasRoomView>(R.id.gas_room_data_view)
        private val gasRoomGraphView = view.findViewById<LineChart>(R.id.gas_room_graph_view)
        var count = 0f
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

        fun graphBind(entry: Entry) {
            setLineChart(entry)
        }

        fun setLineChart(entry: Entry) {
            val xAxis: XAxis = gasRoomGraphView.xAxis
            val yAxis: YAxis = gasRoomGraphView.axisLeft

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textSize = 10f
                setDrawGridLines(false)//?????? ????????? ?????? ??????
                //granularity = 1f // x??? ????????? ?????? ??????
                //axisMinimum = 2f// x??? ???????????? ?????? ?????????
                isGranularityEnabled = true // x??? ????????? ???????????? ????????? ??????
            }
            yAxis.apply {
                setDrawGridLines(false)
//                axisMinimum = 0f
//                axisMaximum = 20f

            }
            gasRoomGraphView.apply {
                axisRight.isEnabled = false //y?????? ????????? ????????? ????????????
//                legend.apply { //?????? ??????
//                    textSize = 15f
//                    verticalAlignment = Legend.LegendVerticalAlignment.TOP // ?????? ??????
//                    horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER // ?????? ??????
//                    orientation = Legend.LegendOrientation.HORIZONTAL// ????????? ?????? ??????
//                    setDrawInside(false)// ???????????? ????????????????
//                }
                legend.isEnabled = false
            }
            val lineData = LineData()
            gasRoomGraphView.data = lineData// ?????? ?????? ????????? ??????
            addEntry(entry)
        }

        fun addEntry(entry: Entry) {
            val data = gasRoomGraphView.data
            data?.let {
                var set: ILineDataSet? = data.getDataSetByIndex(0)
                //????????? ????????? ??? (0????????? ??????)
                if (set == null) {
                    set = createSet()
                    data.addDataSet(set)
                }
                //????????? ????????? ??????
                data.addEntry(entry, 0)
                data.notifyDataChanged()//????????? ????????????
                count++
                gasRoomGraphView.apply {
                    notifyDataSetChanged()//?????? ???????????? ??????.
                    moveViewToX(entry.x)
//                    setVisibleXRangeMaximum(20f)// x??? ????????? ?????? ?????? ??????
                    //setPinchZoom(false)// ?????? ??????
                    isDoubleTapToZoomEnabled = false// ????????? ?????? ??????
                    description.text = "${testTime}" + "(m)T"
                    setBackgroundColor(Color.parseColor("#ffffff"))
                    description.textSize = 15f
                    setExtraOffsets(8f, 15f, 8f, 15f)
                }

            }
        }

        private fun createSet(): LineDataSet {
            val set = LineDataSet(null, "Psi")
            set.apply {
                axisDependency = YAxis.AxisDependency.LEFT//y??? ????????? ????????????
                color = Color.parseColor("#ff9800")
                setCircleColor(Color.parseColor("#ff9800"))// ????????? ?????? ??? ??????
                valueTextSize = 10f //??? ?????? ??????
                lineWidth = 10f// ?????? ??????
                circleRadius = 3f
                fillAlpha = 0//????????? ?????????
                fillColor = Color.parseColor("#ff9800") // ?????? ??? ??????
                highLightColor = Color.parseColor("#ff9800")
                setDrawValues(true)//?????? ?????????
            }
            return set
        }

    }


}