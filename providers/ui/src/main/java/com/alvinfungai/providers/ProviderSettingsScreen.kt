package com.alvinfungai.providers

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvinfungai.providers.domain.model.ServiceProvider
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProviderSettingsScreen(
    onBackClick: () -> Unit,
    viewModel: ProviderSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Observe the updateSuccess event from the ViewModel
    LaunchedEffect(Unit) {
        viewModel.updateSuccess.collect {
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is ProviderSettingsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ProviderSettingsUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { /* Retry logic */ }) {
                            Text("Retry")
                        }
                    }
                }
                is ProviderSettingsUiState.Success -> {
                    SettingsContent(
                        provider = state.provider,
                        onSave = { viewModel.updateProfile(it) }
                    )
                }
                is ProviderSettingsUiState.Updating -> {
                    SettingsContent(
                        provider = state.provider,
                        enabled = false,
                        onSave = {}
                    )
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsContent(
    provider: ServiceProvider,
    enabled: Boolean = true,
    onSave: (ServiceProvider) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(provider.name) }
    var profession by remember { mutableStateOf(provider.profession) }
    var category by remember { mutableStateOf(provider.category) }
    var description by remember { mutableStateOf(provider.description) }
    var hourlyRate by remember { mutableStateOf(provider.hourlyRate.toString()) }
    var serviceRadius by remember { mutableStateOf(provider.serviceRadiusKm.toString()) }
    
    val availableServices = listOf("Plumbing", "Electrical", "Carpentry", "Painting", "Gardening",
        "Deep Cleaning", "Recovery", "Towing", "Catering", "Mobile", "Events", "Accommodation")
    var selectedServices by remember { mutableStateOf(provider.services.toSet()) }
    
    var latitude by remember { mutableStateOf(provider.latitude) }
    var longitude by remember { mutableStateOf(provider.longitude) }
    var isCapturingLocation by remember { mutableStateOf(false) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isCapturingLocation = true
        }
    }

    LaunchedEffect(isCapturingLocation) @androidx.annotation.RequiresPermission(anyOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION]) {
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
                                latitude = loc.latitude
                                longitude = loc.longitude
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
                Log.e("SettingsContent", "Error getting location", e)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Business Name") },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        )

        OutlinedTextField(
            value = profession,
            onValueChange = { profession = it },
            label = { Text("Profession") },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        )

        Text("Business Category", style = MaterialTheme.typography.titleSmall)
        val categories = listOf("All", "Cleaning", "Repair", "Design", "Tech", "Automotive","Mobile", "Events", "Catering", "Education", "Training")
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            categories.forEach { cat ->
                FilterChip(
                    selected = category == cat,
                    onClick = { category = cat },
                    label = { Text(cat) },
                    modifier = Modifier.padding(end = 8.dp),
                    enabled = enabled
                )
            }
        }

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
                    } else null,
                    enabled = enabled
                )
            }
        }

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
                    enabled = enabled && !isCapturingLocation
                ) {
                    if (isCapturingLocation) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        val hasLocation = latitude != 0.0 || longitude != 0.0
                        Text(if (!hasLocation) "Capture Current Location" else "Location Captured")
                    }
                }

                if (latitude != 0.0 || longitude != 0.0) {
                    Text(
                        "Lat: ${String.format("%.6f", latitude)}, Lng: ${String.format("%.6f", longitude)}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = serviceRadius,
                    onValueChange = { serviceRadius = it },
                    label = { Text("Service Radius (Km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled
                )
            }
        }

        OutlinedTextField(
            value = hourlyRate,
            onValueChange = { hourlyRate = it },
            label = { Text("Hourly Rate ($)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            enabled = enabled
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            enabled = enabled
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onSave(
                    provider.copy(
                        name = name,
                        profession = profession,
                        category = category,
                        description = description,
                        hourlyRate = hourlyRate.toDoubleOrNull() ?: provider.hourlyRate,
                        serviceRadiusKm = serviceRadius.toDoubleOrNull() ?: provider.serviceRadiusKm,
                        services = selectedServices.toList(),
                        latitude = latitude,
                        longitude = longitude
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled && name.isNotBlank()
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Save Changes")
        }
    }
}
