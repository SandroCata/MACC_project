package com.example.radarphone.screens

import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.radarphone.R
import com.example.radarphone.viewModels.RegLogViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController, regLogViewModel: RegLogViewModel) {

    val configuration = LocalConfiguration.current

    val context = LocalContext.current

    val changeSize = (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)

    val buttonWidthSize = if (changeSize) {
        200.dp
    } else {
        300.dp
    }

    val fontSize = if (changeSize) {
        14.sp
    } else {
        16.sp
    }

    val spacing = if (changeSize) {
        5.dp
    } else {
        50.dp
    }

    val screenPadding = if (changeSize) {
        16.dp
    } else {
        80.dp
    }

    // Remember a CoroutineScope
    val coroutineScope = rememberCoroutineScope()

    //background image
    Image(
        painter = painterResource(id = R.drawable.mainscreen),
        contentDescription = "Background Image",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop // Adjust scaling as needed
    )


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Home Screen", color = Color.White)
        Spacer(modifier = Modifier.height(spacing))
        Button(
            modifier = Modifier.size(width = buttonWidthSize, height = 38.dp),
            onClick = { /* Handle signout (both if authenticated through google or through form) and navigate to RegLog_screen */
                coroutineScope.launch {
                    val signOutResult = regLogViewModel.signout()
                    if (signOutResult.first) {
                        // Sign-out successful
                        Toast.makeText(context, signOutResult.second, Toast.LENGTH_SHORT).show()
                        navController.navigate("RegLog_screen") {
                            // Clear back stack (Removes all destinations from the back stack up to and including the RegLog_screen.
                            // As a result, the back stack is now empty, and the RegLog_screen is the only destination in the navigation history.
                            // This ensures that the user cannot accidentally go back to the HomeScreen after signing out.)
                            popUpTo("RegLog_screen") { inclusive = true }
                        }
                    } else {
                        // Sign-out failed
                        Toast.makeText(context, signOutResult.second, Toast.LENGTH_SHORT).show()
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Magenta,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Sign out",
                fontSize = fontSize
            )
        }
    }
}