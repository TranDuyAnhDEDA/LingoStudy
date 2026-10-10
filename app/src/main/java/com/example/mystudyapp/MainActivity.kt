package com.example.mystudyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.mystudyapp.data.seed.FirestoreSeeder
import com.example.mystudyapp.ui.navigation.AppNavigation
import com.example.mystudyapp.ui.theme.MyStudyAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                FirestoreSeeder.seedSampleData()
            } catch (e: Exception) {
                // Ignore background seed exception if offline
            }
        }

        setContent {
            MyStudyAppTheme {
                AppNavigation()
            }
        }
    }
}
