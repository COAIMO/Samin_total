package com.coai.samin_total.Steamer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coai.samin_total.R
import com.coai.uikit.samin.status.SteamView

class Steamer_RecycleAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var setSteamerViewData = listOf<SetSteamerViewData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return steamerViewHodler(
            LayoutInflater.from(parent.context).inflate(R.layout.steamer_view, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return setSteamerViewData.size
    }


    fun submitList(viewData: List<SetSteamerViewData>) {
        this.setSteamerViewData = viewData
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as steamerViewHodler).bind(setSteamerViewData[position])
        holder.setIsRecyclable(false)
    }

    inner class steamerViewHodler(view: View) : RecyclerView.ViewHolder(view) {
        private val steamerView =
            view.findViewById<SteamView>(R.id.steamer_view)

        fun bind(setSteamerViewData: SetSteamerViewData) {
            steamerView.setAlertLow(setSteamerViewData.isAlertLow)
            steamerView.setTempMin(setSteamerViewData.isTempMin)
            steamerView.setTemp(setSteamerViewData.isTemp)
//            steamerView.setTempUnit(setSteamerViewData.unit)
            steamerView.setAlertTemp(setSteamerViewData.isAlertTemp)
        }
    }

}