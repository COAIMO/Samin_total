package com.coai.samin_total.GasRoom

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.coai.samin_total.Logic.AutoUpdatableAdapter
import com.coai.samin_total.R
import com.coai.uikit.samin.status.GasRoomView
import com.coai.uikit.samin.status.GasStorageView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.LineData
import kotlin.properties.Delegates

class GasRoom_RecycleAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    AutoUpdatableAdapter {

    var setGasRoomViewData = listOf<SetGasRoomViewData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return gasRoomViewHodler(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.gas_room_view, parent, false)
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as gasRoomViewHodler).bind(setGasRoomViewData[position])
        holder.setIsRecyclable(false)
    }

    inner class gasRoomViewHodler(view: View) : RecyclerView.ViewHolder(view) {
        private val gasRoomView =
            view.findViewById<GasRoomView>(R.id.gas_room_view)

        fun bind(setGasRoomViewData: SetGasRoomViewData) {
            gasRoomView.setGasName(setGasRoomViewData.gasName)
            gasRoomView.setGasColor(setGasRoomViewData.gasColor)
            gasRoomView.setPressure(setGasRoomViewData.pressure)
            gasRoomView.setPressureMax(setGasRoomViewData.pressure_Max)
            gasRoomView.setGasUnit(setGasRoomViewData.unit)
            gasRoomView.setGasIndex(setGasRoomViewData.gasIndex)
            gasRoomView.heartBeat(setGasRoomViewData.heartbeatCount)
            gasRoomView.setAlert(setGasRoomViewData.isAlert)
            gasRoomView.setLimitMax(setGasRoomViewData.limit_max)
            gasRoomView.setLimitMin(setGasRoomViewData.limit_min)
            gasRoomView.setSlopeAlert(setGasRoomViewData.isSlopeAlert)
            gasRoomView.setPressureAlert(setGasRoomViewData.isPressAlert)
        }
    }
}