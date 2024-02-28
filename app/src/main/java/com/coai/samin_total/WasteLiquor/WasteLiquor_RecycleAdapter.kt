package com.coai.samin_total.WasteLiquor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coai.samin_total.Logic.AutoUpdatableAdapter
import com.coai.samin_total.R
import com.coai.uikit.samin.status.WastebottleView

class WasteLiquor_RecycleAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    AutoUpdatableAdapter {

    var setWasteLiquorViewData = listOf<SetWasteLiquorViewData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return WasteLiquorViewHodler(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.waste_liquor_view, parent, false)
        )

    }

    override fun getItemCount(): Int {
        return setWasteLiquorViewData.size
    }


    fun submitList(viewData: List<SetWasteLiquorViewData>) {
        val tmp = viewData.filter {
            it.usable
        }
        this.setWasteLiquorViewData = tmp
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as WasteLiquorViewHodler).bind(setWasteLiquorViewData[position])
        holder.setIsRecyclable(false)
    }


    inner class WasteLiquorViewHodler(view: View) : RecyclerView.ViewHolder(view) {
        private val wasteLiquorView =
            view.findViewById<WastebottleView>(R.id.waste_liquor_view)

        fun bind(setWasteLiquorViewData: SetWasteLiquorViewData) {
            if (!wasteLiquorView.getWasteName().equals(setWasteLiquorViewData.liquidName))
                wasteLiquorView.setWasteName(setWasteLiquorViewData.liquidName)

            if (!wasteLiquorView.isAlert().equals(setWasteLiquorViewData.isAlert))
                wasteLiquorView.setAlert(setWasteLiquorViewData.isAlert)

            wasteLiquorView.heartBeat(setWasteLiquorViewData.heartbeatCount)
        }
    }

}