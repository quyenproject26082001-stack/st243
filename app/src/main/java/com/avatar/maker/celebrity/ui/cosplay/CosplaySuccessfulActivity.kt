package com.avatar.maker.celebrity.ui.cosplay

import android.annotation.SuppressLint
import androidx.constraintlayout.widget.ConstraintSet
import android.content.pm.PackageManager
import android.os.Build
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.avatar.maker.celebrity.R
import com.avatar.maker.celebrity.core.base.BaseActivity
import com.avatar.maker.celebrity.core.extensions.checkPermissions
import com.avatar.maker.celebrity.core.extensions.goToSettings
import com.avatar.maker.celebrity.core.extensions.gone
import com.avatar.maker.celebrity.core.extensions.handleBackLeftToRight
import com.avatar.maker.celebrity.core.extensions.loadImage
import com.avatar.maker.celebrity.core.extensions.loadNativeCollabAds
import com.avatar.maker.celebrity.core.extensions.requestPermission
import com.avatar.maker.celebrity.core.extensions.showInterAll
import com.avatar.maker.celebrity.core.extensions.startIntentWithClearTop
import com.avatar.maker.celebrity.core.extensions.tap
import com.avatar.maker.celebrity.core.extensions.visible
import com.avatar.maker.celebrity.core.utils.key.IntentKey
import com.avatar.maker.celebrity.core.utils.key.RequestKey
import com.avatar.maker.celebrity.core.utils.state.HandleState
import com.avatar.maker.celebrity.databinding.ActivityCosplaySuccessfulBinding
import com.avatar.maker.celebrity.ui.home.HomeActivity
import com.avatar.maker.celebrity.ui.permission.PermissionViewModel
import com.avatar.maker.celebrity.ui.success.SuccessViewModel
import com.lvt.ads.util.Admob
import kotlinx.coroutines.launch

class CosplaySuccessfulActivity : BaseActivity<ActivityCosplaySuccessfulBinding>() {

    private val viewModel: SuccessViewModel by viewModels()
    private val permissionViewModel: PermissionViewModel by viewModels()

    private var finalProgress = 0
    private var targetImagePath = ""

    override fun setViewBinding(): ActivityCosplaySuccessfulBinding {
        return ActivityCosplaySuccessfulBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        val path = intent.getStringExtra(IntentKey.INTENT_KEY) ?: ""
        finalProgress = intent.getIntExtra(IntentKey.STATUS_KEY, 0)
        targetImagePath = intent.getStringExtra(IntentKey.PATH_KEY) ?: ""
        viewModel.setPath(path)
        updateProgressDisplay()
        if (targetImagePath.isNotEmpty()) {
            loadImage(this, targetImagePath, binding.btnSamplePhoto)
        }
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pathInternal.collect { path ->
                    if (path.isNotEmpty()) {
                        loadImage(this@CosplaySuccessfulActivity, path, binding.imvImage)
                    }
                }
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.btnActionBarRight.tap {
                showInterAll {
                    startIntentWithClearTop(HomeActivity::class.java)
                }

            }
            actionBar.btnActionBarLeft.tap{
                handleBackLeftToRight()
            }
            includeLayoutBottom.btnWhatsapp.tap(800) {
                showInterAll {
                    startIntentWithClearTop(CosplayRandomActivity::class.java)
                }
            }



            includeLayoutBottom.btnTelegram.tap(2000) {
                viewModel.saveToAvatar(this@CosplaySuccessfulActivity) {
                    showToast(R.string.image_has_been_saved_successfully)
                }
            }
        }
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            btnActionBarRight.visible()
            btnActionBarRight.setImageResource(R.drawable.ic_home)
            tvCenter.visible()
            tvCenter.setText(R.string.cosplay)
        }
    }

    private fun updateProgressDisplay() {
        binding.progressBar.pivotX = 0f
        binding.progressBar.scaleX = finalProgress / 100f
        binding.progressContainer.post {
            if (finalProgress == 100) {
                binding.icThumb.translationX = 0f
                val cs = ConstraintSet()
                cs.clone(binding.main)
                cs.connect(R.id.ic_thumb, ConstraintSet.START, R.id.progressContainer, ConstraintSet.END)
                cs.connect(R.id.ic_thumb, ConstraintSet.END, R.id.progressContainer, ConstraintSet.END)
                cs.applyTo(binding.main)
            } else {
                val containerW = binding.progressContainer.width
                val thumbW = binding.icThumb.width
                val maxX = (containerW - thumbW).toFloat().coerceAtLeast(0f)
                val offsetPx = 5f * resources.displayMetrics.density
                binding.icThumb.translationX = (finalProgress / 100f) * maxX - offsetPx
            }
            binding.tvProgress.text = "$finalProgress/100"
        }
        val starRes = when {
            finalProgress < 20  -> R.drawable.one_star_ss
            finalProgress < 40  -> R.drawable.two_star_ss
            finalProgress < 60  -> R.drawable.three_star_ss
            finalProgress < 80  -> R.drawable.four_star_ss
            else                -> R.drawable.five_star_ss
        }
        binding.starSS.setImageResource(starRes)
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            handleDownload()
        } else {
            val perms = permissionViewModel.getStoragePermissions()
            if (checkPermissions(perms)) {
                handleDownload()
            } else if (permissionViewModel.needGoToSettings(sharePreference, true)) {
                goToSettings()
            } else {
                requestPermission(perms, RequestKey.STORAGE_PERMISSION_CODE)
            }
        }
    }

    private fun handleDownload() {
        lifecycleScope.launch {
            viewModel.downloadFiles(this@CosplaySuccessfulActivity).collect { state ->
                when (state) {
                    HandleState.LOADING -> showLoading()
                    HandleState.SUCCESS -> {
                        dismissLoading()
                        showToast(R.string.download_success)
                    }
                    else -> {
                        dismissLoading()
                        showToast(R.string.download_failed_please_try_again_later)
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RequestKey.STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                permissionViewModel.updateStorageGranted(sharePreference, true)
                handleDownload()
            } else {
                permissionViewModel.updateStorageGranted(sharePreference, false)
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        handleBackLeftToRight()
    }


    fun initNativeCollab() {
        loadNativeCollabAds(R.string.native_cl_cosplayDone, binding.flNativeCollab)    }

    override fun initAds() {
        initNativeCollab()
    }

    override fun onRestart() {
        super.onRestart()
    }



}
