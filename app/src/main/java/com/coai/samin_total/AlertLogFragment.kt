package com.coai.samin_total

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.coai.samin_total.CustomView.SpaceDecoration
import com.coai.samin_total.Dialog.AlertDialog_RecyclerAdapter
import com.coai.samin_total.Dialog.SetAlertData
import com.coai.samin_total.Logic.AppDatabase
import com.coai.samin_total.R
import com.coai.samin_total.database.*
import com.coai.samin_total.databinding.FragmentAlertLogBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private lateinit var pageListAdapter : PageListAdapter

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

        this.pageListAdapter = PageListAdapter()
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

        mBinding.alertLogRecyclerView.adapter = this.pageListAdapter

        GlobalScope.launch {
            addData()
            showData()
        }

        return mBinding.root
    }

    suspend fun showData(){
        activity?.viewModel?.data?.collectLatest {
            pageListAdapter.submitData(it)
        }
    }
    suspend fun addData() =
        withContext(Dispatchers.IO) {
            var dao = Room.databaseBuilder(requireActivity().application!!,
                AlertDatabase::class.java,
                "alertLogs")
                .build()
                .alertDAO()
            var data: AlertData
            for (i in 0..10) {
                data = AlertData(
                    "sdafasdfa",
                    5,
                    i,
                    "Error : sdfkjasdlkfjASdfasd",
                    1,
                    false
                )
            dao.insertData(data)
            }
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
        viewmodel.alertInfo
    }
    private fun initVieiw() {
//        alertData.clear()
//        for (i in viewmodel.alertInfo.value!!) {
//            alertData.add(i)
//            recycleAdapter.submitList(alertData.asReversed())
//        }
//        recycleAdapter.notifyDataSetChanged()

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