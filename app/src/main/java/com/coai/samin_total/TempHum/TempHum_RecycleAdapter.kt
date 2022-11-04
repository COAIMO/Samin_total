package com.coai.samin_total.TempHum

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coai.samin_total.Logic.AutoUpdatableAdapter
import com.coai.samin_total.R
import com.coai.uikit.samin.status.TempHumView

class TempHum_RecycleAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    AutoUpdatableAdapter {

    var setTemphumViewData = listOf<SetTempHumViewData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TempHumViewHodler(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.temp_hum_view, parent, false)
        )

    }

    override fun getItemCount(): Int {
        return setTemphumViewData.size
    }


    fun submitList(viewData: List<SetTempHumViewData>) {
        val tmp = viewData.filter {
            it.usable
        }
        this.setTemphumViewData = tmp
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as TempHumViewHodler).bind(setTemphumViewData[position])
        holder.setIsRecyclable(false)
    }


    inner class TempHumViewHodler(view: View) : RecyclerView.ViewHolder(view) {
        private val tempHumView =
            view.findViewById<TempHumView>(R.id.tempHum_view)

        fun bind(setTempHumViewData: SetTempHumViewData) {
//            tempHumView.setWasteName(setTempHumViewData.liquidName)
//            tempHumView.setAlert(setTempHumViewData.isAlert)
//            tempHumView.heartBeat(setTempHumViewData.heartbeatCount)
        }
    }

}