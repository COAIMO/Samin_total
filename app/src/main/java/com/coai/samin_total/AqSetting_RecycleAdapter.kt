package com.coai.samin_total

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class AqSetting_RecycleAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var setAqInfo = mutableListOf<SetAqInfo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return aqSettingViewHodler(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.board_info, parent, false)
        )

    }

    override fun getItemCount(): Int {
        return setAqInfo.size
    }


    fun submitList(viewData: MutableList<SetAqInfo>) {
        this.setAqInfo = viewData
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
         (holder as aqSettingViewHodler).bind(setAqInfo[position])
        holder.setIsRecyclable(false)

        // (1) 리스트 내 항목 클릭 시 onClick() 호출
        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }
    // (2) 리스너 인터페이스
    interface OnItemClickListener {
        fun onClick(v:View, position: Int)
    }
    // (3) 외부에서 클릭 시 이벤트 설정
    fun setItemClickListener(onItemClickListener: OnItemClickListener){
        this.itemClickListener = onItemClickListener
    }
    // (4) setItemClickListener로 설정한 함수 실행
    private lateinit var itemClickListener: OnItemClickListener

    inner class aqSettingViewHodler(view: View) : RecyclerView.ViewHolder(view) {
        private val boardInfoView =
            view.findViewById<ConstraintLayout>(R.id.board_view)
        private val modelNum = view.findViewById<TextView>(R.id.input_modelnum_tv)
        private val idNum = view.findViewById<TextView>(R.id.input_idnum_tv)

        fun bind(setAqInfo: SetAqInfo) {
            modelNum.text = setAqInfo.model.toString()
            idNum.text = setAqInfo.id.toString()
        }
    }
}