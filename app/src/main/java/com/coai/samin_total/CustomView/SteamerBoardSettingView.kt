package com.coai.samin_total.CustomView

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import com.coai.samin_total.R

class SteamerBoardSettingView constructor(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs){
    var mSensorUsable_Sw: SwitchCompat
    var mLevelSensorType_Sp: Spinner
    var mTempSensorType_Sp: Spinner
    var mTemp_minValue_et: EditText
    val levelsensorType = arrayListOf<String>(
        "BS1"
    )
    val tempsensorType = arrayListOf<String>(
        "SST2109"
    )
    var selected_levelsensorType = ""
    var selected_tempsensorType =""

    private fun setLevelSensorTypeSpinner() {
        val arrayAdapter = ArrayAdapter(
            context,
            R.layout.support_simple_spinner_dropdown_item,
            levelsensorType
        )
        mLevelSensorType_Sp.adapter = arrayAdapter
        mLevelSensorType_Sp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
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

    private fun setTempSensorTypeSpinner() {
        val arrayAdapter = ArrayAdapter(
            context,
            R.layout.support_simple_spinner_dropdown_item,
            tempsensorType
        )
        mTempSensorType_Sp.adapter = arrayAdapter
        mTempSensorType_Sp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selected_tempsensorType = tempsensorType[position]
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
        LayoutInflater.from(context).inflate(R.layout.steamer_board_setting_view, this, true)
        mSensorUsable_Sw = findViewById(R.id.sw_use_sensor)
        mLevelSensorType_Sp = findViewById(R.id.sp_level_sensor_type)
        mTempSensorType_Sp = findViewById(R.id.sp_temp_sensor_type)
        mTemp_minValue_et = findViewById(R.id.et_temp_alert)

        setLevelSensorTypeSpinner()
        setTempSensorTypeSpinner()
        setIsSensor()
    }
}