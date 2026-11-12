package com.keepfocus.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.keepfocus.app.R
import com.keepfocus.app.data.local.DailyStatEntity
import com.keepfocus.app.ui.viewmodel.MainViewModel
import com.keepfocus.app.util.PermissionHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val profile by viewModel.userProfile.collectAsState()
    val todayStat by viewModel.todayStats.collectAsState()
    val weeklyStats by viewModel.weeklyStats.collectAsState()

    var accessibilityOk by remember { mutableStateOf(PermissionHelper.isAccessibilityEnabled(context)) }
    var overlayOk by remember { mutableStateOf(PermissionHelper.isOverlayEnabled(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                accessibilityOk = PermissionHelper.isAccessibilityEnabled(context)
                overlayOk = PermissionHelper.isOverlayEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title), fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Permission warning banner if missing
            if (!accessibilityOk || !overlayOk) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Permissions required for reel detection",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Button(onClick = {
                            if (!accessibilityOk) PermissionHelper.openAccessibilitySettings(context)
                            else PermissionHelper.openOverlaySettings(context)
                        }) {
                            Text("Fix")
                        }
                    }
                }
            }

            // Protection Toggle Card
            val isProtectOn = profile?.protectActive ?: true
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (isProtectOn) stringResource(R.string.protection_active) else stringResource(R.string.protection_paused),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Popup every ${profile?.threshold ?: 30} reels",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Switch(
                        checked = isProtectOn,
                        onCheckedChange = { viewModel.toggleProtection(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.secondary,
                            checkedTrackColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Today's Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = stringResource(R.string.reels_today),
                    value = (todayStat?.reelsWatched ?: 0).toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = stringResource(R.string.popups_shown),
                    value = (todayStat?.popupsShown ?: 0).toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = stringResource(R.string.apps_closed),
                    value = (todayStat?.closeAppClicked ?: 0).toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            // Weekly Bar Chart Card (Compose Canvas)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    val totalWeek = weeklyStats.sumOf { it.reelsWatched }
                    Text(
                        text = stringResource(R.string.reels_weekly),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Total this week: $totalWeek reels",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    WeeklyBarChart(stats = weeklyStats.reversed())
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun WeeklyBarChart(stats: List<DailyStatEntity>) {
    val barColor = MaterialTheme.colorScheme.secondary
    val trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    val displayList = if (stats.isEmpty()) List(7) { DailyStatEntity(date = "", reelsWatched = 0) } else stats

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        val maxVal = (displayList.maxOfOrNull { it.reelsWatched } ?: 10).coerceAtLeast(10)
        val count = displayList.size
        val spacing = size.width / (count * 2f + 1f)
        val barWidth = spacing

        displayList.forEachIndexed { index, item ->
            val left = spacing + index * (barWidth + spacing)
            // Background track
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(left, 0f),
                size = Size(barWidth, size.height),
                cornerRadius = CornerRadius(12f, 12f)
            )
            // Value bar
            val ratio = (item.reelsWatched.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f)
            val barHeight = size.height * ratio
            if (barHeight > 0f) {
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(left, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(12f, 12f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsState()
    val currentThreshold = profile?.threshold ?: 30
    val trackInsta = profile?.trackInstagram ?: true
    val trackYt = profile?.trackYouTube ?: true

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Reel Threshold (Reels before popup)", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(10, 20, 30, 50).forEach { option ->
                            FilterChip(
                                selected = currentThreshold == option,
                                onClick = {
                                    viewModel.updateTrackingSettings(option, trackInsta, trackYt)
                                },
                                label = { Text("$option") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Monitored Apps", fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Instagram Reels")
                        Switch(
                            checked = trackInsta,
                            onCheckedChange = { viewModel.updateTrackingSettings(currentThreshold, it, trackYt) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("YouTube Shorts")
                        Switch(
                            checked = trackYt,
                            onCheckedChange = { viewModel.updateTrackingSettings(currentThreshold, trackInsta, it) }
                        )
                    }
                }
            }

            // Inline Profile Editor
            ProfileSetupScreen(
                viewModel = viewModel,
                isEditing = true,
                onSaved = onBack
            )
        }
    }
}
