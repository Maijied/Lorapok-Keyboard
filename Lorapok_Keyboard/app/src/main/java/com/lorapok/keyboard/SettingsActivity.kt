package com.lorapok.keyboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lorapok.keyboard.ui.theme.LorapokKeyboardTheme

/**
 * Lorapok Keyboard — Settings Activity
 *
 * Compose-based settings screen providing user-customizable options:
 * - Default language (Bengali / English)
 * - Dark / Light theme
 * - Haptic feedback toggle
 * - Key click sound toggle
 * - Autocorrect toggle
 * - About section
 *
 * All preferences are stored in SharedPreferences("lorapok_settings").
 */
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LorapokKeyboardTheme {
                SettingsScreen(onBack = { finish() })
            }
        }
    }
}

object KeyboardPrefs {
    const val PREFS_NAME = "lorapok_settings"
    const val KEY_DEFAULT_BENGALI = "default_bengali"
    const val KEY_DARK_THEME = "dark_theme"
    const val KEY_HAPTIC_ENABLED = "haptic_enabled"
    const val KEY_SOUND_ENABLED = "sound_enabled"
    const val KEY_AUTOCORRECT_ENABLED = "autocorrect_enabled"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences(KeyboardPrefs.PREFS_NAME, android.content.Context.MODE_PRIVATE)
    }

    var defaultBengali by remember { mutableStateOf(prefs.getBoolean(KeyboardPrefs.KEY_DEFAULT_BENGALI, true)) }
    var darkTheme by remember { mutableStateOf(prefs.getBoolean(KeyboardPrefs.KEY_DARK_THEME, true)) }
    var hapticEnabled by remember { mutableStateOf(prefs.getBoolean(KeyboardPrefs.KEY_HAPTIC_ENABLED, true)) }
    var soundEnabled by remember { mutableStateOf(prefs.getBoolean(KeyboardPrefs.KEY_SOUND_ENABLED, true)) }
    var autocorrectEnabled by remember { mutableStateOf(prefs.getBoolean(KeyboardPrefs.KEY_AUTOCORRECT_ENABLED, true)) }

    fun savePref(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    val accentIndigo = Color(0xFF6366F1)
    val bgSlate = Color(0xFF0F172A)
    val bgGradient = Brush.verticalGradient(
        colors = listOf(bgSlate, Color(0xFF1E293B), Color(0xFF334155))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top bar
            TopAppBar(
                title = {
                    Text(
                        "⚙️ সেটিংস",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // ── Language Section ──
                SettingsSectionHeader("🔤 ভাষা (Language)")

                SettingsToggleItem(
                    title = "ডিফল্ট বাংলা",
                    subtitle = "Default language: ${if (defaultBengali) "বাংলা" else "English"}",
                    checked = defaultBengali,
                    accentColor = accentIndigo,
                    onCheckedChange = {
                        defaultBengali = it
                        savePref(KeyboardPrefs.KEY_DEFAULT_BENGALI, it)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── Appearance Section ──
                SettingsSectionHeader("🎨 থিম (Theme)")

                SettingsToggleItem(
                    title = "ডার্ক থিম",
                    subtitle = "Current: ${if (darkTheme) "Dark Slate/Indigo" else "Light"}",
                    checked = darkTheme,
                    accentColor = accentIndigo,
                    onCheckedChange = {
                        darkTheme = it
                        savePref(KeyboardPrefs.KEY_DARK_THEME, it)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── Input Section ──
                SettingsSectionHeader("⌨️ ইনপুট (Input)")

                SettingsToggleItem(
                    title = "হ্যাপটিক ফিডব্যাক",
                    subtitle = "Vibrate on key press",
                    checked = hapticEnabled,
                    accentColor = accentIndigo,
                    onCheckedChange = {
                        hapticEnabled = it
                        savePref(KeyboardPrefs.KEY_HAPTIC_ENABLED, it)
                    }
                )

                SettingsToggleItem(
                    title = "কী ক্লিক সাউন্ড",
                    subtitle = "Sound on key press",
                    checked = soundEnabled,
                    accentColor = accentIndigo,
                    onCheckedChange = {
                        soundEnabled = it
                        savePref(KeyboardPrefs.KEY_SOUND_ENABLED, it)
                    }
                )

                SettingsToggleItem(
                    title = "অটোকারেক্ট",
                    subtitle = "Auto-correct common mistakes",
                    checked = autocorrectEnabled,
                    accentColor = accentIndigo,
                    onCheckedChange = {
                        autocorrectEnabled = it
                        savePref(KeyboardPrefs.KEY_AUTOCORRECT_ENABLED, it)
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ── About Section ──
                SettingsSectionHeader("ℹ️ সম্পর্কে (About)")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.08f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("⌨️", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Lorapok Keyboard",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "v1.0.0",
                            fontSize = 14.sp,
                            color = accentIndigo
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "AI-Powered Bengali Keyboard\nwith Contextual Tone Adaptation",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "268 million Bengali speakers worldwide 🇧🇩",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF6366F1),
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingsToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    accentColor: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = accentColor,
                    uncheckedThumbColor = Color.White.copy(alpha = 0.7f),
                    uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
                )
            )
        }
    }
}
