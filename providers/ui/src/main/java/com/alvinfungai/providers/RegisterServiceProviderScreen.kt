package com.alvinfungai.providers

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RegisterServiceProviderScreen(
    viewModel: ServiceProviderViewModel = hiltViewModel(),
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    // Form States
    var name by remember { mutableStateOf("") }
    var profession by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var serviceRadiusKm by remember { mutableStateOf("") }

    // Multi-selection state for services
    val availableServices = listOf("Plumbing", "Electrical", "Carpentry", "Painting", "Gardening",
        "Deep Cleaning", "Recovery", "Towing", "Catering", "Mobile", "Events", "Accommodation")
    var selectedServices by remember { mutableStateOf(setOf<String>()) }

    // Location state
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var isCapturingLocation by remember { mutableStateOf(false) }

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
                        .addOnSuccessListener { location ->
                            if (location != null) {
                                latitude = location.latitude
                                longitude = location.longitude
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
                Log.e("RegisterScreen", "Error getting location", e)
            }
        }
    }

    // Observe Success
    LaunchedEffect(uiState) {
        Log.d("RegisterScreen", "uiState changed: $uiState")
        uiState?.onSuccess { 
            Log.d("RegisterScreen", "Success received! Calling onSuccess callback.")
            onSuccess() 
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Become a Provider") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Basic Info
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Business/Full Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = profession,
                onValueChange = { profession = it },
                label = { Text("Profession (e.g. Master Plumber)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Single Selection: Category
            Text("Business Category", style = MaterialTheme.typography.titleSmall)
            val categories = listOf("Cleaning", "Repair", "Design", "Tech", "Automotive", "Auto",
                "Car", "Mobile", "Events", "Catering")
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            // Multi-Selection: Specific Services Provided
            Text("Specific Services Offered", style = MaterialTheme.typography.titleSmall)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableServices.forEach { service ->
                    val isSelected = selectedServices.contains(service)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedServices = if (isSelected) {
                                selectedServices - service
                            } else {
                                selectedServices + service
                            }
                        },
                        label = { Text(service) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }

            // Location Picker
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Service Area", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
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
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isCapturingLocation
                    ) {
                        if (isCapturingLocation) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.LocationOn, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (latitude == null) "Capture Current Location" else "Location Captured")
                        }
                    }

                    if (latitude != null && longitude != null) {
                        Text(
                            "Lat: ${String.format("%.6f", latitude)}, Lng: ${String.format("%.6f", longitude)}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = serviceRadiusKm,
                        onValueChange = { serviceRadiusKm = it },
                        label = { Text("Service Radius (Km)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            OutlinedTextField(
                value = rate,
                onValueChange = { rate = it },
                label = { Text("Hourly Rate ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description of Services") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            Button(
                onClick = {
                    viewModel.registerProvider(
                        name = name,
                        profession = profession,
                        category = category,
                        description = description,
                        hourlyRate = rate.toDoubleOrNull() ?: 0.0,
                        serviceRadiusKm = serviceRadiusKm.toDoubleOrNull() ?: 0.0,
                        services = selectedServices.toList(),
                        latitude = latitude ?: 0.0,
                        longitude = longitude ?: 0.0
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && name.isNotBlank() && latitude != null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Submit Registration")
                }
            }

            uiState?.onFailure { error ->
                Text(error.message ?: "Error", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
