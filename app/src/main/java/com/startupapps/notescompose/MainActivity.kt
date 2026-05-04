package com.startupapps.notescompose

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.arkivanov.decompose.defaultComponentContext
import com.google.android.gms.ads.MobileAds
import com.startupapps.notescompose.app.root.component.DefaultRootComponent
import com.startupapps.notescompose.app.root.ui.RootScreen
import com.startupapps.notescompose.ui.theme.NotesComposeTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MobileAds.initialize(this)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }


        val appGraph = (application as NotesApp).appGraph


        val root = DefaultRootComponent(
            componentContext = defaultComponentContext(),
            appGraph = appGraph
        )

        enableEdgeToEdge()

        setContent {
            NotesComposeTheme {
                RootScreen(root = root)
            }
        }
    }
}