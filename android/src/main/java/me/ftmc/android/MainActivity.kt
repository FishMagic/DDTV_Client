package me.ftmc.android

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.window.layout.WindowMetricsCalculator
import me.ftmc.android.ui.theme.AppTheme
import me.ftmc.common.App
import me.ftmc.common.darkMode
import me.ftmc.common.saveWindowsSizeType
import me.ftmc.common.sharedRef

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    getWindowWidth()
    sharedRef = this.getSharedPreferences("Client_Config",Context.MODE_PRIVATE)
    setContent {
      AppTheme(darkTheme = darkMode ?: isSystemInDarkTheme()) {
        App()
      }
    }
  }

  private fun getWindowWidth() {
    val metrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(this)

    val widthDp = metrics.bounds.width() / resources.displayMetrics.density
    saveWindowsSizeType(widthDp.dp)
  }
}