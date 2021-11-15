package com.example.learning_android_music_app_kulakov.ui.fragments.main

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.learning_android_music_app_kulakov.databinding.FragmentMainBinding
import com.example.learning_android_music_app_kulakov.ui.adapters.MusicAdapter
import com.example.learning_android_music_app_kulakov.ui.fragments.base.BaseFragment
import moxy.ktx.moxyPresenter
import timber.log.Timber


class MainFragment: BaseFragment<FragmentMainBinding>(FragmentMainBinding::inflate), MainView {

    private val presenter by moxyPresenter {
        MainPresenter(requireContext().applicationContext)
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        presenter.onPermissionsResult(it)
    }

    private val musicAdapter = MusicAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.musicList.adapter = musicAdapter

        binding.musicSwipeLayout.setOnRefreshListener {
            presenter.refreshMusic()
        }

        binding.musicList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
    }

    override fun setLoadingState(isLoading: Boolean) {
        Timber.w("isLoading $isLoading")
        binding.progressBar.visibility = if(isLoading) View.VISIBLE else View.GONE
        if(!isLoading) binding.musicSwipeLayout.isRefreshing = false
    }

    override fun setData(data: List<MusicItem>) {
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