package com.coai.samin_total

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import com.coai.samin_total.Logic.ThreadSynchronied
import com.coai.samin_total.databinding.FragmentAqSettingBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


// TODO: 2022-01-28 AQ ID는 앱시작 시 호출되서 저장되어있으며, 저장된 아이디값은 받아와야야됨 (알람켜기, 끄기, led경고, 정상만)UI 작업
class AqSettingFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    var activity: MainActivity? = null
    private lateinit var onBackPressed: OnBackPressedCallback
    private lateinit var mBinding: FragmentAqSettingBinding
    private val viewmodel by activityViewModels<MainViewModel>()
    private val aqInfoData = mutableListOf<SetAqInfo>()
    private lateinit var recycleAdapter: AqSetting_RecycleAdapter
    val aqSettingViewList = mutableListOf<View>()
    val aqInfo_ViewMap = HashMap<SetAqInfo, View>()
    private lateinit var sendThread: Thread
    private lateinit var progressThread: Thread
    private val progressSync = ThreadSynchronied()
    private val sendSync = ThreadSynchronied()

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
        setButtonClickEvent()




        return mBinding.root
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
    }



    private fun onClick(view: View) {
        when (view) {
            mBinding.saveBtn -> {
                viewmodel.labName = mBinding.etNewName.text.toString()
                activity?.onFragmentChange(MainViewModel.ADMINFRAGMENT)
            }
            mBinding.cancelBtn ->{
                activity?.onFragmentChange(MainViewModel.ADMINFRAGMENT)
            }
            mBinding.btnBack ->{
                activity?.onFragmentChange(MainViewModel.ADMINFRAGMENT)
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