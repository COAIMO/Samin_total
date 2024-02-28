package com.coai.samin_total

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.coai.samin_total.CustomView.SpaceDecoration
import com.coai.samin_total.database.AlertData
import com.coai.samin_total.database.PageListAdapter
import com.coai.samin_total.databinding.FragmentAlertLogBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter


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
    var activity: MainActivity? = null
    private lateinit var onBackPressed: OnBackPressedCallback
    lateinit var mBinding: FragmentAlertLogBinding
//    private lateinit var recycleAdapter: AlertLog_RecyclerAdapter
//    private val viewmodel by activityViewModels<MainViewModel>()
//    private val alertData = mutableListOf<SetAlertData>()
    private lateinit var pageListAdapter: PageListAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = getActivity() as MainActivity
        onBackPressed = object : OnBackPressedCallback(true) {
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

    lateinit var datas: List<AlertData>
    var outPutStirng = ""
    val CREATE_FILE = 1
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentAlertLogBinding.inflate(inflater, container, false)
        initRecycler()

        mBinding.btnBack.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.MAINSETTINGFRAGMENT)
        }

        mBinding.alertLogRecyclerView.adapter = this.pageListAdapter
        GlobalScope.launch {
            Log.d("showData", " showData ================================================================================ showData")
            showData()
        }

        mBinding.btnFileOut.setOnClickListener {

            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/txt"
                putExtra(Intent.EXTRA_TITLE, "invoice.txt")
            }
            startActivityForResult(intent, CREATE_FILE)

        }

        mBinding.btnClear.setOnClickListener {
            GlobalScope.launch {
                activity?.dao?.deleteAllData()
            }
            activity?.onFragmentChange(MainViewModel.MAINSETTINGFRAGMENT)
            Toast.makeText(requireContext(), "알람 로그가 클리어 되었습니다.", Toast.LENGTH_SHORT).show()
        }
        return mBinding.root
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CREATE_FILE && resultCode == Activity.RESULT_OK) {
            GlobalScope.launch {
                data?.data?.also {
                    alterDocument(it)
                }
            }
        }
    }


    private fun alterDocument(uri: Uri) {
        try {
            val contentResolver = requireContext().applicationContext.contentResolver
            datas = activity?.dao?.getAll()!!
            contentResolver.openFileDescriptor(uri, "w")?.use {
//                FileOutputStream(it.fileDescriptor).use {
//                    it.write(datas.toString().toByteArray())
//                }
                val tmp = OutputStreamWriter(FileOutputStream(it.fileDescriptor), "UTF-8")
                tmp.write("\uFEFF" + BuildConfig.VERSION_NAME + "\r\n")
                tmp.write(datas.toString())
                tmp.flush()
            }

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    suspend fun showData() {
        activity?.viewModel?.data?.collectLatest {
            Log.d("showData", " showData ================================================================================ inshowData")
            pageListAdapter.submitData(it)
        }
    }

    private fun initRecycler() {
        mBinding.alertLogRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(context)

            //아이템 높이 간격 조절
            val decoration = SpaceDecoration(20, 20)
            addItemDecoration(decoration)

//            recycleAdapter = AlertLog_RecyclerAdapter()
////            recycleAdapter.submitList(singleDockViewData)
//            adapter = recycleAdapter
        }
//        viewmodel.alertInfo
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