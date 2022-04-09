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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import me.ftmc.common.ConnectStatus
import me.ftmc.common.LocalLogger
import me.ftmc.common.backend.APIError
import me.ftmc.common.backend.APIErrorType
import me.ftmc.common.backend.systemInfoFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectStatusCard(
  apiUsable: Boolean,
  connectStatus: ConnectStatus,
  connectAddExpanded: Boolean,
  connectSelectExpanded: Boolean,
  apiUsableUpdater: (Boolean) -> Unit,
  connectStatusUpdater: (ConnectStatus) -> Unit,
  connectAddExpandedUpdater: (Boolean) -> Unit,
  connectSelectExpandedUpdater: (Boolean) -> Unit
) {
  val logger = remember { LocalLogger() }
  OutlinedCard(modifier = Modifier.fillMaxWidth()) {
    LaunchedEffect(true) {
      logger.info("[ConnectStatusCard] 卡片加载")
    }
    var ddtvCoreVersion by remember { mutableStateOf("") }
    var webCoreVersion by remember { mutableStateOf("") }
    var dotnetVersion by remember { mutableStateOf("") }
    LaunchedEffect(apiUsable) {
      systemInfoFlow.catch {
        if (it is APIError) {
          connectStatusUpdater(
            when (it.errorType) {
              APIErrorType.SERVER_CONFIG_NULL -> ConnectStatus.DISCONNECT
              APIErrorType.NETWORK_CONNECT_FAILED -> ConnectStatus.NET_ERROR
              APIErrorType.API_NOT_FOUND -> ConnectStatus.UNKNOWN_ERROR
              APIErrorType.SEVER_INTERNAL_ERROR -> ConnectStatus.UNKNOWN_ERROR
              APIErrorType.UNKNOWN_ERROR -> ConnectStatus.UNKNOWN_ERROR
              APIErrorType.COOKIE_TIMEOUT -> ConnectStatus.COOKIE_TIMEOUT
              APIErrorType.LOGIN_FAILED -> ConnectStatus.LOGIN_FAILED
              APIErrorType.SIG_FAILED -> ConnectStatus.SIG_FAILED
              APIErrorType.CMD_FAILED -> ConnectStatus.CMD_FAILED
            }
          )
        }
        apiUsableUpdater(false)
      }.collect {
        ddtvCoreVersion = it.DDTVCore_Ver
        webCoreVersion = it.os_Info.WebCore_Ver
        dotnetVersion = it.os_Info.AppCore_Ver
        connectStatusUpdater(ConnectStatus.CONNECT)
        logger.debug("[ConnectStatusCard] 心跳响应成功")
      }
    }
    Column(modifier = Modifier.padding(16.dp)) {
      Text(text = "服务器信息", style = MaterialTheme.typography.headlineSmall)
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text("连接状态：${connectStatus.statusString}", style = MaterialTheme.typography.bodySmall)
        AnimatedVisibility(
          connectStatus != ConnectStatus.CONNECT, enter = fadeIn(), exit = fadeOut()
        ) {
          TextButton(onClick = {
            apiUsableUpdater(true)
            logger.debug("[ConnectStatusCard] 重试连接")
          }) {
            Text(text = "重试")
          }
        }
      }
      AnimatedVisibility(
        connectStatus == ConnectStatus.CONNECT,
        enter = expandIn(expandFrom = Alignment.TopCenter) + fadeIn(),
        exit = shrinkOut(shrinkTowards = Alignment.TopCenter) + fadeOut()
      ) {
        Column {
          Text(text = "DDTV 核心版本：$ddtvCoreVersion", style = MaterialTheme.typography.bodySmall)
          Text(text = "DDTV WEB核心版本：$webCoreVersion", style = MaterialTheme.typography.bodySmall)
          Text(text = "DDTV 运行环境版本：$dotnetVersion", style = MaterialTheme.typography.bodySmall)
        }
      }
      Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = { connectSelectExpandedUpdater(!connectSelectExpanded) }) {
          Text(text = "选择服务器")
        }
        TextButton(onClick = { connectAddExpandedUpdater(!connectAddExpanded) }) {
          Text(text = "添加服务器")
        }
      }
    }
  }
}