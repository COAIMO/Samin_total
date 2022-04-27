package com.coai.samin_total.Steamer

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coai.samin_total.Logic.AutoUpdatableAdapter
import com.coai.samin_total.R
import com.coai.uikit.samin.status.SteamView
import kotlin.system.measureTimeMillis

class Steamer_RecycleAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    AutoUpdatableAdapter {

    var setSteamerViewData = listOf<SetSteamerViewData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val tmp = steamerViewHodler(
            LayoutInflater.from(parent.context).inflate(R.layout.steamer_view, parent, false)
        )
//        tmp.setIsRecyclable(false)
        return tmp
    }

    override fun getItemCount(): Int {
        return setSteamerViewData.size
    }


    fun submitList(viewData: List<SetSteamerViewData>) {
        val tmp = viewData.filter {
            it.usable
        }
        this.setSteamerViewData = tmp
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as steamerViewHodler).bind(setSteamerViewData[position])
    }

    inner class steamerViewHodler(view: View) : RecyclerView.ViewHolder(view) {
        private val steamerView =
            view.findViewById<SteamView>(R.id.steamer_view)

        fun bind(setSteamerViewData: SetSteamerViewData) {
            val elapsed: Long = measureTimeMillis {
                steamerView.setAlertLow(setSteamerViewData.isAlertLow)
                steamerView.setTempMin(setSteamerViewData.tempMax)
                steamerView.setTemp(setSteamerViewData.isTemp)
                steamerView.setTempUnit(setSteamerViewData.unit)
                steamerView.setAlertTemp(setSteamerViewData.isAlertTemp)
                steamerView.heartBeat(setSteamerViewData.heartbeatCount)
            }
            Log.d("steamerViewHodler", "bind measureTimeMillis : $elapsed")
        }
    }

}