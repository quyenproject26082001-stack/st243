package com.avatar.maker.celebrity.ui.success

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lvt.ads.util.Admob
import com.avatar.maker.celebrity.R
import com.avatar.maker.celebrity.core.base.BaseActivity
import com.avatar.maker.celebrity.core.extensions.checkPermissions
import com.avatar.maker.celebrity.core.extensions.goToSettings
import com.avatar.maker.celebrity.core.extensions.gone
import com.avatar.maker.celebrity.core.extensions.handleBackLeftToRight
import com.avatar.maker.celebrity.core.extensions.invisible
import com.avatar.maker.celebrity.core.extensions.loadImage
import com.avatar.maker.celebrity.core.extensions.loadNativeCollabAds
import com.avatar.maker.celebrity.core.extensions.requestPermission
import com.avatar.maker.celebrity.core.extensions.select
import com.avatar.maker.celebrity.core.extensions.setImageActionBar
import com.avatar.maker.celebrity.core.extensions.setTextActionBar
import com.avatar.maker.celebrity.core.extensions.showInterAll
import com.avatar.maker.celebrity.core.extensions.startIntentRightToLeft
import com.avatar.maker.celebrity.core.extensions.startIntentWithClearTop
import com.avatar.maker.celebrity.core.extensions.strings
import com.avatar.maker.celebrity.core.extensions.tap
import com.avatar.maker.celebrity.core.extensions.visible
import com.avatar.maker.celebrity.core.helper.UnitHelper
import com.avatar.maker.celebrity.core.utils.key.IntentKey
import com.avatar.maker.celebrity.core.utils.key.RequestKey
import com.avatar.maker.celebrity.core.utils.key.ValueKey
import com.avatar.maker.celebrity.core.utils.state.HandleState
import com.avatar.maker.celebrity.databinding.ActivitySuccessBinding
import com.avatar.maker.celebrity.ui.home.HomeActivity
import com.avatar.maker.celebrity.ui.my_creation.MyCreationActivity
import com.avatar.maker.celebrity.ui.permission.PermissionViewModel
import kotlinx.coroutines.launch

class SuccessActivity : BaseActivity<ActivitySuccessBinding>() {
    private val viewModel: SuccessViewModel by viewModels()
    private val permissionViewModel: PermissionViewModel by viewModels()

    override fun setViewBinding(): ActivitySuccessBinding {
        return ActivitySuccessBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        viewModel.setPath(intent.getStringExtra(IntentKey.INTENT_KEY) ?: "")
        setButtonBackgrounds()
    }

    private fun setButtonBackgrounds() {
        binding.includeLayoutBottom.apply {
            
            tvDownload.select()
            tvShare.select()

        }
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.pathInternal.collect { path ->
                        if (path.isNotEmpty()) {
                            loadImage(this@SuccessActivity, path, binding.imvImage)
                        }
                    }
                }
            }
        }
    }

    private fun handleBack() {
        handleBackLeftToRight()
    }
    override fun viewListener() {
        binding.apply {
            actionBar.apply {
                btnActionBarNextRight.tap {

                        viewModel.shareFiles(this@SuccessActivity)

                }
                btnActionBarLeft.tap {  handleBack()  }

                btnActionBarRight.tap(2000){
                    showInterAll {  startIntentWithClearTop(HomeActivity::class.java)}

                }
            }

            // My Album button
            includeLayoutBottom.btnWhatsapp.tap(2590) {
                showInterAll {
                    startIntentRightToLeft(MyCreationActivity::class.java, IntentKey.TAB_KEY, ValueKey.MY_DESIGN_TYPE)
                }
            }

            // Download button
            includeLayoutBottom.btnTelegram.tap(2000) {
                checkStoragePermission()
            }

        }
    }

    override fun initActionBar() {
        binding.actionBar.apply {

            btnActionBarLeft.visible()
            btnActionBarRight.visible()
            btnActionBarRight.setImageResource(R.drawable.ic_home)
            tvCenter.visible()
            tvCenter.setText(R.string.successfully)
            imgCenter.gone()
                setImageActionBar(btnActionBarNextRight, R.drawable.ic_share)
            btnActionBarNextRight.visible()


        }
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
            viewModel.downloadFiles(this@SuccessActivity).collect { state ->
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
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

    override fun initAds() {
        initNativeCollab()
    }

    fun initNativeCollab() {

        Admob.getInstance().loadNativeAd(this@SuccessActivity, getString(R.string.native_success), binding.nativeAds, R.layout.ads_native_big_btn_top)


    }

    @android.annotation.SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        handleBackLeftToRight()
    }
}
