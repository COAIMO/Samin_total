package com.coai.samin_total.GasDock

import android.util.Log
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
//        return datas.size
        return setGasdockViewData.size
    }


    fun submitList(viewData: List<SetGasStorageViewData>) {
        this.setGasdockViewData = viewData
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d(TAG, "RecyclerViewAdapter - onBindViewHolder() called / position : $position")

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
            gasDcokView.setGasName(setGasStorageViewData.gasName!!)
            gasDcokView.setGasColor(setGasStorageViewData.gasColor!!)
            setGasStorageViewData.pressure_Min?.let { gasDcokView.setPressureMin(it) }
            setGasStorageViewData.pressure_Max?.let { gasDcokView.setPressureMax(it) }
            setGasStorageViewData.gasIndex?.let { gasDcokView.setGasIndex(it) }
            setGasStorageViewData.isAlert?.let { gasDcokView.setAlert(it) }
            setGasStorageViewData.isAlertLeft?.let { gasDcokView.setAlertLeft(it) }
            setGasStorageViewData.isAlertRight?.let { gasDcokView.setAlertRight(it) }
            setGasStorageViewData.pressure?.let { gasDcokView.setPressure(it) }
            gasDcokView.setGasUnit(setGasStorageViewData.unit)
            gasDcokView.heartBeat(setGasStorageViewData.heartbeatCount)
        }
    }

    inner class gasDockDualViewHodler(view: View) : RecyclerView.ViewHolder(view) {
        private val gasDcokView = view.findViewById<GasStorageView>(R.id.gas_storage_dual_view)

        fun bind(setGasStorageViewData: SetGasStorageViewData) {
            gasDcokView.setGasName(setGasStorageViewData.gasName!!)
            gasDcokView.setGasColor(setGasStorageViewData.gasColor!!)
            setGasStorageViewData.pressure_Min?.let { gasDcokView.setPressureMin(it) }
            setGasStorageViewData.pressure_Max?.let { gasDcokView.setPressureMax(it) }
            setGasStorageViewData.gasIndex?.let { gasDcokView.setGasIndex(it) }
            setGasStorageViewData.isAlert?.let { gasDcokView.setAlert(it) }
            setGasStorageViewData.isAlertLeft?.let { gasDcokView.setAlertLeft(it) }
            setGasStorageViewData.isAlertRight?.let { gasDcokView.setAlertRight(it) }
//            setGasStorageViewData.pressure?.let { gasDcokView.setPressure(it) }
            setGasStorageViewData.pressureLeft?.let { gasDcokView.setPressureLeft(it) }
            setGasStorageViewData.pressureRight?.let { gasDcokView.setPressureRight(it) }
            gasDcokView.setGasUnit(setGasStorageViewData.unit)
            gasDcokView.heartBeat(setGasStorageViewData.heartbeatCount)

        }
    }

    inner class gasDockAutoChangerViewHodler(view: View) : RecyclerView.ViewHolder(view) {
        private val gasDcokView =
            view.findViewById<GasStorageView>(R.id.gas_storage_autochanger_view)

        fun bind(setGasStorageViewData: SetGasStorageViewData) {
            gasDcokView.setGasName(setGasStorageViewData.gasName!!)
            gasDcokView.setGasColor(setGasStorageViewData.gasColor!!)
            setGasStorageViewData.pressure_Min?.let { gasDcokView.setPressureMin(it) }
            setGasStorageViewData.pressure_Max?.let { gasDcokView.setPressureMax(it) }
            setGasStorageViewData.gasIndex?.let { gasDcokView.setGasIndex(it) }
            setGasStorageViewData.isAlert?.let { gasDcokView.setAlert(it) }
            setGasStorageViewData.isAlertLeft?.let { gasDcokView.setAlertLeft(it) }
            setGasStorageViewData.isAlertRight?.let { gasDcokView.setAlertRight(it) }
//            setGasStorageViewData.pressure?.let { gasDcokView.setPressure(it) }
            setGasStorageViewData.pressureLeft?.let { gasDcokView.setPressureLeft(it) }
            setGasStorageViewData.pressureRight?.let { gasDcokView.setPressureRight(it) }
            gasDcokView.setGasUnit(setGasStorageViewData.unit)
            gasDcokView.heartBeat(setGasStorageViewData.heartbeatCount)
        }
    }
}