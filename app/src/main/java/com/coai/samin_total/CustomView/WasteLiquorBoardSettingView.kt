package com.coai.samin_total.CustomView

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import com.coai.samin_total.R

class WasteLiquorBoardSettingView constructor(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {
    var mSensorUsable_Sw: SwitchCompat
    var mSensorType_Sp: Spinner
    var mWasteName_Et: EditText
    val levelsensorType = arrayListOf<String>(
        "LEVEL-01"
    )
    var selected_levelsensorType = ""

    private fun setLevelSensorTypeSpinner() {
        val arrayAdapter = ArrayAdapter(
            context,
            R.layout.support_simple_spinner_dropdown_item,
            levelsensorType
        )
        mSensorType_Sp.adapter = arrayAdapter
        mSensorType_Sp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selected_levelsensorType = levelsensorType[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(context, "센서 타입을 선택해주세요.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun setIsSensor() {
        var ret:Boolean
        mSensorUsable_Sw.setOnClickListener {
            ret = mSensorUsable_Sw.isChecked
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.wasteliquor_board_setting_view, this, true)
        mSensorUsable_Sw = findViewById(R.id.sw_use_sensor)
        mSensorType_Sp = findViewById(R.id.sp_sensor_type)
        mWasteName_Et = findViewById(R.id.et_wasteliquor_name)
        setLevelSensorTypeSpinner()
        setIsSensor()
    }
}