package com.coai.uikit.samin

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.coai.uikit.R

import android.view.LayoutInflater




class MenuIconView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    init {

        val infService = Context.LAYOUT_INFLATER_SERVICE
        val li = getContext().getSystemService(infService) as LayoutInflater
        val v: View = li.inflate(R.layout.menuiconview, this, false)
        addView(v)

        val type = context.obtainStyledAttributes(attrs, R.styleable.MenuIconView).getInt(R.styleable.MenuIconView_menu_type, 0)
        val text = context.obtainStyledAttributes(attrs, R.styleable.MenuIconView).getText(R.styleable.MenuIconView_menu_text)

        val txtview = findViewById<TextView>(R.id.txtView)
        val imgview = findViewById<ImageView>(R.id.imgView)
        txtview.text = text
        imgview.setImageDrawable(when(type) {
            1-> context.getDrawable(R.drawable.ic_menu_manager)
            2-> context.getDrawable(R.drawable.ic_menu_setting)
            3-> context.getDrawable(R.drawable.ic_menu_control)
            4-> context.getDrawable(R.drawable.ic_menu_version)
            5-> context.getDrawable(R.drawable.ic_menu_password)
            else -> context.getDrawable(R.drawable.ic_menu_alarm)
        })
    }
}