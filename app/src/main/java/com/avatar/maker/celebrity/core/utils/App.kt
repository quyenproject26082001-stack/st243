package com.avatar.maker.celebrity.core.utils

import android.content.Context
import com.lvt.ads.util.AdsApplication
import com.lvt.ads.util.AppOpenManager
import com.avatar.maker.celebrity.R
import com.avatar.maker.celebrity.ui.splash.SplashActivity
import kotlin.jvm.java

class App : AdsApplication() {


    override fun onCreate() {
        super.onCreate()
        AppOpenManager.getInstance().disableAppResumeWithActivity(SplashActivity::class.java)
    }

    override fun enableAdsResume(): Boolean {
        return false
    }

    override fun getListTestDeviceId(): MutableList<String>? {
        return null
    }

    override fun getResumeAdId(): String {
        return getString(R.string.open_resume)
    }

    override fun buildDebug(): Boolean {
        return true
    }

}