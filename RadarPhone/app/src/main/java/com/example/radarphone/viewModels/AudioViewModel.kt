package com.example.radarphone.viewModels

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.radarphone.R

class AudioViewModel : ViewModel() {
    var mediaPlayer: MediaPlayer? = null
    private var isMediaPlayerInitialized = false
    private var songList: List<Int>? = null
    private var currentSongIndex = 0

    fun initializeMediaPlayer(context: Context, resId: Int) {
        if (!isMediaPlayerInitialized) {
            songList = listOf(
                R.raw.beat_it,
                R.raw.sweet_dreams,
                R.raw.feel_good_inc,
                R.raw.bye_bye_bye,
                R.raw.take_on_me
            )
            mediaPlayer = MediaPlayer.create(context, resId)
            mediaPlayer?.isLooping = false // Disable looping for individual songs
            mediaPlayer?.setVolume(1f, 1f)
            mediaPlayer?.setOnCompletionListener {
                // Play the next song when the current one finishes
                playNextSong(context)
            }
            mediaPlayer?.start()
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

    fun playNextSong(context: Context): String {
        if (songList == null || songList!!.isEmpty()) {
            return ""
        }
        // Release the previous resources
        mediaPlayer?.release()
        // Calculate the index of the next song
        val nextSongResId = songList!![currentSongIndex]
        Log.d("AudioViewModel", "Playing next song: $nextSongResId")

        // Create a new MediaPlayer for the next song
        mediaPlayer = MediaPlayer.create(context, nextSongResId)
        mediaPlayer?.isLooping = false // Disable looping for individual songs
        mediaPlayer?.setVolume(1f, 1f)
        mediaPlayer?.setOnCompletionListener {
            // Play the next song when the current one finishes
            playNextSong(context)
        }
        mediaPlayer?.start()

        currentSongIndex = (currentSongIndex + 1) % songList!!.size

        return when (nextSongResId) {
            R.raw.beat_it -> "Beat It"
            R.raw.feel_good_inc -> "Feel Good Inc"
            R.raw.bye_bye_bye -> "Bye Bye Bye"
            R.raw.sweet_dreams -> "Sweet Dreams"
            R.raw.take_on_me -> "Take On Me"
            else -> ""
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}