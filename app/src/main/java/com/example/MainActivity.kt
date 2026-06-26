package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.GoalStreamApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodels.GoalStreamViewModel
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize the Mobile Ads SDK
        MobileAds.initialize(this) {}

        setContent {
            MyApplicationTheme {
                val viewModel: GoalStreamViewModel = viewModel()
                GoalStreamApp(viewModel = viewModel)
            }
        }
    }
}
