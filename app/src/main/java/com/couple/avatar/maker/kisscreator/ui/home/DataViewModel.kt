package com.couple.avatar.maker.kisscreator.ui.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.couple.avatar.maker.kisscreator.core.helper.AssetHelper
import com.couple.avatar.maker.kisscreator.core.helper.InternetHelper
import com.couple.avatar.maker.kisscreator.core.helper.MediaHelper
import com.couple.avatar.maker.kisscreator.core.service.RetrofitClient
import com.couple.avatar.maker.kisscreator.core.service.RetrofitPreventive
import com.couple.avatar.maker.kisscreator.core.utils.DataLocal.isFailBaseURL
import com.couple.avatar.maker.kisscreator.core.utils.key.AssetsKey
import com.couple.avatar.maker.kisscreator.core.utils.key.DomainKey
import com.couple.avatar.maker.kisscreator.core.utils.key.ValueKey
import com.couple.avatar.maker.kisscreator.core.utils.state.HandleState
import com.couple.avatar.maker.kisscreator.data.model.DataAPI
import com.couple.avatar.maker.kisscreator.data.model.PartAPI
import com.couple.avatar.maker.kisscreator.data.model.custom.ColorModel
import com.couple.avatar.maker.kisscreator.data.model.custom.CustomizeModel
import com.couple.avatar.maker.kisscreator.data.model.custom.LayerListModel
import com.couple.avatar.maker.kisscreator.data.model.custom.LayerModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import kotlin.collections.forEachIndexed

class DataViewModel() : ViewModel() {
    private val _allData = MutableStateFlow<ArrayList<CustomizeModel>>(arrayListOf())
    val allData: StateFlow<ArrayList<CustomizeModel>> = _allData.asStateFlow()
    private val _getDataAPI = MutableLiveData<List<PartAPI>>()
    val getDataAPI: LiveData<List<PartAPI>> get() = _getDataAPI

    fun saveAndReadData(context: Context) {
        viewModelScope.launch {
            val timeStart = System.currentTimeMillis()
            Log.d("TIMING_TRENDING", "saveAndReadData() START")
            val list = withContext(Dispatchers.IO) {
                // Lần đầu vào app -> Load data Asset -> Lưu file internal
                val assetStart = System.currentTimeMillis()
                val isFirstRun = !MediaHelper.checkFileInternal(context, ValueKey.DATA_FILE_INTERNAL)
                if (isFirstRun) {
                    AssetHelper.getDataFromAsset(context)
                    Log.d("TIMING_TRENDING", "  [Asset] copy từ assets → internal: ${System.currentTimeMillis() - assetStart}ms")
                }

                val localStart = System.currentTimeMillis()
                val totalData = MediaHelper.readListFromFile<CustomizeModel>(context, ValueKey.DATA_FILE_INTERNAL)
                    .toCollection(ArrayList())
                Log.d("TIMING_TRENDING", "  [Local] đọc DATA_FILE_INTERNAL: ${System.currentTimeMillis() - localStart}ms | items=${totalData.size}")

                val apiCacheStart = System.currentTimeMillis()
                var dataApi = MediaHelper.readListFromFile<CustomizeModel>(context, ValueKey.DATA_FILE_API_INTERNAL)
                    ?: arrayListOf()
                Log.d("TIMING_TRENDING", "  [Local] đọc DATA_FILE_API_INTERNAL (cache): ${System.currentTimeMillis() - apiCacheStart}ms | items=${dataApi.size}")

                if (dataApi.isEmpty() && InternetHelper.checkInternet(context)) {
                    Log.d("TIMING_TRENDING", "  [API] cache rỗng + có mạng → gọi API...")
                    val apiCallStart = System.currentTimeMillis()
                    getAllParts(context).collect { state ->
                        when (state) {
                            HandleState.LOADING -> {}
                            HandleState.SUCCESS -> {
                                Log.d("TIMING_TRENDING", "  [API] response + parse + lưu file: ${System.currentTimeMillis() - apiCallStart}ms")
                                dataApi = MediaHelper.readListFromFile<CustomizeModel>(context, ValueKey.DATA_FILE_API_INTERNAL)
                                Log.d("TIMING_TRENDING", "  [API] đọc lại file sau lưu: items=${dataApi?.size ?: 0}")
                            }
                            else -> {
                                Log.d("TIMING_TRENDING", "  [API] FAIL sau ${System.currentTimeMillis() - apiCallStart}ms")
                            }
                        }
                    }
                } else {
                    Log.d("TIMING_TRENDING", "  [API] bỏ qua (cache có data hoặc không có mạng)")
                }

                totalData.addAll(dataApi)
                // Sort all data by level (ascending order)
                totalData.sortBy { it.level }
                totalData
            }
            _allData.value = list
            val timeEnd = System.currentTimeMillis()
            Log.d("TIMING_TRENDING", "saveAndReadData() DONE | tổng: ${timeEnd - timeStart}ms | allData.size=${list.size}")
            Log.d("nbhieu", "time load data: ${timeEnd - timeStart}")
        }
    }

