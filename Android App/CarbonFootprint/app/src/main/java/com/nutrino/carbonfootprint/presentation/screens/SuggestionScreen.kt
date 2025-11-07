package com.nutrino.carbonfootprint.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nutrino.carbonfootprint.presentation.state.MeUIState
import com.nutrino.carbonfootprint.presentation.state.SuggestionUIState
import com.nutrino.carbonfootprint.presentation.viewmodels.AnalyticsViewmodel
import com.nutrino.carbonfootprint.presentation.viewmodels.UserViewmodel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionScreen(
    navController: NavController,
    analyticsViewmodel: AnalyticsViewmodel = hiltViewModel(),
    userViewmodel: UserViewmodel = hiltViewModel()
) {
    val suggestionState by analyticsViewmodel.suggestionState.collectAsStateWithLifecycle()
    val meState by userViewmodel.meState.collectAsStateWithLifecycle()

    var userId by remember { mutableStateOf<Int?>(null) }
    var orgId by remember { mutableStateOf<Int?>(null) }

    // Load user data when screen loads
    LaunchedEffect(Unit) {
        userViewmodel.getMe()
    }

    // Extract user ID from meState
    LaunchedEffect(meState) {
        when (meState) {
            is MeUIState.Success -> {
                (meState as MeUIState.Success).data?.let { user ->
                    userId = user.id
                    orgId = user.org.id
                    // Auto-load suggestions once we have user ID
                    if (userId != null && orgId != null) {
                        analyticsViewmodel.getSuggestion(id = orgId!!, userId = userId!!)
                    }
                }
            }
            else -> { /* Do nothing */ }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Suggestions",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text("AI Suggestions")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            userId?.let { uid ->
                                orgId?.let { oid ->
                                    analyticsViewmodel.getSuggestion(id = oid, userId = uid)
                                }
                            }
                        },
                        enabled = userId != null && orgId != null
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (suggestionState) {
                is SuggestionUIState.Idle -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Suggestions",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "Loading suggestions...",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                is SuggestionUIState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "Analyzing your data...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                is SuggestionUIState.Success -> {
                    (suggestionState as SuggestionUIState.Success).data?.let { suggestion ->
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lightbulb,
                                            contentDescription = "AI Insight",
                                            modifier = Modifier.size(40.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Column {
                                            Text(
                                                text = "🌱 AI-Powered Insights",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                text = "Personalized sustainability recommendations",
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp)
                                    ) {
                                        Text(
                                            text = suggestion.message,
                                            style = MaterialTheme.typography.bodyLarge,
                                            lineHeight = 26.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = "🌿 Quick Actions",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            ActionItem("📊 Review your emissions data regularly")
                                            ActionItem("🎯 Set reduction targets")
                                            ActionItem("📈 Track your progress monthly")
                                            ActionItem("👥 Engage your team in sustainability efforts")
                                        }
                                    }
                                }
                            }

                            // Add bottom padding to ensure content is not cut off
                            item {
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                    } ?: run {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No suggestions available",
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                is SuggestionUIState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                text = "⚠️",
                                fontSize = 48.sp
                            )
                            Text(
                                text = "Error Loading Suggestions",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = (suggestionState as SuggestionUIState.Error).error,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Button(
                                onClick = {
                                    userId?.let { uid ->
                                        orgId?.let { oid ->
                                            analyticsViewmodel.getSuggestion(id = oid, userId = uid)
                                        }
                                    }
                                }
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.weight(1f)
        )
    }
}

