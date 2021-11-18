package com.example.learning_android_music_app_kulakov.ui.fragments.main

import android.app.Application
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.learning_android_music_app_kulakov.R
import com.example.learning_android_music_app_kulakov.exoplayer.MusicServiceConnection
import com.example.learning_android_music_app_kulakov.exoplayer.isPlayEnabled
import com.example.learning_android_music_app_kulakov.exoplayer.isPlaying
import com.example.learning_android_music_app_kulakov.exoplayer.isPrepared
import com.example.learning_android_music_app_kulakov.models.Song
import com.example.learning_android_music_app_kulakov.other.Constants.MEDIA_ROOT_ID
import com.example.learning_android_music_app_kulakov.other.Resource
import timber.log.Timber

class MainVM(
    private val app: Application,
    private val musicServiceConnection: MusicServiceConnection
): ViewModel() {

    private val _mediaItems = MutableLiveData<Resource<List<Song>>>()
    val mediaItems: LiveData<Resource<List<Song>>> = _mediaItems

    val networkError = musicServiceConnection.networkError
    val curPlayingSong = musicServiceConnection.curPlayingSong
    val playbackState = musicServiceConnection.playbackState

    fun connectToService() {
        _mediaItems.value = Resource.loading(null)
        musicServiceConnection.connect()
        musicServiceConnection.subscribe(MEDIA_ROOT_ID, object: MediaBrowserCompat.SubscriptionCallback() {
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                super.onChildrenLoaded(parentId, children)
                val items = children.map {
                    Timber.e("metadata ${it.description.extras}")

                    Song(
                        it.mediaId!!,
                        it.description.title.toString(),
                        it.description.subtitle.toString(),
                        it.description.mediaUri.toString(),
                        it.description.iconUri.toString(),
                    )
                }
                _mediaItems.value = Resource.success(items)
            }
        })
    }

    fun skipToNextSong() {
        if(curPlayingSong.value?.description?.mediaId == mediaItems.value?.data?.last()?.mediaId)
            moveToFirst()
        else
            musicServiceConnection.transportControls.skipToNext()
    }

    fun skipToPreviousSong() {
        if(curPlayingSong.value?.description?.mediaId == mediaItems.value?.data?.first()?.mediaId)
            moveToLast()
        else
            musicServiceConnection.transportControls.skipToPrevious()
    }

    fun moveToFirst() {
        if(!mediaItems.value?.data.isNullOrEmpty())
            musicServiceConnection.transportControls.playFromMediaId(mediaItems.value!!.data!!.first().mediaId, null)
    }

    fun moveToLast() {
        if(!mediaItems.value?.data.isNullOrEmpty())
            musicServiceConnection.transportControls.playFromMediaId(mediaItems.value!!.data!!.last().mediaId, null)
    }

    fun seekTo(pos: Long) {
        musicServiceConnection.transportControls.seekTo(pos)
    }

    fun playOrToggleSong(mediaItem: Song, toggle: Boolean = false) {
        val isPrepared = playbackState.value?.isPrepared ?: false
        if(isPrepared && mediaItem.mediaId ==
            curPlayingSong.value?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)) {
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
        if(result) connectToService()
        else _mediaItems.value = Resource.error(app.getString(R.string.permission_denied))
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe(MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback() {})
    }

}