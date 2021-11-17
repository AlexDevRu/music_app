package com.example.learning_android_music_app_kulakov.ui.fragments.song

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import com.example.learning_android_music_app_kulakov.databinding.FragmentSongBinding
import com.example.learning_android_music_app_kulakov.exoplayer.MusicServiceConnection
import com.example.learning_android_music_app_kulakov.models.Song
import com.example.learning_android_music_app_kulakov.ui.fragments.base.BaseFragment
import com.example.learning_android_music_app_kulakov.ui.fragments.main.MainPresenter
import com.example.learning_android_music_app_kulakov.ui.fragments.main.MainView
import moxy.ktx.moxyPresenter
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.*

class SongFragment: BaseFragment<FragmentSongBinding>(FragmentSongBinding::inflate), SongView, MainView {

    private var shouldUpdateSeekbar = true

    private val musicServiceConnection by inject<MusicServiceConnection>()
    private val presenter by moxyPresenter {
        MainPresenter(musicServiceConnection)
    }

    private val songPresenter by moxyPresenter {
        SongPresenter(musicServiceConnection)
    }

    private var curPlayingSong: Song? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivPlayPauseDetail.setOnClickListener {
            curPlayingSong?.let {
                presenter.playOrToggleSong(it, true)
            }
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser) {
                    setCurPlayerTimeToTextView(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                shouldUpdateSeekbar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    presenter.seekTo(it.progress.toLong())
                    shouldUpdateSeekbar = true
                }
            }
        })

        binding.ivSkipPrevious.setOnClickListener {
            presenter.skipToPrevious()
        }

        binding.ivSkip.setOnClickListener {
            presenter.skipToNext()
        }
    }

    private fun updateTitleAndSongImage(song: Song) {
        val title = "${song.title} - ${song.subtitle}"
        binding.tvSongName.text = title
    }

    override fun setCurPlayerTimeToTextView(ms: Long) {
        if(shouldUpdateSeekbar) {
            binding.seekBar.progress = ms.toInt()
            setCurPlayerTimeToTextView(ms)
        }
    }

    override fun updateCurSongDuration(ms: Long) {
        binding.seekBar.max = ms.toInt()
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        binding.tvSongDuration.text = dateFormat.format(ms)
    }

    override fun updateCurPlayerPosition(pos: Long) {
        if(shouldUpdateSeekbar) {
            binding.seekBar.progress = pos.toInt()
            setCurPlayerTimeToTextView(pos)
        }
    }

    override fun setLoadingState(isLoading: Boolean) {

    }

    override fun setData(data: List<Song>) {
        if(curPlayingSong == null && data.isNotEmpty()) {
            curPlayingSong = data[0]
            updateTitleAndSongImage(data[0])
        }
    }

    override fun showError(message: String) {

    }

    override fun requestReadExternalStorage() {

    }
}