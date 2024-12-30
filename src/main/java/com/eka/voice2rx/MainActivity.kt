package com.eka.voice2rx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.eka.voice2rx.sdkinit.Voice2RxInit
import com.eka.voice2rx.sdkinit.Voice2RxInitConfig
import com.eka.voice2rx.ui.theme.VADSampleProjectTheme

class MainActivity : ComponentActivity() {
    private val viewModel: VADViewModel by viewModels()
    private lateinit var combiner: AudioCombiner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        combiner = AudioCombiner()
        val voice2Rx = Voice2RxInit.getVoice2RxInitConfiguration()

        setContent {
            VADSampleProjectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    voice2Rx.voice2RxScreen.invoke(
                        onStart = {

                        },
                        onStop = {

                        }
                    )


                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding),
                        viewModel = viewModel,
                        combiner = combiner,
                        voice2RxConfig = voice2Rx
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(
    name: String,
    modifier: Modifier = Modifier,
    viewModel: VADViewModel,
    combiner: AudioCombiner,
    voice2RxConfig: Voice2RxInitConfig
) {
    val recordingResponse by viewModel.recordingResponse.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
//        viewModel.startRecording()
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = recordingResponse)
        Button(onClick = {
            viewModel.startRecording()
        }) {
            Text(text = "Play")
        }
        Button(onClick = {
            viewModel.stopRecording()
        }) {
            Text(text = "Stop")
        }
    }
}