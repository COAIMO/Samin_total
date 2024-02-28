package com.coai.samin_total.Oxygen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coai.samin_total.Logic.AutoUpdatableAdapter
import com.coai.samin_total.R
import com.coai.uikit.samin.status.OxyzenView

class Oxygen_RecycleAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    AutoUpdatableAdapter {

    var setOxygenViewData = listOf<SetOxygenViewData>()
    var masterOxygenData: SetOxygenViewData? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return oxygenViewHodler(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.oxygen_view, parent, false)
        )


    }

    override fun getItemCount(): Int {
//        var count = -1
//        if (masterOxygenData == null) {
//            count = 0
//        }else count =1
//        return count
        return  setOxygenViewData.size
    }


    fun submitList(viewData: List<SetOxygenViewData>) {
        val tmp = viewData.filter {
            it.usable
        }
        this.setOxygenViewData = tmp
    }

    fun setData(viewData: SetOxygenViewData) {
        masterOxygenData = viewData
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        (holder as oxygenViewHodler).bind(masterOxygenData!!)
//        holder.setIsRecyclable(false)
        (holder as oxygenViewHodler).bind(setOxygenViewData[position])
        holder.setIsRecyclable(false)
    }

    inner class oxygenViewHodler(view: View) : RecyclerView.ViewHolder(view) {
        private val oxygenView =
            view.findViewById<OxyzenView>(R.id.oxygen_view)

        fun bind(setOxygenViewData: SetOxygenViewData) {
            oxygenView.id = setOxygenViewData.id
            if (!oxygenView.isAlert().equals(setOxygenViewData.isAlert))
                oxygenView.setAlert(setOxygenViewData.isAlert)
            if (!oxygenView.isOxyzenData().equals(setOxygenViewData.setValue))
                oxygenView.setOxyzenData(setOxygenViewData.setValue)
            if (!oxygenView.isMinOxyzen().equals(setOxygenViewData.setMinValue))
                oxygenView.setMinOxyzen(setOxygenViewData.setMinValue)
            if (oxygenView.getName() == null || !oxygenView.getName()!!.equals(setOxygenViewData.name))
                oxygenView.setName(setOxygenViewData.name)
            oxygenView.heartBeat(setOxygenViewData.heartbeatCount)
        }

    }

}