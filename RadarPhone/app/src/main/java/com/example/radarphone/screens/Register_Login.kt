package com.example.radarphone.screens

import android.app.Activity
import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.radarphone.R
import com.example.radarphone.viewModels.RegLogViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import com.example.radarphone.dataStructures.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegLogScreen(navController: NavController, regLogViewModel: RegLogViewModel) {

    //Used for OutlinedTextFields colors
    MaterialTheme { // Add MaterialTheme here

        //this part is useful for device orientation and size changes
        val configuration = LocalConfiguration.current

        val context = LocalContext.current

        val auth = Firebase.auth

        // Google Sign-In client
        val googleSignInClient = GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id)) // Request ID token
                .requestEmail()
                .build()
        )

        val changeSize = (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
        //Log.d("SecondScreen", "Change Size: $changeSize")

        val fontSize = if (changeSize) {
            14.sp
        } else {
            16.sp
        }

        val buttonWidthSize = if (changeSize) {
            200.dp
        } else {
            300.dp
        }

        val inputWidthSize = if (changeSize) {
            200.dp
        } else {
            300.dp
        }

        val inputHeightSize = if (changeSize) {
            60.dp
        } else {
            70.dp
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

        //variables for the form
        var username by rememberSaveable { mutableStateOf("") }
        var email by rememberSaveable { mutableStateOf("") }
        var password by rememberSaveable { mutableStateOf("") }
        var confirmPassword by rememberSaveable { mutableStateOf("") }

        //switch between registration and login
        var isRegistering by rememberSaveable { mutableStateOf(true) } // Toggle between registration and login

        var registered by remember { mutableStateOf(Pair(false, "")) }
        var logged by remember { mutableStateOf(Pair(false, "")) }

        // Remember a CoroutineScope
        val coroutineScope = rememberCoroutineScope()

        // Focus states for label visibility
        var usernameFocused by remember { mutableStateOf(false) }
        var emailFocused by remember { mutableStateOf(false) }
        var passwordFocused by remember { mutableStateOf(false) }
        var confirmPasswordFocused by remember { mutableStateOf(false) }

        // Focus Requesters
        val usernameFocusRequester = remember { FocusRequester() }
        val emailFocusRequester = remember { FocusRequester() }
        val passwordFocusRequester = remember { FocusRequester() }
        val confirmPasswordFocusRequester = remember { FocusRequester() }


        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                val user2 = User(
                                    uid = user?.uid ?: "",
                                    username = user?.displayName ?: "",
                                    email = user?.email ?: "",
                                    profilePicture = user?.photoUrl.toString()
                                )

                                // Check if the user is new or existing
                                regLogViewModel.checkUserExists(user2.uid) { exists ->
                                    if (!exists) {
                                        // New user, create account
                                        regLogViewModel.createUser(user2)
                                        Log.d("RegLogScreen", "New user created")
                                    } else {
                                        // Existing user, log in
                                        Log.d("RegLogScreen", "Existing user logged in")
                                    }

                                    // Navigate to home screen
                                    navController.navigate("Home_screen")
                                    Log.d("RegLogScreen", "signInWithCredential:success")
                                    Toast.makeText(context, "Google sign-in success", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // Handle sign-in failure
                                Log.w("RegLogScreen", "signInWithCredential:failure", task.exception)
                                Toast.makeText(context, "Google sign-in failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                } catch (e: ApiException) {
                    // Handle Google Sign-In failure
                    Log.w("RegLogScreen", "Google sign in failed", e)
                    Toast.makeText(context, "Google sign-in failed", Toast.LENGTH_SHORT).show()
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

        //organizes the structure of the elements
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            if (isRegistering) {
                OutlinedTextField(
                    modifier = Modifier
                        .width(inputWidthSize)
                        .height(inputHeightSize)
                        .focusRequester(usernameFocusRequester)
                        .onFocusEvent {
                            usernameFocused = it.isFocused
                        },
                    value = username,
                    onValueChange = { username = it },
                    label = {
                        if ((usernameFocused || username.isNotEmpty())) {
                            Text("")
                        } else {
                            Text("Username")
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(unfocusedBorderColor = Color.White,
                        focusedBorderColor = Color.White,
                        cursorColor = Color.Black,
                        containerColor = Color.White,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black
                    )
                )
            }
            OutlinedTextField(
                modifier = Modifier
                    .width(inputWidthSize)
                    .height(inputHeightSize)
                    .focusRequester(emailFocusRequester)
                    .onFocusEvent {
                        emailFocused = it.isFocused
                    },
                value = email,
                onValueChange = { email = it },
                label = {
                    if ((emailFocused || email.isNotEmpty())) {
                        Text("")
                    } else {
                        Text("Email")
                    }
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.White,
                    focusedBorderColor = Color.White,
                    cursorColor = Color.Black,
                    containerColor = Color.White,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Black
                )
            )
            OutlinedTextField(
                modifier = Modifier
                    .width(inputWidthSize)
                    .height(inputHeightSize)
                    .focusRequester(passwordFocusRequester)
                    .onFocusEvent {
                        passwordFocused = it.isFocused
                    },
                value = password,
                onValueChange = { password = it },
                label = {
                    if ((passwordFocused || password.isNotEmpty())) {
                        Text("")
                    } else {
                        Text("Password")
                    }
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.White,
                    focusedBorderColor = Color.White,
                    cursorColor = Color.Black,
                    containerColor = Color.White,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Black
                ),
                visualTransformation = PasswordVisualTransformation()
            )
            if (isRegistering) {
                OutlinedTextField(
                    modifier = Modifier
                        .width(inputWidthSize)
                        .height(inputHeightSize)
                        .focusRequester(confirmPasswordFocusRequester)
                        .onFocusEvent {
                            confirmPasswordFocused = it.isFocused
                        },
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = {
                        if ((confirmPasswordFocused || confirmPassword.isNotEmpty())) {
                            Text("")
                        } else {
                            Text("Confirm Password")
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = Color.White,
                        focusedBorderColor = Color.White,
                        cursorColor = Color.Black,
                        containerColor = Color.White,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black
                    ),
                    visualTransformation = PasswordVisualTransformation()
                )
            }
            Spacer(modifier = Modifier.height(spacing))
            Button(modifier = Modifier.size(width = buttonWidthSize, height = 38.dp),
                onClick = { /* Handle registration/login */
                    coroutineScope.launch {
                        if (isRegistering) {
                            registered = regLogViewModel.signup(email, password, username, confirmPassword)
                            Log.d("registered", "$registered")
                            if (registered.first) {
                                // Display pop-up or message for successful registration
                                Toast.makeText(context, registered.second, Toast.LENGTH_SHORT)
                                    .show()
                                // Switch to login form
                                isRegistering = false
                            } else {
                                // Display error message
                                Toast.makeText(context, registered.second, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        } else {
                            logged = regLogViewModel.login(email, password)
                            Log.d("logged", "$logged")
                            if (logged.first) {
                                // Navigate to the next screen
                                Toast.makeText(context, logged.second, Toast.LENGTH_SHORT).show()
                                navController.navigate("Home_screen")
                            } else {
                                // Display error message
                                Toast.makeText(context, logged.second, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                          },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Magenta,
                    contentColor = Color.White )
            ) {
                Text(text = if (isRegistering) "Sign up" else "Sign in",
                    fontSize = fontSize)
            }
            Spacer(modifier = Modifier.height(spacing))
            Button(modifier = Modifier.size(width = buttonWidthSize, height = 38.dp),
                onClick = { /* Handle Google OAuth */
                    coroutineScope.launch {
                        // Sign out before starting sign-in flow
                        googleSignInClient.signOut()

                        val signInIntent = googleSignInClient.signInIntent
                        launcher.launch(signInIntent)
                    }
                          },
                colors = ButtonDefaults.buttonColors
                    (
                    containerColor = Color.Magenta,
                    contentColor = Color.White )
            ) {
                Text(text = "Google sign up/sign in",
                    fontSize = fontSize)
            }
            Spacer(modifier = Modifier.height(spacing))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isRegistering) "Already a player?" else "Not a player?",
                    fontSize = fontSize,
                    color = Color.White
                )
                TextButton(onClick = { isRegistering = !isRegistering }) {
                    Text(
                        text = if (isRegistering) "Sign in" else "Sign up",
                        fontSize = fontSize,
                        color = Color.Magenta
                    )
                }
            }
        }
    }
}