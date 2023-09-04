package com.azimzada.healthapp.dtos


data class GoogleFitDataDTO(
    val day: Int,
    val date: String,
    val steps: Int,
    val calories: Float,
    val activity: Int
)
