package com.alvinfungai.bookings

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBookingScreen(
    providerId: String,
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: BookingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var notes by remember { mutableStateOf("") }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var userLatitude by remember { mutableStateOf<Double?>(null) }
    var userLongitude by remember { mutableStateOf<Double?>(null) }
    var isCapturingLocation by remember { mutableStateOf(false) }

    val bookingStatus by viewModel.bookingStatus.collectAsState()
    val providerResult by viewModel.provider.collectAsState()

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isCapturingLocation = true
        }
    }

    LaunchedEffect(isCapturingLocation) {
        if (isCapturingLocation) {
            try {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { loc ->
                            if (loc != null) {
                                userLatitude = loc.latitude
                                userLongitude = loc.longitude
                            }
                            isCapturingLocation = false
                        }
                        .addOnFailureListener {
                            isCapturingLocation = false
                        }
                } else {
                    isCapturingLocation = false
                }
            } catch (e: Exception) {
                isCapturingLocation = false
                Log.e("CreateBookingScreen", "Error getting location", e)
            }
        }
    }

    LaunchedEffect(providerId) {
        viewModel.loadProvider(providerId)
    }

    LaunchedEffect(bookingStatus) {
        if (bookingStatus?.isSuccess == true) {
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirm Booking") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            val providerName = providerResult?.getOrNull()?.name ?: providerId
            Text("Booking for Provider: $providerName", style = MaterialTheme.typography.titleMedium)
            
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes for the provider") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Location Picker Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        isCapturingLocation = true
                    } else {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isCapturingLocation) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (userLatitude == null || userLongitude == null) {
                            if (isCapturingLocation) "Capturing location..." else "Select Service Location"
                        } else {
                            "Location Set: ${String.format(Locale.getDefault(), "%.6f", userLatitude)}, ${String.format(Locale.getDefault(), "%.6f", userLongitude)}"
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val dateString = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(selectedDate))
            Text("Scheduled Time: $dateString")

            Button(onClick = { /* Show DatePicker */ }) {
                Text("Change Time")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.createBooking(
                        providerId = providerId,
                        scheduledTime = selectedDate,
                        notes = notes,
                        latitude = userLatitude,
                        longitude = userLongitude
                    )
                },
                enabled = userLatitude != null && userLongitude != null && !isCapturingLocation,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirm and Book")
            }
            
            if (bookingStatus?.isFailure == true) {
                Text(
                    text = bookingStatus?.exceptionOrNull()?.message ?: "Error",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
