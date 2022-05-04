package com.coai.samin_total

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.coai.samin_total.CustomView.SpaceDecoration
import com.coai.samin_total.Dialog.SetAlertData
import com.coai.samin_total.database.*
import com.coai.samin_total.databinding.FragmentAlertLogBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.*


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
    private lateinit var recycleAdapter: AlertLog_RecyclerAdapter
    private val viewmodel by activityViewModels<MainViewModel>()
    private val alertData = mutableListOf<SetAlertData>()
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
//            addData()
            showData()
        }

        mBinding.btnFileOut.setOnClickListener {
//            GlobalScope.launch {
//                datas = activity?.dao?.getAll()!!
//                val pathPrimary = Environment.getExternalStorageDirectory()
//                log("primary 공용루트 추상경로 : $pathPrimary")
//                log("로그 : ${datas}")
//                val tmp = datas.toString().format("%0X")
//
//                if (!checkExternalStoragePermission()) {
//                    log("권한이 없어 파일 저장 불가")
//                    ActivityCompat.requestPermissions(
//                        requireActivity(),
//                        arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1000
//                    )
//                } else {
//                    val pathDocuments =
//                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
//
//                    if (checkAbstractPath(pathDocuments)) {
//                        writeFileToExternalSharedStorage(pathDocuments.toString(), tmp)
//                        log("외부저장소 공유공간 파일 저장")
//                    } else {
//                        log("외부저장소 공유공간 파일 저장 실패")
//                    }
//                }
//
//            }
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
        }
        return mBinding.root
    }

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
                tmp.write("\uFEFF" + datas.toString())
                tmp.flush()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun checkExternalStoragePermission(): Boolean {
        val haveRead = ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val haveWrite = ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if ((haveRead == PackageManager.PERMISSION_GRANTED) && (haveWrite == PackageManager.PERMISSION_GRANTED)) {
            log("권한 허용")
            return true
        } else {
            log("권한 거부")
            return false
        }
    }

    private fun writeFileToExternalSharedStorage(pathString: String, content: String) {
        try {
            val file = File("$pathString/파일이름.txt")
            log("저장될 파일 위치 : ${file}")
            file.writeText(content)
//            val fileWriter=  FileWriter(file, false)
//            val bufferedWriter = BufferedWriter(fileWriter)
//            bufferedWriter.append(content)
//
//            bufferedWriter.close()

//            val filename = "$pathString/파일이름.txt"
//            val out: Writer = BufferedWriter(
//                OutputStreamWriter(
//                    FileOutputStream(filename), "UTF-8"
//                )
//            )
//
//            try {
//                out.write(content)
//            } finally {
//                out.close()
//            }
        } catch (e: FileNotFoundException) {
            log("FileNotFoundException")
        } catch (e: Exception) {
            log("Exception")
        }
    }

    //파일이 저장될 상위 경로 유효성 검사
    private fun checkAbstractPath(file: File): Boolean {

        log("파일이 저장될 상위경로 유효성 검사")

        val value: Boolean

        if (file.exists()) {//상위 경로에 파일,디렉토리 둘중하나 존재
            log("상위경로에 동일한 이름의 파일,디렉토리 무엇인가 존재")

            if (file.isDirectory) {//디렉토리에 해당
                log("상위경로에 해당하는 것이 디렉토리")

                //디렉토리 내부에 동일한 이름의 파일또는 디렉토리가 있는지 확인
                val savedFile = File("$file/파일이름")

//                if (savedFile.exists()) {
//                    log("디렉토리 내부에 동일이름의 무엇인가 존재")
//                    log("파일 생성 불가")
//                    log("(동일한 이름의 파일이 있을 때만 생성이 불가능한 것이 아니라")
//                    log("(같은 이름의 디렉토리가 있을 때도 파일 생성이 불가능하다)")
//                    value = false
//                } else {
//                    log("저장될 파일경로에 아무것도 존재하지 않음")
//                    log("파일 생성 가능")
//                    value = true
//                }
                value = true

            } else { //파일에 해당
                log("상위 경로가 파일이라 하위에 파일 생성 불가")
                value = false
            }

        } else { //추상 경로에 실제 파일이나, 디렉토리나 아무것도 존재하지 않음
            log("상위 경로에 물리적인 것이 존재하지 않음")

            if (file.mkdirs()) { //mkdirs: 최하위까지 연쇄적으로 폴더 생성, 성공- true 실패 -false
                value = true
                log("상위 경로까지 디렉토리를 생성")
            } else {
                log("mkdirs() 알수 없는 문제 발생")
                value = false
            }

        }

        return value

    }

    private fun log(str: String?) {
        Log.d("테스트", str.toString())
    }

    suspend fun showData() {
        activity?.viewModel?.data?.collectLatest {
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

            recycleAdapter = AlertLog_RecyclerAdapter()
//            recycleAdapter.submitList(singleDockViewData)
            adapter = recycleAdapter
        }
        viewmodel.alertInfo
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