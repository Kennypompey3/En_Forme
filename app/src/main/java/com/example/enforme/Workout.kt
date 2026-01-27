package com.example.enforme

data class Workout(
    val id: Int,
    val name: String,
    val description: String,
    val duration: Int // in minutes
)

val sampleWorkouts = listOf(
    Workout(1, "Full Body Strength", "A workout to target all major muscle groups.", 60),
    Workout(2, "Cardio Blast", "A high-intensity cardio workout to burn calories.", 30),
    Workout(3, "Yoga Flow", "A relaxing yoga flow to improve flexibility and reduce stress.", 45)
)
