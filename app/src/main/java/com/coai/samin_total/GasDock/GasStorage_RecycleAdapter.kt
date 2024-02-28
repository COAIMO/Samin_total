package com.coai.samin_total.GasDock

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coai.samin_total.Logic.AutoUpdatableAdapter
import com.coai.samin_total.R
import com.coai.uikit.samin.status.GasStorageView

class GasStorage_RecycleAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    AutoUpdatableAdapter {


    private val TAG = "로그"
    var setGasdockViewData = listOf<SetGasStorageViewData>()
    var mItemViewType:Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View?

        return when (viewType) {
            0 -> {
                view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.gas_storage_single_view, parent, false)
                gasDockSingleViewHodler(view)
            }
            1 -> {
                view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.gas_storage_dual_view, parent, false)
                gasDockDualViewHodler(view)
            }
            2 -> {
                view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.gas_storage_autochanger_view, parent, false)
                gasDockAutoChangerViewHodler(view)
            }

            else -> {
                view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.gas_storage_single_view, parent, false)
                gasDockSingleViewHodler(view)
            }

        }
    }

    override fun getItemCount(): Int {
        return setGasdockViewData.size
    }


    fun submitList(viewData: List<SetGasStorageViewData>) {
        val tmp = viewData.filter {
            it.usable
        }
        this.setGasdockViewData = tmp
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (setGasdockViewData[position].ViewType) {
            0 -> {
                (holder as gasDockSingleViewHodler).bind(setGasdockViewData[position])
                holder.setIsRecyclable(false)

            }
            1 -> {
                (holder as gasDockDualViewHodler).bind(setGasdockViewData[position])
                holder.setIsRecyclable(false)
            }
            2 -> {
                (holder as gasDockAutoChangerViewHodler).bind(setGasdockViewData[position])
                holder.setIsRecyclable(false)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
//        return datas[position].type!!
        return setGasdockViewData[position].ViewType
    }

    inner class gasDockSingleViewHodler(view: View) : RecyclerView.ViewHolder(view) {
        private val gasDcokView = view.findViewById<GasStorageView>(R.id.gas_storage_single_view)

        fun bind(setGasStorageViewData: SetGasStorageViewData) {
            if (!gasDcokView.getGasName().equals(setGasStorageViewData.gasName!!))
                gasDcokView.setGasName(setGasStorageViewData.gasName!!)
            if (!gasDcokView.getGasColor().equals(setGasStorageViewData.gasColor!!))
                gasDcokView.setGasColor(setGasStorageViewData.gasColor!!)
            setGasStorageViewData.pressure_Min?.let {
                if (!gasDcokView.getPressureMin().equals(it))
                    gasDcokView.setPressureMin(it)
            }
            setGasStorageViewData.pressure_Max?.let {
                if (!gasDcokView.getPressureMax().equals(it))
                    gasDcokView.setPressureMax(it)
            }
            setGasStorageViewData.gasIndex?.let {
                if (!gasDcokView.getGasIndex().equals(it))
                    gasDcokView.setGasIndex(it)
            }

            gasDcokView.setLowLeftPressAlert(setGasStorageViewData.isLowLeftPressAlert)

            setGasStorageViewData.isAlert?.let {
                if (!gasDcokView.isAlert().equals(it))
                    gasDcokView.setAlert(it)
            }
            setGasStorageViewData.pressure?.let {
                if (!gasDcokView.getPressure().equals(it))
                    gasDcokView.setPressure(it)
            }
            if (!gasDcokView.getGasUnit().equals(setGasStorageViewData.unit))
                gasDcokView.setGasUnit(setGasStorageViewData.unit)

            gasDcokView.heartBeat(setGasStorageViewData.heartbeatCount)
        }
    }

    inner class gasDockDualViewHodler(view: View) : RecyclerView.ViewHolder(view) {
        private val gasDcokView = view.findViewById<GasStorageView>(R.id.gas_storage_dual_view)

        fun bind(setGasStorageViewData: SetGasStorageViewData) {
            if (!gasDcokView.getGasName().equals(setGasStorageViewData.gasName!!))
                gasDcokView.setGasName(setGasStorageViewData.gasName!!)
            if (!gasDcokView.getGasColor().equals(setGasStorageViewData.gasColor!!))
                gasDcokView.setGasColor(setGasStorageViewData.gasColor!!)

            setGasStorageViewData.pressure_Min?.let {
                if (!gasDcokView.getPressureMin().equals(it))
                    gasDcokView.setPressureMin(it)
            }
            setGasStorageViewData.pressure_Max?.let {
                if (!gasDcokView.getPressureMax().equals(it))
                    gasDcokView.setPressureMax(it)
            }
            setGasStorageViewData.gasIndex?.let {
                if (!gasDcokView.getGasIndex().equals(it))
                    gasDcokView.setGasIndex(it)
            }
            gasDcokView.setLowLeftPressAlert(setGasStorageViewData.isLowLeftPressAlert)
            gasDcokView.setLowRightPressAlert(setGasStorageViewData.isLowRightPressAlert)
            setGasStorageViewData.isAlert?.let {
                if (!gasDcokView.isAlert().equals(it))
                    gasDcokView.setAlert(it)
            }
            setGasStorageViewData.isAlertLeft?.let {
                if (!gasDcokView.isAlertLeft().equals(it))
                    gasDcokView.setAlertLeft(it)
            }
            setGasStorageViewData.isAlertRight?.let {
                if(!gasDcokView.isAlertRight().equals(it))
                    gasDcokView.setAlertRight(it)
            }
//            setGasStorageViewData.pressure?.let { gasDcokView.setPressure(it) }
            setGasStorageViewData.pressureLeft?.let {
                if (!gasDcokView.getPressureLeft().equals(it))
                    gasDcokView.setPressureLeft(it)
            }
            setGasStorageViewData.pressureRight?.let {
                if (!gasDcokView.getPressureRight().equals(it))
                    gasDcokView.setPressureRight(it)
            }
            if (!gasDcokView.getGasUnit().equals(setGasStorageViewData.unit))
                gasDcokView.setGasUnit(setGasStorageViewData.unit)

            gasDcokView.heartBeat(setGasStorageViewData.heartbeatCount)

        }

//        fun bind(left_data: SetGasStorageViewData, right_data: SetGasStorageViewData) {
//            gasDcokView.setGasName(left_data.gasName!!)
//            gasDcokView.setGasColor(left_data.gasColor!!)
//            left_data.pressure_Min?.let { gasDcokView.setPressureMin(it) }
//            left_data.pressure_Max?.let { gasDcokView.setPressureMax(it) }
//            left_data.gasIndex?.let { gasDcokView.setGasIndex(it) }
//            left_data.isAlertLeft?.let { gasDcokView.setAlertLeft(it) }
//            right_data.isAlertRight?.let { gasDcokView.setAlertRight(it) }
//            left_data.pressureLeft?.let { gasDcokView.setPressureLeft(it) }
//            right_data.pressureRight?.let { gasDcokView.setPressureRight(it) }
//            gasDcokView.setGasUnit(left_data.unit)
//            gasDcokView.heartBeat(left_data.heartbeatCount)
//        }
    }

    inner class gasDockAutoChangerViewHodler(view: View) : RecyclerView.ViewHolder(view) {
        private val gasDcokView =
            view.findViewById<GasStorageView>(R.id.gas_storage_autochanger_view)

        fun bind(setGasStorageViewData: SetGasStorageViewData) {
            if (!gasDcokView.getGasName().equals(setGasStorageViewData.gasName!!))
                gasDcokView.setGasName(setGasStorageViewData.gasName!!)
            if (!gasDcokView.getGasColor().equals(setGasStorageViewData.gasColor!!))
                gasDcokView.setGasColor(setGasStorageViewData.gasColor!!)

            setGasStorageViewData.pressure_Min?.let {
                if (!gasDcokView.getPressureMin().equals(it))
                    gasDcokView.setPressureMin(it)
            }
            setGasStorageViewData.pressure_Max?.let {
                if (!gasDcokView.getPressureMax().equals(it))
                    gasDcokView.setPressureMax(it)
            }
            setGasStorageViewData.gasIndex?.let {
                if (!gasDcokView.getGasIndex().equals(it))
                    gasDcokView.setGasIndex(it)
            }

            gasDcokView.setLowLeftPressAlert(setGasStorageViewData.isLowLeftPressAlert)
            gasDcokView.setLowRightPressAlert(setGasStorageViewData.isLowRightPressAlert)

            setGasStorageViewData.isAlert?.let {
                if (!gasDcokView.isAlert().equals(it))
                    gasDcokView.setAlert(it)
            }
            setGasStorageViewData.isAlertLeft?.let {
                if (!gasDcokView.isAlertLeft().equals(it))
                    gasDcokView.setAlertLeft(it)
            }
            setGasStorageViewData.isAlertRight?.let {
                if(!gasDcokView.isAlertRight().equals(it))
                    gasDcokView.setAlertRight(it)
            }
//            setGasStorageViewData.pressure?.let { gasDcokView.setPressure(it) }
            setGasStorageViewData.pressureLeft?.let {
                if (!gasDcokView.getPressureLeft().equals(it))
                    gasDcokView.setPressureLeft(it)
            }
            setGasStorageViewData.pressureRight?.let {
                if (!gasDcokView.getPressureRight().equals(it))
                    gasDcokView.setPressureRight(it)
            }
            if (!gasDcokView.getGasUnit().equals(setGasStorageViewData.unit))
                gasDcokView.setGasUnit(setGasStorageViewData.unit)

            gasDcokView.heartBeat(setGasStorageViewData.heartbeatCount)
        }
    }
}