package com.coai.samin_total

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Message
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import com.coai.samin_total.Dialog.ScanAlertDialogFragment
import com.coai.samin_total.Logic.InputFilterMinMax
import com.coai.samin_total.Logic.SaminProtocol
import com.coai.samin_total.Logic.SaminSharedPreference
import com.coai.samin_total.Service.SerialService
import com.coai.samin_total.databinding.FragmentAqSettingBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AqSettingFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    var activity: MainActivity? = null
    private lateinit var onBackPressed: OnBackPressedCallback
    private lateinit var mBinding: FragmentAqSettingBinding
    lateinit var shared: SaminSharedPreference
    private val viewmodel: MainViewModel by activityViewModels()
    lateinit var progress_Dialog: ProgressDialog
    lateinit var sendThread: Thread
    private val scanAlertDialogFragment = ScanAlertDialogFragment()

    val baudrate = arrayListOf<String>(
        "2400",
        "4800",
        "9600",
        "14400",
        "19200",
        "28800",
        "38400",
        "57600",
        "76800",
        "115200",
        "230400",
        "250000",
        "500000",
        "1000000",
    )
    var selected_baudrate = 1000000

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
        mBinding = FragmentAqSettingBinding.inflate(inflater, container, false)
        shared = SaminSharedPreference(requireContext())

        initView()
        setBaudrateSpinner()
        setButtonClickEvent()

        selected_baudrate = activity?.baudrate!!.value
        val ididx = baudrate.indexOf(selected_baudrate.toString())
        mBinding.spBaudrate.setSelection(ididx)

        return mBinding.root
    }

    private fun initView() {
        if (!shared.loadLabNameData().isEmpty()) {
            mBinding.etNewName.setText(shared.loadLabNameData())
        }
        mBinding.swCheckTimeout.isChecked = shared.getTimeOutState()
        mBinding.etFeedbackTiming.setText(shared.getFeedbackTiming().toString())
        mBinding.etFeedbackTiming.filters = arrayOf(InputFilterMinMax("0", "400"))
    }

    private fun setButtonClickEvent() {
        mBinding.saveBtn.setOnClickListener {
            onClick(mBinding.saveBtn)
        }
        mBinding.cancelBtn.setOnClickListener {
            onClick(mBinding.cancelBtn)
        }
        mBinding.btnBack.setOnClickListener {
            onClick(mBinding.btnBack)
        }
        mBinding.btnScan.setOnClickListener {
            onClick(mBinding.btnScan)
        }
    }

    private fun onClick(view: View) {
        when (view) {
            mBinding.saveBtn -> {
                val labName = mBinding.etNewName.text.toString()
                activity?.shared?.labNameSave(SaminSharedPreference.LABNAME, labName)
                viewmodel.isCheckTimeOut = mBinding.swCheckTimeout.isChecked
                activity?.shared?.SavecheckTimeOutState(viewmodel.isCheckTimeOut)

                if (shared.getTimeOutState()) {
                    activity?.discallTimemout()
                } else {
                    activity?.callTimemout()
                }

                val baud = selected_baudrate
                var feedbacktimeValue: Long = 50
                val feedbacktime = mBinding.etFeedbackTiming.text.toString().trim()
                if (!feedbacktime.isNullOrEmpty()) {
                    feedbacktimeValue = feedbacktime.toLong()
                }

//                Log.d("이상해", "selected_baudrate: ${baud}, feedbacktimeValue: ${feedbacktimeValue}")
                when(baud) {
                    2400 -> {
                        if (feedbacktimeValue < 200) {
                            feedbacktimeValue = 200
                        }
                    }
                    4800 -> {
                        if (feedbacktimeValue < 100) {
                            feedbacktimeValue = 100
                        }
                    }
                }

                if (feedbacktimeValue < 20) feedbacktimeValue = 20

                shared.SaveFeedbackTiming(feedbacktimeValue)
                Thread.sleep(1000)
                activity?.setBaudrate(baud)
                Thread.sleep(500)

                getActivity()?.let { ActivityCompat.finishAffinity(it) }
                System.exit(0)
//                activity?.onFragmentChange(MainViewModel.ADMINFRAGMENT)
            }
            mBinding.cancelBtn -> {
                activity?.onFragmentChange(MainViewModel.ADMINFRAGMENT)
            }
            mBinding.btnBack -> {
                activity?.onFragmentChange(MainViewModel.ADMINFRAGMENT)
            }
            mBinding.btnScan -> {
                if (!scanAlertDialogFragment.isAdded) {
                    scanAlertDialogFragment.show(parentFragmentManager, "")
                }

            }
        }

    }

    private fun setBaudrateSpinner() {
        val arrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            baudrate
        )
        mBinding.spBaudrate.adapter = arrayAdapter
        mBinding.spBaudrate.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selected_baudrate = baudrate[position].toInt()
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
         * @return A new instance of fragment AqSettingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AqSettingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}