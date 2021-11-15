package com.example.learning_android_music_app_kulakov.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.learning_android_music_app_kulakov.databinding.ViewholderMusicItemBinding
import com.example.learning_android_music_app_kulakov.ui.fragments.main.MusicItem
import timber.log.Timber

class MusicAdapter: ListAdapter<MusicItem, MusicAdapter.MusicItemViewHolder>(DIFF_UTIL) {

    companion object {
        private val DIFF_UTIL = object: DiffUtil.ItemCallback<MusicItem>() {
            override fun areItemsTheSame(oldItem: MusicItem, newItem: MusicItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: MusicItem, newItem: MusicItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class MusicItemViewHolder(
        private val binding: ViewholderMusicItemBinding
    ): RecyclerView.ViewHolder(binding.root) {

        private val millisInHour = 3600 * 1000
        private val millisInMinute = 60 * 1000

        fun bind(musicItem: MusicItem) {
            binding.name.text = musicItem.name
            binding.artistName.text = musicItem.artistName

            val hours = musicItem.duration / millisInHour
            val minutes = (musicItem.duration % millisInHour) / millisInMinute
            val seconds = ((musicItem.duration % millisInHour) % millisInMinute) / 1000

            binding.duration.text = "$hours:$minutes:$seconds"
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