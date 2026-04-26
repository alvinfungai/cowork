package com.alvinfungai.cowork

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.alvinfungai.app.core.ThemePreferences
import com.alvinfungai.cowork.navigation.AuthNavGraph
import com.alvinfungai.cowork.navigation.MainNavGraph
import com.alvinfungai.cowork.ui.theme.CoworkTheme
import com.alvinfungai.providers.domain.usecase.UpdateProviderLastActiveUseCase
import com.alvinfungai.users.domain.usecase.UpdateLastActiveUseCase
import com.google.android.gms.maps.MapsInitializer
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var updateLastActiveUseCase: UpdateLastActiveUseCase

    @Inject
    lateinit var updateProviderLastActiveUseCase: UpdateProviderLastActiveUseCase

    @Inject
    lateinit var themePreferences: ThemePreferences

    private val activityScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST) { renderer ->
            when (renderer) {
                MapsInitializer.Renderer.LATEST -> Log.d("MAPS", "The latest renderer is in use.")
                MapsInitializer.Renderer.LEGACY -> Log.d("MAPS", "The legacy renderer is in use.")
            }
        }

        enableEdgeToEdge()
        setContent {
            val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = false)

            CoworkTheme(darkTheme = isDarkMode) {
                NotificationPermissionEffect()

                AppLifecycleObserver(onAppForegrounded = {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    currentUser?.uid?.let { uid ->
                        activityScope.launch {
                            launch {
                                updateLastActiveUseCase(uid).collect { result ->
                                    Log.d("COWORK_DEBUG", "Firestore last active update success: ${result.isSuccess}")
                                }
                            }
                            launch {
                                updateProviderLastActiveUseCase(uid).collect { result ->
                                    Log.d("COWORK_DEBUG", "Supabase provider last active update success: ${result.isSuccess}")
                                }
                            }
                        }
                    }
                })

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
                    
                    val startDestination = remember {
                        if (FirebaseAuth.getInstance().currentUser != null) {
                            MainNavGraph.NavGraph
                        } else {
                            AuthNavGraph.Destination.Root
                        }
                    }

                    LaunchedEffect(intent) {
                        handleNotificationIntent(navController, intent)
                    }

                    RootNavHost(navController = navController, startDestination = startDestination)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun handleNotificationIntent(navController: NavHostController, intent: Intent?) {
        if (intent?.getStringExtra("NAVIGATE_TO") == "WORK_REQUESTS") {
            navController.navigate(MainNavGraph.Destination.ManageWorkRequests) {
                launchSingleTop = true
                popUpTo(MainNavGraph.NavGraph) {
                    saveState = true
                }
                restoreState = true
            }
            intent.removeExtra("NAVIGATE_TO")
        }
    }

    @Composable
    private fun NotificationPermissionEffect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val context = LocalContext.current
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    Log.d("FCM", "Notification permission granted")
                } else {
                    Log.d("FCM", "Notification permission denied")
                }
            }

            LaunchedEffect(Unit) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

                if (!hasPermission) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    @Composable
    fun AppLifecycleObserver(onAppForegrounded: () -> Unit) {
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_START) {
                    onAppForegrounded()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }

    @Composable
    fun RootNavHost(
        modifier: Modifier = Modifier,
        navController: NavHostController,
        startDestination: Any
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            AuthNavGraph.build(Modifier, navController, this)
            MainNavGraph.build(Modifier, navController, this)
        }
    }
}
