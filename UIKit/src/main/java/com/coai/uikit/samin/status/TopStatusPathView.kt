package com.coai.uikit.samin.status

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import com.coai.uikit.R
import com.coai.uikit.vectorchildfinder.VectorChildFinder
import com.coai.uikit.vectorchildfinder.VectorDrawableCompat

@SuppressLint("AppCompatCustomView")
class TopStatusPathView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ImageView(context, attrs) {
    private var mIsAlert: Boolean = false
    private var mHeartBeat: Boolean = false

    private var mAlertGorup : VectorDrawableCompat.VGroup? = null
    private var mAlert1 : VectorDrawableCompat.VFullPath? = null
    private var mAlert2 : VectorDrawableCompat.VFullPath? = null
    private var mAlert3 : VectorDrawableCompat.VFullPath? = null
    private var mRing : VectorDrawableCompat.VFullPath? = null

    fun isHeartBeat():Boolean = mHeartBeat
    fun setHeartBeat(heartbeat: Boolean) {
        mHeartBeat = heartbeat

        if (mHeartBeat) {
            mAlert1!!.fillAlpha = 1f
            mAlert2!!.fillAlpha = 1f
            mAlert3!!.fillAlpha = 1f
        } else {
            mAlert1!!.fillAlpha = 0f
            mAlert2!!.fillAlpha = 0f
            mAlert3!!.fillAlpha = 0f
        }
        invalidate()
    }

    fun isAlert(): Boolean = mIsAlert
    fun setAlert(isAlert: Boolean) {
        mIsAlert = isAlert
        if (mIsAlert) {
            mRing?.fillAlpha = 1f
            mAlertGorup!!.translateY = 0f
        } else {
            mRing?.fillAlpha = 0f
            mAlertGorup!!.translateY = 20f
        }
        invalidate()
    }

    init {
        val type = context.obtainStyledAttributes(attrs, R.styleable.TopStatusView).getInt(R.styleable.TopStatusView_status_type, 0)
        mIsAlert = context.obtainStyledAttributes(attrs, R.styleable.TopStatusView).getBoolean(R.styleable.TopStatusView_isAlert, false)

        val resid = when(type){
            1-> R.drawable.ic_topstatus_gasroom
            2-> R.drawable.ic_topstatus_waste
            3-> R.drawable.ic_topstatus_oxyzen
            4-> R.drawable.ic_topstatus_steamer
            else -> R.drawable.ic_topstatus_gasdock
        }

        val vector = VectorChildFinder(context, resid, this)
        mAlertGorup = vector.findGroupByName("groupOfAlert")
        mAlert1 = vector.findPathByName("markOfAlert1")
        mAlert2 = vector.findPathByName("markOfAlert2")
        mAlert3 = vector.findPathByName("markOfAlert3")
        mRing = vector.findPathByName("ringAlert")
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    override fun invalidate() {
        super.invalidate()
    }
}