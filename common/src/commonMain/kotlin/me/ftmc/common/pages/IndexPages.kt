package me.ftmc.common.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import io.ktor.client.request.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import me.ftmc.common.ConnectStatus
import me.ftmc.common.backend.APIError
import me.ftmc.common.backend.accessKeyId
import me.ftmc.common.backend.accessKeySecret
import me.ftmc.common.backend.getRequestURL
import me.ftmc.common.backend.httpClient
import me.ftmc.common.backend.loginStatusFlow
import me.ftmc.common.backend.systemInfoFlow
import me.ftmc.common.backend.url
import me.ftmc.common.byteArrayToImageBitmap
import me.ftmc.common.currentScreenWidth
import me.ftmc.common.saveConfig
import me.ftmc.common.screenTypeChangeWidth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndexPage() {
  var connectStatus by remember { mutableStateOf(ConnectStatus.DISCONNECT) }
  var apiUsable by remember { mutableStateOf(true) }
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
    Column(
      modifier = if (currentScreenWidth >= screenTypeChangeWidth) {
        Modifier.width(350.dp).padding(start = 16.dp)
      } else {
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp)
      }.verticalScroll(rememberScrollState())
    ) {
      var connectSettingExpanded by remember { mutableStateOf(false) }
      ConnectStatusCard(apiUsable,
        connectStatus,
        connectSettingExpanded,
        { apiUsable = it },
        { connectStatus = it },
        { connectSettingExpanded = it })
      if (connectSettingExpanded) {
        Spacer(Modifier.height(8.dp))
      }
      ConnectSettingsCard(connectSettingExpanded) {
        apiUsable = true
        connectSettingExpanded = false
      }
      if (currentScreenWidth < screenTypeChangeWidth) {
        if (connectStatus == ConnectStatus.CONNECT) {
          Spacer(Modifier.height(8.dp))
        }
        LoginInfoCard(connectStatus)
      }
      if (currentScreenWidth < screenTypeChangeWidth) {
        Spacer(Modifier.height(90.dp))
      }
    }
    if (currentScreenWidth >= screenTypeChangeWidth) {
      Spacer(modifier = Modifier.width(8.dp))
      Column(modifier = Modifier.width(350.dp).padding(end = 16.dp)) {
        LoginInfoCard(connectStatus)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConnectStatusCard(
  apiUsable: Boolean,
  connectStatus: ConnectStatus,
  connectSettingExpanded: Boolean,
  apiUsableUpdater: (Boolean) -> Unit,
  connectStatusUpdater: (ConnectStatus) -> Unit,
  connectSettingExpandedUpdater: (Boolean) -> Unit
) {
  OutlinedCard(modifier = Modifier.fillMaxWidth()) {
    var ddtvCoreVersion by remember { mutableStateOf("") }
    var webCoreVersion by remember { mutableStateOf("") }
    var dotnetVersion by remember { mutableStateOf("") }
    LaunchedEffect(apiUsable) {
      systemInfoFlow.catch {
        if (it is APIError) {
          val tempConnectStatus = when (it.code) {
            6000 -> ConnectStatus.COOKIE_TIMEOUT
            6001 -> ConnectStatus.LOGIN_FAILED
            6002 -> ConnectStatus.SIG_FAILED
            7000 -> ConnectStatus.CMD_FAILED
            else -> ConnectStatus.UNKNOWN_ERROR
          }
          connectStatusUpdater(tempConnectStatus)
        } else {
          connectStatusUpdater(ConnectStatus.NET_ERROR)
        }
        apiUsableUpdater(false)
      }.collect {
        ddtvCoreVersion = it.DDTVCore_Ver
        webCoreVersion = it.os_Info.WebCore_Ver
        dotnetVersion = it.os_Info.AppCore_Ver
        connectStatusUpdater(ConnectStatus.CONNECT)
      }
    }
    Column(modifier = Modifier.padding(16.dp)) {
      Text(text = "服务器信息", style = MaterialTheme.typography.headlineSmall)
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text("连接状态：${connectStatus.statusString}", style = MaterialTheme.typography.bodySmall)
        TextButton(onClick = { connectSettingExpandedUpdater(!connectSettingExpanded) }) {
          Text(text = "设置", style = MaterialTheme.typography.bodySmall)
        }
        AnimatedVisibility(
          connectStatus != ConnectStatus.CONNECT, enter = fadeIn(), exit = fadeOut()
        ) {
          TextButton(onClick = { apiUsableUpdater(true) }) {
            Text(text = "重试", style = MaterialTheme.typography.bodySmall)
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
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConnectSettingsCard(connectSettingExpanded: Boolean, settingSaveUpdater: () -> Unit) {
  AnimatedVisibility(
    connectSettingExpanded,
    enter = expandIn(expandFrom = Alignment.TopCenter) + fadeIn(),
    exit = shrinkOut(shrinkTowards = Alignment.TopCenter) + fadeOut()
  ) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
      var tempURL by remember { mutableStateOf(url) }
      var tempAccessKeyId by remember { mutableStateOf(accessKeyId) }
      var tempAccessKeySecret by remember { mutableStateOf(accessKeySecret) }
      Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "连接设置", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = tempURL,
          onValueChange = { tempURL = it },
          placeholder = { androidx.compose.material.Text(text = "包含 http:// 或 https://") },
          label = { androidx.compose.material.Text(text = "服务器地址") })
        OutlinedTextField(value = tempAccessKeyId,
          onValueChange = { tempAccessKeyId = it },
          label = { androidx.compose.material.Text(text = "AccessKeyId") })
        OutlinedTextField(value = tempAccessKeySecret,
          onValueChange = { tempAccessKeySecret = it },
          label = { androidx.compose.material.Text(text = "AccessKeySecret") })
        TextButton(onClick = {
          url = tempURL
          accessKeyId = tempAccessKeyId
          accessKeySecret = tempAccessKeySecret
          saveConfig()
          settingSaveUpdater()
        }) {
          Text(text = "保存", style = MaterialTheme.typography.bodySmall)
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginInfoCard(connectStatus: ConnectStatus) {
  AnimatedVisibility(
    connectStatus == ConnectStatus.CONNECT,
    enter = expandIn(expandFrom = Alignment.TopCenter) + fadeIn(),
    exit = shrinkOut(shrinkTowards = Alignment.TopCenter) + fadeOut()
  ) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
      var loginStatus by remember { mutableStateOf(false) }
      var qrCodeExpanded by remember { mutableStateOf(false) }
      LaunchedEffect(true) {
        loginStatusFlow.collect {
          loginStatus = it
          if (!loginStatus) {
            qrCodeExpanded = true
          }
        }
      }
      Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Bilibili 登录", style = MaterialTheme.typography.headlineSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(text = "登录状态：${if (loginStatus) "已登录" else "未登录"}", style = MaterialTheme.typography.bodySmall)
          TextButton(onClick = { qrCodeExpanded = !qrCodeExpanded }) {
            Text(text = "登录二维码", style = MaterialTheme.typography.bodySmall)
          }
        }
        AnimatedVisibility(
          qrCodeExpanded,
          enter = expandIn(expandFrom = Alignment.TopCenter) + fadeIn(),
          exit = shrinkOut(shrinkTowards = Alignment.TopCenter) + fadeOut()
        ) {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            var imageLoading by remember { mutableStateOf(true) }
            var imageBitmap: ImageBitmap? by remember { mutableStateOf(null) }
            LaunchedEffect(true) {
              while (true) {
                imageLoading = true
                try {
                  val imageByteArray: ByteArray = httpClient.get(urlString = getRequestURL("loginqr"))
                  imageBitmap = byteArrayToImageBitmap(imageByteArray)
                  imageLoading = false
                  delay(30000L)
                } catch (e: Exception) {
                  delay(500L)
                }
              }
            }
            if (imageLoading || imageBitmap == null) {
              CircularProgressIndicator(modifier = Modifier.size(256.dp))
            } else {
              Image(bitmap = imageBitmap!!, contentDescription = null, modifier = Modifier.size(256.dp))
            }
          }
        }
      }
    }
  }
}