package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.navigation.VMNavigation
import com.example.ui.theme.PureBlack
import com.example.ui.theme.VMManagerTheme
import com.example.ui.viewmodel.VMViewModel

class MainActivity : ComponentActivity() {

    companion object {
        init {
            try {
                System.loadLibrary("native_lib")
            } catch (e: UnsatisfiedLinkError) {
                // Graceful fallback if native library is unavailable
            }
        }
    }

    external fun stringFromJNI(): String

    private val viewModel: VMViewModel by viewModels {
        VMViewModel.Factory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VMManagerTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PureBlack),
                    color = PureBlack
                ) {
                    VMNavigation(viewModel = viewModel)
                }
            }
        }
    }
}
