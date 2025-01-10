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
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.radarphone.R
import com.example.radarphone.viewModels.AudioViewModel
import com.example.radarphone.viewModels.RegLogViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, regLogViewModel: RegLogViewModel, audioViewModel: AudioViewModel) {

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

    val user = FirebaseAuth.getInstance().currentUser
    val databaseRef = FirebaseDatabase.getInstance().reference
    var username by remember { mutableStateOf("") }
    var song: String? = null

    // Remember a CoroutineScope
    val coroutineScope = rememberCoroutineScope()

    // Scrollable select button
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("","Restaurant", "Supermarket", "Gas Station", "Park")
    var selectedOptionText by remember { mutableStateOf(options[0]) }

    LaunchedEffect(user) {
        if (user != null) {
            // ... (existing code to fetch profile picture) ...

            // Fetch username from Firebase
            databaseRef
                .child("users/${user.uid}/username")
                .get()
                .addOnSuccessListener { snapshot ->
                    username = snapshot.getValue(String::class.java) ?: ""
                }
        }
    }

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
            text = buildAnnotatedString {
                append("Welcome, ")
                withStyle(style = SpanStyle(color = Color.Red)) {
                    append(username)
                }
                append("!")
                append("\nTime to play ;)")
            },
            color = Color.White,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            fontSize = 22.sp
        )
        Spacer(modifier = Modifier.height(spacing-5.dp))

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
        Spacer(modifier = Modifier.height(spacing-15.dp))
        // Start the game button
        Button(
            onClick = {
                if (selectedOptionText.isNotEmpty() && selectedOptionText != options[0]) {
                    // Start the game logic
                    navController.navigate("Game_screen")

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
        Spacer(modifier = Modifier.height(spacing-15.dp))
        // About the game button
        Button(
            onClick = { /* Navigate to About screen */
                navController.navigate("About_screen")
            },
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
        Spacer(modifier = Modifier.height(spacing-15.dp))
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(5.dp), // Add padding around the row
            horizontalArrangement = Arrangement.SpaceBetween, // Space elements evenly
            verticalAlignment = Alignment.Bottom // Align to the bottom
        ) {
            // Music icon button
            IconButton(
                onClick = { /* Play next song logic */
                    song = audioViewModel.playNextSong(context)
                    Toast.makeText(context, "Now playing: $song", Toast.LENGTH_SHORT).show()},
                modifier = Modifier.size(width = iconWidthSize - 20.dp, height = 38.dp)
            ) {
                Icon(
                    modifier = Modifier.size(width = iconWidthSize, height = 38.dp),
                    imageVector = Icons.Filled.PlayArrow, // Use music note icon
                    contentDescription = "Next Song",
                    tint = Color.White
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
        if(!changeSize)
            Spacer(modifier = Modifier.height(spacing))
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
    }
}