package com.example.learning_android_music_app_kulakov.exoplayer

import android.support.v4.media.MediaMetadataCompat
import com.example.learning_android_music_app_kulakov.models.Song

fun MediaMetadataCompat.toSong(): Song? {
    return description?.let {
        Song(
            it.mediaId ?: "",
            it.title.toString(),
            it.subtitle.toString(),
            it.mediaUri.toString(),
            it.iconUri.toString()
        )
    }
}