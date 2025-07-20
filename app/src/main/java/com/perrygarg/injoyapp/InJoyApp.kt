package com.perrygarg.injoyapp

import android.app.Application
import android.util.Log
import com.perrygarg.injoyapp.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import com.perrygarg.injoyapp.BuildConfig

class InJoyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("InJoyApp", "TMDB_BEARER_TOKEN: ${BuildConfig.TMDB_BEARER_TOKEN}")
        startKoin {
            androidContext(this@InJoyApp)
            modules(appModule)
        }
    }
} 