package com.coai.samin_total

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.coai.samin_total.CustomView.SpaceDecoration
import com.coai.samin_total.Dialog.AlertDialog_RecyclerAdapter
import com.coai.samin_total.Dialog.SetAlertData
import com.coai.samin_total.R
import com.coai.samin_total.databinding.FragmentAlertLogBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AlertLogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AlertLogFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var activity:MainActivity? = null
    private lateinit var onBackPressed: OnBackPressedCallback
    lateinit var mBinding:FragmentAlertLogBinding
    private lateinit var recycleAdapter: AlertLog_RecyclerAdapter
    private val viewmodel by activityViewModels<MainViewModel>()
    private val alertData = mutableListOf<SetAlertData>()

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
        mBinding = FragmentAlertLogBinding.inflate(inflater, container, false)
        initRecycler()
        initVieiw()

        mBinding.btnBack.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.MAINSETTINGFRAGMENT)
        }

        return mBinding.root
    }

    private fun initRecycler() {
        mBinding.alertLogRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(context)

            //아이템 높이 간격 조절
            val decoration = SpaceDecoration(20, 20)
            addItemDecoration(decoration)

            recycleAdapter = AlertLog_RecyclerAdapter()
//            recycleAdapter.submitList(singleDockViewData)
            adapter = recycleAdapter
        }

    }
    private fun initVieiw() {
        for (i in viewmodel.alertInfo.value!!) {
            alertData.add(i)
            recycleAdapter.submitList(alertData)
        }
        recycleAdapter.notifyDataSetChanged()

    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AlertLogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AlertLogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}