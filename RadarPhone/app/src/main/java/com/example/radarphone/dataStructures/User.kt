package com.example.radarphone.dataStructures

import com.example.radarphone.R

data class User(val uid: String, val username: String, val email: String, val password: String? = null, val profilePicture: String ? = null, val profilePictureDefault: Int? = R.drawable.profilepic) // Store as Int