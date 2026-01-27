package com.example.enforme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun WorkoutScreen(workouts: List<Workout>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        items(workouts) { workout ->
            WorkoutCard(workout = workout)
        }
    }
}

@Composable
fun WorkoutCard(workout: Workout) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = workout.name, style = MaterialTheme.typography.headlineSmall)
            Text(text = workout.description)
            Text(text = "Duration: ${workout.duration} minutes")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WorkoutScreenPreview() {
    WorkoutScreen(workouts = sampleWorkouts)
}
