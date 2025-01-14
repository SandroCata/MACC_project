package com.example.radarphone.screens

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.radarphone.R
import com.example.radarphone.viewModels.AudioViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID
import kotlin.compareTo
import android.Manifest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, audioViewModel: AudioViewModel) {

    val blackListCharsUsername = listOf('\'', '"', ' ', ',', ';', '(', ')', '[', ']', '{', '}', '<', '>', ':', '?', '!', '$', '&', '*', '#', '^', '/', '|', '-', '+', '~', '.')

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
    val inputWidthSize = if (changeSize) {
        500.dp
    } else {
        330.dp
    }
    val inputHeightSize = if (changeSize) {
        50.dp
    } else {
        70.dp
    }
    val imageSize = if (changeSize) {
        50.dp
    } else {
        120.dp
    }

    var isMuted by rememberSaveable { mutableStateOf(false) }

    var sliderPosition by rememberSaveable { mutableFloatStateOf(1f) } // Initial volume

    var username by remember { mutableStateOf("") }

    var newUsername by remember { mutableStateOf(username) }

    val user = FirebaseAuth.getInstance().currentUser

    val databaseRef = FirebaseDatabase.getInstance().reference

    var profilePicture by remember { mutableStateOf<Any?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    //var capturedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val scope = rememberCoroutineScope()

    // Fetch profile picture URL from Firebase database
    LaunchedEffect(user) {
        if (user != null) {
            // Retrieve username from Firebase
            databaseRef
                .child("users/${user.uid}/username")
                .get()
                .addOnSuccessListener { snapshot ->
                    username = snapshot.getValue(String::class.java) ?: ""
                }
            //Firstly, try to retrieve the profile picture
            databaseRef
                .child("users/${user.uid}/profilePicture")
                .get()
                .addOnSuccessListener { uriSnapshot ->
                    profilePicture = uriSnapshot.getValue(String::class.java)
                    // If it is not found, try to load the default profile picture
                    if (profilePicture == null) {
                        databaseRef
                            .child("users/${user.uid}/profilePictureDefault")
                            .get()
                            .addOnSuccessListener { snapshot ->
                                profilePicture = snapshot.getValue(Int::class.java)
                            }
                    }
                }
        }
    }

    //this changes profilePicture in Firebase with the one selected in gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            scope.launch {
                try {
                    // Update profilePictureResourceId in Firebase database
                    databaseRef.child("users/${user?.uid}/profilePicture").setValue(uri.toString())

                    // Update profilePicture state
                    profilePicture = uri

                    Toast.makeText(context, "Profile picture updated", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to update profile picture", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    // This changes profilePicture in Firebase with the one taken with the Camera
    var photoUri: Uri? by remember { mutableStateOf(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            photoUri?.let { uri ->
                scope.launch {
                    try {
                        databaseRef.child("users/${user?.uid}/profilePicture").setValue(uri.toString()).await()
                        profilePicture = uri
                        Toast.makeText(context, "Profile picture updated", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Failed to update profile picture", Toast.LENGTH_SHORT).show()
                    }
                }
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
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if(changeSize) Arrangement.Top else Arrangement.Center
    ) {
        Text(text = "Settings", color = Color.White, fontSize = if(!changeSize) 30.sp else 24.sp)
        Spacer(modifier = Modifier.height(spacing))
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(profilePicture)
                .crossfade(true)
                .build(),
            contentDescription = "Profile Picture",
            contentScale = ContentScale.Crop, // Add contentScale here
            modifier = Modifier
                .size(imageSize)
                .border(2.dp, Color.White, CircleShape) // Add border here
                .clip(CircleShape) // Clip applied after border
                .clickable {
                    // Show options to choose from gallery or camera
                    // For now, we'll just launch the gallery
                    galleryLauncher.launch("image/*")
                }
        )
        Spacer(modifier = Modifier.height(spacing-25.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    // Check for camera permission
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        // Create a file to store the image
                        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                        val storageDir: File = context.filesDir
                        val file = File.createTempFile(
                            "JPEG_${timeStamp}_", /* prefix */
                            ".jpg", /* suffix */
                            storageDir /* directory */
                        )
                        photoUri = FileProvider.getUriForFile(
                            context,
                            "com.example.radarphone.fileprovider",
                            file
                        )
                        cameraLauncher.launch(photoUri!!)
                    } else {
                        // Request camera permission
                        // You can use a permission launcher here if you want to handle the permission request more gracefully
                        Toast.makeText(context, "Activate camera permission for the app", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Magenta,
                    contentColor = Color.White
                )
            ) {
                Text("Take a photo")
            }
        }
        //TO DO:Modify the Text below to display the username of the user
        Text(text = username, color = Color.White, fontSize = fontSize, fontFamily = FontFamily.Serif)
        //TO DO: add one input field to type new user name and a button below to confirm and update username also in firebase realtime database
        Spacer(modifier = Modifier.height(spacing))
        // Input field for new username
        OutlinedTextField(
            value = newUsername,
            onValueChange = { newUsername = it },
            placeholder = { Text("Type new Username(min 3 and no special characters)", color = Color.Black, fontSize = 11.sp) }, // Use placeholder instead of label
            modifier = Modifier
                .width(inputWidthSize)
                .height(inputHeightSize),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                cursorColor = Color.Black,
                containerColor = Color.White, // Set text color to white
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White
            )
        )
        // Button to update username
        Button(
            onClick = {
                scope.launch {
                    try {
                        databaseRef.child("users/${user?.uid}/username").setValue(newUsername).await()
                        username = newUsername // Update displayed username
                        Toast.makeText(context, "Username updated", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Failed to update username", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            enabled = newUsername.length >= 3 && !newUsername.any { it in blackListCharsUsername }, // Disable button if newUsername contains blacklist characters // Disable button if newUsername is less than 3 characters
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Magenta,
                contentColor = Color.White
            )
        ) {
            Text("Update Username")
        }
            Text(text = "Volume", color = Color.White) // Volume text
            Slider(
                value = sliderPosition,
                onValueChange = { sliderPosition = it },
                onValueChangeFinished = {
                    audioViewModel.setVolume(sliderPosition)
                },
                valueRange = 0f..1f // Volume range from 0 to 1
            )
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
                    }
                )
                Text(text = if (isMuted) "Muted" else "Unmuted", color = Color.White)
            }
            if(!changeSize)
                Spacer(modifier = Modifier.height(spacing))
            Button(
                modifier = Modifier.size(width = buttonWidthSize, height = 34.dp),
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