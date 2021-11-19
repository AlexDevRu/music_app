package com.example.learning_android_music_app_kulakov.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.learning_android_music_app_kulakov.R
import com.example.learning_android_music_app_kulakov.databinding.ViewholderMusicItemBinding
import com.example.learning_android_music_app_kulakov.models.Song
import java.text.SimpleDateFormat
import java.util.*


class MusicAdapter(
    private val onItemClickListener: (Song) -> Unit,
    private val onPlayClickListener: (Song) -> Unit,
): ListAdapter<Song, MusicAdapter.SongViewHolder>(DIFF_UTIL) {

    companion object {
        private val DIFF_UTIL = object: DiffUtil.ItemCallback<Song>() {
            override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
                return oldItem.mediaId == newItem.mediaId
            }

            override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
                return oldItem == newItem
            }
        }
    }

    private val timeFormatter = SimpleDateFormat("mm:ss", Locale.getDefault())

    private var pendingProgress: Long? = null
    private var currentMedia: SongViewHolder? = null
        set(value) {
            field = value
            if(pendingProgress != null) field?.updateCurrentMediaProgress(pendingProgress!!)
        }


    inner class SongViewHolder(
        private val binding: ViewholderMusicItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Song) {
            binding.name.text = song.title
            binding.artistName.text = song.subtitle

            binding.displayDuration.text = timeFormatter.format(song.duration)
            binding.durationProgress.max = song.duration.toInt()

            Glide.with(binding.root)
                .load(song.imageUrl)
                .placeholder(R.drawable.ic_baseline_music_note_24)
                .error(R.drawable.ic_baseline_music_note_24)
                .into(binding.image)

            binding.playButton.setOnClickListener {
                onPlayClickListener(song)
            }

            binding.root.setOnClickListener {
                onItemClickListener(song)
            }

            togglePlay(song.isPlaying)

            binding.durationProgress.isVisible = song.isCurrent
            binding.duration.isVisible = song.isCurrent
            binding.displayDuration.isVisible = !song.isCurrent
        }

        private fun togglePlay(isPlay: Boolean) {
            if(isPlay) binding.playButton.setImageResource(R.drawable.ic_pause)
            else binding.playButton.setImageResource(R.drawable.ic_play)
        }

        @SuppressLint("SetTextI18n")
        fun updateCurrentMediaProgress(value: Long) {
            if(value in 0..binding.durationProgress.max)
                binding.durationProgress.progress = value.toInt()
            binding.duration.text = "${timeFormatter.format(value)} - ${timeFormatter.format(binding.durationProgress.max)}"
        }
    }

    fun updateCurrentMediaProgress(value: Long) {
        if(currentMedia == null) pendingProgress = value
        else {
            pendingProgress = null
            currentMedia?.updateCurrentMediaProgress(value)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ViewholderMusicItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        if(item.isCurrent) currentMedia = holder
    }
}