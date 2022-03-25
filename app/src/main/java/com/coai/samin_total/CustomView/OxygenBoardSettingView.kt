package com.coai.samin_total.CustomView

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import com.coai.samin_total.R

class OxygenBoardSettingView constructor(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {
    var mSensorUsable_Sw: SwitchCompat
    var mSensorType_Sp: Spinner
    var mOxygen_minValue_et: EditText
    val sensorType = arrayListOf<String>(
        "LOX-02",
    )
    var selected_SensorType =""

    private fun setSensorTypeSpinner() {
        val arrayAdapter = ArrayAdapter(
            context,
            R.layout.support_simple_spinner_dropdown_item,
            sensorType
        )
        mSensorType_Sp.adapter = arrayAdapter
        mSensorType_Sp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selected_SensorType = sensorType[position]
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
        LayoutInflater.from(context).inflate(R.layout.oxygen_board_setting_view, this, true)
        mSensorUsable_Sw = findViewById(R.id.sw_use_sensor)
        mSensorType_Sp = findViewById(R.id.sp_sensor_type)
        mOxygen_minValue_et = findViewById(R.id.et_oxygen_Minalert)

        setSensorTypeSpinner()
        setIsSensor()
    }
}