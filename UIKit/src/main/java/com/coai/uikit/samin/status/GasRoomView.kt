package com.coai.uikit.samin.status

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.coai.uikit.R
import com.coai.uikit.vectorchildfinder.VectorChildFinder
import com.coai.uikit.vectorchildfinder.VectorDrawableCompat
import org.w3c.dom.Text
import kotlin.math.roundToInt

class GasRoomView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

//    var gasColor: Color = Color("#6599CD")

    private var mPathGauge: VectorDrawableCompat.VFullPath? = null
    private var mGaugeGroup: VectorDrawableCompat.VGroup? = null
    private var mtxtUnit:TextView? = null
    private var mtxtPressure:TextView? = null
    private var mtxtGas:TextView? = null
    private var mImgView: ImageView? = null

    var mPressureMax: Float = 142f
    var mPressure: Float = 0f
    var mGasName: String = ""
    var mGasUnit: Int = 0
    var mGasColor: Int = Color.parseColor("#6599CD")

    fun getPressureMax() = mPressureMax
    fun setPressureMax(value: Float) {
        mPressureMax = value
        invalidate()
    }

    fun getGasUnit() = mGasUnit
    fun setGasUnit(value: Int) {
        mGasUnit = value
        calcDisplayValue()
        invalidate()
    }

    fun getPressure() = mPressure
    fun setPressure(value: Float) {
        mPressure = value
        calcDisplayValue()
        mGaugeGroup?.rotation = mPressure / mPressureMax * 180f
        mImgView?.invalidate()
        invalidate()
    }

    fun getGasColor() = mGasColor
    fun setGasColor(value: Int){
        mGasColor = value
        mPathGauge?.fillColor = mGasColor
        mImgView?.invalidate()
        invalidate()
    }

    fun getGasName() = mGasName
    fun setGasName(value: String){
        mGasName = value
        mtxtGas?.text = if (mGasName.isNullOrEmpty()) "H₂" else mGasName
        invalidate()
    }


    fun calcDisplayValue() {
        val value = mPressure * when(mGasUnit){
            1-> 0.0689476141537538f
            2-> 0.070307f
            3-> 6.89476f
            else -> 1f
        }
        mtxtPressure?.text = "%.0f".format(value)
    }

    init {
//        val infService = Context.LAYOUT_INFLATER_SERVICE
//        val li = getContext().getSystemService(infService) as LayoutInflater
//        val v: View = li.inflate(R.layout.gasroomview, this, true)
//        addView(v)
        LayoutInflater.from(context)
            .inflate(R.layout.gasroomview, this, true)

        val viewset = context.obtainStyledAttributes(attrs, R.styleable.GasRoomView)
        val gasColor = viewset.getColor(
            R.styleable.GasRoomView_gasColor,
            Color.parseColor("#6599CD"))
        val pressureMax = viewset.getFloat(R.styleable.GasRoomView_pressureMax, 142f)
        val pressure = viewset.getFloat(R.styleable.GasRoomView_pressure, 0f)
        val gasName = viewset.getString(R.styleable.GasRoomView_gasName)
        val gasUnit = viewset.getInt(R.styleable.GasRoomView_gasUnit, 0)

        mImgView = findViewById<ImageView>(R.id.imgView)
        mImgView?.let {
            val vector = VectorChildFinder(context, R.drawable.ic_gasroom_pressure, it)
            mPathGauge = vector.findPathByName("color_gauge")
            mGaugeGroup = vector.findGroupByName("gauge")
            mPathGauge?.fillColor = gasColor
            mGaugeGroup?.rotation = pressure / pressureMax * 180f
        }

        mtxtPressure = findViewById<TextView>(R.id.txtPressure)
        mtxtUnit = findViewById<TextView>(R.id.txtUnit)
        mtxtGas = findViewById<TextView>(R.id.txtGas)

        mtxtUnit?.text = when(gasUnit) {
            1 -> "bar"
            2 -> "kgf/cm2"
            3 -> "kPa"
            else -> "psi"
        }
        mtxtGas?.text = if (gasName.isNullOrEmpty()) "H₂" else gasName
        mtxtPressure?.text = "%.0f".format(pressure)

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val calcW = MeasureSpec.getSize(widthMeasureSpec)
        val calcH = calcW / 2163282f * 680095f
        setMeasuredDimension(
            MeasureSpec.makeMeasureSpec(calcW, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(calcH.roundToInt(), MeasureSpec.EXACTLY))
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(calcW, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(calcH.roundToInt(), MeasureSpec.EXACTLY))
    }
}