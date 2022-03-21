package com.coai.samin_total

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coai.samin_total.Steamer.SetSteamerViewData
import com.coai.samin_total.WasteLiquor.WasteLiquor_RecycleAdapter

class MainViewModel : ViewModel() {
    companion object {
        const val MAINFRAGMENT = 0
        const val MAINSETTINGFRAGMENT = 1
        const val ALERTDIALOGFRAGMENT = 2
        const val SCANDIALOGFRAGMENT = 3
        const val ALERTLOGFRGAMENT = 4
        const val ADMINFRAGMENT = 5
        const val AQSETTINGFRAGMENT = 6
        const val CONTROLFRAGMENT = 7
        const val VERSIONFRAGMENT = 8
        const val PASSWORDFRAGMENT = 9
        const val GASDOCKMAINFRAGMENT = 10
        const val GASROOMMAINFRAGMENT = 11
        const val WASTELIQUORMAINFRAGMENT = 12
        const val OXYGENMAINFRAGMENT = 13
        const val STEAMERMAINFRAGMENT = 14
        const val GASSTORAGESETTINGFRAGMENT = 15
        const val GasDockStorage = 1.toByte()
        const val GasRoom = 2.toByte()
        const val WasteLiquor = 3.toByte()
        const val Oxygen = 4.toByte()
        const val Steamer = 5.toByte()
        const val Temp_Hum = 6.toByte()

    }

    //내부에서 설정하는 자료형은 뮤터블로
    //변경가능하도록 설정
    private val _model_ID_Data = MutableLiveData<HashMap<String, Byte>>()
    private val _D_size = MutableLiveData<Int>()

    private val _LevelValue = MutableLiveData<Int>()
    private val _TempValue = MutableLiveData<Int>()
    private val _WaterGauge = MutableLiveData<Boolean>()
    private val _SteamerData = MutableLiveData<SetSteamerViewData>()
    private val _GasStorageData = MutableLiveData<Float>()
    private val _GasRoomData = MutableLiveData<Float>()


    // 변경되지 않는 데이터를 가져올때 이름을 _ 언더스코어 없이 설정
    // 공개적으로 가져오는 변수는 private 이 아닌 퍼블릭으로 외부에서도 접근가능하도록 설정
    // 하지만 값을 직접 라이브데이터에 접근하지 않고 뷰모델을 통해 가져올수 있도록 설정
    val model_ID_Data: MutableLiveData<HashMap<String, Byte>>
        get() = _model_ID_Data

    val D_size: MutableLiveData<Int>
        get() = _D_size

    val LevelValue: MutableLiveData<Int>
        get() = _LevelValue

    val TempValue: MutableLiveData<Int>
        get() = _TempValue

    val WaterGauge: MutableLiveData<Boolean>
        get() = _WaterGauge

    val SteamerData: MutableLiveData<SetSteamerViewData>
        get() = _SteamerData

    val GasStorageData: MutableLiveData<Float>
        get() = _GasStorageData

    val GasRoomData: MutableLiveData<Float>
        get() = _GasRoomData

    val modelMap = HashMap<String, ByteArray>()

    init {
//        Log.d(TAG, "MainViewModel - 생성자 호출")
//        Log.d(TAG, "MainViewModel _currentValue : $_currentValue")
//        _IDsList.value = IDs


    }
}

class ListLiveData<T> : MutableLiveData<MutableList<T>>() {
    private val temp = mutableListOf<T>()

    init {
        value = temp
    }
}