package com.coai.samin_total

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import com.coai.samin_total.databinding.FragmentAdminBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AdminFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AdminFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var mBinding : FragmentAdminBinding
    var activity:MainActivity? = null
    private lateinit var onBackPressed: OnBackPressedCallback

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = getActivity() as MainActivity
        onBackPressed = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                activity!!.onFragmentChange(MainViewModel.MAINSETTINGFRAGMENT)
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
        mBinding = FragmentAdminBinding.inflate(inflater, container, false)

        mBinding.btnAqsetting.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.AQSETTINGFRAGMENT)
        }
        mBinding.btnControl.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.CONTROLFRAGMENT)
        }
        mBinding.btnVersion.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.VERSIONFRAGMENT)
        }
        mBinding.btnPassword.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.PASSWORDFRAGMENT)
        }
        mBinding.btnConnect.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.CONNECTTESTFRAGEMNT)
        }
        mBinding.btnBack.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.MAINSETTINGFRAGMENT)
        }

        return mBinding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AdminFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AdminFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}