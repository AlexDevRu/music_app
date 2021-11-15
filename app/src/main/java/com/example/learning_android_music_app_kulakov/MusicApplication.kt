package com.example.learning_android_music_app_kulakov

import android.app.Application
import timber.log.Timber

class MusicApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}