package com.coai.samin_total.GasRoom

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.coai.samin_total.GasDock.SetGasStorageViewData
import com.coai.samin_total.R

class GasRoomSetting_RecycleAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    var gasRoomInfo = mutableListOf<SetGasRoomViewData>()

    private var oldPosition = -1
    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return GasStorageSettingViewHodler(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.board_info, parent, false)
        )

    }

    override fun getItemCount(): Int {
        return gasRoomInfo.size
    }


    fun submitList(viewData: MutableList<SetGasRoomViewData>) {
        this.gasRoomInfo = viewData
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as GasStorageSettingViewHodler).bind(gasRoomInfo[position])
        holder.setIsRecyclable(true)
        val tmpPositon = position
        //색변경
        if (selectedPosition == tmpPositon){
            holder.itemView.setBackgroundColor(Color.parseColor("#ff9800"))
        }else{
            holder.itemView.setBackgroundColor(Color.parseColor("#ffffff"))
        }


        // (1) 리스트 내 항목 클릭 시 onClick() 호출
        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, tmpPositon)
            oldPosition = selectedPosition
            selectedPosition = tmpPositon

            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)
        }
    }
    // (2) 리스너 인터페이스
    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }
    // (3) 외부에서 클릭 시 이벤트 설정
    fun setItemClickListener(onItemClickListener: OnItemClickListener){
        this.itemClickListener = onItemClickListener
    }
    // (4) setItemClickListener로 설정한 함수 실행
    private lateinit var itemClickListener: OnItemClickListener


    inner class GasStorageSettingViewHodler(view: View) : RecyclerView.ViewHolder(view) {
        private val boardInfoView =
            view.findViewById<ConstraintLayout>(R.id.board_view)
        private val modelNum = view.findViewById<TextView>(R.id.input_modelnum_tv)
        private val idNum = view.findViewById<TextView>(R.id.input_idnum_tv)
        private val sensorNum = view.findViewById<TextView>(R.id.input_sensorNum_tv)

        fun bind(gasRoomInfo: SetGasRoomViewData) {
            modelNum.text = gasRoomInfo.model.toString()
            idNum.text = gasRoomInfo.id.toString()
            sensorNum.text = gasRoomInfo.port.toString()
        }
    }
}