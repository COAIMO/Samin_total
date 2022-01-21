package com.coai.uikit.samin

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.coai.uikit.R

class VersionView@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs)  {
    init {
        val infService = Context.LAYOUT_INFLATER_SERVICE
        val li = getContext().getSystemService(infService) as LayoutInflater
        val v: View = li.inflate(R.layout.versionview, this, false)
        addView(v)

        val text = context.obtainStyledAttributes(attrs, R.styleable.VersionView).getText(R.styleable.VersionView_app_version)
        val txtview = findViewById<TextView>(R.id.textView)
        txtview.text = text
    }
}