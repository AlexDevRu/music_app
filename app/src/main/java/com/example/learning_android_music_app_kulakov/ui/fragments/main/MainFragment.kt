package com.example.learning_android_music_app_kulakov.ui.fragments.main

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.learning_android_music_app_kulakov.databinding.FragmentMainBinding
import com.example.learning_android_music_app_kulakov.exoplayer.MusicServiceConnection
import com.example.learning_android_music_app_kulakov.models.Song
import com.example.learning_android_music_app_kulakov.ui.adapters.MusicAdapter
import com.example.learning_android_music_app_kulakov.ui.fragments.base.BaseFragment
import moxy.ktx.moxyPresenter
import org.koin.android.ext.android.inject
import timber.log.Timber


class MainFragment: BaseFragment<FragmentMainBinding>(FragmentMainBinding::inflate), MainView {

    private val musicServiceConnection by inject<MusicServiceConnection>()

    private val presenter by moxyPresenter {
        MainPresenter(musicServiceConnection)
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        presenter.onPermissionsResult(it)
    }

    private lateinit var musicAdapter: MusicAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.musicSwipeLayout.setOnRefreshListener {
            presenter.connectToService()
        }

        binding.musicList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        musicAdapter = MusicAdapter {
            presenter.playOrToggleSong(it)
        }

        binding.musicList.adapter = musicAdapter
    }

    override fun setLoadingState(isLoading: Boolean) {
        Timber.w("isLoading $isLoading")
        binding.progressBar.visibility = if(isLoading) View.VISIBLE else View.GONE
        if(!isLoading) binding.musicSwipeLayout.isRefreshing = false
    }

    override fun setData(data: List<Song>) {
        Timber.w("setData $data")
        musicAdapter.submitList(data)
    }

    override fun showError(message: String) {
        Timber.w("showError $message")
        showSnackBar(message)
    }

    override fun requestReadExternalStorage() {
        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}