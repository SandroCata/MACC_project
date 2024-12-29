package com.example.radarphone

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.Composable
import com.example.radarphone.screens.AboutScreen
import com.example.radarphone.screens.FirstScreen
import com.example.radarphone.screens.HomeScreen
import com.example.radarphone.screens.RegLogScreen
import com.example.radarphone.screens.SettingsScreen
import com.example.radarphone.viewModels.AudioViewModel
import com.example.radarphone.viewModels.RegLogViewModel

@Composable
fun NavGraph(regLogViewModel: RegLogViewModel, audioViewModel: AudioViewModel) {

    //function to navigate from one screen to another
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "first_screen") {
        composable("first_screen") { FirstScreen(navController) }
        composable("RegLog_screen") { RegLogScreen(navController, regLogViewModel) }
        composable("Home_screen") { HomeScreen(navController, regLogViewModel, audioViewModel) }
        composable("Settings_screen") { SettingsScreen(navController, audioViewModel) }
        composable("About_screen") { AboutScreen(navController)}
    }
}