package com.example.radarphone.ui.theme

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.radarphone.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegLogScreen() {

    //Used for OutlinedTextFields colors
    MaterialTheme { // Add MaterialTheme here

        //this part is useful for device orientation and size changes
        val configuration = LocalConfiguration.current

        val changeSize = (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
        //Log.d("SecondScreen", "Change Size: $changeSize")

        val fontSize = if (changeSize) {
            20.sp
        } else {
            16.sp
        }

        val buttonSize = if (changeSize) {
            200.dp
        } else {
            300.dp
        }

        val inputSize = if (changeSize) {
            220.dp
        } else {
            320.dp
        }

        val switchFormSize = if (changeSize) {
            220.dp
        } else {
            320.dp
        }
        val spacing = if (changeSize) {
            5.dp
        } else {
            50.dp
        }

        //variables for the form
        var username by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }

        //switch between registration and login
        var isRegistering by remember { mutableStateOf(true) } // Toggle between registration and login

        //background image
        Image(
            painter = painterResource(id = R.drawable.mainscreen),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Adjust scaling as needed
        )

        //organizes the structure of the elements
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            if (isRegistering) {
                OutlinedTextField(modifier = Modifier.width(inputSize),
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = Color.White,
                        focusedBorderColor = Color.White,
                        cursorColor = Color.White,
                        containerColor = Color.White // Set text color to white
                    )
                )
            }
            OutlinedTextField(modifier = Modifier.width(inputSize),
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.White,
                    focusedBorderColor = Color.White,
                    cursorColor = Color.White,
                    containerColor = Color.White // Set text color to white
                )
            )
            OutlinedTextField(modifier = Modifier.width(inputSize),
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.White,
                    focusedBorderColor = Color.White,
                    cursorColor = Color.White,
                    containerColor = Color.White // Set text color to white
                ),
                visualTransformation = PasswordVisualTransformation()
            )
            if (isRegistering) {
                OutlinedTextField(modifier = Modifier.width(inputSize),
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = Color.White,
                        focusedBorderColor = Color.White,
                        cursorColor = Color.White,
                        containerColor = Color.White // Set text color to white
                    ),
                    visualTransformation = PasswordVisualTransformation()
                )
            }
            Spacer(modifier = Modifier.height(spacing))
            Button(modifier = Modifier.size(width = buttonSize, height = 38.dp),
                onClick = { /* Handle registration/login */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Magenta,
                    contentColor = Color.White )
            ) {
                Text(if (isRegistering) "Register" else "Login")
            }
            Spacer(modifier = Modifier.height(spacing))
            Button(modifier = Modifier.size(width = buttonSize, height = 38.dp),
                onClick = { /* Handle Google OAuth */ },
                colors = ButtonDefaults.buttonColors
                    (
                    containerColor = Color.Magenta,
                    contentColor = Color.White )
            ) {
                Text(if (isRegistering) "Register with Google" else "Login with Google")
            }
            Spacer(modifier = Modifier.height(spacing))
            Text(
                text = if (isRegistering) "Already have an account? Login" else "Don't have an account? Register",
                fontSize = fontSize,
                modifier = Modifier
                    .clickable { isRegistering = !isRegistering }
                    .padding(4.dp),
                color = Color.Yellow
            )
        }
    }
}