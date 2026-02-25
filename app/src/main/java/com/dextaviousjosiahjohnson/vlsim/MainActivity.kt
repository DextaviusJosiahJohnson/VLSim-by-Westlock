package com.dextaviousjosiahjohnson.vlsim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.dextaviousjosiahjohnson.vlsim.data.VlsmDatabase
import com.dextaviousjosiahjohnson.vlsim.ui.MainScreen
import com.dextaviousjosiahjohnson.vlsim.ui.VlsmViewModel
import com.dextaviousjosiahjohnson.vlsim.ui.VlsmViewModelFactory
import com.dextaviousjosiahjohnson.vlsim.ui.theme.ThemeState
import com.dextaviousjosiahjohnson.vlsim.ui.theme.VLSimTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use the Singleton Database Provider
        val db = VlsmDatabase.getDatabase(this)
        val dao = db.calculationDao()

        // Initialize ViewModel using the Factory
        val viewModel: VlsmViewModel by viewModels { VlsmViewModelFactory(dao) }

        setContent {
            val themeState = remember { mutableStateOf(ThemeState()) }

            VLSimTheme(themeState = themeState.value) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = viewModel, themeState = themeState)
                }
            }
        }
    }
}