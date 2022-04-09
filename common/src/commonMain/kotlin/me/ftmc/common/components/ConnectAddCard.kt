package me.ftmc.common.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
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
fun ConnectAddCard(connectAddExpanded: Boolean, settingSaveUpdater: () -> Unit) {
  AnimatedVisibility(
    connectAddExpanded,
    enter = expandIn(expandFrom = Alignment.TopCenter) + fadeIn(),
    exit = shrinkOut(shrinkTowards = Alignment.TopCenter) + fadeOut()
  ) {
    val logger = remember { LocalLogger() }
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
      LaunchedEffect(true) { logger.info("[ConnectAddCard] 卡片加载") }
      var tempURL by remember { mutableStateOf("") }
      var tempAccessKeyId by remember { mutableStateOf("") }
      var tempAccessKeySecret by remember { mutableStateOf("") }
      Column(modifier = Modifier.padding(16.dp)) {
        var serverExist by remember { mutableStateOf(false) }
        Text(text = "服务器添加", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
          value = tempURL,
          onValueChange = {
            tempURL = it
            for (server in serverList) {
              if (server.url == it) {
                serverExist = true
                logger.warn("检测到重复服务器")
                return@OutlinedTextField
              }
              serverExist = false
            }
          },
          placeholder = { androidx.compose.material.Text(text = "包含 http:// 或 https:// 部分") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
          label = { androidx.compose.material.Text(text = "服务器地址") },
          isError = serverExist
        )
        OutlinedTextField(value = tempAccessKeyId,
          onValueChange = { tempAccessKeyId = it },
          label = { androidx.compose.material.Text(text = "AccessKeyId") })
        OutlinedTextField(value = tempAccessKeySecret,
          onValueChange = { tempAccessKeySecret = it },
          label = { androidx.compose.material.Text(text = "AccessKeySecret") })
        TextButton(onClick = {
          serverList.forEach { it.selected = false }
          serverList.add(Server(tempURL, tempAccessKeyId, tempAccessKeySecret, true))
          url = tempURL
          accessKeyId = tempAccessKeyId
          accessKeySecret = tempAccessKeySecret
          saveConfig()
          settingSaveUpdater()
          logger.info("[ConnectAddCard] 配置保存成功")
        }, enabled = !serverExist) {
          Text(text = "保存")
        }
      }
    }
  }
}