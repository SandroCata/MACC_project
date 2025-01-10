package com.example.radarphone.screens

import android.content.res.Configuration
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.radarphone.R

@Composable
fun AboutScreen(navController: NavController) {
    val configuration = LocalConfiguration.current
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
        30.dp
    }

    val screenPadding = if (changeSize) {
        16.dp
    } else {
        80.dp
    }

    // Background image
    Image(
        painter = painterResource(id = R.drawable.mainscreen),
        contentDescription = "Background Image",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Title
        Text(
            text = "About the Game",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(spacing))

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Brief Description
            Text(
                text = "RadarPhone is an engaging radar game that uses your phone's GPS to guide you to a hidden location. Follow the radar, explore, and uncover the target!",
                color = Color.White,
                fontSize = fontSize,
                textAlign = TextAlign.Left
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "How to Play:",
                color = Color.Magenta,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "1. Choose a target location type (e.g., park, restaurant).",
                color = Color.White,
                fontSize = fontSize,
                textAlign = TextAlign.Left
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "2. Use the radar to navigate and get closer to the target. The radar updates as you move in the real world.",
                color = Color.White,
                fontSize = fontSize,
                textAlign = TextAlign.Left
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "3. Move physically to align yourself with the radar's directions. Your movements will reflect in the radar display.",
                color = Color.White,
                fontSize = fontSize,
                textAlign = TextAlign.Left
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "4. Reach the destination to win!",
                color = Color.White,
                fontSize = fontSize,
                textAlign = TextAlign.Left
            )
            Spacer(modifier = Modifier.height(spacing))

            // Button to navigate back to Main Menu
            Button(
                modifier = Modifier
                    .size(width = buttonWidthSize, height = 38.dp),
                onClick = {
                    navController.navigate("Home_screen")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Magenta,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Main Menu",
                    fontSize = fontSize
                )
            }
        }
    }
}
