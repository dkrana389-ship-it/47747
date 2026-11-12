package com.keepfocus.app.ui.popup

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.keepfocus.app.R
import com.keepfocus.app.ui.theme.KeepFocusTheme
import com.keepfocus.app.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.io.File

@AndroidEntryPoint
class MotivationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        triggerSoftHaptic()
        val sessionReels = intent.getIntExtra("session_reels", 30)

        setContent {
            KeepFocusTheme {
                val viewModel: MainViewModel = hiltViewModel()
                val profileState by viewModel.userProfile.collectAsState()
                val profile = profileState

                val headings = listOf(
                    "Pause & Breathe",
                    "Eyes on Your Dream",
                    "Time Check",
                    "Your Future Needs You"
                )
                val heading = remember { headings.random() }

                var secondsRemaining by remember { mutableIntStateOf(5) }
                LaunchedEffect(Unit) {
                    while (secondsRemaining > 0) {
                        delay(1000L)
                        secondsRemaining--
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xE60F172A))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = heading.uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Profile Avatar or Initials
                            val photoPath = profile?.photoPath.orEmpty()
                            val hasPhoto = photoPath.isNotEmpty() && File(photoPath).exists()
                            if (hasPhoto) {
                                AsyncImage(
                                    model = File(photoPath),
                                    contentDescription = "Profile Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                val initial = (profile?.nickname?.firstOrNull()
                                    ?: profile?.fullName?.firstOrNull()
                                    ?: 'F').uppercaseChar()
                                Box(
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initial.toString(),
                                        fontSize = 44.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = ""${profile?.dreamLine ?: "Stay focused on what truly matters."}"",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            val displayNick = profile?.nickname?.ifEmpty { "Friend" } ?: "Friend"
                            Text(
                                text = "Hey $displayNick, remember why you started.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "You just watched $sessionReels reels in a row.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            Button(
                                onClick = {
                                    viewModel.recordCloseAppClicked()
                                    val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                        addCategory(Intent.CATEGORY_HOME)
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    startActivity(homeIntent)
                                    finish()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.close_app),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = { finish() },
                                enabled = secondsRemaining == 0,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = if (secondsRemaining > 0) {
                                        stringResource(R.string.give_5_mins, secondsRemaining)
                                    } else {
                                        stringResource(R.string.give_5_mins_ready)
                                    },
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun triggerSoftHaptic() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = getSystemService(VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator?.vibrate(VibrationEffect.createOneShot(120L, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (_: Exception) {
        }
    }
}
