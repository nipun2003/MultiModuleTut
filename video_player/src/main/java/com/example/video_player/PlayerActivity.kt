package com.example.video_player

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.video_player.databinding.ActivityVideoPlayerBinding
import com.example.video_player.databinding.VideoPlayerControllerBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.coroutines.flow.collectLatest

class PlayerActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer

    private var _binding: ActivityVideoPlayerBinding? = null
    private val binding: ActivityVideoPlayerBinding get() = _binding!!

    private var _videoControlBinding: VideoPlayerControllerBinding? = null
    private val videoControlBinding get() = _videoControlBinding!!

    private var pausedByUser = false

    private val playerViewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        _videoControlBinding = binding.videoController
        setContentView(binding.root)
        player = ExoPlayer.Builder(this).build()
        val offlineUri = intent.data
        offlineUri?.let { videoUri ->
            playVideo(videoUri.toString())
            return
        }
        val urlText = intent.getStringExtra(EXTRA_VIDEO_URI)
        urlText?.let { text ->
            playVideo(text)
        }

        binding.root.setOnClickListener { playerViewModel.toggleController() }
        subscribeToPlayerViewModelObserver()
        handleVideoControlButtonClick()
    }

    private fun handleVideoControlButtonClick() {
        videoControlBinding.exoPlay.setOnClickListener {
            playerViewModel.togglePlayPause(true)
            pausedByUser = false
        }

        videoControlBinding.exoPause.setOnClickListener {
            playerViewModel.togglePlayPause(false)
            pausedByUser = true
        }
    }


    private fun subscribeToPlayerViewModelObserver() {
        playerViewModel.showController.observe(this) { show ->
            videoControlBinding.root.isVisible = show
        }

        playerViewModel.playerState.observe(this) { playerState ->
            when (playerState) {
                PlayerViewModel.PlayerState.PLAYING -> {
                    togglePlayPauseButton(true)
                    player.play()
                }
                PlayerViewModel.PlayerState.PAUSE, PlayerViewModel.PlayerState.READY -> {
                    togglePlayPauseButton(false)
                    player.pause()
                }
                else -> Unit
            }
        }
    }

    private fun togglePlayPauseButton(playing: Boolean) {
        videoControlBinding.exoPlay.isVisible = !playing
        videoControlBinding.exoPause.isVisible = playing
        binding.root.keepScreenOn = playing
    }


    private fun playVideo(url: String) {
        binding.playerView.player = player

        val item = MediaItem.fromUri(url)
        player.setMediaItem(item)
        player.prepare()
        playerViewModel.togglePlayPause(true)
        player.setHandleAudioBecomingNoisy(true)

    }

    override fun onPause() {
        super.onPause()
        playerViewModel.togglePlayPause(false)
    }

    override fun onResume() {
        super.onResume()
        if (!pausedByUser)
            playerViewModel.togglePlayPause(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }


    private fun releasePlayer() {
        player.release()
    }


    companion object {
        const val EXTRA_VIDEO_URI = "extra_video_uri"
    }
}