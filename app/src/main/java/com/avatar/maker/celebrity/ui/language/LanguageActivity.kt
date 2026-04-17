package com.avatar.maker.celebrity.ui.language

import android.annotation.SuppressLint
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lvt.ads.util.Admob
import com.avatar.maker.celebrity.R
import com.avatar.maker.celebrity.core.base.BaseActivity
import com.avatar.maker.celebrity.core.extensions.gone
import com.avatar.maker.celebrity.core.extensions.handleBackLeftToRight
import com.avatar.maker.celebrity.core.extensions.invisible
import com.avatar.maker.celebrity.core.extensions.select
import com.avatar.maker.celebrity.core.extensions.startIntentRightToLeft
import com.avatar.maker.celebrity.core.extensions.startIntentWithClearTop
import com.avatar.maker.celebrity.core.extensions.visible
import com.avatar.maker.celebrity.core.utils.key.IntentKey
import com.avatar.maker.celebrity.databinding.ActivityLanguageBinding
import com.avatar.maker.celebrity.ui.home.HomeActivity
import com.avatar.maker.celebrity.ui.intro.IntroActivity
import com.avatar.maker.celebrity.core.extensions.tap
import com.avatar.maker.celebrity.core.extensions.strings
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class LanguageActivity : BaseActivity<ActivityLanguageBinding>() {
    private val viewModel: LanguageViewModel by viewModels()

    private val languageAdapter by lazy { LanguageAdapter(this) }

    override fun setViewBinding(): ActivityLanguageBinding {
        return ActivityLanguageBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initRcv()
        val intentValue = intent.getStringExtra(IntentKey.INTENT_KEY)
        val currentLang = sharePreference.getPreLanguage()
        viewModel.setFirstLanguage(intentValue == null)
        viewModel.loadLanguages(currentLang)
        binding.tvLang.select()
        binding.tvLangSetting.select()
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isFirstLanguage.collect { isFirst ->
                        languageAdapter.isFirstLanguage = isFirst
                        if (isFirst) {
                            binding.btnDone.invisible()
                            binding.tvLang.visible()
                            binding.tvLangSetting.invisible()
                        } else {
                            binding.btnDone.invisible()
                            binding.btnBackLangSetting.visible()
                            binding.btnDoneLangSetting.visible()
                            binding.tvLang.invisible()
                            binding.tvLangSetting.visible()
                        }
                    }
                }
                launch {
                    viewModel.languageList.collect { list ->
                        languageAdapter.submitList(list)
                    }
                }
                launch {
                    viewModel.codeLang.collect { code ->
                        if (code.isNotEmpty() && viewModel.isFirstLanguage.value) {
                            binding.btnDone.visible()
                        }
                    }
                }
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            btnBackLangSetting.tap { handleBackLeftToRight() }
            btnDone.tap { handleDone() }
            btnDoneLangSetting.tap { handleDone() }
        }
        handleRcv()
    }

    override fun initText() {
        // binding.actionBar.tvCenter.select()
        //binding.actionBar.tvStart.select()
    }

    override fun initActionBar() {
//        binding.actionBar.apply {
//            btnActionBarLeft.setImageResource(R.drawable.ic_back)
//            btnDone.setImageResource(R.drawable.ic_done)
//            val text = R.string.language
//            tvCenter.text = strings(text)
//            tvStart.text = strings(text)
//        }
    }

    private fun initRcv() {
        binding.rcv.apply {
            adapter = languageAdapter
            itemAnimator = null
        }
    }

    private fun handleRcv() {
        binding.apply {
            languageAdapter.onItemClick = { code ->
                //  binding.actionBar.btnDone.visible()
                viewModel.selectLanguage(code)
            }
        }
    }

    private fun handleDone() {
        val code = viewModel.codeLang.value
        if (code.isEmpty()) {
            showToast(R.string.not_select_lang)
            return
        }
        sharePreference.setPreLanguage(code)

        if (viewModel.isFirstLanguage.value) {
            sharePreference.setIsFirstLang(false)
            startIntentRightToLeft(IntroActivity::class.java)
            finishAffinity()
        } else {
            startIntentWithClearTop(HomeActivity::class.java)
        }
    }

    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() {
        if (!viewModel.isFirstLanguage.value) {
            handleBackLeftToRight()
        } else {
            exitProcess(0)
        }
    }

    // Chỉ tắt nhạc khi là màn language đầu tiên
    override fun shouldPlayBackgroundMusic(): Boolean = false

    override fun initAds() {
        Admob.getInstance().loadNativeAd(this@LanguageActivity, getString(R.string.native_language), binding.nativeAds, R.layout.ads_native_big_btn_top)
    }

}