package com.example.learning_android_music_app_kulakov.ui.fragments.main

import com.example.learning_android_music_app_kulakov.models.Song
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEnd
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution
import moxy.viewstate.strategy.alias.Skip

@AddToEndSingle
interface MainView: MvpView {
    fun setLoadingState(isLoading: Boolean)
    fun setData(data: List<Song>)
    fun showError(message: String)
    fun requestReadExternalStorage()
}