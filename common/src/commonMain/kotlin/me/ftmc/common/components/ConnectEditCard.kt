package me.ftmc.common.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import java.net.URL
import java.util.UUID
import me.ftmc.common.LocalLogger
import me.ftmc.common.Server
import me.ftmc.common.globalConfigObject
import me.ftmc.common.saveConfig
import me.ftmc.common.selectedServer
import me.ftmc.common.selectedServerName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectEditCard(connectEditExpanded: Boolean, editingUUID: String? = null, settingSaveUpdater: () -> Unit) {
  AnimatedVisibility(
    connectEditExpanded,
    enter = expandIn(expandFrom = Alignment.TopCenter) + fadeIn(),
    exit = shrinkOut(shrinkTowards = Alignment.TopCenter) + fadeOut()
  ) {
    val logger = remember { LocalLogger() }
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
      LaunchedEffect(true) { logger.info("[ConnectAddCard] 卡片加载") }
      val tempUUID = remember { editingUUID ?: UUID.randomUUID().toString() }
      val editServer = remember { globalConfigObject.serverListWithID[tempUUID]?.copy() ?: Server() }
      var tempName by remember { mutableStateOf(editServer.name) }
      var tempURL by remember { mutableStateOf(editServer.url) }
      var tempAccessKeyId by remember { mutableStateOf(editServer.accessKeyId) }
      var tempAccessKeySecret by remember { mutableStateOf(editServer.accessKeySecret) }
      Column(modifier = Modifier.padding(16.dp)) {
        var nameCannotUse by remember { mutableStateOf(false) }
        var serverExist by remember { mutableStateOf(false) }
        var serverURLError by remember { mutableStateOf(false) }
        Text(text = "服务器添加", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = tempName, onValueChange = {
          tempName = it
          editServer.name = it
          nameCannotUse = it == "无服务器"
        }, label = { androidx.compose.material.Text(text = "服务器名称") }, isError = nameCannotUse
        )
        OutlinedTextField(
          value = tempURL,
          onValueChange = {
            tempURL = it
            editServer.url = it
            serverExist = editServer in globalConfigObject.serverListWithID.values
            serverURLError = try {
              URL(tempURL)
              !(tempURL.startsWith("http://") || tempURL.startsWith("https://")) ||
                  (tempURL.endsWith("/") || tempURL.endsWith("\\"))
            } catch (_: Exception) {
              true
            }
          },
          placeholder = { androidx.compose.material.Text(text = "包含 http:// 或 https:// 部分") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
          label = { androidx.compose.material.Text(text = "服务器地址") },
          isError = serverExist || serverURLError
        )
        OutlinedTextField(value = tempAccessKeyId, onValueChange = {
          tempAccessKeyId = it
          editServer.accessKeyId = it
        }, label = { androidx.compose.material.Text(text = "AccessKeyId") })
        OutlinedTextField(value = tempAccessKeySecret, onValueChange = {
          tempAccessKeySecret = it
          editServer.accessKeySecret = it
        }, label = { androidx.compose.material.Text(text = "AccessKeySecret") })
        Row {
          TextButton(onClick = {
            globalConfigObject.serverListWithID[tempUUID] = editServer
            if (selectedServer == editServer) {
              selectedServerName = tempName
            }
            saveConfig()
            settingSaveUpdater()
            logger.info("[ConnectAddCard] 配置保存成功")
          }, enabled = !(nameCannotUse || serverExist || serverURLError)) {
            Text(text = "保存")
          }
          TextButton(onClick = {
            globalConfigObject.serverListWithID[tempUUID] = editServer
            selectedServer = editServer
            globalConfigObject.selectedUUID = tempUUID
            selectedServerName = tempName
            saveConfig()
            settingSaveUpdater()
            logger.info("[ConnectAddCard] 配置保存成功")
          }, enabled = !(nameCannotUse || serverExist)) {
            Text(text = "保存并选中")
          }
        }
      }
    }
  }
}