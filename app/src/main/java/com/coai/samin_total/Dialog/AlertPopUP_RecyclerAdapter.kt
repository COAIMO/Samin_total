package com.coai.samin_total.Dialog

import android.annotation.SuppressLint
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

class AlertPopUP_RecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var alertData = listOf<SetAlertData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = AlertViewHodler(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.popup_content_view, parent, false)
        )
        return binding
    }

    override fun getItemCount(): Int {
        return alertData.size
    }


    @SuppressLint("NotifyDataSetChanged")
    fun submitList(viewData: List<SetAlertData>) {
        this.alertData = viewData
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as AlertViewHodler).bind(alertData[position])
        holder.setIsRecyclable(false)

        val btn = holder.itemView.findViewById<Button>(R.id.btn_move)
        btn.setOnClickListener {
            buttonClickListener.onClick(it, position)
        }
    }

    interface OnButtonClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setButtonClickListener(onButtonClickListener: OnButtonClickListener) {
        this.buttonClickListener = onButtonClickListener
    }

    private lateinit var buttonClickListener: OnButtonClickListener

    inner class AlertViewHodler(view: View) : RecyclerView.ViewHolder(view) {
        private val tv_model = view.findViewById<TextView>(R.id.tv_model)
        private val tv_id = view.findViewById<TextView>(R.id.tv_id)
        private val tv_error_content = view.findViewById<TextView>(R.id.tv_error_content)
        private val tv_time = view.findViewById<TextView>(R.id.tv_time)
        private val tv_port = view.findViewById<TextView>(R.id.tv_port)

        fun bind(setAlertData: SetAlertData) {
            tv_model.text = setAlertData.model.apply {
                when (this) {
                    1 -> "가스저장고"
                    2 -> "가스룸"
                    3 -> "폐액"
                    4 -> "산소농도모듈"
                    5 -> "스팀기"
                }
            }.toString()
            tv_id.text = setAlertData.id.toString()
            tv_error_content.text = setAlertData.content
            tv_time.text = setAlertData.time
            tv_port.text = setAlertData.port.toString()
        }
    }
}