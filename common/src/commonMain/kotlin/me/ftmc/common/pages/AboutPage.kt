package me.ftmc.common.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.client.request.get
import me.ftmc.common.LocalLogger
import me.ftmc.common.backend.httpClient
import me.ftmc.common.currentScreenWidth
import me.ftmc.common.navigationBarsHeightModifier
import me.ftmc.common.screenTypeChangeWidth

private const val version = "1.2.0 Alpha"
private const val versionCode = 3
private const val updateCheckURL = "https://fishmagic.github.io/DDTV_Updater/releases/client/version"

@Composable
fun AboutPage() {
  Row(
    modifier = Modifier.fillMaxSize().padding(16.dp),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      var updateCheckString by remember { mutableStateOf("正在检查更新") }
      Text(text = "非官方", style = MaterialTheme.typography.headlineSmall)
      Text(text = "DDTV客户端", style = MaterialTheme.typography.headlineLarge)
      Text(text = "基于官方WEB API", style = MaterialTheme.typography.headlineMedium)
      Text(text = "Version: $version", style = MaterialTheme.typography.bodyLarge)
      Text(text = "由 Laevatein Scarlet (FishMagic) 编译", style = MaterialTheme.typography.bodySmall)
      Text(text = "发行版使用 CC-BY-NC-SA 3.0 许可证发行", style = MaterialTheme.typography.bodySmall)
      Spacer(Modifier.height(16.dp))
      Text(text = updateCheckString)
      LaunchedEffect(true) {
        updateCheckString = try {
          val remoteVersionCode: Int = httpClient.get(updateCheckURL)
          if (remoteVersionCode > versionCode) {
            "发现新版本"
          } else {
            "已是最新版本"
          }
        } catch (e: Exception) {
          val logger = LocalLogger()
          logger.errorCatch(e)
          "检查版本更新出错"
        }
      }
      if (currentScreenWidth >= screenTypeChangeWidth) {
        Spacer(Modifier.height(120.dp))
      } else {
        Spacer(Modifier.height(90.dp))
        Spacer(Modifier.navigationBarsHeightModifier())
      }
      if (currentScreenWidth >= screenTypeChangeWidth) {
        Spacer(Modifier.width(80.dp))
      }
    }
  }
}