package com.example.radarphone.screens

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.radarphone.R
import kotlinx.coroutines.delay

@Composable
fun GameScreen(navController: NavController) {
    val configuration = LocalConfiguration.current
    val changeSize = (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)

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
        Spacer(modifier = Modifier.weight(1f))

        // Timer and Back Button in a row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Timer at the bottom left
            var timeLeft by remember { mutableStateOf(120) } // 2 minutes in seconds

            LaunchedEffect(Unit) {
                while (timeLeft > 0) {
                    delay(1000L) // Delay for 1 second
                    timeLeft--
                }
            }

            Text(
                text = "Time left: ${timeLeft / 60}:${(timeLeft % 60).toString().padStart(2, '0')}",
                color = Color.White,
                fontSize = 16.sp
            )

            // Back Button at the bottom right
            Button(
                onClick = {
                    navController.popBackStack()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Magenta,
                    contentColor = Color.White
                )
            ) {
                Text("Quit")
            }
        }
    }
}
