package com.example.enforme

data class MembershipPlan(
    val name: String,
    val price: String,
    val period: String,
    val description: String,
    val features: List<String>,
    val isFeatured: Boolean = false
)

val sampleMembershipPlans = listOf(
    MembershipPlan(
        name = "Starter",
        price = "₦20,000",
        period = "/month",
        description = "Perfect for beginners starting their fitness journey",
        features = listOf(
            "1 personalized training session/month",
            "Access to all gym equipment",
            "Basic Nutrition Plan",
            "Locker Access"
        )
    ),
    MembershipPlan(
        name = "Pro",
        price = "₦30,000",
        period = "/month",
        description = "For dedicated fitness enthusiasts",
        features = listOf(
            "4 personalized training sessions/month",
            "Unlimited group classes",
            "Custom nutrition plan",
            "Locker Access",
            "Body-composition analysis",
            "Priority class booking"
        ),
        isFeatured = true
    ),
    MembershipPlan(
        name = "Elite",
        price = "₦54,000",
        period = "/month",
        description = "Maximum results with premium support",
        features = listOf(
            "Unlimited personal training",
            "Unlimited group classes",
            "Premium nutrition consultation",
            "Private locker",
            "Monthly body-composition analysis",
            "Priority booking",
            "Guest passes (2/month)"
        )
    )
)
