package com.example.learning_android_music_app_kulakov.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.learning_android_music_app_kulakov.databinding.ViewholderMusicItemBinding
import com.example.learning_android_music_app_kulakov.models.Song
import java.text.SimpleDateFormat
import java.util.*

class MusicAdapter(
    private val onItemClickListener: (Song) -> Unit
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

        fun bind(musicItem: Song) {
            binding.name.text = musicItem.title
            binding.artistName.text = musicItem.subtitle

            //binding.duration.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(musicItem.duration)

            Glide.with(binding.root).asBitmap()
                .load(musicItem.imageUrl)
                .into(binding.image)

            binding.root.setOnClickListener {
                onItemClickListener(musicItem)
            }
        }
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
        holder.bind(getItem(position))
    }

}