package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.components.AppScreen
import com.example.ui.components.FogOfWarMap
import com.example.ui.components.LoveMapBottomBar
import com.example.ui.screens.AddMemoryScreen
import com.example.ui.screens.AllMemoriesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoveCounterScreen
import com.example.ui.screens.MemoryDetailScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TimelineScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.LoveMapTheme
import com.example.ui.viewmodel.LoveMapViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LoveMapViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoveMapTheme {
                LoveMapApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun LoveMapApp(viewModel: LoveMapViewModel) {
    // Navigation Screen State
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    var previousScreen by remember { mutableStateOf(AppScreen.HOME) }

    // Coordinates for passing to AddMemory
    var addMemoryLat by remember { mutableStateOf<Double?>(null) }
    var addMemoryLng by remember { mutableStateOf<Double?>(null) }
    var addMemoryPlaceName by remember { mutableStateOf<String?>(null) }

    val memories by viewModel.memories.collectAsState()
    val selectedMemory by viewModel.selectedMemory.collectAsState()
    val targetMapCoordinate by viewModel.targetMapLocation.collectAsState()

    // Handle system back navigation
    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        when (currentScreen) {
            AppScreen.MEMORY_DETAIL -> {
                viewModel.selectMemory(null)
                currentScreen = previousScreen
            }
            AppScreen.ADD_MEMORY -> {
                currentScreen = AppScreen.MAP
            }
            AppScreen.COUNTER, AppScreen.ALL_MEMORIES, AppScreen.SETTINGS, AppScreen.MAP, AppScreen.TIMELINE, AppScreen.WELCOME -> {
                currentScreen = AppScreen.HOME
            }
            else -> {
                currentScreen = AppScreen.HOME
            }
        }
    }

    val showBottomBar = currentScreen in listOf(
        AppScreen.HOME,
        AppScreen.MAP,
        AppScreen.TIMELINE,
        AppScreen.SETTINGS
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                LoveMapBottomBar(
                    currentScreen = currentScreen,
                    onNavigate = { screen ->
                        if (screen == AppScreen.ADD_MEMORY) {
                            addMemoryLat = null
                            addMemoryLng = null
                            addMemoryPlaceName = null
                        }
                        previousScreen = currentScreen
                        currentScreen = screen
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    AppScreen.HOME -> {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigate = { nextScreen ->
                                previousScreen = currentScreen
                                currentScreen = nextScreen
                            },
                            onSelectMemory = { memory ->
                                viewModel.selectMemory(memory)
                                previousScreen = AppScreen.HOME
                                currentScreen = AppScreen.MEMORY_DETAIL
                            }
                        )
                    }

                    AppScreen.MAP -> {
                        FogOfWarMap(
                            memories = memories,
                            targetCoordinate = targetMapCoordinate,
                            selectedMemory = selectedMemory,
                            onSelectMemory = { memory ->
                                viewModel.selectMemory(memory)
                            },
                            onContinueToForm = { lat, lng, placeName ->
                                addMemoryLat = lat
                                addMemoryLng = lng
                                addMemoryPlaceName = placeName
                                previousScreen = AppScreen.MAP
                                currentScreen = AppScreen.ADD_MEMORY
                            },
                            onViewMemoryDetail = { memory ->
                                viewModel.selectMemory(memory)
                                previousScreen = AppScreen.MAP
                                currentScreen = AppScreen.MEMORY_DETAIL
                            }
                        )
                    }

                    AppScreen.ADD_MEMORY -> {
                        AddMemoryScreen(
                            viewModel = viewModel,
                            initialLat = addMemoryLat,
                            initialLng = addMemoryLng,
                            initialPlaceName = addMemoryPlaceName,
                            onBack = {
                                currentScreen = previousScreen
                            },
                            onSaved = { id ->
                                currentScreen = AppScreen.MAP
                            },
                            onPickOnMap = {
                                previousScreen = AppScreen.ADD_MEMORY
                                currentScreen = AppScreen.MAP
                            }
                        )
                    }

                    AppScreen.MEMORY_DETAIL -> {
                        val memory = selectedMemory ?: memories.firstOrNull()
                        if (memory != null) {
                            MemoryDetailScreen(
                                memory = memory,
                                viewModel = viewModel,
                                onBack = {
                                    viewModel.selectMemory(null)
                                    currentScreen = previousScreen
                                },
                                onViewOnMap = { lat, lng ->
                                    viewModel.setTargetMapLocation(lat, lng)
                                    previousScreen = AppScreen.MEMORY_DETAIL
                                    currentScreen = AppScreen.MAP
                                }
                            )
                        } else {
                            currentScreen = AppScreen.HOME
                        }
                    }

                    AppScreen.COUNTER -> {
                        LoveCounterScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = AppScreen.HOME }
                        )
                    }

                    AppScreen.TIMELINE -> {
                        TimelineScreen(
                            viewModel = viewModel,
                            onSelectMemory = { memory ->
                                viewModel.selectMemory(memory)
                                previousScreen = AppScreen.TIMELINE
                                currentScreen = AppScreen.MEMORY_DETAIL
                            },
                            onNavigateToMap = { lat, lng ->
                                viewModel.setTargetMapLocation(lat, lng)
                                previousScreen = AppScreen.TIMELINE
                                currentScreen = AppScreen.MAP
                            }
                        )
                    }

                    AppScreen.ALL_MEMORIES -> {
                        AllMemoriesScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = AppScreen.HOME },
                            onSelectMemory = { memory ->
                                viewModel.selectMemory(memory)
                                previousScreen = AppScreen.ALL_MEMORIES
                                currentScreen = AppScreen.MEMORY_DETAIL
                            }
                        )
                    }

                    AppScreen.SETTINGS -> {
                        SettingsScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = AppScreen.HOME },
                            onNavigateToWelcome = {
                                previousScreen = AppScreen.SETTINGS
                                currentScreen = AppScreen.WELCOME
                            }
                        )
                    }

                    AppScreen.WELCOME -> {
                        WelcomeScreen(
                            onStart = {
                                currentScreen = AppScreen.HOME
                            }
                        )
                    }
                }
            }
        }
    }
}
