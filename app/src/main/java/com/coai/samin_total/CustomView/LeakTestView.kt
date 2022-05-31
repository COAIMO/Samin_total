package com.coai.samin_total.CustomView

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.coai.samin_total.R
import com.coai.uikit.samin.status.GasRoomView
import com.github.mikephil.charting.charts.LineChart

class LeakTestView(context: Context, attrs: AttributeSet? = null) :
    ConstraintLayout(context, attrs) {
    var gasRoomView: GasRoomView
    var lineChart: LineChart

    init {
        LayoutInflater.from(context).inflate(R.layout.gas_room_leaktest_view, this, true)
        gasRoomView = findViewById(R.id.gas_room_data_view)
        lineChart = findViewById(R.id.gas_room_graph_view)

    }

    fun setModelText(num: Int) {
//        modelNumText.text = num.toString()
        onRefresh()
    }

    fun setIdText(num: Int) {
//        idNumText.text = num.toString()
        onRefresh()
    }

    private fun onRefresh() {
        invalidate()
        requestLayout()
    }
}