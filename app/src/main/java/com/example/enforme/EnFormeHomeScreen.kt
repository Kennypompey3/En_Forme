package com.example.enforme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.enforme.ui.theme.EnFormeTheme

@Composable
fun EnFormeHomeScreen() {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { HeroSection() }
        item { TestimonialsSection() }
        item { MembershipProgramsSection() }
    }
}

@Composable
fun HeroSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.home_hero_background),
            contentDescription = "Hero background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "EN FORME",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Flexible memberships, multi-outlet access, and a smooth experience from sign-up to renewal. Train with confidence, manage your plan effortlessly and unlock more with sessions and add-ons whenever you\'re ready.",
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
fun TestimonialsSection() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Testimonials",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "...still in doubt of the efficacy of our Program? hear real stories, from real people.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        sampleTestimonials.forEach { testimonial ->
            TestimonialCard(testimonial = testimonial)
        }
    }
}

@Composable
fun TestimonialCard(testimonial: Testimonial) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                repeat(testimonial.rating) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                }
            }
            Text(
                text = testimonial.text,
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                text = testimonial.name,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = testimonial.subtitle,
                color = Color.White
            )
        }
    }
}

@Composable
fun MembershipProgramsSection() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Membership Programs",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Choose the perfect plan to match your fitness goals.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        sampleMembershipPlans.forEach { plan ->
            MembershipPlanCard(plan = plan)
        }
    }
}

@Composable
fun MembershipPlanCard(plan: MembershipPlan) {
    val cardColors = if (plan.isFeatured) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    } else {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    }
    val textColor = if (plan.isFeatured) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = cardColors
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = plan.name, style = MaterialTheme.typography.headlineSmall, color = textColor)
            Row {
                Text(text = plan.price, style = MaterialTheme.typography.headlineMedium, color = textColor, fontWeight = FontWeight.Bold)
                Text(text = plan.period, color = textColor, modifier = Modifier.align(Alignment.Bottom))
            }
            Text(text = plan.description, style = MaterialTheme.typography.bodyMedium, color = textColor)
            Spacer(modifier = Modifier.height(8.dp))
            plan.features.forEach { feature ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = feature, color = textColor)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            val buttonColors = if (plan.isFeatured) {
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            } else {
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            }
            Button(
                onClick = { /* Handle button click */ },
                modifier = Modifier.fillMaxWidth(),
                colors = buttonColors
            ) {
                Text(text = "Get Started", color = if(plan.isFeatured) Color.Black else Color.White)
            }
        }
    }
}
