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
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class MusicAdapter(
    private val onItemClickListener: (Song) -> Unit,
    private val onPlayClickListener: (Song) -> Unit,
): ListAdapter<Song, MusicAdapter.MusicItemViewHolder>(DIFF_UTIL) {

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

    inner class MusicItemViewHolder(
        private val binding: ViewholderMusicItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(musicItem: Song) {
            binding.name.text = musicItem.title
            binding.artistName.text = musicItem.subtitle

            Glide.with(binding.root)
                .load(musicItem.imageUrl)
                .placeholder(R.drawable.ic_baseline_music_note_24)
                .error(R.drawable.ic_baseline_music_note_24)
                .into(binding.image)

            binding.playButton.setOnClickListener {
                onPlayClickListener(musicItem)
                //togglePlay(!musicItem.isPlaying)
            }

            binding.root.setOnClickListener {
                onItemClickListener(musicItem)
            }

            togglePlay(musicItem.isPlaying)

            binding.durationProgress.isVisible = musicItem.isCurrent
            binding.duration.isVisible = musicItem.isCurrent
        }

        fun togglePlay(isPlay: Boolean) {
            if(isPlay) binding.playButton.setImageResource(R.drawable.ic_pause)
            else binding.playButton.setImageResource(R.drawable.ic_play)
        }

        fun updateCurrentMediaDuration(duration: Long) {
            binding.durationProgress.max = duration.toInt()
        }

        @SuppressLint("SetTextI18n")
        fun updateCurrentMediaProgress(value: Long) {
            if(value in 0..binding.durationProgress.max)
                binding.durationProgress.progress = value.toInt()
            binding.duration.text = "${timeFormatter.format(value)} - ${timeFormatter.format(binding.durationProgress.max)}"
        }
    }

    private var pendingDuration: Long? = null
    private var pendingProgress: Long? = null

    fun updateCurrentMediaDuration(duration: Long) {
        if(currentMedia == null) pendingDuration = duration
        else {
            pendingDuration = null
            currentMedia?.updateCurrentMediaDuration(duration)
        }
    }
    fun updateCurrentMediaProgress(value: Long) {
        if(currentMedia == null) pendingProgress = value
        else {
            pendingProgress = null
            currentMedia?.updateCurrentMediaProgress(value)
        }
    }

    private val holderMap = mutableMapOf<String, Int>()

    private var currentMedia: MusicItemViewHolder? = null
        set(value) {
            field = value
            if(pendingDuration != null) field?.updateCurrentMediaDuration(pendingDuration!!)
            if(pendingProgress != null) field?.updateCurrentMediaProgress(pendingProgress!!)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicItemViewHolder {
        val binding = ViewholderMusicItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return MusicItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MusicItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holderMap[item.mediaId] = position
        if(item.isCurrent) currentMedia = holder
    }
}