package com.example.radarphone.screens

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.radarphone.R
import com.example.radarphone.viewModels.RegLogViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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

    val iconWidthSize = if (changeSize) {
        80.dp
    } else {
        100.dp
    }

    val fontSize = if (changeSize) {
        14.sp
    } else {
        16.sp
    }

    val spacing = if (changeSize) {
        10.dp
    } else {
        80.dp
    }

    val screenPadding = if (changeSize) {
        16.dp
    } else {
        80.dp
    }

    // Remember a CoroutineScope
    val coroutineScope = rememberCoroutineScope()

    // Scrollable select button
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("","Restaurant", "Supermarket", "Gas Station", "Park")
    var selectedOptionText by remember { mutableStateOf(options[0]) }

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
        Text(
            text = "Hello there, Seeker! Time to play ;)",
            color = Color.White,
            fontFamily = FontFamily.Cursive,
            fontWeight = FontWeight.Medium,
            fontSize = 35.sp
        )
        Spacer(modifier = Modifier.height(spacing))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.padding(16.dp)
        ) {
            TextField(
                readOnly = true,
                value = selectedOptionText,
                onValueChange = { },
                label = { Text("Choose a location to find") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            selectedOptionText = selectionOption
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(spacing))
        // Start the game button
        Button(
            onClick = {
                if (selectedOptionText.isNotEmpty() && selectedOptionText != options[0]) {
                    // Start the game logic

                }
                else {
                    Toast.makeText(context, "Select a location from dropdown window above", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .wrapContentSize()
                .widthIn(max = buttonWidthSize),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedOptionText.isNotEmpty() && selectedOptionText != options[0]) Color.Magenta else Color.LightGray, // Change color based on selection
                contentColor = if (selectedOptionText.isNotEmpty() && selectedOptionText != options[0]) Color.White else Color.DarkGray // Change content color based on selection
            )
        ) {
            Text(text = "Start the game", fontSize = fontSize)
        }
        Spacer(modifier = Modifier.height(spacing))
        // About the game button
        Button(
            onClick = { /* Navigate to About screen */ },
            modifier = Modifier
                .wrapContentSize()
                .widthIn(max = buttonWidthSize),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Magenta,
                contentColor = Color.White
            )
        ) {
            Text(text = "About the game", fontSize = fontSize)
        }
        Spacer(modifier = Modifier.height(spacing))
        Row(
            modifier = Modifier
                .fillMaxSize() // Occupy the entire width
                .align(Alignment.CenterHorizontally)
                .padding(16.dp), // Add padding around the row
            horizontalArrangement = Arrangement.SpaceBetween, // Space elements evenly
            verticalAlignment = Alignment.Bottom // Align to the bottom
        ) {
            Button(
                modifier = Modifier
                    .height(38.dp)
                    .wrapContentSize() // Allow button to wrap its content
                    .widthIn(max = buttonWidthSize),// Limit maximum width,
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
            IconButton(
                onClick = { navController.navigate("Settings_screen") }, // Navigate to Settings screen
                modifier = Modifier.size(width=iconWidthSize-20.dp, height = 38.dp) //to make the right area clickable adjust the width size
            ) {
                Icon(
                    modifier = Modifier.size(width=iconWidthSize, height = 38.dp),
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = Color.White // Set icon color to white
                )
            }
        }
    }
}