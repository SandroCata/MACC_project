package com.example.radarphone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.NavGraph
import com.example.radarphone.viewModels.RegLogViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val regLogViewModel : RegLogViewModel by viewModels()
        enableEdgeToEdge()
        setContent {
            NavGraph(regLogViewModel)
        }
    }
}



