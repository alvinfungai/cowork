package com.alvinfungai.bookings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.alvinfungai.app.core.CurrencyUtils
import com.alvinfungai.bookings.domain.model.Booking
import com.alvinfungai.bookings.domain.model.BookingStatus
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderBookingsScreen(
    onBackClick: () -> Unit,
    viewModel: ProviderBookingsViewModel = hiltViewModel()
) {
    val bookingsResult by viewModel.bookings.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Active", "History")

    LaunchedEffect(Unit) {
        viewModel.loadProviderBookings()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Work Requests") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            val result = bookingsResult
            if (result == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                result.onSuccess { bookings ->
                    val filteredBookings = when (selectedTab) {
                        0 -> bookings.filter { it.status == BookingStatus.PENDING || it.status == BookingStatus.ACCEPTED || it.status == BookingStatus.STARTED }
                        else -> bookings.filter { it.status == BookingStatus.COMPLETED || it.status == BookingStatus.REJECTED || it.status == BookingStatus.CANCELLED }
                    }

                    if (filteredBookings.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (selectedTab == 0) "No active requests." else "No booking history.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredBookings) { booking ->
                                ProviderBookingItem(
                                    booking = booking,
                                    onAccept = { viewModel.acceptBooking(booking.id) },
                                    onReject = { viewModel.rejectBooking(booking.id) },
                                    onStart = { viewModel.startBooking(booking.id) },
                                    onComplete = { viewModel.completeBooking(booking.id) }
                                )
                            }
                        }
                    }
                }.onFailure { error ->
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${error.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderBookingItem(
    booking: Booking,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onStart: () -> Unit,
    onComplete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = booking.customerName.ifBlank { "Customer" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                BookingStatusBadge(status = booking.status)
            }

            if (booking.customerPhone.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = booking.customerPhone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            
            Text(text = "Scheduled: ${dateFormat.format(Date(booking.scheduledTime))}", style = MaterialTheme.typography.bodyMedium)

            val startTime = booking.startTime
            if (startTime != null) {
                Text(
                    text = "Started: ${dateFormat.format(Date(startTime))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50)
                )
            }
            
            val endTime = booking.endTime
            if (endTime != null) {
                Text(
                    text = "Completed: ${dateFormat.format(Date(endTime))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF2196F3)
                )
            }

            // Calculate amount due
            if (startTime != null && endTime != null && booking.hourlyRate > 0) {
                val durationMillis = endTime - startTime
                val durationMinutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis)
                
                // Round up to the nearest 15 minutes for fairness
                val roundedMinutes = ceil(durationMinutes / 15.0).toLong() * 15
                val hours = roundedMinutes.toDouble() / 60.0
                val amountDue = hours * booking.hourlyRate

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Duration: ${String.format(Locale.getDefault(), "%.1f", hours)} hrs",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (roundedMinutes > durationMinutes) {
                            Text(
                                text = "(Rounded to nearest 15m)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "Amount Due: ${CurrencyUtils.formatUsd(amountDue)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (booking.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Notes: ${booking.notes}",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (booking.status == BookingStatus.PENDING || booking.status == BookingStatus.ACCEPTED || booking.status == BookingStatus.STARTED) {
                Spacer(modifier = Modifier.height(16.dp))

                when (booking.status) {
                    BookingStatus.PENDING -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onReject,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Decline")
                            }
                            Button(
                                onClick = onAccept,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Accept")
                            }
                        }
                    }
                    BookingStatus.ACCEPTED -> {
                        Button(
                            onClick = onStart,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                        ) {
                            Text("Start Job")
                        }
                    }
                    BookingStatus.STARTED -> {
                        Button(
                            onClick = onComplete,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("Mark as Completed")
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun BookingStatusBadge(status: BookingStatus) {
    val color = when (status) {
        BookingStatus.PENDING -> Color(0xFFFFA500)
        BookingStatus.ACCEPTED -> Color(0xFF4CAF50)
        BookingStatus.STARTED -> Color(0xFF2196F3)
        BookingStatus.REJECTED -> Color(0xFFF44336)
        BookingStatus.COMPLETED -> Color(0xFF2196F3)
        BookingStatus.CANCELLED -> Color.Gray
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small,
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
