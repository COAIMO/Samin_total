package com.coai.samin_total

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import com.coai.samin_total.Logic.ControlData
import com.coai.samin_total.Logic.ModbusBaudrate
import com.coai.samin_total.Logic.SaminSharedPreference
import com.coai.samin_total.databinding.FragmentControlBinding
import kotlin.system.exitProcess

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ControlFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ControlFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var activity:MainActivity? = null
    private lateinit var mBinding: FragmentControlBinding
    private lateinit var onBackPressed: OnBackPressedCallback
    lateinit var shared: SaminSharedPreference
    private val viewmodel by activityViewModels<MainViewModel>()
    val buadrate = arrayListOf<String>(
        "9600",
        "19200",
        "57600",
        "115200",
        "230400",
        "250000",
        "500000",
        "1000000",
        )
    val modbusID = arrayListOf<String>(
        "1",
        "2",
        "3",
        "4",
        "5",
        "6",
        "7"
    )
    var selected_Id = 1
    var selected_buadrate = 9600

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = getActivity() as MainActivity
        onBackPressed = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                activity!!.onFragmentChange(MainViewModel.ADMINFRAGMENT)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressed)
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
        onBackPressed.remove()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    private fun setSaveData() {
        val tmp = ControlData()
        tmp.isMirrorMode = mBinding.swMirror.isChecked
        tmp.modbusBaudrate = ModbusBaudrate.codesMap.get(selected_buadrate)!!
        tmp.modbusRTUID = selected_Id
        tmp.useModbusRTU = mBinding.swConnectModbus.isChecked
        tmp.useSettingShare = mBinding.swConnectSetting.isChecked
        shared.saveBoardSetData(SaminSharedPreference.CONTROL, tmp)
        Thread.sleep(500)
        exitProcess(-1)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentControlBinding.inflate(inflater, container, false)
        shared = SaminSharedPreference(requireContext())
        setModbusIDSpinner()
        setModbusBuadrateSpinner()

        mBinding.swMirror.isChecked = viewmodel.controlData.isMirrorMode
        val ididx = modbusID.indexOf(viewmodel.controlData.modbusRTUID.toString())
        mBinding.spModbusId.setSelection(ididx)
        val baudidx = buadrate.indexOf(viewmodel.controlData.modbusBaudrate.value.toString())
        mBinding.spModbusBuadrate.setSelection(baudidx)
        mBinding.swConnectModbus.isChecked = viewmodel.controlData.useModbusRTU
        mBinding.swConnectSetting.isChecked = viewmodel.controlData.useSettingShare

        mBinding.saveBtn.setOnClickListener{
            setSaveData()
        }

        mBinding.cancelBtn.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.ADMINFRAGMENT)
        }

        mBinding.btnBack.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.ADMINFRAGMENT)
        }
        return mBinding.root
    }
    private fun setModbusIDSpinner() {
        val arrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            modbusID
        )
        mBinding.spModbusId.adapter = arrayAdapter
        mBinding.spModbusId.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selected_Id = modbusID[position].toInt()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(context, "아이디를 선택해주세요.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun setModbusBuadrateSpinner() {
        val arrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            buadrate
        )
        mBinding.spModbusBuadrate.adapter = arrayAdapter
        mBinding.spModbusBuadrate.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selected_buadrate = buadrate[position].toInt()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(context, "통신속도를 선택해주세요.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ControlFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ControlFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}