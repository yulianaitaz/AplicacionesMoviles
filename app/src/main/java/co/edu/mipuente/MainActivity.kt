package co.edu.mipuente

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import co.edu.mipuente.ui.MiPuenteApp
import co.edu.mipuente.ui.theme.MiPuenteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiPuenteTheme {
                MiPuenteApp()
            }
        }
    }
}
