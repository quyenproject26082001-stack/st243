package com.avatar.maker.celebrity.ui.cosplay

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.avatar.maker.celebrity.R
import com.avatar.maker.celebrity.core.base.BaseActivity
import com.avatar.maker.celebrity.core.extensions.gone
import com.avatar.maker.celebrity.core.extensions.handleBackLeftToRight
import com.avatar.maker.celebrity.core.extensions.hideNavigation
import com.avatar.maker.celebrity.core.extensions.invisible
import com.avatar.maker.celebrity.core.extensions.showInterAll
import com.avatar.maker.celebrity.core.extensions.tap
import com.avatar.maker.celebrity.core.extensions.visible
import com.avatar.maker.celebrity.core.helper.BitmapHelper
import com.avatar.maker.celebrity.core.helper.MediaHelper
import com.avatar.maker.celebrity.core.utils.key.IntentKey
import com.avatar.maker.celebrity.core.utils.key.ValueKey
import com.avatar.maker.celebrity.core.utils.state.SaveState
import com.avatar.maker.celebrity.data.model.custom.SuggestionModel
import com.avatar.maker.celebrity.databinding.ActivityCosplayCustomizeBinding
import com.avatar.maker.celebrity.ui.customize.BottomNavigationCustomizeAdapter
import com.avatar.maker.celebrity.ui.customize.ColorLayerCustomizeAdapter
import com.avatar.maker.celebrity.ui.customize.CustomizeCharacterViewModel
import com.avatar.maker.celebrity.ui.customize.LayerCustomizeAdapter
import com.avatar.maker.celebrity.ui.home.DataViewModel
import com.lvt.ads.util.Admob
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CosplayCustomizeActivity : BaseActivity<ActivityCosplayCustomizeBinding>() {

    private val viewModel: CustomizeCharacterViewModel by viewModels()
    private val dataViewModel: DataViewModel by viewModels()

    val layerCustomizeAdapter by lazy { LayerCustomizeAdapter(this) }
    val colorLayerCustomizeAdapter by lazy { ColorLayerCustomizeAdapter(this) }
    val bottomNavigationCustomizeAdapter by lazy { BottomNavigationCustomizeAdapter(this) }

    private var suggestionModel = SuggestionModel()
    private var countdownJob: Job? = null
    private var currentProgress = 0

    override fun setViewBinding(): ActivityCosplayCustomizeBinding {
        return ActivityCosplayCustomizeBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initRcv()
        binding.tvCountDown.text = "10:00"
        binding.progressContainer.post {
            val containerH = binding.progressContainer.height.toFloat()
            val thumbH = binding.icThumb.height.toFloat()
            // Ẩn toàn bộ progressBar (0%)
            binding.progressBar.clipBounds = Rect(0, containerH.toInt(), binding.progressBar.width, containerH.toInt())
            // Thumb bắt đầu ở dưới cùng (bottom thumb = bottom container)
            binding.icThumb.translationY = containerH / 2f - thumbH / 2f  // = (containerH - thumbH/2) - containerH/2
        }
        lifecycleScope.launch { showLoading() }
        dataViewModel.ensureData(this)
    }


    override fun dataObservable() {
        lifecycleScope.launch {
            launch {
                dataViewModel.allData.collect { list ->
                    if (list.isNotEmpty()) {
                        viewModel.positionSelected = intent.getIntExtra(IntentKey.INTENT_KEY, 0)
                        val safePos = viewModel.positionSelected.coerceIn(0, list.size - 1)
                        viewModel.setDataCustomize(list[safePos])
                        viewModel.setIsDataAPI(list[safePos].isFromAPI)
                        initData()
                    }
                }
            }
            launch {
                viewModel.bottomNavigationList.collect { navList ->
                    if (navList.isNotEmpty()) {
                        bottomNavigationCustomizeAdapter.submitList(navList)
                        layerCustomizeAdapter.submitList(viewModel.itemNavList[viewModel.positionNavSelected])
                        colorLayerCustomizeAdapter.submitList(viewModel.colorItemNavList[viewModel.positionNavSelected])
                    }
                }
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            btnSamplePhoto.tap(300) { toggleOverlay() }
            btnCloseImage.tap(300) { hideOverlay() }
            btnMan.tap(300) { handleGenderSwitch(1) }
            btnWooman.tap(300) { handleGenderSwitch(2) }
            btnColor.tap(300) { if (binding.flColor.visibility == View.VISIBLE) binding.flColor.invisible() else binding.flColor.visible() }
            icArrowLeft.tap(300) { binding.flColor.invisible() }
            actionBar.apply {
                btnActionBarLeft.tap { confirmExit() }
                btnActionBarRight.tap(800) { navigateToSuccess() }
            }
        }
        handleAdapters()
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            btnActionBarLeft.visible()
            tvCenter.gone()

            btnActionBarCenter.invisible()
            btnActionBarRight.visible()
        }
    }

    private fun initRcv() {
        binding.apply {
            rcvLayer.apply { adapter = layerCustomizeAdapter; itemAnimator = null }
            rcvColor.apply { adapter = colorLayerCustomizeAdapter; itemAnimator = null }
            rcvNavigation.apply { adapter = bottomNavigationCustomizeAdapter; itemAnimator = null }
        }
    }

    private fun handleAdapters() {
        layerCustomizeAdapter.onItemClick = { item, position ->
            viewModel.checkDataInternet(this@CosplayCustomizeActivity) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val path = viewModel.setClickFillLayer(item, position)
                    withContext(Dispatchers.Main) {
                        Glide.with(this@CosplayCustomizeActivity).load(path)
                            .into(viewModel.imageViewList[viewModel.positionCustom])
                        layerCustomizeAdapter.submitList(viewModel.itemNavList[viewModel.positionNavSelected])
                        updateProgress()
                    }
                }
            }
        }
        layerCustomizeAdapter.onNoneClick = { position ->
            viewModel.checkDataInternet(this@CosplayCustomizeActivity) {
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.setIsSelectedItem(viewModel.positionCustom)
                    viewModel.setPathSelected(viewModel.positionCustom, "")
                    viewModel.setKeySelected(viewModel.positionNavSelected, "")
                    viewModel.setItemNavList(viewModel.positionNavSelected, position)
                    withContext(Dispatchers.Main) {
                        Glide.with(this@CosplayCustomizeActivity)
                            .clear(viewModel.imageViewList[viewModel.positionCustom])
                        layerCustomizeAdapter.submitList(viewModel.itemNavList[viewModel.positionNavSelected])
                        updateProgress()
                    }
                }
            }
        }
        layerCustomizeAdapter.onRandomClick = {
            viewModel.checkDataInternet(this@CosplayCustomizeActivity) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val (path, isMoreColors) = viewModel.setClickRandomLayer()
                    withContext(Dispatchers.Main) {
                        Glide.with(this@CosplayCustomizeActivity).load(path)
                            .into(viewModel.imageViewList[viewModel.positionCustom])
                        layerCustomizeAdapter.submitList(viewModel.itemNavList[viewModel.positionNavSelected])
                        if (isMoreColors) {
                            colorLayerCustomizeAdapter.submitList(viewModel.colorItemNavList[viewModel.positionNavSelected])
                        }
                        updateProgress()
                    }
                }
            }
        }
        colorLayerCustomizeAdapter.onItemClick = { position ->
            viewModel.checkDataInternet(this@CosplayCustomizeActivity) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val pathColor = viewModel.setClickChangeColor(position)
                    viewModel.updateAllItemsColor(position)
                    withContext(Dispatchers.Main) {
                        if (pathColor.isNotEmpty()) {
                            Glide.with(this@CosplayCustomizeActivity).load(pathColor)
                                .into(viewModel.imageViewList[viewModel.positionCustom])
                        }
                        colorLayerCustomizeAdapter.submitList(viewModel.colorItemNavList[viewModel.positionNavSelected])
                        layerCustomizeAdapter.submitList(viewModel.itemNavList[viewModel.positionNavSelected].toList())
                        checkStatusColor()
                        updateProgress()
                    }
                }
            }
        }
        bottomNavigationCustomizeAdapter.onItemClick = { pos ->
            viewModel.checkDataInternet(this@CosplayCustomizeActivity) {
                val layerIndex = viewModel.bottomNavigationList.value.getOrNull(pos)?.layerIndex ?: pos
                if (layerIndex != viewModel.positionNavSelected) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        viewModel.setPositionNavSelected(layerIndex)
                        viewModel.setPositionCustom(viewModel.dataCustomize.value!!.layerList[layerIndex].positionCustom)
                        viewModel.setClickBottomNavigation(pos)
                        withContext(Dispatchers.Main) {
                            layerCustomizeAdapter.submitList(viewModel.itemNavList[viewModel.positionNavSelected])
                            colorLayerCustomizeAdapter.submitList(viewModel.colorItemNavList[viewModel.positionNavSelected])
                            checkStatusColor()
                        }
                    }
                }
            }
        }
    }

    private fun initData() {
        val exHandler = CoroutineExceptionHandler { _, _ -> }
        CoroutineScope(SupervisorJob() + Dispatchers.IO + exHandler).launch {
            var pathDefault = ""
            val d1 = async {
                viewModel.updateAvatarPath(viewModel.dataCustomize.value!!.avatar)
                // Load the target suggestion (for comparison / sample photo display)
                val model = MediaHelper.readModelFromFile<SuggestionModel>(
                    this@CosplayCustomizeActivity, ValueKey.SUGGESTION_FILE_INTERNAL
                )
                if (model != null) {
                    suggestionModel = model
                }
                // Always start fresh so the user builds the character from scratch
                viewModel.resetDataList()
                viewModel.addValueToItemNavList()
                viewModel.setItemColorDefault()
                viewModel.setBottomNavigationListDefault()
                val firstLayerIndex = viewModel.bottomNavigationList.value.first().layerIndex
                viewModel.setPositionNavSelected(firstLayerIndex)
                viewModel.setPositionCustom(viewModel.dataCustomize.value!!.layerList[firstLayerIndex].positionCustom)
                val layers = viewModel.dataCustomize.value!!.layerList
                val manBodyIndex = layers.indexOfFirst { it.type == 1 || it.type == 0 }
                val womanBodyIndex = layers.indexOfFirst { it.type == 2 || it.type == 0 }
                setOf(manBodyIndex, womanBodyIndex).filter { it >= 0 }.distinct()
                    .forEach { bodyIdx ->
                        val posCustom = layers[bodyIdx].positionCustom
                        val posNav = layers[bodyIdx].positionNavigation
                        if (viewModel.pathSelectedList.getOrNull(posCustom).isNullOrEmpty()) {
                            val defaultPath = layers[bodyIdx].layer.first().image
                            viewModel.setIsSelectedItem(posCustom)
                            viewModel.setPathSelected(posCustom, defaultPath)
                            viewModel.setKeySelected(posNav, defaultPath)
                        }
                    }
                pathDefault = layers[firstLayerIndex].layer.firstOrNull()?.image ?: ""
                setOf(manBodyIndex, womanBodyIndex).filter { it >= 0 }.distinct().forEach { bodyIdx ->
                    val bodyDefaultPath = layers[bodyIdx].layer.firstOrNull()?.image ?: ""
                    val defaultItemIndex = viewModel.itemNavList[bodyIdx].indexOfFirst { it.path == bodyDefaultPath }
                    if (defaultItemIndex >= 0) viewModel.setItemNavList(bodyIdx, defaultItemIndex)
                }
                return@async true
            }
            val d2 = async(Dispatchers.Main) {
                if (d1.await()) viewModel.setImageViewList(binding.layoutCustomLayer)
                return@async true
            }
            withContext(Dispatchers.Main) {
                if (d1.await() && d2.await()) {
                    // Load body default cho cả 2 gender
                    val layers = viewModel.dataCustomize.value!!.layerList
                    val manBodyIndex = layers.indexOfFirst { it.type == 1 || it.type == 0 }
                    val womanBodyIndex = layers.indexOfFirst { it.type == 2 || it.type == 0 }
                    setOf(manBodyIndex, womanBodyIndex).filter { it >= 0 }.distinct().forEach { bodyIdx ->
                        val posCustom = layers[bodyIdx].positionCustom
                        val path = viewModel.pathSelectedList.getOrNull(posCustom) ?: ""
                        if (path.isNotEmpty()) {
                            Glide.with(this@CosplayCustomizeActivity).load(path)
                                .into(viewModel.imageViewList[posCustom])
                        }
                    }

                    binding.btnMan.isSelected = true
                    binding.btnWooman.isSelected = false
                    layerCustomizeAdapter.submitList(viewModel.itemNavList[viewModel.positionNavSelected])
                    colorLayerCustomizeAdapter.submitList(viewModel.colorItemNavList[viewModel.positionNavSelected])
                    checkStatusColor()

                    // Show target in sample photo button
                    if (suggestionModel.pathInternalRandom.isNotEmpty()) {
                        Glide.with(this@CosplayCustomizeActivity)
                            .load(suggestionModel.pathInternalRandom)
                            .into(binding.btnSamplePhoto)
                        Glide.with(this@CosplayCustomizeActivity)
                            .load(suggestionModel.pathInternalRandom)
                            .into(binding.btnSamplePhotoShow)
                    }

                    viewModel.setIsCreated(true)
                    updateProgress()
                    startCountdown()
                    dismissLoading()
                    hideNavigation(false)
                }
            }
        }
    }

    // ── Color visibility ─────────────────────────────────────────────────────

    private fun handleGenderSwitch(gender: Int) {
        if (viewModel.selectedGender.value == gender) return
        binding.btnMan.isSelected = gender == 1
        binding.btnWooman.isSelected = gender == 2
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.setGender(gender)
            viewModel.setBottomNavigationListDefault()
            val firstLayerIndex = viewModel.bottomNavigationList.value.first().layerIndex
            viewModel.setPositionNavSelected(firstLayerIndex)
            viewModel.setPositionCustom(viewModel.dataCustomize.value!!.layerList[firstLayerIndex].positionCustom)
            withContext(Dispatchers.Main) {
                layerCustomizeAdapter.submitList(viewModel.itemNavList[viewModel.positionNavSelected])
                colorLayerCustomizeAdapter.submitList(viewModel.colorItemNavList[viewModel.positionNavSelected])
                checkStatusColor()
            }
        }
    }

    private fun checkStatusColor() {
        val pos = viewModel.positionNavSelected
        if (pos >= 0 && viewModel.colorItemNavList.size > pos &&
            viewModel.colorItemNavList[pos].isNotEmpty()
        ) {
            binding.color.visible()
            binding.btnColor.visible()
            binding.flColor.visible()
        } else {
            binding.color.invisible()
            binding.flColor.invisible()
        }
    }

    // ── Overlay toggle ──────────────────────────────────────────────────────

    private fun toggleOverlay() {
        if (binding.btnOverLay.visibility == View.VISIBLE) {
            hideOverlay()
        } else {
            binding.btnOverLay.visible()
            binding.containerBtnSamplePhotoShow.visible()
        }
    }

    private fun hideOverlay() {
        binding.btnOverLay.gone()
    }

    // ── Progress ─────────────────────────────────────────────────────────────

    private fun calculateProgress(): Int {
        val targetPaths = suggestionModel.pathSelectedList
        val currentPaths = viewModel.pathSelectedList
        if (targetPaths.isEmpty()) return 0
        val totalNonEmpty = targetPaths.count { it.isNotEmpty() }
        if (totalNonEmpty == 0) return 0
        var matches = 0
        for (i in currentPaths.indices) {
            if (i < targetPaths.size && currentPaths[i].isNotEmpty() && currentPaths[i] == targetPaths[i]) {
                matches++
            }
        }
        return (matches * 100 / totalNonEmpty).coerceIn(0, 100)
    }

    private fun updateProgress() {
        val progress = calculateProgress()
        currentProgress = progress
        binding.progressContainer.post {
            val containerH = binding.progressContainer.height.toFloat()
            val clipH = (containerH * progress / 100f).toInt()
            // Lộ từ dưới lên
            binding.progressBar.clipBounds = Rect(0, (containerH - clipH).toInt(), binding.progressBar.width, containerH.toInt())
            // Thumb bám ranh giới fill: tính từ center của container
            val thumbH = binding.icThumb.height.toFloat()
            val fillBoundary = containerH - clipH  // top của vùng fill (tính từ top container)
            // Bottom của thumb bám vào top của fill: center = fillBoundary - thumbH/2
            val thumbCenter = (fillBoundary - thumbH / 4f).coerceIn(thumbH / 2f, containerH - thumbH / 2f)
            binding.icThumb.translationY = thumbCenter - containerH / 2f
            binding.tvThumbProgress.text = "$progress%"
            if (progress == 100) navigateToSuccess()
        }
    }

    // ── Countdown ────────────────────────────────────────────────────────────

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = lifecycleScope.launch {
            var secondsLeft = 600
            while (secondsLeft >= 0) {
                val m = secondsLeft / 60
                val s = secondsLeft % 60
                binding.tvCountDown.text = String.format("%02d:%02d", m, s)
                if (secondsLeft == 0) {
                    navigateToSuccess()
                    break
                }
                delay(1000)
                secondsLeft--
            }
        }
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    private fun navigateToSuccess() {
        countdownJob?.cancel()
        lifecycleScope.launch {
            showLoading()
          //  binding.layoutCustomLayer.background = null
            val savedPath = withContext(Dispatchers.IO) {
                try {
                    val bitmap = BitmapHelper.createBimapFromView(binding.layoutCustomLayer)
                    var path = ""
                    MediaHelper.saveBitmapToInternalStorageZip(
                        this@CosplayCustomizeActivity, ValueKey.DOWNLOAD_ALBUM_BACKGROUND, bitmap
                    ).collect { state ->
                        if (state is SaveState.Success) path = state.path
                    }
                    Log.d("CosplayCustomize", "savedPath=$path")
                    Log.d("CosplayCustomize", "file exists=${File(path).exists()} size=${File(path).length()}")
                    path
                } catch (e: Exception) {
                    Log.e("CosplayCustomize", "saveBitmap exception: ${e.message}")
                    ""
                }
            }
           // binding.layoutCustomLayer.setBackgroundResource(R.drawable.bg_4_solid_white_stroke_red)
            val currentSuggestion = viewModel.getSuggestionList().apply {
                pathInternalRandom = suggestionModel.pathInternalRandom
            }
            MediaHelper.writeModelToFile(this@CosplayCustomizeActivity, ValueKey.SUGGESTION_FILE_INTERNAL, currentSuggestion)
            dismissLoading()
            val intent = Intent(this@CosplayCustomizeActivity, CosplaySuccessfulActivity::class.java).apply {
                putExtra(IntentKey.INTENT_KEY, savedPath)
                putExtra(IntentKey.STATUS_KEY, currentProgress)
                putExtra(IntentKey.PATH_KEY, suggestionModel.pathInternalRandom)
            }
            val opt = ActivityOptions.makeCustomAnimation(
                this@CosplayCustomizeActivity, R.anim.slide_in_right, R.anim.slide_out_left
            )
           showInterAll {   startActivity(intent, opt.toBundle())
               finish()
           }
        }
    }

    private fun confirmExit() {

      handleBackLeftToRight()
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onDestroy() {
        super.onDestroy()
        countdownJob?.cancel()
        viewModel.setIsCreated(false)
    }

    @SuppressLint("GestureBackNavigation", "MissingSuperCall")
    override fun onBackPressed() {
        confirmExit()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            applyUiCustomize()
            hideNavigation(false)
            window.decorView.removeCallbacks(reHideRunnable)
            window.decorView.postDelayed(reHideRunnable, 1500)
        } else {
            window.decorView.removeCallbacks(reHideRunnable)
        }
    }

    private val reHideRunnable = Runnable {
        applyUiCustomize()
        hideNavigation(false)
    }

    @Suppress("DEPRECATION")
    private fun applyUiCustomize() {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    fun initNativeCollab() {
        Admob.getInstance().loadNativeCollapNotBanner(this,getString(R.string.native_cl_cosplayPlay), binding.flNativeCollab)
    }

    override fun initAds() {
        initNativeCollab()
    }

    override fun onRestart() {
        super.onRestart()
        initNativeCollab()
    }
}
