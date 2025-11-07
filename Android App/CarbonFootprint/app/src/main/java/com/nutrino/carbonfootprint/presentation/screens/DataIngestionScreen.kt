package com.nutrino.carbonfootprint.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nutrino.carbonfootprint.data.remote.ingestion.EventRequest
import com.nutrino.carbonfootprint.data.remote.ingestion.IngestEventsRequest
import com.nutrino.carbonfootprint.presentation.state.IngestEventsUIState
import com.nutrino.carbonfootprint.presentation.viewmodels.IngestionViewmodel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataIngestionScreen(
    ingestionViewmodel: IngestionViewmodel = hiltViewModel()
) {
    val ingestEventsState by ingestionViewmodel.ingestEventsState.collectAsStateWithLifecycle()

    // Form state variables
    var category by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var valueNumeric by remember { mutableStateOf("") }
    var occurredAt by remember { mutableStateOf(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)) }

    // Form validation states
    var categoryError by remember { mutableStateOf("") }
    var unitError by remember { mutableStateOf("") }
    var valueError by remember { mutableStateOf("") }

    // Handle ingest success
    LaunchedEffect(ingestEventsState) {
        when (ingestEventsState) {
            is IngestEventsUIState.Success -> {
                // Clear form on success
                category = ""
                unit = ""
                valueNumeric = ""
                occurredAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Title and subtitle
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "📊",
                    fontSize = 32.sp
                )
                Text(
                    text = "Data Ingestion",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "Record your carbon footprint data",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "✏️",
                        fontSize = 24.sp
                    )
                    Text(
                        text = "Manual Event Entry",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Category Field
                OutlinedTextField(
                    value = category,
                    onValueChange = {
                        category = it
                        categoryError = ""
                    },
                    label = { Text("Category *") },
                    placeholder = { Text("e.g., electricity, natural_gas, fuel") },
                    isError = categoryError.isNotEmpty(),
                    supportingText = if (categoryError.isNotEmpty()) {
                        { Text(categoryError, color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Unit Field
                OutlinedTextField(
                    value = unit,
                    onValueChange = {
                        unit = it
                        unitError = ""
                    },
                    label = { Text("Unit *") },
                    placeholder = { Text("e.g., kWh, kg, liter, m³") },
                    isError = unitError.isNotEmpty(),
                    supportingText = if (unitError.isNotEmpty()) {
                        { Text(unitError, color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Value Numeric Field
                OutlinedTextField(
                    value = valueNumeric,
                    onValueChange = {
                        valueNumeric = it
                        valueError = ""
                    },
                    label = { Text("Value *") },
                    placeholder = { Text("e.g., 100.5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = valueError.isNotEmpty(),
                    supportingText = if (valueError.isNotEmpty()) {
                        { Text(valueError, color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Occurred At Field
                OutlinedTextField(
                    value = occurredAt,
                    onValueChange = { occurredAt = it },
                    label = { Text("Occurred At") },
                    placeholder = { Text("ISO-8601 format") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Success/Error Messages
                when (ingestEventsState) {
                    is IngestEventsUIState.Success -> {
                        val response = (ingestEventsState as IngestEventsUIState.Success).data
                        if (response != null) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = "Success!",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Created Events: ${response.created_events}",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Created Emissions: ${response.created_emissions}",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    if (response.skipped_duplicates > 0) {
                                        Text(
                                            text = "Skipped Duplicates: ${response.skipped_duplicates}",
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                    is IngestEventsUIState.Error -> {
                        val error = (ingestEventsState as IngestEventsUIState.Error).error
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = "Error: $error",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                    else -> {}
                }

                // Submit Button
                Button(
                    onClick = {
                        // Validate form
                        var isValid = true

                        if (category.isBlank()) {
                            categoryError = "Category is required"
                            isValid = false
                        }

                        if (unit.isBlank()) {
                            unitError = "Unit is required"
                            isValid = false
                        }

                        if (valueNumeric.isBlank()) {
                            valueError = "Value is required"
                            isValid = false
                        } else {
                            try {
                                valueNumeric.toDouble()
                            } catch (e: NumberFormatException) {
                                valueError = "Value must be a valid number"
                                isValid = false
                            }
                        }

                        if (isValid) {
                            val eventRequest = EventRequest(
                                occurred_at = occurredAt,
                                category = category.trim(),
                                unit = unit.trim(),
                                value_numeric = valueNumeric.toDouble(),
                                facility_id = null
                            )

                            ingestionViewmodel.ingestEvents(
                                IngestEventsRequest(events = listOf(eventRequest))
                            )
                        }
                    },
                    enabled = ingestEventsState !is IngestEventsUIState.Loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (ingestEventsState is IngestEventsUIState.Loading) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text("Ingesting Event...")
                        }
                    } else {
                        Text("Ingest Event")
                    }
                }
            }
        }

        // Common Units Reference
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Common Units Reference",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val commonUnits = listOf(
                    "Energy: kWh, MWh, BTU, GJ",
                    "Volume: liter, gallon, m³, ft³",
                    "Weight: kg, ton, lb, oz",
                    "Distance: km, mile, meter"
                )

                commonUnits.forEach { unitInfo ->
                    Text(
                        text = "• $unitInfo",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
