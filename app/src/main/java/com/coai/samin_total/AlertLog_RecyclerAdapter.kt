package com.coai.samin_total

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coai.samin_total.Dialog.SetAlertData

class AlertLog_RecyclerAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var alertData = listOf<SetAlertData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return AlertViewHodler(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.alertlog_view, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return alertData.size
    }


    fun submitList(viewData: List<SetAlertData>) {
        this.alertData = viewData
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as AlertViewHodler).bind(alertData[position])
//        holder.setIsRecyclable(false)
    }

    inner class AlertViewHodler(view: View) : RecyclerView.ViewHolder(view) {
        private val tv_model = view.findViewById<TextView>(R.id.tv_model)
        private val tv_id = view.findViewById<TextView>(R.id.tv_id)
        private val tv_error_content = view.findViewById<TextView>(R.id.tv_error_content)
        private val tv_time = view.findViewById<TextView>(R.id.tv_time)
        private val tv_port = view.findViewById<TextView>(R.id.tv_port)
        fun bind(setAlertData: SetAlertData) {
            tv_model.text = setAlertData.model.toString()
            tv_id.text = setAlertData.id.toString()
            tv_error_content.text = setAlertData.content
            tv_time.text = setAlertData.time
            tv_port.text = setAlertData.port.toString()
        }
    }
}

