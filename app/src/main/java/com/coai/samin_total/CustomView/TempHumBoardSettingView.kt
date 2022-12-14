package com.coai.samin_total.CustomView

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.widget.SwitchCompat
import com.coai.samin_total.R

class TempHumBoardSettingView constructor(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {
    var mSensorUsable_Sw: SwitchCompat
    var mMaxTemp_Et: EditText
    var mMinTemp_Et: EditText
    var mMaxHum_Et: EditText
    var mMinHUm_Et: EditText
    var mName_Et: EditText
    var mTemp_zeropoint_Et:EditText
    var mHum_zeroposint_Et:EditText

    private fun setIsSensor() {
        var ret: Boolean
        mSensorUsable_Sw.setOnClickListener {
            ret = mSensorUsable_Sw.isChecked
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.temphum_board_setting_view, this, true)
        mSensorUsable_Sw = findViewById(R.id.sw_use_sensor)
        mMaxTemp_Et = findViewById(R.id.et_temp_MaxAlert)
        mMinTemp_Et = findViewById(R.id.et_temp_MinAlert)
        mMaxHum_Et = findViewById(R.id.et_hum_MaxAlert)
        mMinHUm_Et = findViewById(R.id.et_hum_MinAlert)
        mName_Et = findViewById(R.id.et_tempHum_name)
        mTemp_zeropoint_Et = findViewById(R.id.et_temp_zeropoint)
        mHum_zeroposint_Et = findViewById(R.id.et_hum_zeropoint)
        setIsSensor()
    }
}