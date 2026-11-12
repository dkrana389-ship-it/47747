package com.keepfocus.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil3.compose.AsyncImage
import com.keepfocus.app.R
import com.keepfocus.app.ui.viewmodel.MainViewModel
import com.keepfocus.app.util.PermissionHelper
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun WelcomeScreen(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(52.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = stringResource(R.string.welcome_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.welcome_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = stringResource(R.string.continue_btn), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProfileSetupScreen(
    viewModel: MainViewModel,
    isEditing: Boolean,
    onSaved: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsState()
    val scope = rememberCoroutineScope()

    var fullName by remember(profile) { mutableStateOf(profile?.fullName ?: "") }
    var nickname by remember(profile) { mutableStateOf(profile?.nickname ?: "") }
    var dream by remember(profile) { mutableStateOf(profile?.dream ?: "") }
    var ageText by remember(profile) { mutableStateOf(if (profile?.onboardingCompleted == true) profile!!.age.toString() else "") }
    var profession by remember(profile) { mutableStateOf(profile?.profession ?: "") }
    var dreamLine by remember(profile) { mutableStateOf(profile?.dreamLine ?: "") }
    var photoPath by remember(profile) { mutableStateOf(profile?.photoPath ?: "") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var dreamError by remember { mutableStateOf<String?>(null) }
    var ageError by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val savedPath = viewModel.copyPhoto(uri)
                if (savedPath.isNotEmpty()) {
                    photoPath = savedPath
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.profile_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Photo Picker Avatar
        Box(
            modifier = Modifier
                .size(104.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .clickable {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (photoPath.isNotEmpty() && File(photoPath).exists()) {
                AsyncImage(
                    model = File(photoPath),
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        TextButton(onClick = {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }) {
            Text(text = stringResource(R.string.pick_photo), color = MaterialTheme.colorScheme.secondary)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it; nameError = null },
            label = { Text(stringResource(R.string.full_name)) },
            isError = nameError != null,
            supportingText = { nameError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text(stringResource(R.string.nickname)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = dream,
            onValueChange = { dream = it; dreamError = null },
            label = { Text(stringResource(R.string.life_goal)) },
            isError = dreamError != null,
            supportingText = { dreamError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )

        OutlinedTextField(
            value = ageText,
            onValueChange = { ageText = it.filter { c -> c.isDigit() }; ageError = null },
            label = { Text(stringResource(R.string.age)) },
            isError = ageError != null,
            supportingText = { ageError?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = profession,
            onValueChange = { profession = it },
            label = { Text(stringResource(R.string.profession)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = dreamLine,
            onValueChange = { dreamLine = it },
            label = { Text(stringResource(R.string.dream_line)) },
            placeholder = { Text("e.g., I will build my own company and make my family proud.") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                var valid = true
                if (fullName.isBlank()) {
                    nameError = "Full name is required"
                    valid = false
                }
                if (dream.isBlank()) {
                    dreamError = "Please enter your life goal or dream"
                    valid = false
                }
                val parsedAge = ageText.toIntOrNull()
                if (parsedAge == null || parsedAge !in 10..100) {
                    ageError = "Age must be between 10 and 100"
                    valid = false
                }

                if (valid && parsedAge != null) {
                    viewModel.saveProfile(
                        fullName = fullName,
                        nickname = nickname,
                        dream = dream,
                        age = parsedAge,
                        profession = profession,
                        photoPath = photoPath,
                        dreamLine = dreamLine,
                        onComplete = onSaved
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (isEditing) stringResource(R.string.save_changes) else stringResource(R.string.continue_btn),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PermissionsConsentScreen(
    onContinue: () -> Unit,
    onDeny: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var accessibilityGranted by remember { mutableStateOf(PermissionHelper.isAccessibilityEnabled(context)) }
    var overlayGranted by remember { mutableStateOf(PermissionHelper.isOverlayEnabled(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                accessibilityGranted = PermissionHelper.isAccessibilityEnabled(context)
                overlayGranted = PermissionHelper.isOverlayEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.permissions_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Prominent Disclosure & Data Privacy",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.permissions_desc),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Permission status cards
        PermissionItemRow(
            title = "1. Accessibility Service",
            subtitle = "Detects Instagram Reels & YouTube Shorts scrolls",
            granted = accessibilityGranted,
            onEnable = { PermissionHelper.openAccessibilitySettings(context) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        PermissionItemRow(
            title = "2. Display Over Other Apps",
            subtitle = "Shows your dream reminder popup over video apps",
            granted = overlayGranted,
            onEnable = { PermissionHelper.openOverlaySettings(context) }
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                if (!accessibilityGranted) {
                    PermissionHelper.openAccessibilitySettings(context)
                } else if (!overlayGranted) {
                    PermissionHelper.openOverlaySettings(context)
                } else {
                    onContinue()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (accessibilityGranted && overlayGranted) stringResource(R.string.continue_btn) else stringResource(R.string.agree_btn),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onDeny) {
            Text(text = stringResource(R.string.deny_btn))
        }
    }
}

@Composable
private fun PermissionItemRow(
    title: String,
    subtitle: String,
    granted: Boolean,
    onEnable: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            if (granted) {
                Text(
                    text = "Active",
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            } else {
                FilledTonalButton(onClick = onEnable) {
                    Text("Enable")
                }
            }
        }
    }
}

@Composable
fun DoneScreen(onFinish: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.done_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Keep Focus is ready to protect your time and remind you of your dream.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(36.dp))
        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = "Go to Dashboard", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
