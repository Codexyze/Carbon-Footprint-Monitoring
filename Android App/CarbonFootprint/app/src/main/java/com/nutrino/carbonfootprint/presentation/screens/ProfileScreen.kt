package com.nutrino.carbonfootprint.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nutrino.carbonfootprint.presentation.state.MeUIState
import com.nutrino.carbonfootprint.presentation.viewmodels.UserPreferenceViewModel
import com.nutrino.carbonfootprint.presentation.viewmodels.UserViewmodel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    userViewmodel: UserViewmodel = hiltViewModel(),
    userPreferenceViewModel: UserPreferenceViewModel = hiltViewModel()
) {
    val meState by userViewmodel.meState.collectAsStateWithLifecycle()

    // Load user profile when screen loads
    LaunchedEffect(Unit) {
        userViewmodel.getMe()
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
                    text = "👤",
                    fontSize = 32.sp
                )
                Text(
                    text = "Profile",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "Your account and organization details",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        // User Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "ℹ️",
                        fontSize = 24.sp
                    )
                    Text(
                        text = "User Information",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                when (meState) {
                    is MeUIState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is MeUIState.Success -> {
                        (meState as MeUIState.Success).data?.let { user ->
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ProfileInfoRow(label = "Email", value = user.email)
                                ProfileInfoRow(label = "Role", value = user.role.replaceFirstChar { it.uppercase() })
                                ProfileInfoRow(label = "Organization", value = user.org.name)
                                ProfileInfoRow(label = "Plan", value = user.org.plan.replaceFirstChar { it.uppercase() })
                            }
                        }
                    }
                    is MeUIState.Error -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error loading profile",
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = (meState as MeUIState.Error).error,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { userViewmodel.getMe() }
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                    is MeUIState.Idle -> {
                        Text("Loading profile...")
                    }
                }
            }
        }

        // Actions Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Actions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Logout Button
                OutlinedButton(
                    onClick = {
                        userPreferenceViewModel.clearAllData()
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Logout")
                }
            }
        }

        // App Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "About",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Carbon Footprint Monitoring",
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Track and analyze your organization's carbon emissions",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Version 1.0.0",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
        Text(
            text = value,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}
