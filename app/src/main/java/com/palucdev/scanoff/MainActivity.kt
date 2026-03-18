package com.palucdev.scanoff

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.palucdev.scanoff.navigation.AppNavHost
import com.palucdev.scanoff.ui.theme.ScanOffTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            ScanOffTheme {
                val navController = rememberNavController()
                AppNavHost(navController = navController)
            }
        }
    }
}
