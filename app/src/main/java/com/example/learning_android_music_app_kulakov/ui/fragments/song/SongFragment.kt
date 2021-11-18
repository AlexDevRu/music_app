package com.example.learning_android_music_app_kulakov.ui.fragments.song

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import com.bumptech.glide.RequestManager
import com.example.learning_android_music_app_kulakov.R
import com.example.learning_android_music_app_kulakov.databinding.FragmentSongBinding
import com.example.learning_android_music_app_kulakov.exoplayer.isPlaying
import com.example.learning_android_music_app_kulakov.exoplayer.toSong
import com.example.learning_android_music_app_kulakov.models.Song
import com.example.learning_android_music_app_kulakov.other.Status
import com.example.learning_android_music_app_kulakov.ui.fragments.base.BaseFragment
import com.example.learning_android_music_app_kulakov.ui.fragments.main.MainVM
import com.google.android.material.slider.Slider
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

class SongFragment: BaseFragment<FragmentSongBinding>(FragmentSongBinding::inflate) {

    private val glide: RequestManager by inject()

    private var shouldUpdateSeekbar = true

    private var curPlayingSong: Song? = null

    private var playbackState: PlaybackStateCompat? = null

    private val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())

    private val songViewModel by viewModel<SongVM>()
    private val mainViewModel by sharedViewModel<MainVM>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeToObservers()

        binding.ivPlayPauseDetail.setOnClickListener {
            curPlayingSong?.let {
                mainViewModel.playOrToggleSong(it, true)
            }
        }

        binding.seekBar.addOnChangeListener { _, value, fromUser ->
            if(fromUser) {
                setCurPlayerTimeToTextView(value.toLong())
            }
        }
        binding.seekBar.addOnSliderTouchListener(object: Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                shouldUpdateSeekbar = false
            }

            override fun onStopTrackingTouch(slider: Slider) {
                mainViewModel.seekTo(slider.value.toLong())
                shouldUpdateSeekbar = true
            }
        })

        binding.seekBar.setLabelFormatter {
            dateFormat.format(it.toLong())
        }

        binding.ivSkipPrevious.setOnClickListener {
            mainViewModel.skipToPreviousSong()
        }

        binding.ivSkip.setOnClickListener {
            mainViewModel.skipToNextSong()
        }
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) {
            it?.let { result ->
                when(result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { songs ->
                            if(curPlayingSong == null && songs.isNotEmpty()) {
                                curPlayingSong = songs[0]
                                updateTitleAndSongImage(songs[0])
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }
        mainViewModel.curPlayingSong.observe(viewLifecycleOwner) {
            if(it == null) return@observe
            curPlayingSong = it.toSong()
            updateTitleAndSongImage(curPlayingSong!!)
        }
        mainViewModel.playbackState.observe(viewLifecycleOwner) {
            playbackState = it
            binding.ivPlayPauseDetail.setImageResource(
                if(playbackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
            )
            binding.seekBar.value = it?.position?.toFloat() ?: 0f
        }
        songViewModel.curPlayerPosition.observe(viewLifecycleOwner) {
            if(shouldUpdateSeekbar) {
                binding.seekBar.value = it.toFloat()
                setCurPlayerTimeToTextView(it)
            }
        }
        songViewModel.curSongDuration.observe(viewLifecycleOwner) {
            if(it > 0) {
                binding.seekBar.valueTo = it.toFloat()
                binding.tvSongDuration.text = dateFormat.format(it)
            }
        }
    }

    private fun setCurPlayerTimeToTextView(ms: Long) {
        binding.tvCurTime.text = dateFormat.format(ms)
    }

    private fun updateTitleAndSongImage(song: Song) {
        val title = "${song.title} - ${song.subtitle}"
        binding.tvSongName.text = title
        glide.load(song.imageUrl).into(binding.ivSongImage)
    }
}