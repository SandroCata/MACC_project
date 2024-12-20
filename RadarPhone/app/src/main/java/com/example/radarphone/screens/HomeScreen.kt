package com.example.radarphone.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.radarphone.viewModels.RegLogViewModel

@Composable
fun HomeScreen(navController: NavController, regLogViewModel: RegLogViewModel) {
    Text(text = "Home Screen")
}