package com.example.radarphone.dataStructures

import com.example.radarphone.R

data class Player(val uid: String, val username: String, val email: String, val password: String? = null, val profilePicture: String = (R.drawable.profilepic).toString())