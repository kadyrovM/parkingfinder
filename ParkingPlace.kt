package com.example.parkingfinder

data class ParkingPlace(
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val rating: Float,
    val distanceMeters: Float
)