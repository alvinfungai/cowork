package com.alvinfungai.reviews.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReviewScreen(
    bookingId: String,
    providerId: String,
    onReviewSubmitted: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddReviewViewModel = hiltViewModel()
) {
    var rating by remember { mutableDoubleStateOf(5.0) }
    var comment by remember { mutableStateOf("") }
    val reviewStatus by viewModel.reviewStatus.collectAsState()

    LaunchedEffect(reviewStatus) {
        if (reviewStatus?.isSuccess == true) {
            onReviewSubmitted()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rate Service") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "How was your experience?",
                style = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            StarRatingBar(
                rating = rating,
                onRatingChanged = { rating = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Write a comment (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.submitReview(
                        bookingId = bookingId,
                        providerId = providerId,
                        rating = rating,
                        comment = comment
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = rating > 0
            ) {
                Text("Submit Review")
            }

            if (reviewStatus?.isFailure == true) {
                Text(
                    text = reviewStatus?.exceptionOrNull()?.message ?: "Error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun StarRatingBar(
    rating: Double,
    onRatingChanged: (Double) -> Unit
) {
    Row {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = if (i <= rating) Color(0xFFFFD700) else Color.Gray,
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onRatingChanged(i.toDouble()) }
            )
        }
    }
}
