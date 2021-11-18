package com.example.learning_android_music_app_kulakov.di

import com.example.learning_android_music_app_kulakov.ui.fragments.main.MainVM
import com.example.learning_android_music_app_kulakov.ui.fragments.song.SongVM
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        MainVM(androidApplication(), get())
    }

    viewModel {
        SongVM(get())
    }
}