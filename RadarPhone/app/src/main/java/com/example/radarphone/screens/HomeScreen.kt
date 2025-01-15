package com.example.radarphone.screens

import android.content.pm.PackageManager
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.radarphone.R
import com.example.radarphone.server.ApiService
import com.example.radarphone.server.LocationRequest
import com.example.radarphone.server.RetrofitClient
import com.example.radarphone.viewModels.AudioViewModel
import com.example.radarphone.viewModels.RegLogViewModel
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import android.Manifest
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.text.font.Font
import com.example.radarphone.server.Place
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    regLogViewModel: RegLogViewModel,
    audioViewModel: AudioViewModel
) {

    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val changeSize = (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)

    val buttonWidthSize = if (changeSize) 200.dp else 300.dp
    val iconWidthSize = if (changeSize) 80.dp else 100.dp
    val fontSize = if (changeSize) 14.sp else 16.sp
    val spacing = if (changeSize) 10.dp else 55.dp
    val screenPadding = if (changeSize) 16.dp else 80.dp

    val user = FirebaseAuth.getInstance().currentUser
    val databaseRef = FirebaseDatabase.getInstance().reference
    var username by remember { mutableStateOf("") }
    var song: String? = null
    val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(LocalContext.current)

    val coroutineScope = rememberCoroutineScope()

    // Dropdown menu
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("", "Restaurant", "Supermarket", "Fuel", "Cafe")
    var selectedOptionText by remember { mutableStateOf(options[0]) }

    // Game state
    var isGameStarting by remember { mutableStateOf(false) }
    var canRetry by remember { mutableStateOf(false) } // New state variable

    val myCustomFontFamily = androidx.compose.ui.text.font.FontFamily(
        Font(R.font.mandalore, FontWeight.Normal))

    LaunchedEffect(user) {
        if (user != null) {
            databaseRef.child("users/${user.uid}/username").get()
                .addOnSuccessListener { snapshot ->
                    username = snapshot.getValue(String::class.java) ?: ""
                }
        }
    }

    // Function to handle the API call and navigation
    fun startGame() {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val apiService = RetrofitClient.instance.create(ApiService::class.java)
                    val request = LocationRequest(
                        lat = location.latitude,
                        lon = location.longitude,
                        typ = selectedOptionText.lowercase()
                    )
                    apiService.searchPlace(request).enqueue(object : Callback<Place> {
                        override fun onResponse(call: Call<Place>, response: Response<Place>) {
                            if (response.isSuccessful) {
                                val place = response.body()
                                if (place != null) {
                                    navController.navigate("Game_screen/${place.name}/${place.findlocation[0]}/${place.findlocation[1]}/${place.address}")
                                    canRetry = false
                                } else {
                                    Toast.makeText(
                                        context,
                                        "No nearby $selectedOptionText  was found",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    canRetry = true
                                }
                            } else {
                                Log.d(
                                    "Response unsuccessful",
                                    "${response.errorBody()?.string()}"
                                )
                                Toast.makeText(
                                    context,
                                    "Error: ${response.errorBody()?.string()}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                canRetry = true
                            }
                            isGameStarting = false
                        }

                        override fun onFailure(call: Call<Place>, t: Throwable) {
                            Log.e("Start game", "Error in request: ${t.message}", t)
                            Toast.makeText(
                                context,
                                "Error in request: ${t.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            canRetry = true
                            isGameStarting = false
                        }
                    })
                } else {
                    Toast.makeText(context, "Failed to get location", Toast.LENGTH_SHORT).show()
                    isGameStarting = false
                    canRetry = true
                }
            }
        } else {
            Toast.makeText(context, "Activate location permission for the app", Toast.LENGTH_SHORT)
                .show()
            isGameStarting = false
            canRetry = true
        }
    }

    //background image
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
        Text(
            modifier = Modifier.padding(0.dp, 0.dp, 0.dp, screenPadding),
            text = buildAnnotatedString {
                append("Hi,")
                withStyle(style = SpanStyle(color = Color.Yellow, fontFamily = myCustomFontFamily, fontSize = 26.sp)) {
                    append(username)
                }
                append("!")
                append("\n Time to play ;)")
            },
            color = Color.White,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            fontSize = 22.sp
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.padding(16.dp)
        ) {
            TextField(
                readOnly = true,
                value = selectedOptionText,
                onValueChange = { },
                label = { Text("Choose a location to find")},
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
        Spacer(modifier = Modifier.height(spacing - 15.dp))
        // Start the game button
        Button(
            onClick = {
                if (!isGameStarting) {
                    if (selectedOptionText.isNotEmpty() && selectedOptionText != options[0]) {
                        isGameStarting = true
                        canRetry = false
                        startGame()
                    } else {
                        Toast.makeText(
                            context,
                            "Select a location from dropdown window above",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else if (canRetry) {
                    isGameStarting = true
                    canRetry = false
                    startGame()
                }
            },
            modifier = Modifier
                .wrapContentSize()
                .widthIn(max = buttonWidthSize),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedOptionText.isNotEmpty() && selectedOptionText != options[0]) Color.Magenta else Color.LightGray,
                contentColor = if (selectedOptionText.isNotEmpty() && selectedOptionText != options[0]) Color.White else Color.DarkGray
            )
        ) {
            Text(text = if (canRetry) "Retry" else "Start the game", fontSize = fontSize)
        }
        Spacer(modifier = Modifier.height(spacing - 15.dp))
        // About the game button
        Button(
            onClick = {
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
        Spacer(modifier = Modifier.height(spacing - 15.dp))
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Music icon button
            IconButton(
                onClick = {
                    song = audioViewModel.playNextSong(context)
                    Toast.makeText(context, "Now playing: $song", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(width = iconWidthSize - 20.dp, height = 38.dp)
            ) {
                Icon(
                    modifier = Modifier.size(width = iconWidthSize, height = 38.dp),
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Next Song",
                    tint = Color.White
                )
            }
            IconButton(
                onClick = { navController.navigate("Settings_screen") },
                modifier = Modifier.size(width = iconWidthSize - 20.dp, height = 38.dp)
            ) {
                Icon(
                    modifier = Modifier.size(width = iconWidthSize, height = 38.dp),
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }
        }
        if (!changeSize)
            Spacer(modifier = Modifier.height(spacing))
        Button(
            modifier = Modifier
                .height(38.dp)
                .wrapContentSize()
                .widthIn(max = buttonWidthSize),
            onClick = {
                coroutineScope.launch {
                    val signOutResult = regLogViewModel.signout()
                    if (signOutResult.first) {
                        Toast.makeText(context, signOutResult.second, Toast.LENGTH_SHORT).show()
                        navController.navigate("RegLog_screen") {
                            popUpTo("RegLog_screen") { inclusive = true }
                        }
                    } else {
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