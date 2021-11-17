package com.example.learning_android_music_app_kulakov.exoplayer

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.example.learning_android_music_app_kulakov.exoplayer.State.*
import com.example.learning_android_music_app_kulakov.models.Song
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber


class MusicSource(
    private val context: Context
){

    var songs = emptyList<MediaMetadataCompat>()

    suspend fun fetchMediaData() = withContext(Dispatchers.IO) {
        state = STATE_INITIALIZING

        val allSongs = withContext(Dispatchers.IO) {
            val list = mutableListOf<Song>()

            val c = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Albums.ALBUM_ID,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.ARTIST
                ),
                null, null, null
            )

            while(c?.moveToNext() == true) {
                val idIndex = c.getColumnIndex(MediaStore.Audio.Media._ID)
                val nameIndex = c.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val artistIndex = c.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val durationIndex = c.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val albumIdIndex = c.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ID)

                val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
                val imageUri = ContentUris.withAppendedId(sArtworkUri, c.getLong(albumIdIndex))

                val uriImage = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, c.getString(idIndex))

                Timber.d("duration ${c.getLong(durationIndex)}")

                list.add(
                    Song(
                        mediaId = c.getString(idIndex),
                        title = c.getString(nameIndex),
                        subtitle = c.getString(artistIndex),
                        songUrl = uriImage.toString(),
                        imageUrl = imageUri.toString(),
                        duration = c.getLong(durationIndex)
                    )
                )
            }

            c?.close()

            list
        }

        songs = allSongs.map { song ->
            Builder()
                .putString(METADATA_KEY_ARTIST, song.subtitle)
                .putString(METADATA_KEY_MEDIA_ID, song.mediaId)
                .putString(METADATA_KEY_TITLE, song.title)
                .putString(METADATA_KEY_DISPLAY_TITLE, song.title)
                .putString(METADATA_KEY_DISPLAY_ICON_URI, song.imageUrl)
                .putString(METADATA_KEY_MEDIA_URI, song.songUrl)
                .putString(METADATA_KEY_ALBUM_ART_URI, song.imageUrl)
                .putString(METADATA_KEY_DISPLAY_SUBTITLE, song.subtitle)
                .putString(METADATA_KEY_DISPLAY_DESCRIPTION, song.subtitle)
                .build()
        }

        state = STATE_INITIALIZED
    }

    fun asMediaSource(dataSourceFactory: DefaultDataSource.Factory): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        songs.forEach { song ->
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(song.getString(METADATA_KEY_MEDIA_URI)))
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

    fun asMediaItems() = songs.map { song ->
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(song.description.title)
            .setSubtitle(song.description.subtitle)
            .setMediaId(song.description.mediaId)
            .setIconUri(song.description.iconUri)
            .build()
        MediaBrowserCompat.MediaItem(desc, FLAG_PLAYABLE)
    }.toMutableList()

    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    private var state: State = STATE_CREATED
        set(value) {
            if(value == STATE_INITIALIZED || value == STATE_ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(state == STATE_INITIALIZED)
                    }
                }
            } else {
                field = value
            }
        }

    fun whenReady(action: (Boolean) -> Unit): Boolean {
        if(state == STATE_CREATED || state == STATE_INITIALIZING) {
            onReadyListeners += action
            return false
        } else {
            action(state == STATE_INITIALIZED)
            return true
        }
    }
}

enum class State {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}















