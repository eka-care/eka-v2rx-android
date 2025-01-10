package com.eka.voice2rx_sdk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eka.voice2rx_sdk.sdkinit.Voice2RxInit
import com.eka.voice2rx_sdk.sdkinit.Voice2RxInitConfig
import com.eka.voice2rx_sdk.ui.theme.VADSampleProjectTheme

class MainActivity : ComponentActivity() {
    private val viewModel: V2RxViewModel by viewModels()
    private lateinit var combiner: AudioCombiner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        combiner = AudioCombiner()
        val voice2Rx = Voice2RxInit.getVoice2RxInitConfiguration()

        setContent {
            VADSampleProjectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    val navController = rememberNavController()
                    var mode by remember { mutableStateOf("dictation") }

                    NavHost(navController = navController, startDestination = "mode_selection") {
                        composable("greeting") {
                            Greeting(
                                name = "Android",
                                modifier = Modifier.padding(innerPadding),
                                viewModel = viewModel,
                                combiner = combiner,
                                voice2RxConfig = voice2Rx,
                                mode = mode
                            )
                        }
                        composable("mode_selection") {
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(
    name: String,
    modifier: Modifier = Modifier,
    viewModel: V2RxViewModel,
    combiner: AudioCombiner,
    voice2RxConfig: Voice2RxInitConfig,
    mode : String
) {
    val recordingState by viewModel.recordingState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
    }
}