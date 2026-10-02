package com.example.mystudyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.mystudyapp.ui.navigation.AppNavigation
import com.example.mystudyapp.ui.theme.MyStudyAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyStudyAppTheme {
                AppNavigation()
            }
        }
    }
}