    fun ensureData(context: Context) {
        if (_allData.value.isEmpty()) {
            saveAndReadData(context)
        }
    }

    fun getAllParts(context: Context): Flow<HandleState> = flow {
        Log.d("nbhieu", "API Calling...")
        Log.d("TIMING_TRENDING", "  [API] getAllParts() START")
        emit(HandleState.LOADING)

        val primaryStart = System.currentTimeMillis()
        val response = withTimeoutOrNull(5_000) {
            try {
                RetrofitClient.api.getAllData()
            } catch (e: Exception) {
                Log.e("nbhieu", "BASE_URL failed: ${e.message}")
                Log.e("TIMING_TRENDING", "  [API] BASE_URL fail sau ${System.currentTimeMillis() - primaryStart}ms: ${e.message}")
                null
            }
        }.also {
            if (it != null) Log.d("TIMING_TRENDING", "  [API] BASE_URL response: ${System.currentTimeMillis() - primaryStart}ms | success=${it.isSuccessful}")
            else if (System.currentTimeMillis() - primaryStart >= 4900) Log.w("TIMING_TRENDING", "  [API] BASE_URL timeout (5s)")
        } ?: withTimeoutOrNull(5_000) {
            val fallbackStart = System.currentTimeMillis()
            try {
                RetrofitPreventive.api.getAllData()
            } catch (e: Exception) {
                Log.e("nbhieu", "BASE_URL_PREVENTIVE failed: ${e.message}")
                Log.e("TIMING_TRENDING", "  [API] BASE_URL_PREVENTIVE fail sau ${System.currentTimeMillis() - fallbackStart}ms: ${e.message}")
                null
            }
        }.also {
            if (it != null) Log.d("TIMING_TRENDING", "  [API] BASE_URL_PREVENTIVE response: ok")
            else Log.w("TIMING_TRENDING", "  [API] BASE_URL_PREVENTIVE cũng fail/timeout")
        }

        if (response != null && response.isSuccessful && response.body() != null) {
            val dataMap = ArrayList<DataAPI>()
            response.body()?.forEach { (key, dataBody) ->
                dataMap.add(DataAPI(key, dataBody))
            }
            withContext(Dispatchers.IO) {
                getDataAPI(context, dataMap)
            }
            emit(HandleState.SUCCESS)
        } else {
            val file = File(context.filesDir, ValueKey.DATA_FILE_API_INTERNAL)
            if (file.exists()) file.delete()
            emit(HandleState.FAIL)
        }
    }

