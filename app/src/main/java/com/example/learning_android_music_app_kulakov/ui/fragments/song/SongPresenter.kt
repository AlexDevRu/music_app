package com.example.learning_android_music_app_kulakov.ui.fragments.song

import com.example.learning_android_music_app_kulakov.exoplayer.MusicService
import com.example.learning_android_music_app_kulakov.exoplayer.MusicServiceConnection
import com.example.learning_android_music_app_kulakov.exoplayer.currentPlaybackPosition
import com.example.learning_android_music_app_kulakov.other.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moxy.InjectViewState
import moxy.MvpPresenter
import moxy.presenterScope

@InjectViewState
class SongPresenter(
    musicServiceConnection: MusicServiceConnection
): MvpPresenter<SongView>() {

    private val playbackState = musicServiceConnection.playbackState

    private var curSongDuration = 0L
    private var curPlayerPosition = 0L

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        updateCurrentPlayerPosition()
    }

    private fun updateCurrentPlayerPosition() {
        presenterScope.launch {
            while(true) {
                val pos = playbackState.value?.currentPlaybackPosition
                if(curPlayerPosition != pos) {
                    curPlayerPosition = pos ?: 0L
                    curSongDuration = MusicService.curSongDuration

                    viewState.setCurPlayerTimeToTextView(MusicService.curSongDuration)
                }
                delay(Constants.UPDATE_PLAYER_POSITION_INTERVAL)
            }
        }
    }

}