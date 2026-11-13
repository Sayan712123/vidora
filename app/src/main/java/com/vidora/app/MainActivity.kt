package com.vidora.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vidora.app.data.AppGraph
import com.vidora.app.ui.VidoraRoot
import com.vidora.app.ui.theme.VidoraTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // themeMode is a snapshot state property, so switching it in Settings
            // recomposes every screen with the new palette immediately.
            VidoraTheme(mode = AppGraph.store.themeMode) {
                VidoraRoot()
            }
        }
    }
}
