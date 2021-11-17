package com.example.learning_android_music_app_kulakov.di

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.learning_android_music_app_kulakov.R
import com.example.learning_android_music_app_kulakov.exoplayer.MusicServiceConnection
import com.example.learning_android_music_app_kulakov.exoplayer.MusicSource
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.upstream.DefaultDataSource
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module


val appModule = module {
    single {
        AudioAttributes.Builder().setContentType(C.CONTENT_TYPE_MUSIC).setUsage(C.USAGE_MEDIA).build()
    }

    single {
        ExoPlayer.Builder(androidContext()).build().apply {
            setAudioAttributes(get(), true)
            setHandleAudioBecomingNoisy(true)
        }
    }

    single {
        DefaultDataSource.Factory(androidContext())
    }

    single {
        MusicServiceConnection(androidContext())
    }

    single {
        MusicSource(androidContext())
    }

    single {
        Glide.with(androidContext()).setDefaultRequestOptions(
            RequestOptions()
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_image)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
        )
    }
}