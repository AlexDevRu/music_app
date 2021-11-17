package com.example.learning_android_music_app_kulakov.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
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

    inner class MusicItemViewHolder(
        private val binding: ViewholderMusicItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(musicItem: Song, position: Int) {
            binding.name.text = musicItem.title
            binding.artistName.text = musicItem.subtitle

            Timber.d("duration ${musicItem.duration}")
            binding.duration.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(musicItem.duration)

            Glide.with(binding.root)
                .load(musicItem.imageUrl)
                .into(binding.image)

            binding.playButton.setOnClickListener {
                onPlayClickListener(musicItem)
                togglePlay(!musicItem.isPlaying)
            }

            binding.root.setOnClickListener {
                onItemClickListener(musicItem)
            }

            togglePlay(musicItem.isPlaying)
        }

        fun togglePlay(isPlay: Boolean) {
            if(isPlay) binding.playButton.setImageResource(R.drawable.ic_pause)
            else binding.playButton.setImageResource(R.drawable.ic_play)
        }
    }

    fun updateCurrentMedia(mediaId: String) {
        holderMap[mediaId]?.let { notifyItemChanged(it) }
    }

    private val holderMap = mutableMapOf<String, Int>()

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
        holder.bind(item, position)
        holderMap[item.mediaId] = position
    }
}