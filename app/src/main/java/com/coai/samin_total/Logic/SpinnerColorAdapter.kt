package com.coai.samin_total.Logic

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.coai.samin_total.R

class SpinnerColorAdapter:BaseAdapter {
    private val context:Context
    private val colorItemList: List<SpinnerColorItem>
    constructor(context: Context, colorItemList: ArrayList<SpinnerColorItem>){
        this.context = context
        this.colorItemList = colorItemList
    }

    override fun getCount(): Int {
        return colorItemList.size
    }

    override fun getItem(position: Int): Any {
        return colorItemList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rootView:View = LayoutInflater.from(context)
            .inflate(R.layout.custom_dropdown, parent, false)
        val colorName = rootView.findViewById<TextView>(R.id.tv_colorName)
        val colorBox = rootView.findViewById<ImageView>(R.id.iv_color)

        colorName.setText(colorItemList.get(position).getSpinnerColorName())
        colorBox.setBackgroundColor(colorItemList.get(position).getColor())
        return rootView
    }
}