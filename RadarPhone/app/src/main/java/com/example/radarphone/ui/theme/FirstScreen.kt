package com.example.radarphone.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.radarphone.R

@Composable
fun FirstScreen(navController: NavController) {

    //box encapsulating all
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Transparent) // Ensure background is transparent for image
        .clickable { navController.navigate("RegLog_screen") }) {

        //background image
        Image(
            painter = painterResource(id = R.drawable.mainscreen),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Adjust scaling as needed
        )

        //title writing
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "RadarPhone", // Your title
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 50.dp), // Add padding as needed
                fontSize = 60.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                color = Color.White //
            )
        }

        //tap anywhere to start writing
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Tap anywhere to start",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp), // Add padding as needed
                fontSize = 35.sp,
                fontFamily = FontFamily.Cursive,
                fontWeight = FontWeight.Medium,
                color = Color.White //
            )
        }

        //tap anywhere to start
        Box(modifier = Modifier
            .fillMaxSize()
            .clickable { navController.navigate("RegLog_screen") }) {
        }
    }
}
