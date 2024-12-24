package com.example.radarphone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.radarphone.viewModels.AudioViewModel
import com.example.radarphone.viewModels.RegLogViewModel

class MainActivity : ComponentActivity() {

    private val audioViewModel: AudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val regLogViewModel : RegLogViewModel by viewModels()
        enableEdgeToEdge()
        setContent {
            NavGraph(regLogViewModel, audioViewModel)
        }
    }
    override fun onStart() {
        super.onStart()
        audioViewModel.initializeMediaPlayer(this, R.raw.background_music) // Initialize if not already
        audioViewModel.mediaPlayer?.start() // Resume playback if paused
    }

    override fun onStop() {
        super.onStop()
        audioViewModel.mediaPlayer?.pause() // Pause playback
    }

}



