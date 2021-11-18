package com.example.learning_android_music_app_kulakov.ui.fragments.main

import android.Manifest
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.learning_android_music_app_kulakov.R
import com.example.learning_android_music_app_kulakov.databinding.FragmentMainBinding
import com.example.learning_android_music_app_kulakov.exoplayer.isPlaying
import com.example.learning_android_music_app_kulakov.other.Status
import com.example.learning_android_music_app_kulakov.ui.adapters.MusicAdapter
import com.example.learning_android_music_app_kulakov.ui.fragments.base.BaseFragment
import com.example.learning_android_music_app_kulakov.ui.fragments.song.SongVM
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber


class MainFragment: BaseFragment<FragmentMainBinding>(FragmentMainBinding::inflate) {

    private val mainViewModel by sharedViewModel<MainVM>()
    private val songVM by sharedViewModel<SongVM>()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        mainViewModel.onPermissionsResult(it)
    }

    private lateinit var musicAdapter: MusicAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_menu, menu)
        val searchView = menu.findItem(R.id.app_bar_search).actionView as SearchView
        searchView.setOnQueryTextListener(
            object: SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?) = false

                override fun onQueryTextChange(newText: String?): Boolean {
                    mainViewModel.searchByQuery(newText.orEmpty())
                    return true
                }
            }
        )
        searchView.maxWidth = Int.MAX_VALUE
    }

    private fun observe() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) { result ->
            when(result.status) {
                Status.SUCCESS -> {
                    binding.progressBar.isVisible = false
                    result.data?.let { songs ->
                        songs.forEach {
                            val isCurrent = it.mediaId == mainViewModel.curPlayingSong.value?.description?.mediaId
                            it.isPlaying = isCurrent && mainViewModel.playbackState.value?.isPlaying == true
                            it.isCurrent = isCurrent
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
            Timber.w("curPlayingSong changed")

            val list = mainViewModel.mediaItems.value?.data?.map {
                val isCurrent = it.mediaId == metadata?.description?.mediaId
                it.copy(
                    isCurrent = isCurrent,
                    isPlaying = isCurrent && mainViewModel.playbackState.value?.isPlaying == true,
                )
            }

            musicAdapter.submitList(list)
        }

        mainViewModel.playbackState.observe(viewLifecycleOwner) { playState ->
            Timber.w("playbackstate changed")

            val list = mainViewModel.mediaItems.value?.data?.map {
                val isCurrent = it.mediaId == mainViewModel.curPlayingSong.value?.description?.mediaId
                it.copy(
                    isCurrent = isCurrent,
                    isPlaying = isCurrent && playState?.isPlaying == true,
                )
            }
            musicAdapter.submitList(list)
        }

        mainViewModel.networkError.observe(viewLifecycleOwner) {
            if(!it.hasBeenHandled)
                showSnackBar(it.getContentIfNotHandled()?.message.orEmpty())
        }

        songVM.curSongDuration.observe(viewLifecycleOwner) {
            Timber.w("curSongDuration changed $it")
            musicAdapter.updateCurrentMediaDuration(it)
            musicAdapter.updateCurrentMediaProgress(songVM.curPlayerPosition.value ?: 0L)
        }

        songVM.curPlayerPosition.observe(viewLifecycleOwner) {
            Timber.w("curPlayerPosition changed $it")
            musicAdapter.updateCurrentMediaProgress(it)
        }
    }
}