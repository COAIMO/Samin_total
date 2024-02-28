package com.coai.samin_total.GasRoom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coai.samin_total.Logic.AutoUpdatableAdapter
import com.coai.samin_total.R
import com.coai.uikit.samin.status.GasRoomView

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
            if (!gasRoomView.getGasName().equals(setGasRoomViewData.gasName))
                gasRoomView.setGasName(setGasRoomViewData.gasName)
            if (!gasRoomView.getGasColor().equals(setGasRoomViewData.gasColor))
                gasRoomView.setGasColor(setGasRoomViewData.gasColor)
            if (!gasRoomView.getPressure().equals(setGasRoomViewData.pressure))
                gasRoomView.setPressure(setGasRoomViewData.pressure)
            if (!gasRoomView.getPressureMax().equals(setGasRoomViewData.pressure_Max))
                gasRoomView.setPressureMax(setGasRoomViewData.pressure_Max)
            if (!gasRoomView.getGasUnit().equals(setGasRoomViewData.unit))
                gasRoomView.setGasUnit(setGasRoomViewData.unit)
            if (!gasRoomView.getGasIndex().equals(setGasRoomViewData.gasIndex))
                gasRoomView.setGasIndex(setGasRoomViewData.gasIndex)

            if(!gasRoomView.isAlert().equals(setGasRoomViewData.isAlert))
                gasRoomView.setAlert(setGasRoomViewData.isAlert)
            if (!gasRoomView.mLimitMax.equals(setGasRoomViewData.limit_max))
                gasRoomView.setLimitMax(setGasRoomViewData.limit_max)
            if (!gasRoomView.mLimitMin.equals(setGasRoomViewData.limit_min))
                gasRoomView.setLimitMin(setGasRoomViewData.limit_min)
            if (!gasRoomView.mSlopeAlert.equals(setGasRoomViewData.isSlopeAlert))
                gasRoomView.setSlopeAlert(setGasRoomViewData.isSlopeAlert)
            if (!gasRoomView.mPressureAlert.equals(setGasRoomViewData.isPressAlert))
                gasRoomView.setPressureAlert(setGasRoomViewData.isPressAlert)

            gasRoomView.heartBeat(setGasRoomViewData.heartbeatCount)
        }
    }
}