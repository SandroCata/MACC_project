package com.example.radarphone.googleoauth

data class SignInResult(
    val data: User?,
    val errorMessage: String?
)

data class User(
    val uid: String,
    val username: String?,
    val email: String?,
    val password: String?,
    val profilePicture: String?
)

data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)