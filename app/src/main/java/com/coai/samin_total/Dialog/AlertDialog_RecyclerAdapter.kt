package com.coai.samin_total.Dialog

import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.coai.samin_total.GasRoom.SetGasRoomViewData
import com.coai.samin_total.R
import com.coai.uikit.samin.status.GasRoomView

class AlertDialog_RecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var alertData = mutableListOf<SetAlertData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return AlertViewHodler(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.alert_content_view, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return alertData.size
    }


    fun submitList(viewData: MutableList<SetAlertData>) {
        this.alertData = viewData
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        (holder as AlertViewHodler).bind(alertData[position])
//        holder.setIsRecyclable(false)
        try {
            val tmp = alertData[position].copy()
            (holder as AlertViewHodler).bind(tmp)
            holder.setIsRecyclable(false)
        }
        catch (e: Exception) {

        }
    }

    inner class AlertViewHodler(view: View) : RecyclerView.ViewHolder(view) {
        private val tv_model = view.findViewById<TextView>(R.id.tv_model)
        private val tv_id = view.findViewById<TextView>(R.id.tv_id)
        private val tv_error_content = view.findViewById<TextView>(R.id.tv_error_content)
        private val tv_time = view.findViewById<TextView>(R.id.tv_time)
        private val tv_port = view.findViewById<TextView>(R.id.tv_port)

        fun bind(setAlertData: SetAlertData) {
            when(setAlertData.model){
                1 -> tv_model.text = "가스저장고"
                2 -> tv_model.text ="가스룸"
                3 -> tv_model.text ="폐액"
                4 -> tv_model.text ="산소농도모듈"
                5 -> tv_model.text ="스팀기"
            }
            tv_id.text = setAlertData.id.toString()
            tv_error_content.text = setAlertData.content
            tv_time.text = setAlertData.time
            tv_port.text = setAlertData.port.toString()
        }
    }
}