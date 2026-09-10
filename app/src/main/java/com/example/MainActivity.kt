package com.example

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainScreen
import com.example.ui.MainViewModel
import com.example.ui.theme.AppThemePackage
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ThemePreferences

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply native XML theme for window background and system bars
        setTheme(R.style.Theme_Deklinatio_Schiefer)

        super.onCreate(savedInstanceState)

        // Force hardware accelerated window rendering
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )

        // Enforce maximum display refresh rate (144Hz / 120Hz synchronization)
        configureMaxRefreshRate()

        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val isSystemDark = isSystemInDarkTheme()
            val isDark = when (uiState.themeMode.lowercase()) {
                "light", "hell" -> false
                "dark", "dunkel" -> true
                else -> isSystemDark
            }
            MyApplicationTheme(
                selectedTheme = uiState.selectedTheme,
                darkTheme = isDark
            ) {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    private fun configureMaxRefreshRate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val display = display
                val maxRefreshMode = display?.supportedModes?.maxByOrNull { it.refreshRate }
                if (maxRefreshMode != null) {
                    val layoutParams = window.attributes
                    layoutParams.preferredDisplayModeId = maxRefreshMode.modeId
                    window.attributes = layoutParams
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                @Suppress("DEPRECATION")
                val windowManager = getSystemService(WINDOW_SERVICE) as? WindowManager
                @Suppress("DEPRECATION")
                val display = windowManager?.defaultDisplay
                val maxRefreshMode = display?.supportedModes?.maxByOrNull { it.refreshRate }
                if (maxRefreshMode != null) {
                    val layoutParams = window.attributes
                    layoutParams.preferredDisplayModeId = maxRefreshMode.modeId
                    window.attributes = layoutParams
                }
            }
        } catch (_: Exception) {
            // Graceful fallback if device restricts display mode adjustments
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Android") }
}
