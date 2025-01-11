package com.example.radarphone.server

data class Place(
    val name: String,
    val location: Location,
    val address: String
)

data class Location(
    val lat: Double,
    val lng: Double
)