    fun getDataAPI(context: Context, dataList: ArrayList<DataAPI>) {
        val allDataAPI: ArrayList<CustomizeModel> = arrayListOf()
        // Character 1, Character 2,...
        dataList.forEachIndexed { indexCharacter, data ->
            ///public/app/ChibiMaker/1/avatar.png

            //https://lvt-api-tech.io.vn/public/app/st225_couplemakerkisscreator/data3/avatar.png
            //https://lvt-api-tech.io.vn/public/app/st225_couplemakerkisscreator/data3/10-19-2/FFFFFF/1.png
            val baseDomain = if (!isFailBaseURL) DomainKey.BASE_URL else DomainKey.BASE_URL_PREVENTIVE
            val avatarCharacter = "$baseDomain${DomainKey.SUB_DOMAIN}/${data.name}/${DomainKey.AVATAR_CHARACTER_API}"
            val layerList = ArrayList<LayerListModel>(data.parts.size)

            // Sort parts by level in ascending order
            val sortedParts = data.parts.sortedBy { it.level }

            sortedParts.forEachIndexed { indexLayer, dataLayer ->
                // Handle both "-" and "_" delimiters, similar to local asset loading
                val layerName = if (dataLayer.parts.contains("-")) {
                    dataLayer.parts.split("-")
                } else {
                    dataLayer.parts.split("_")
                }
                val positionCustom = layerName[0].toInt() - 1
                val positionNavigation = layerName[1].toInt() - 1
                val type = if (layerName.size >= 3) layerName[2].toIntOrNull() ?: 0 else 0
                val imageNavigation = "${baseDomain}${DomainKey.SUB_DOMAIN}/${data.name}/${dataLayer.parts}/${DomainKey.IMAGE_NAVIGATION}"
                val layer = getDataLayer(baseDomain, dataLayer, dataLayer.parts)

                val layerListModel = LayerListModel(
                    positionCustom = positionCustom,
                    positionNavigation = positionNavigation,
                    imageNavigation = imageNavigation,
                    layer = layer,
                    type = type
                )
                layerList.add(layerListModel)
            }
            layerList.sortBy { it.positionNavigation }

            // Use the minimum level from all parts as the character level
            val characterLevel = sortedParts.minOfOrNull { it.level } ?: 100

            val dataApi = CustomizeModel(
                dataName = data.name,
                avatar = avatarCharacter,
                layerList = layerList,
                level = characterLevel,
                isFromAPI = true
            )
            allDataAPI.add(dataApi)
        }
        MediaHelper.writeListToFile(context, ValueKey.DATA_FILE_API_INTERNAL, allDataAPI)
        allDataAPI.forEach {
            Log.d("nbhieu", "avatar: ${it.avatar}")
        }
    }

    private fun getDataLayer(baseDomain: String, partData: PartAPI, layer: String): ArrayList<LayerModel> {
        return if (partData.colorArray != "" || partData.colorArray.isNotEmpty()) {
            getDataAPIColor(baseDomain, partData, layer)
        } else {
            getDataAPINoColor(baseDomain, partData, layer)
        }
    }

    private fun getDataAPINoColor(baseDomain: String, part: PartAPI, layer: String): ArrayList<LayerModel> {
        val actualQuantity = if (part.position == "data3") part.quantity / 2 else part.quantity
        val layerPath = ArrayList<LayerModel>(actualQuantity)
        val prefix = "$baseDomain${DomainKey.SUB_DOMAIN}/${part.position}/${layer}/"
        val suffix = DomainKey.LAYER_EXTENSION
        for (i in 1..actualQuantity) {
            layerPath.add(
                LayerModel(
                    "$prefix${i}$suffix",
                    false,
                    arrayListOf()
                )
            )
        }
        return layerPath
    }

    private fun getDataAPIColor(baseDomain: String, part: PartAPI, layer: String): ArrayList<LayerModel> {
        val layerPath = ArrayList<LayerModel>(part.quantity)
        val getColorCode = part.colorArray.split(",")
        val prefix = "$baseDomain${DomainKey.SUB_DOMAIN}/${part.position}/${layer}/"
        val suffix = DomainKey.LAYER_EXTENSION

        for (i in 1..part.quantity) {
            val listColor = ArrayList<ColorModel>(getColorCode.size)
            for (j in 0 until getColorCode.size) {
                listColor.add(
                    ColorModel(
                        "#${getColorCode[j]}",
                        "$prefix${getColorCode[j]}/${i}$suffix"
                    )
                )
            }
            layerPath.add(LayerModel(listColor.first().path, true, listColor))
        }
        return layerPath
    }
}