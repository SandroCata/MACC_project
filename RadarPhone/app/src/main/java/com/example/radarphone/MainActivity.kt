package com.example.radarphone

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels

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



