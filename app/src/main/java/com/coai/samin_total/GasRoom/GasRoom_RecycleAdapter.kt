package com.coai.samin_total.GasRoom

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coai.samin_total.R
import com.coai.uikit.samin.status.GasRoomView
import com.coai.uikit.samin.status.GasStorageView

class GasRoom_RecycleAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var setGasRoomViewData = mutableListOf<SetGasRoomViewData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return gasRoomViewHodler(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.gas_room_view, parent, false)
        )

    }

    override fun getItemCount(): Int {
        return setGasRoomViewData.size
    }


    fun submitList(viewData: MutableList<SetGasRoomViewData>) {
        this.setGasRoomViewData = viewData
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
            gasRoomView.setPressureMax(setGasRoomViewData.pressureMax)
            gasRoomView.setGasUnit(setGasRoomViewData.gasUnit)
            gasRoomView.setGasIndex(setGasRoomViewData.gasIndex)
            gasRoomView.setAlert(setGasRoomViewData.isAlert)
        }
    }

}