package com.example.learning_android_music_app_kulakov.exoplayer.callbacks

import android.widget.Toast
import com.example.learning_android_music_app_kulakov.R
import com.example.learning_android_music_app_kulakov.exoplayer.MusicService
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player

class MusicPlayerEventListener(
    private val musicService: MusicService
) : Player.Listener {

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        if(playbackState == Player.STATE_READY) {
            musicService.stopForeground(false)
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(musicService, musicService.getString(R.string.unknown_error), Toast.LENGTH_LONG).show()
    }
}