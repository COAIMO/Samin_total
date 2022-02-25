package com.coai.samin_total.CustomView

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.coai.samin_total.R

open class AQInfoView(context: Context, attrs: AttributeSet? = null): ConstraintLayout(context, attrs) {

    private var modelNumText: TextView
    private var idNumText: TextView

    init {
        val v = View.inflate(context, R.layout.board_info, this)
        modelNumText = v.findViewById(R.id.input_modelnum_tv)
        idNumText = v.findViewById(R.id.input_idnum_tv)

    }

    fun setModelText(num: Int){
        modelNumText.text = num.toString()
        onRefresh()
    }

    fun setIdText(num: Int){
        idNumText.text = num.toString()
        onRefresh()
    }
    private fun onRefresh(){
        invalidate()
        requestLayout()
    }
}