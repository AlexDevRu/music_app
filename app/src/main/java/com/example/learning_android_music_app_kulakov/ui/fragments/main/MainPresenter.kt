package com.example.learning_android_music_app_kulakov.ui.fragments.main

import android.content.Context
import android.provider.MediaStore
import com.example.learning_android_music_app_kulakov.R
import kotlinx.coroutines.*
import moxy.InjectViewState
import moxy.MvpPresenter
import moxy.presenterScope
import timber.log.Timber
import java.util.*

data class MusicItem(
    val id: String,
    val name: String,
    val artistName: String,
    val duration: Long
)

@InjectViewState
class MainPresenter(
    private val context: Context
): MvpPresenter<MainView>() {

    private var job: Job? = null

    private var musicList: List<MusicItem>? = null

    init {
        viewState.requestReadExternalStorage()
    }

    fun getMusic() {
        if(musicList != null) {
            viewState.setData(musicList.orEmpty())
            return
        }

        refreshMusic()
    }

    fun refreshMusic() {
        job?.cancel()
        job = presenterScope.launch {
            viewState.setLoadingState(true)

            val list = mutableListOf<MusicItem>()

            try {
                withContext(Dispatchers.IO) {
                    val c = context.contentResolver.query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        arrayOf(
                            MediaStore.Audio.Media._ID,
                            MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media.ARTIST,
                            MediaStore.Audio.Media.DURATION,
                        ),
                        null, null, null
                    )

                    while(c?.moveToNext() == true) {
                        val idIndex = c.getColumnIndex(MediaStore.Audio.Media._ID)
                        val nameIndex = c.getColumnIndex(MediaStore.Audio.Media.TITLE)
                        val artistIndex = c.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                        val durationIndex = c.getColumnIndex(MediaStore.Audio.Media.DURATION)

                        list.add(
                            MusicItem(
                                id = c.getString(idIndex),
                                name = c.getString(nameIndex),
                                artistName = c.getString(artistIndex),
                                duration = c.getLong(durationIndex)
                            )
                        )
                    }

                    c?.close()
                }

                musicList = list
                viewState.setData(musicList.orEmpty())
            } catch(e: Exception) {
                viewState.showError(e.message ?: context.getString(R.string.unknown_error))
            }

            viewState.setLoadingState(false)
        }
    }

    fun onPermissionsResult(result: Boolean) {
        if(result) getMusic()
    }
}