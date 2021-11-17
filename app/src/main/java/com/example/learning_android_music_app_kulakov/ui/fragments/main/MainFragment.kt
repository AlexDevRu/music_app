package com.example.learning_android_music_app_kulakov.ui.fragments.main

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.learning_android_music_app_kulakov.databinding.FragmentMainBinding
import com.example.learning_android_music_app_kulakov.exoplayer.isPlaying
import com.example.learning_android_music_app_kulakov.exoplayer.toSong
import com.example.learning_android_music_app_kulakov.models.Song
import com.example.learning_android_music_app_kulakov.other.Status
import com.example.learning_android_music_app_kulakov.ui.adapters.MusicAdapter
import com.example.learning_android_music_app_kulakov.ui.fragments.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class MainFragment: BaseFragment<FragmentMainBinding>(FragmentMainBinding::inflate) {

    private val mainViewModel by sharedViewModel<MainVM>()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        mainViewModel.onPermissionsResult(it)
    }

    private lateinit var musicAdapter: MusicAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.musicSwipeLayout.setOnRefreshListener {
            mainViewModel.connectToService()
        }

        binding.musicList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        musicAdapter = MusicAdapter(
            onPlayClickListener = {
                mainViewModel.playOrToggleSong(it, true)
            },
            onItemClickListener = {
                mainViewModel.playOrToggleSong(it)
                val action = MainFragmentDirections.actionMainFragmentToSongFragment()
                findNavController().navigate(action)
            }
        )

        binding.musicList.adapter = musicAdapter

        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

        observe()
    }

    private fun observe() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) { result ->
            when(result.status) {
                Status.SUCCESS -> {
                    binding.progressBar.isVisible = false
                    result.data?.let { songs ->
                        songs.forEach {
                            it.isPlaying = it.mediaId == mainViewModel.curPlayingSong.value?.description?.mediaId &&
                                    mainViewModel.playbackState.value?.isPlaying == true
                        }
                        musicAdapter.submitList(songs)
                    }
                }
                Status.ERROR -> {
                    showSnackBar(result.message.orEmpty())
                }
                Status.LOADING -> {
                    binding.progressBar.isVisible = true
                }
            }
            binding.musicSwipeLayout.isRefreshing = result.status == Status.LOADING
        }

        mainViewModel.curPlayingSong.observe(viewLifecycleOwner) { metadata ->
            mainViewModel.mediaItems.value?.data?.forEach {
                it.isPlaying = it.mediaId == metadata?.description?.mediaId && mainViewModel.playbackState.value?.isPlaying == true
            }
            musicAdapter.submitList(mainViewModel.mediaItems.value?.data)
            musicAdapter.notifyDataSetChanged()
        }

        mainViewModel.playbackState.observe(viewLifecycleOwner) { playState ->
            mainViewModel.mediaItems.value?.data?.forEach {
                it.isPlaying = it.mediaId == mainViewModel.curPlayingSong.value?.description?.mediaId && playState?.isPlaying == true
            }
            musicAdapter.submitList(mainViewModel.mediaItems.value?.data)
            musicAdapter.notifyDataSetChanged()
        }
    }
}