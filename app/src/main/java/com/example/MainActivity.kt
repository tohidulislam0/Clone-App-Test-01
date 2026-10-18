package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.TwinSpaceApp
import com.example.ui.TwinSpaceViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  private val viewModel: TwinSpaceViewModel by viewModels {
    TwinSpaceViewModel.provideFactory(application)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    handleLaunchShortcutIntent(intent)

    setContent {
      MyApplicationTheme(darkTheme = true) {
        TwinSpaceApp(viewModel = viewModel)
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    handleLaunchShortcutIntent(intent)
  }

  private fun handleLaunchShortcutIntent(intent: Intent?) {
    val instanceId = intent?.getLongExtra("EXTRA_LAUNCH_INSTANCE_ID", -1L) ?: -1L
    if (instanceId > 0L) {
      viewModel.handleDeepLinkInstanceId(instanceId)
    }
  }
}
