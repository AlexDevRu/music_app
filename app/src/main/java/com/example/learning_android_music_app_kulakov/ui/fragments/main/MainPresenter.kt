package com.example.learning_android_music_app_kulakov.ui.fragments.main

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import com.example.learning_android_music_app_kulakov.exoplayer.MusicServiceConnection
import com.example.learning_android_music_app_kulakov.exoplayer.isPlayEnabled
import com.example.learning_android_music_app_kulakov.exoplayer.isPlaying
import com.example.learning_android_music_app_kulakov.exoplayer.isPrepared
import com.example.learning_android_music_app_kulakov.models.Song
import com.example.learning_android_music_app_kulakov.other.Constants.MEDIA_ROOT_ID
import moxy.InjectViewState
import moxy.MvpPresenter


@InjectViewState
class MainPresenter(
    private val musicServiceConnection: MusicServiceConnection
): MvpPresenter<MainView>() {

    private var musicList: List<Song>? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.requestReadExternalStorage()
    }

    fun getMusic() {
        if(musicList != null) {
            viewState.setData(musicList.orEmpty())
            return
        }

        connectToService()
    }

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val curPlayingSong = musicServiceConnection.curPlayingSong
    val playbackState = musicServiceConnection.playbackState

    fun connectToService() {
        viewState.setLoadingState(true)
        musicServiceConnection.connect()
        musicServiceConnection.subscribe(MEDIA_ROOT_ID, object: MediaBrowserCompat.SubscriptionCallback() {
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                super.onChildrenLoaded(parentId, children)
                val items = children.map {
                    Song(
                        it.mediaId!!,
                        it.description.title.toString(),
                        it.description.subtitle.toString(),
                        it.description.mediaUri.toString(),
                        it.description.iconUri.toString()
                    )
                }
                viewState.setLoadingState(false)
                viewState.setData(items)
            }
        })
    }

    fun skipToNext() {
        musicServiceConnection.transportControls.skipToNext()
    }

    fun skipToPrevious() {
        musicServiceConnection.transportControls.skipToPrevious()
    }

    fun seekTo(pos: Long) {
        musicServiceConnection.transportControls.seekTo(pos)
    }

    fun playOrToggleSong(mediaItem: Song, toggle: Boolean = false) {
        val isPrepared = playbackState.value?.isPrepared ?: false
        if(isPrepared && mediaItem.mediaId ==
            curPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)) {
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> if(toggle) musicServiceConnection.transportControls.pause()
                    playbackState.isPlayEnabled -> musicServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        } else {
            musicServiceConnection.transportControls.playFromMediaId(mediaItem.mediaId, null)
        }
    }

    fun onPermissionsResult(result: Boolean) {
        if(result) getMusic()
        else viewState.showError("difjkdj")
    }

    override fun onDestroy() {
        super.onDestroy()
        musicServiceConnection.unsubscribe(MEDIA_ROOT_ID, object: MediaBrowserCompat.SubscriptionCallback() {})
        musicServiceConnection.disconnect()
    }
}
