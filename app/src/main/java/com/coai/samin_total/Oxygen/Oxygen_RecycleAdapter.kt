package com.coai.samin_total.Oxygen

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coai.samin_total.R
import com.coai.uikit.samin.status.GasRoomView
import com.coai.uikit.samin.status.GasStorageView
import com.coai.uikit.samin.status.OxyzenView

class Oxygen_RecycleAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var setOxygenViewData = listOf<SetOxygenViewData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return oxygenViewHodler(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.oxygen_view, parent, false)
        )


    }

    override fun getItemCount(): Int {
        return setOxygenViewData.size
    }


    fun submitList(viewData: List<SetOxygenViewData>) {
        this.setOxygenViewData = viewData
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as oxygenViewHodler).bind(setOxygenViewData[position])
        holder.setIsRecyclable(false)
    }

    inner class oxygenViewHodler(view: View) : RecyclerView.ViewHolder(view) {
        private val oxygenView =
            view.findViewById<OxyzenView>(R.id.oxygen_view)

        fun bind(setOxygenViewData: SetOxygenViewData) {
            oxygenView.id = setOxygenViewData.id
            oxygenView.setAlert(setOxygenViewData.isAlert)
            oxygenView.setOxyzenData(setOxygenViewData.setValue)
            oxygenView.setMinOxyzen(setOxygenViewData.setMinValue)
        }
    }

}