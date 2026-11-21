package cl.comunidad.conectada

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.comunidad.conectada.ui.*

class MainActivity : ComponentActivity() {
    private val model: CommunityViewModel by viewModels()
    private var openAlerts by mutableStateOf(false)
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            model.notifyPermission(it)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        openAlerts = intent.getBooleanExtra("openAlerts", false)
        setContent {
            val state by model.state.collectAsStateWithLifecycle()
            CommunityTheme {
                CommunityApp(
                    state, model, openAlerts, onAlertsOpened = { openAlerts = false },
                    onNotificationPermission = {
                        if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        else model.notifyPermission(true)
                    })
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        openAlerts = intent.getBooleanExtra("openAlerts", false)
    }

    override fun onStart() {
        super.onStart(); model.setForeground(true)
    }

    override fun onStop() {
        model.setForeground(false); super.onStop()
    }
}
