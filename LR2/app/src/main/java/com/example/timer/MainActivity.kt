package com.example.timer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.example.timer.service.TimerService
import com.example.timer.ui.navigation.Routes
import com.example.timer.data.local.preferences.PreferencesManager
import com.example.timer.ui.ViewModelFactory
import com.example.timer.ui.navigation.AppNavHost
import com.example.timer.ui.theme.TimerTheme
import com.example.timer.ui.util.LocaleHelper
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    
    companion object {
        var hasPassedSplash = false
    }
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
    }
    
    override fun attachBaseContext(newBase: Context) {
        val prefs = PreferencesManager.getInstance(newBase)
        val language = runBlocking { 
            prefs.userPreferencesFlow.first().language 
        }
        super.attachBaseContext(LocaleHelper.wrapContext(newBase, language))
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val notificationSequenceId = intent.getLongExtra("EXTRA_SEQUENCE_ID", -1L)
        val hasNotificationIntent = notificationSequenceId != -1L
        
        requestNotificationPermission()
        
        setContent {
            val preferencesManager = PreferencesManager.getInstance(applicationContext)
            val userPreferences by preferencesManager.userPreferencesFlow.collectAsState(
                initial = com.example.timer.data.local.preferences.UserPreferences()
            )
            
            TimerTheme(
                darkTheme = userPreferences.isDarkTheme,
                fontSize = userPreferences.fontSize
            ) {
                val context = LocalContext.current
                val timerState by TimerService.timerState.collectAsState()
                val navController = rememberNavController()
                val viewModelFactory = ViewModelFactory(applicationContext)
                
                LaunchedEffect(Unit) {
                    if (LocaleHelper.getCurrentLocaleCode(context) != userPreferences.language.code) {
                        LocaleHelper.setLocale(context, userPreferences.language)
                    }
                }
                
                AppNavHost(
                    navController = navController,
                    viewModelFactory = viewModelFactory,
                    startDestination = when {
                        hasNotificationIntent && TimerService.isActive() -> Routes.Timer.createRoute(notificationSequenceId)
                        TimerService.isActive() -> {
                            hasPassedSplash = true
                            Routes.Timer.createRoute(timerState.sequenceId)
                        }
                        hasPassedSplash -> Routes.Main.route
                        else -> Routes.Splash.route
                    },
                    modifier = Modifier.fillMaxSize(),
                    onSplashPassed = { hasPassedSplash = true }
                )
            }
        }
    }
    
    fun recreateWithAnimation() {
        intent.removeExtra("EXTRA_SEQUENCE_ID")
        
        finish()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(
                OVERRIDE_TRANSITION_OPEN,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        startActivity(intent)
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
