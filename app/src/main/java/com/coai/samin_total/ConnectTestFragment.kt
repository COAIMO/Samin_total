package com.coai.samin_total

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.coai.samin_total.Logic.SaminProtocol
import com.coai.samin_total.databinding.FragmentConnectTestBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ConnectTestFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ConnectTestFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var onBackPressed: OnBackPressedCallback
    var activity: MainActivity? = null
    lateinit var mBinding: FragmentConnectTestBinding
    val aqModel = arrayListOf<String>(
        "1",
        "2",
        "3",
        "4",
        "5",
        "6",
        "7"
    )
    val aqID = arrayListOf<String>(
        "0",
        "1",
        "2",
        "3",
        "4",
        "5",
        "6",
        "7"
    )
    lateinit var selected_Model: String
    var selected_ID = ""
    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = getActivity() as MainActivity
        onBackPressed = object : OnBackPressedCallback(true) {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentConnectTestBinding.inflate(inflater, container, false)
        setAQIDSpinner()
        setAQmdoelSpinner()

        mBinding.btnBuzzerOn.setOnClickListener {
            activity?.isAnotherJob = true
            Thread.sleep(100)
            for (i in 0..1) {
                val protocol = SaminProtocol()
                protocol.buzzer_On(selected_Model.toInt().toByte(), selected_ID.toInt().toByte())
                activity?.sendProtocolToSerial(protocol.mProtocol)
                Thread.sleep(30)
            }
            activity?.isAnotherJob = false

        }
        mBinding.btnBuzzerOff.setOnClickListener {
            activity?.isAnotherJob = true
            Thread.sleep(100)
            for (i in 0..1) {
                val protocol = SaminProtocol()
                protocol.buzzer_Off(selected_Model.toInt().toByte(), selected_ID.toInt().toByte())
                activity?.sendProtocolToSerial(protocol.mProtocol)
                Thread.sleep(30)
            }
            activity?.isAnotherJob = false
        }
        mBinding.btnLedAlert.setOnClickListener {
            activity?.isAnotherJob = true
            Thread.sleep(100)
            for (i in 0..1) {
                val protocol = SaminProtocol()
                protocol.led_AlertState(
                    selected_Model.toInt().toByte(),
                    selected_ID.toInt().toByte(),
                    true,
                    true,
                    true,
                    true
                )
                activity?.sendProtocolToSerial(protocol.mProtocol)
                Thread.sleep(30)
            }
            activity?.isAnotherJob = false
        }
        mBinding.btnLedNomarl.setOnClickListener {
            activity?.isAnotherJob = true
            Thread.sleep(100)
            for (i in 0..1){
                val protocol = SaminProtocol()
                protocol.led_NormalState(
                    selected_Model.toInt().toByte(),
                    selected_ID.toInt().toByte()
                )
                activity?.sendProtocolToSerial(protocol.mProtocol)
                Thread.sleep(30)
            }
            activity?.isAnotherJob = false
        }
        mBinding.btnBack.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.ADMINFRAGMENT)
        }

        mBinding.btnCheckVersion.setOnClickListener {
            activity?.isAnotherJob = true
            Thread.sleep(100)
            for (i in 0..1){
                val protocol = SaminProtocol()
                protocol.checkVersoin(
                    selected_Model.toInt().toByte(),
                    selected_ID.toInt().toByte()
                )
                activity?.sendProtocolToSerial(protocol.mProtocol)
                Thread.sleep(30)
            }
            activity?.isAnotherJob = false
        }
        return mBinding.root
    }

    private fun setAQmdoelSpinner() {
        val arrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            aqModel
        )
        mBinding.spAQModel.adapter = arrayAdapter
        mBinding.spAQModel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selected_Model = aqModel[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(context, "센서 모델을 선택해주세요.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun setAQIDSpinner() {
        val arrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            aqID
        )
        mBinding.spAQId.adapter = arrayAdapter
        mBinding.spAQId.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selected_ID = aqID[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(context, "센서 아이디를 선택해주세요.", Toast.LENGTH_SHORT)
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
         * @return A new instance of fragment ConnectTestFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ConnectTestFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}