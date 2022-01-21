package com.coai.uikit.samin.status

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import com.coai.uikit.R

@SuppressLint("AppCompatCustomView")
class TopStatusView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ImageView(context, attrs) {
    private var mIsAlert: Boolean = false

    fun isAlert(): Boolean = mIsAlert
    fun setAlert(isAlert: Boolean) {
        mIsAlert = isAlert
        if (mIsAlert) {
            if (drawable is AnimatedVectorDrawable)
                (drawable as AnimatedVectorDrawable).start()
        }
        else {
            if (drawable is AnimatedVectorDrawable) {
//                (drawable as AnimatedVectorDrawable).stop()
                (drawable as AnimatedVectorDrawable).reset()
            }
        }
        invalidate()
        requestLayout()
    }

    init {
        val type = context.obtainStyledAttributes(attrs, R.styleable.TopStatusView).getInt(R.styleable.TopStatusView_status_type, 0)
        mIsAlert = context.obtainStyledAttributes(attrs, R.styleable.TopStatusView).getBoolean(R.styleable.TopStatusView_isAlert, false)

        getVectorDrawable(type)?.let{
            setImageDrawable(it)
        }
    }

    private fun getVectorDrawable(type: Int): Drawable?{
        val ret = when(type){
            1-> context.getDrawable(R.drawable.avd_topstatus_gasroom)
            2-> context.getDrawable(R.drawable.avd_topstatus_waste)
            3-> context.getDrawable(R.drawable.avd_topstatus_oxyzen)
            4-> context.getDrawable(R.drawable.avd_topstatus_steamer)
            else -> context.getDrawable(R.drawable.avd_topstatus_gasdock)
        }
        return ret
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }
}