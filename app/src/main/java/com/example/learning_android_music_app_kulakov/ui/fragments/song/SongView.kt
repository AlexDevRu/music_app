package com.example.learning_android_music_app_kulakov.ui.fragments.song

import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

@AddToEndSingle
interface SongView: MvpView {
    fun setCurPlayerTimeToTextView(ms: Long)
    fun updateCurSongDuration(ms: Long)
    fun updateCurPlayerPosition(pos: Long)
}