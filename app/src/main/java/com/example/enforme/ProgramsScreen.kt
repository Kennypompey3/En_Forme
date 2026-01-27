package com.example.enforme

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Data class to hold program information
data class Program(
    val title: String,
    val description: String,
    val price: String,
    val planCode: String, // Unique identifier for the payment plan
    val imageResId: Int
)

// List of available programs
val programs = listOf(
    Program(
        title = "Singles",
        description = "This is our singles plan",
        price = "₦100/month",
        planCode = "plan-singles-001", // Example plan code
        imageResId = R.drawable.singles_membership
    ),
    Program(
        title = "Couples",
        description = "This is our couples plan",
        price = "₦100/month",
        planCode = "plan-couples-001", // Example plan code
        imageResId = R.drawable.couples_membership
    )
)

@Composable
fun ProgramsScreen(
    modifier: Modifier = Modifier,
    paymentState: PaymentUiState,
    onProgramClicked: (Program) -> Unit // Callback for when a card is clicked
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(programs.size) { index ->
                val program = programs[index]
                ProgramCard(
                    program = program,
                    // Pass the click event up to the parent (HomeScreen)
                    onItemClick = { onProgramClicked(program) }
                )
            }
        }
        if (paymentState is PaymentUiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun ProgramCard(
    program: Program,
    onItemClick: () -> Unit // The card now accepts a simple click lambda
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }, // Execute the lambda on click
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = program.imageResId),
                contentDescription = program.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = program.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = program.price,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary // Use your theme's primary color
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = program.description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
