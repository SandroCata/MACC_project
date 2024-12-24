package com.example.radarphone.viewModels

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel

class AudioViewModel : ViewModel() {
    var mediaPlayer: MediaPlayer? = null
    private var isMediaPlayerInitialized = false

    fun initializeMediaPlayer(context: Context, resId: Int) {
        if (!isMediaPlayerInitialized) {
            mediaPlayer = MediaPlayer.create(context, resId)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start() // Start playback initially
            mediaPlayer?.setVolume(1f, 1f)
            isMediaPlayerInitialized = true
        }
    }

    fun mute() {
        mediaPlayer?.setVolume(0f, 0f)
    }

    fun unmute() {
        mediaPlayer?.setVolume(1f, 1f)
    }

    fun setVolume(volume: Float) {
        mediaPlayer?.setVolume(volume, volume)
    }
}