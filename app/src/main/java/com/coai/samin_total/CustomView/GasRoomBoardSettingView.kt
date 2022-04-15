package com.coai.samin_total.CustomView

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import com.coai.samin_total.R

class GasRoomBoardSettingView constructor(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {
    var mSensorUsable_Sw: SwitchCompat
    var mSensorType_Sp: Spinner
    var mGasType_Sp: Spinner
//    var mCapaAlert_Et: EditText
    var mMaxCapa_Et: EditText
    var mRewardValue_Et: EditText
    var mZeroPoint_Et: EditText
    var mSlopeValue_Et: EditText
    var mSelectedGas_Et:EditText
    var mSelectedGasColor_sp:Spinner
    val sensorType = arrayListOf<String>(
        "WIKAI 16BAR",
        "Sensts 142PSI",
        "Sensts 2000PSI",
        "WIKAI 10BAR",
        "WIKAI 160BAR",
        "Variable Sensor"
    )
    val gasType = arrayListOf<String>(
        "Air",
        "C2H2",
        "CH4",
        "CO2",
        "H2",
        "He",
        "N2",
        "N2O",
        "O2"
    )
    var selected_SensorType = ""
    var selected_GasType = ""

    private fun setSensorTypeSpinner() {
        val arrayAdapter = ArrayAdapter(
            context,
            R.layout.support_simple_spinner_dropdown_item,
            sensorType
        )
        mSensorType_Sp.adapter = arrayAdapter
        mSensorType_Sp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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

    private fun setGasTypeSpinner() {
        val arrayAdapter = ArrayAdapter(
            context,
            R.layout.support_simple_spinner_dropdown_item,
            gasType
        )
        mGasType_Sp.adapter = arrayAdapter
        mGasType_Sp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selected_GasType = gasType[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(context, "센서 타입을 선택해주세요.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun setIsSensor() {
        var ret: Boolean
        mSensorUsable_Sw.setOnClickListener {
            ret = mSensorUsable_Sw.isChecked
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.gas_room_board_setting_view, this, true)
        mSensorUsable_Sw = findViewById(R.id.sw_use_sensor)
        mSensorType_Sp = findViewById<Spinner>(R.id.sp_sensor_type)
        mGasType_Sp = findViewById(R.id.sp_gas_type)
//        mCapaAlert_Et = findViewById(R.id.et_capa_alert)
        mMaxCapa_Et = findViewById(R.id.et_max_capa)
        mRewardValue_Et = findViewById(R.id.et_reward_value)
        mZeroPoint_Et = findViewById(R.id.et_zero_point)
        mSlopeValue_Et = findViewById(R.id.et_slope_value)
        mSelectedGas_Et = findViewById(R.id.et_selectedGas)
        mSelectedGasColor_sp = findViewById(R.id.sp_selectedGasColor)
        setSensorTypeSpinner()
        setGasTypeSpinner()
        setIsSensor()
    }
}