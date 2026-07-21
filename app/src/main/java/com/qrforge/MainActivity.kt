package com.qrforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.qrforge.ui.theme.QRForgeTheme
import com.qrforge.ui.navigation.QRForgeNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            QRForgeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = QRForgeTheme.colors.background0
                ) {
                    QRForgeNavGraph()
                }
            }
        }
    }
}
