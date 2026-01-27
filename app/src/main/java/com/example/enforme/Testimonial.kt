package com.example.enforme

data class Testimonial(
    val name: String,
    val subtitle: String,
    val text: String,
    val rating: Int
)

val sampleTestimonials = listOf(
    Testimonial(
        name = "Sarah Johnson",
        subtitle = "Lost 13.6kg",
        text = "'En Forme completely changed my life. The trainers are incredibly supportive and the community is amazing. I've never felt stronger!'",
        rating = 5
    ),
    Testimonial(
        name = "Michael Chen",
        subtitle = "Marathon Runner",
        text = "'The personalized training program helped me achieve my goal of running a marathon. The nutrition guidance was game-changing.'",
        rating = 5
    ),
    Testimonial(
        name = "Emma Williams",
        subtitle = "Busy Professional",
        text = "'As someone with a hectic schedule, the flexible class times and online support make it easy to stay consistent with my fitness goals.'",
        rating = 5
    )
)
