package com.example.video_player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerViewModel : ViewModel() {

    private val _showController = MutableLiveData(true)
    val showController: LiveData<Boolean> = _showController

    private val _playerState = MutableLiveData(PlayerState.READY)
    val playerState: LiveData<PlayerState> = _playerState

    private var showControllerJob: Job? = null

    init {
        showControllerJob = viewModelScope.launch {
            delay(5000)
            _showController.value = false
        }
    }

    fun toggleController() {
        showControllerJob?.cancel()
        showControllerJob = viewModelScope.launch {
            if (showController.value == true) {
                if (playerState.value != PlayerState.PAUSE)
                    _showController.value = false
            } else {
                _showController.value = true
                delay(3500)
                _showController.value = false
            }
        }
    }

    fun togglePlayPause(startPlay: Boolean) {
        viewModelScope.launch {
            showControllerJob?.cancel()
            if (startPlay) {
                showControllerJob = launch {
                    _showController.value = true
                    delay(3500)
                    _showController.value = false
                }
                _playerState.value = PlayerState.PLAYING
            } else {
                showControllerJob = launch { _showController.value = true }
                _playerState.value = PlayerState.PAUSE
            }
        }
    }

    enum class PlayerState {
        PLAYING,
        PAUSE,
        READY,
        BUFFERING,
        STOP
    }
}