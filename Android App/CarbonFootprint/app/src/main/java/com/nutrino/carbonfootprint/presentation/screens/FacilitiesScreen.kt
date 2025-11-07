package com.nutrino.carbonfootprint.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nutrino.carbonfootprint.data.remote.facilities.CreateFacilityRequest
import com.nutrino.carbonfootprint.data.remote.facilities.FacilityResponse
import com.nutrino.carbonfootprint.presentation.state.CreateFacilityUIState
import com.nutrino.carbonfootprint.presentation.state.FacilitiesUIState
import com.nutrino.carbonfootprint.presentation.viewmodels.FacilityViewmodel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacilitiesScreen(
    facilityViewmodel: FacilityViewmodel = hiltViewModel()
) {
    val facilitiesState by facilityViewmodel.facilitiesState.collectAsStateWithLifecycle()
    val createFacilityState by facilityViewmodel.createFacilityState.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }

    // Load facilities when screen loads
    LaunchedEffect(Unit) {
        facilityViewmodel.getFacilities()
    }

    // Handle create facility success
    LaunchedEffect(createFacilityState) {
        when (createFacilityState) {
            is CreateFacilityUIState.Success -> {
                showCreateDialog = false
                facilityViewmodel.getFacilities() // Refresh list
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🏢",
                        fontSize = 32.sp
                    )
                    Text(
                        text = "Facilities",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "Manage your organization's locations",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Facility")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Facilities List
        when (facilitiesState) {
            is FacilitiesUIState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is FacilitiesUIState.Success -> {
                val facilities = (facilitiesState as FacilitiesUIState.Success).data
                if (facilities != null) {
                    if (facilities.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No facilities found. Add your first facility!",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(facilities) { facility ->
                                FacilityCard(facility = facility)
                            }
                        }
                    }
                }
            }
            is FacilitiesUIState.Error -> {
                val error = (facilitiesState as FacilitiesUIState.Error).error
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error loading facilities",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 16.sp
                        )
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { facilityViewmodel.getFacilities() }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            is FacilitiesUIState.Idle -> {
                Text("Loading facilities...")
            }
        }
    }

    // Create Facility Dialog
    if (showCreateDialog) {
        CreateFacilityDialog(
            onDismiss = { showCreateDialog = false },
            onCreateFacility = { request ->
                facilityViewmodel.createFacility(request)
            },
            createState = createFacilityState
        )
    }
}

@Composable
private fun FacilityCard(
    facility: FacilityResponse
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = facility.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (!facility.country.isNullOrEmpty()) {
                Text(
                    text = "Country: ${facility.country}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!facility.grid_region.isNullOrEmpty()) {
                Text(
                    text = "Grid Region: ${facility.grid_region}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CreateFacilityDialog(
    onDismiss: () -> Unit,
    onCreateFacility: (CreateFacilityRequest) -> Unit,
    createState: CreateFacilityUIState
) {
    var name by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var gridRegion by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create New Facility")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = ""
                    },
                    label = { Text("Facility Name *") },
                    isError = nameError.isNotEmpty(),
                    supportingText = if (nameError.isNotEmpty()) {
                        { Text(nameError, color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Country (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = gridRegion,
                    onValueChange = { gridRegion = it },
                    label = { Text("Grid Region (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (createState is CreateFacilityUIState.Error) {
                    Text(
                        text = createState.error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validate
                    if (name.isBlank()) {
                        nameError = "Facility name is required"
                        return@Button
                    }

                    onCreateFacility(
                        CreateFacilityRequest(
                            name = name.trim(),
                            country = if (country.isNotBlank()) country.trim() else "Unknown",
                            grid_region = if (gridRegion.isNotBlank()) gridRegion.trim() else "Unknown"
                        )
                    )
                },
                enabled = createState !is CreateFacilityUIState.Loading
            ) {
                if (createState is CreateFacilityUIState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
