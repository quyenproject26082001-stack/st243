package com.avatar.maker.celebrity.ui

import android.view.LayoutInflater
import com.avatar.maker.celebrity.R
import com.avatar.maker.celebrity.core.base.BaseActivity
import com.avatar.maker.celebrity.core.extensions.gone
import com.avatar.maker.celebrity.core.extensions.handleBackLeftToRight
import com.avatar.maker.celebrity.core.extensions.policy
import com.avatar.maker.celebrity.core.extensions.select
import com.avatar.maker.celebrity.core.extensions.setImageActionBar
import com.avatar.maker.celebrity.core.extensions.setTextActionBar
import com.avatar.maker.celebrity.core.extensions.shareApp
import com.avatar.maker.celebrity.core.extensions.startIntentRightToLeft
import com.avatar.maker.celebrity.core.extensions.visible
import com.avatar.maker.celebrity.core.utils.key.IntentKey
import com.avatar.maker.celebrity.core.utils.state.RateState
import com.avatar.maker.celebrity.databinding.ActivitySettingsBinding
import com.avatar.maker.celebrity.ui.language.LanguageActivity
import com.avatar.maker.celebrity.core.extensions.tap
import com.avatar.maker.celebrity.core.helper.MusicHelper
import com.avatar.maker.celebrity.core.helper.RateHelper
import kotlin.jvm.java

class SettingsActivity : BaseActivity<ActivitySettingsBinding>() {
    override fun setViewBinding(): ActivitySettingsBinding {
        return ActivitySettingsBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
       // binding.tvMusic.select()
        initRate()
        initMusic()
        binding.tvSetting.select()
    }

    private fun initMusic() {
        updateMusicUI(sharePreference.isMusicEnabled())
    }

    private fun updateMusicUI(isEnabled: Boolean) {
      //  binding.btnMusic.setImageResource(
//            if (isEnabled) R.drawable.ic_sw_on else R.drawable.ic_sw_off_ms
//        )
    }

    private fun toggleMusic() {
        val isEnabled = !sharePreference.isMusicEnabled()
        sharePreference.setMusicEnabled(isEnabled)
        updateMusicUI(isEnabled)
        if (isEnabled) {
            MusicHelper.play()
        } else {
            MusicHelper.pause()
        }
    }

    override fun viewListener() {
        binding.apply {
           btnActionBarLeft.tap { handleBackLeftToRight() }
        //    layoutMusic.tap { toggleMusic() }
            btnLang.tap { startIntentRightToLeft(LanguageActivity::class.java, IntentKey.INTENT_KEY) }
            btnShareApp.tap(1500) { shareApp() }
            btnRate.tap {
                RateHelper.showRateDialog(this@SettingsActivity, sharePreference){ state ->
                    if (state != RateState.CANCEL){
                        btnRate.gone()
                        showToast(R.string.have_rated)
                    }
                }
            }
            btnPolicy.tap(1500) { policy() }
        }
    }

    override fun initText() {
        //binding.actionBar.tvCenter.select()
    }

    override fun initActionBar() {
//        binding.actionBar.apply {
//            setImageActionBar(btnActionBarLeft, R.drawable.ic_back)
//            setTextActionBar(tvCenter, getString(R.string.settings))
//        }
    }

    private fun initRate() {
        if (sharePreference.getIsRate(this)) {
            binding.btnRate.gone()
        } else {
            binding.btnRate.visible()
        }
    }
}