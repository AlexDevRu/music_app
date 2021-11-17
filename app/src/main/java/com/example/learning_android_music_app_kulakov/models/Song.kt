package com.example.learning_android_music_app_kulakov.models

data class Song(
    val mediaId: String = "",
    val title: String = "",
    val subtitle: String = "",
    val songUrl: String = "",
    val imageUrl: String = "",
    val duration: Long = 0L
)