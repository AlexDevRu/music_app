package com.example.learning_android_music_app_kulakov

import android.app.Application
import com.example.learning_android_music_app_kulakov.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class MusicApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        startKoin {
            androidContext(this@MusicApplication)
            modules(appModule)
        }
    }
}