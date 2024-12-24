package com.example.radarphone.screens

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.radarphone.viewModels.AudioViewModel

@Composable
fun SettingsScreen(navController: NavController, audioViewModel: AudioViewModel) {
    val configuration = LocalConfiguration.current

    //we will need that to change username and profilePhoto
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

    var isMuted by remember { mutableStateOf(false) }

    var sliderPosition by remember { mutableFloatStateOf(1f) } // Initial volume

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
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Settings", color = Color.White, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(spacing))
        Column(
            modifier = Modifier.fillMaxSize(), // Occupy remaining space
            horizontalAlignment = Alignment.CenterHorizontally, // Center horizontally
            verticalArrangement = Arrangement.Center // Center vertically
        ) {
            Text(text = "Volume", color = Color.White) // Volume text
            Slider(
                value = sliderPosition,
                onValueChange = { sliderPosition = it },
                onValueChangeFinished = {
                    audioViewModel.setVolume(sliderPosition)
                },
                valueRange = 0f..1f, // Volume range from 0 to 1
                modifier = Modifier.padding(16.dp) // Add padding for better visibility
            )
            Spacer(modifier = Modifier.height(spacing / 2)) // Reduced spacing
            Row(
                verticalAlignment = Alignment.CenterVertically // Align checkbox and text vertically
            ) {
                Checkbox(
                    checked = isMuted,
                    onCheckedChange = {
                        isMuted = it
                        if (isMuted) {
                            audioViewModel.mute()
                        } else {
                            audioViewModel.unmute()
                        }
                    },
                    modifier = Modifier.padding(16.dp) // Add padding for better visibility
                )
                Text(text = if (isMuted) "Muted" else "Unmuted", color = Color.White)
                Spacer(modifier = Modifier.height(spacing))
            }
            Button(
                modifier = Modifier.size(width = buttonWidthSize, height = 38.dp),
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