package me.ftmc.android

import android.content.ActivityNotFoundException
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.window.layout.WindowMetricsCalculator
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import java.util.UUID
import me.ftmc.android.ui.theme.AppTheme
import me.ftmc.common.App
import me.ftmc.common.LocalLogger
import me.ftmc.common.contextCacheFile
import me.ftmc.common.darkMode
import me.ftmc.common.getFileURI
import me.ftmc.common.intentSend
import me.ftmc.common.notification
import me.ftmc.common.saveWindowsSizeType
import me.ftmc.common.sharedRef

class MainActivity : AppCompatActivity() {
  var lastMonitorWorker: UUID = UUID.randomUUID()
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    getWindowWidth()
    sharedRef = this.getSharedPreferences("Client_Config", Context.MODE_PRIVATE)
    contextCacheFile = this.cacheDir
    getFileURI = { FileProvider.getUriForFile(this, "me.ftmc.ddtv_client.fileprovider", it) }
    intentSend = {
      try {
        startActivity(it)
        intent
      } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, "分享失败", Toast.LENGTH_SHORT).show()
      }
    }
    setContent {
      AppTheme(darkTheme = darkMode ?: isSystemInDarkTheme()) {
        App()
      }
    }
  }

  override fun onPause() {
    super.onPause()
    val logger = LocalLogger()
    logger.info("[Activity] onPause")
    if (notification) {
      val monitorWorker =
        OneTimeWorkRequestBuilder<MonitorWorker>().setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
          .build()
      logger.info("[Activity] 工作构建成功")
      lastMonitorWorker = monitorWorker.id
      WorkManager.getInstance(this).enqueue(monitorWorker)
      logger.info("[Activity] 工作添加成功")
    }
  }

  override fun onResume() {
    super.onResume()
    val logger = LocalLogger()
    if (notification) {
      try {
        WorkManager.getInstance(applicationContext).cancelWorkById(lastMonitorWorker)
        logger.debug("[Activity] 工作取消成功")
      } catch (_: Exception) {
      }
    }
  }

  private fun getWindowWidth() {
    val metrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(this)

    val widthDp = metrics.bounds.width() / resources.displayMetrics.density
    saveWindowsSizeType(widthDp.dp)
  }
}