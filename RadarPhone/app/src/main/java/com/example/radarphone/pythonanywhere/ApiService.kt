package com.example.radarphone.server

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class LocationRequest(val lat: Double, val lon: Double, val typ: String)

interface ApiService {
    @POST("search")
    fun searchPlace(@Body request: LocationRequest): Call<Place>
}
