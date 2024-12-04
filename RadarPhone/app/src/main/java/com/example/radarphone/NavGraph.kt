package com.example.radarphone

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.Composable
import com.example.radarphone.ui.theme.FirstScreen
import com.example.radarphone.ui.theme.RegLogScreen

@Composable
fun NavGraph() {

    //function to navigate from one screen to another
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "first_screen") {
        composable("first_screen") { FirstScreen(navController) } // Pass navController
        composable("RegLog_screen") { RegLogScreen() }
    }
}