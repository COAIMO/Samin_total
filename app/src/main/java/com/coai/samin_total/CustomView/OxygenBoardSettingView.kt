package com.coai.samin_total.CustomView

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.coai.samin_total.R

class OxygenBoardSettingView constructor(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {
    var mSensorUsable_Sw:Switch
    var mSensorType_Sp: Spinner
    var mOxygen_minValue_et: EditText
    val sensorType = arrayListOf<String>(
        "LOX-02",
    )

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
                Toast.makeText(context, "$position  // $view", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(context, "센서 타입을 선택해주세요.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.oxygen_board_setting_view, this, true)
        mSensorUsable_Sw = findViewById(R.id.sw_use_sensor)
        mSensorType_Sp = findViewById(R.id.sp_sensor_type)
        mOxygen_minValue_et = findViewById(R.id.et_oxygen_Minalert)

        setSensorTypeSpinner()
    }
}