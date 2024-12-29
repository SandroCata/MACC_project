package com.example.radarphone.viewModels

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import com.example.radarphone.R

class AudioViewModel : ViewModel() {
    var mediaPlayer: MediaPlayer? = null
    private var isMediaPlayerInitialized = false
    private var songList: List<Int>? = null

    fun initializeMediaPlayer(context: Context, resId: Int) {
        if (!isMediaPlayerInitialized) {
            mediaPlayer = MediaPlayer.create(context, resId)
            songList = listOf(
                R.raw.beat_it,
                R.raw.feel_good_inc,
                R.raw.bye_bye_bye,
                R.raw.take_on_me
            )
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

    private var currentSongIndex = 0

    fun playNextSong(context: Context): String {
        if (songList != null && songList!!.isNotEmpty()) {
            currentSongIndex = (currentSongIndex + 1) % songList!!.size
            val nextSongResId = songList!![currentSongIndex]

            mediaPlayer?.reset() // Reset the MediaPlayer
            mediaPlayer?.release() // Release the previous resources
            mediaPlayer = MediaPlayer.create(context, nextSongResId) // Create a new MediaPlayer
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
            mediaPlayer?.setVolume(1f, 1f)
        }
        if(currentSongIndex==0)
            return "Beat It"
        else if(currentSongIndex==1)
            return "Feel Good Inc"
        else if(currentSongIndex==2)
            return "Bye Bye Bye"
        else
            return "Take On Me"
    }
}
