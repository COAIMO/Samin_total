package com.coai.samin_total.GasDock

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coai.samin_total.R
import com.coai.uikit.samin.status.GasRoomView
import com.coai.uikit.samin.status.GasStorageView

class GasStorage_RecycleAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private val TAG = "로그"
    var setGasdockViewData = mutableListOf<SetGasdockViewData>()


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


    fun submitList(viewData: MutableList<SetGasdockViewData>) {
        this.setGasdockViewData = viewData
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d(TAG, "RecyclerViewAdapter - onBindViewHolder() called / position : $position")
//        holder.viewBind(this.datas[position])

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

        fun bind(setGasdockViewData: SetGasdockViewData) {
            gasDcokView.setGasName(setGasdockViewData.gasName)
            gasDcokView.setGasColor(setGasdockViewData.gasColor)
            setGasdockViewData.pressure_Min?.let { gasDcokView.setPressureMin(it) }
            setGasdockViewData.pressure_Max?.let { gasDcokView.setPressureMax(it) }
            setGasdockViewData.gasIndex?.let { gasDcokView.setGasIndex(it) }
            setGasdockViewData.isAlert?.let { gasDcokView.setAlert(it) }
            setGasdockViewData.isAlertLeft?.let { gasDcokView.setAlertLeft(it) }
            setGasdockViewData.isAlertRight?.let { gasDcokView.setAlertRight(it) }
            setGasdockViewData.pressure?.let { gasDcokView.setPressure(it) }
        }
    }

    inner class gasDockDualViewHodler(view: View) : RecyclerView.ViewHolder(view) {
        private val gasDcokView = view.findViewById<GasStorageView>(R.id.gas_storage_dual_view)

        fun bind(setGasdockViewData: SetGasdockViewData) {
            gasDcokView.setGasName(setGasdockViewData.gasName)
            gasDcokView.setGasColor(setGasdockViewData.gasColor)
            setGasdockViewData.pressure_Min?.let { gasDcokView.setPressureMin(it) }
            setGasdockViewData.pressure_Max?.let { gasDcokView.setPressureMax(it) }
            setGasdockViewData.gasIndex?.let { gasDcokView.setGasIndex(it) }
            setGasdockViewData.isAlert?.let { gasDcokView.setAlert(it) }
            setGasdockViewData.isAlertLeft?.let { gasDcokView.setAlertLeft(it) }
            setGasdockViewData.isAlertRight?.let { gasDcokView.setAlertRight(it) }
            setGasdockViewData.pressure?.let { gasDcokView.setPressure(it) }
        }
    }

    inner class gasDockAutoChangerViewHodler(view: View) : RecyclerView.ViewHolder(view) {
        private val gasDcokView =
            view.findViewById<GasStorageView>(R.id.gas_storage_autochanger_view)

        fun bind(setGasdockViewData: SetGasdockViewData) {
            gasDcokView.setGasName(setGasdockViewData.gasName)
            gasDcokView.setGasColor(setGasdockViewData.gasColor)
            setGasdockViewData.pressure_Min?.let { gasDcokView.setPressureMin(it) }
            setGasdockViewData.pressure_Max?.let { gasDcokView.setPressureMax(it) }
            setGasdockViewData.gasIndex?.let { gasDcokView.setGasIndex(it) }
            setGasdockViewData.isAlert?.let { gasDcokView.setAlert(it) }
            setGasdockViewData.isAlertLeft?.let { gasDcokView.setAlertLeft(it) }
            setGasdockViewData.isAlertRight?.let { gasDcokView.setAlertRight(it) }
            setGasdockViewData.pressure?.let { gasDcokView.setPressure(it) }
        }
    }
}