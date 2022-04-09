package me.ftmc.common.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.ftmc.common.LocalLogger
import me.ftmc.common.Server
import me.ftmc.common.accessKeyId
import me.ftmc.common.accessKeySecret
import me.ftmc.common.saveConfig
import me.ftmc.common.serverList
import me.ftmc.common.url


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectSelectCard(connectSelectExpanded: Boolean, settingSaveUpdater: () -> Unit) {
  AnimatedVisibility(
    connectSelectExpanded,
    enter = expandIn(expandFrom = Alignment.TopCenter) + fadeIn(),
    exit = shrinkOut(shrinkTowards = Alignment.TopCenter) + fadeOut()
  ) {
    val logger = remember { LocalLogger() }
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
      LaunchedEffect(true) {
        logger.info("[ConnectSelectCard] 卡片加载")
      }
      val tempSeverList = remember { mutableStateListOf<Server>() }
      LaunchedEffect(true) {
        serverList.forEach { tempSeverList.add(it) }
      }
      Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "服务器选择", style = MaterialTheme.typography.headlineSmall)
        tempSeverList.forEach { rowServer ->
          Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(.6f)) {
              Text(text = rowServer.url, style = MaterialTheme.typography.bodySmall)
            }
            Column(modifier = Modifier.weight(.2f), horizontalAlignment = Alignment.CenterHorizontally) {
              TextButton(onClick = {
                tempSeverList.forEach { server -> server.selected = server.url == rowServer.url }
                serverList.clear()
                tempSeverList.forEach { serverList.add(it) }
                url = rowServer.url
                accessKeyId = rowServer.accessKeyId
                accessKeySecret = rowServer.accessKeySecret
                saveConfig()
                settingSaveUpdater()
                logger.debug("[ConnectSelectCard] 服务器已选择 -> ${rowServer.url}")
              }) {
                Text(text = "选择")
              }
            }
            Column(modifier = Modifier.weight(.2f), horizontalAlignment = Alignment.CenterHorizontally) {
              TextButton(onClick = {
                var removedServer: Server? = null
                tempSeverList.forEach { if (it.url == rowServer.url) removedServer = it }
                if (removedServer != null) {
                  tempSeverList.remove(removedServer)
                  serverList.clear()
                  tempSeverList.forEach { serverList.add(it) }
                  url = ""
                  accessKeyId = ""
                  accessKeySecret = ""
                  saveConfig()
                  logger.debug("[ConnectSelectCard] 服务器已删除 -> ${rowServer.url}")
                }
              }) {
                Text(text = "删除")
              }
            }
          }
        }
      }
    }
  }
}