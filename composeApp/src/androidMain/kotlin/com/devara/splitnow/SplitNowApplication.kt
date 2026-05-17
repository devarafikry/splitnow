package com.devara.splitnow

import android.app.Application
import com.devara.splitnow.data.AndroidContextHolder
import com.devara.splitnow.di.initKoin
import org.koin.android.ext.koin.androidContext

class SplitNowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidContextHolder.context = this
        initKoin { androidContext(this@SplitNowApplication) }
    }
}
