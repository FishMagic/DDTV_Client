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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.ftmc.common.APIError
import me.ftmc.common.ConfigKeys.FlvSplitSize
import me.ftmc.common.ConfigKeys.IsAutoTranscod
import me.ftmc.common.ConfigKeys.IsRecDanmu
import me.ftmc.common.ConfigKeys.values
import me.ftmc.common.ConnectStatus
import me.ftmc.common.LocalLogger
import me.ftmc.common.Server
import me.ftmc.common.accessKeyId
import me.ftmc.common.accessKeySecret
import me.ftmc.common.backend.loginStateFlow
import me.ftmc.common.backend.systemCmdWithBoolean
import me.ftmc.common.backend.systemCmdWithLong
import me.ftmc.common.backend.systemConfigFlow
import me.ftmc.common.backend.systemInfoFlow
import me.ftmc.common.byteArrayToImageBitmap
import me.ftmc.common.currentScreenWidth
import me.ftmc.common.darkMode
import me.ftmc.common.getRequestURL
import me.ftmc.common.httpClient
import me.ftmc.common.navigationBarsHeightModifier
import me.ftmc.common.notification
import me.ftmc.common.saveConfig
import me.ftmc.common.screenTypeChangeWidth
import me.ftmc.common.serverList
import me.ftmc.common.url

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndexPage() {
  val logger = remember { LocalLogger() }
  var connectStatus by remember { mutableStateOf(ConnectStatus.DISCONNECT) }
  var apiUsable by remember { mutableStateOf(true) }
  LaunchedEffect(true) {
    logger.info("[Index] 页面加载")
  }
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
    Column(
      modifier = if (currentScreenWidth >= screenTypeChangeWidth) {
        Modifier.width(350.dp).padding(start = 16.dp, bottom = 16.dp)
      } else {
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp)
      }.verticalScroll(rememberScrollState())
    ) {
      var connectAddExpanded by remember { mutableStateOf(false) }
      var connectSelectExpanded by remember { mutableStateOf(false) }
      ConnectStatusCard(
        apiUsable,
        connectStatus,
        connectAddExpanded,
        connectSelectExpanded,
        apiUsableUpdater = { apiUsable = it },
        connectStatusUpdater = { connectStatus = it },
        connectAddExpandedUpdater = {
          connectAddExpanded = it
          connectSelectExpanded = false
        },
        connectSelectExpandedUpdater = {
          connectSelectExpanded = it
          connectAddExpanded = false
        })
      if (connectAddExpanded) {
        Spacer(Modifier.height(8.dp))
      }
      ConnectAddCard(connectAddExpanded) {
        apiUsable = true
        connectAddExpanded = false
      }
      if (connectSelectExpanded) {
        Spacer(Modifier.height(8.dp))
      }
      ConnectSelectCard(connectSelectExpanded) {
        apiUsable = true
        connectSelectExpanded = false
      }
      if (currentScreenWidth < screenTypeChangeWidth) {
        if (connectStatus == ConnectStatus.CONNECT) {
          Spacer(Modifier.height(8.dp))
        }
        LoginInfoCard(connectStatus)
        if (connectStatus == ConnectStatus.CONNECT) {
          Spacer(Modifier.height(8.dp))
        }
        ServerConfigCard(connectStatus)
      }
      Spacer(Modifier.height(8.dp))
      ClientConfigCard()
      if (currentScreenWidth < screenTypeChangeWidth) {
        Spacer(Modifier.height(90.dp))
        Spacer(Modifier.navigationBarsHeightModifier())
      }
    }
    if (currentScreenWidth >= screenTypeChangeWidth) {
      Spacer(modifier = Modifier.width(8.dp))
      Column(modifier = Modifier.width(350.dp).padding(end = 16.dp)) {
        LoginInfoCard(connectStatus)
        if (connectStatus == ConnectStatus.CONNECT) {
          Spacer(Modifier.height(8.dp))
        }
        ServerConfigCard(connectStatus)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConnectStatusCard(
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
          val tempConnectStatus = when (it.code) {
            -1 -> ConnectStatus.DISCONNECT
            6000 -> ConnectStatus.COOKIE_TIMEOUT
            6001 -> ConnectStatus.LOGIN_FAILED
            6002 -> ConnectStatus.SIG_FAILED
            7000 -> ConnectStatus.CMD_FAILED
            else -> ConnectStatus.UNKNOWN_ERROR
          }
          connectStatusUpdater(tempConnectStatus)
          logger.warn("[ConnectStatusCard] 服务器返回状态 ${tempConnectStatus.statusString}")
        } else {
          connectStatusUpdater(ConnectStatus.NET_ERROR)
          logger.warn("[ConnectStatusCard] 可能存在网络错误")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConnectSelectCard(connectSelectExpanded: Boolean, settingSaveUpdater: () -> Unit) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConnectAddCard(connectAddExpanded: Boolean, settingSaveUpdater: () -> Unit) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginInfoCard(connectStatus: ConnectStatus) {
  AnimatedVisibility(
    connectStatus == ConnectStatus.CONNECT,
    enter = expandIn(expandFrom = Alignment.TopCenter) + fadeIn(),
    exit = shrinkOut(shrinkTowards = Alignment.TopCenter) + fadeOut()
  ) {
    val logger = remember { LocalLogger() }
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
      LaunchedEffect(true) {
        logger.info("[LoginInfoCard] 卡片加载")
      }
      var loginCode by remember { mutableStateOf(0) }
      var qrCodeExpanded by remember { mutableStateOf(false) }
      LaunchedEffect(true) {
        loginStateFlow.collect {
          loginCode = it.LoginState
          qrCodeExpanded = it.LoginState != 1
          logger.debug("[LoginInfoCard] 心跳响应成功 -> $loginCode")
        }
      }
      Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Bilibili 登录", style = MaterialTheme.typography.headlineSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "登录状态：${
              when (loginCode) {
                0 -> "未登录"
                1 -> "已登陆"
                2 -> "登陆失效"
                3 -> "登陆中"
                else -> "未知"
              }
            }", style = MaterialTheme.typography.bodySmall
          )
          AnimatedVisibility(
            loginCode != 1, enter = fadeIn(), exit = fadeOut()
          ) {
            TextButton(onClick = { qrCodeExpanded = !qrCodeExpanded }) {
              Text(text = "登录二维码")
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServerConfigCard(connectStatus: ConnectStatus) {
  AnimatedVisibility(
    connectStatus == ConnectStatus.CONNECT,
    enter = expandIn(expandFrom = Alignment.TopCenter) + fadeIn(),
    exit = shrinkOut(shrinkTowards = Alignment.TopCenter) + fadeOut()
  ) {
    val logger = remember { LocalLogger() }
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
      LaunchedEffect(true) {
        logger.info("[ServerConfigCard] 卡片加载")
      }
      var isAutoTranscod by remember { mutableStateOf(false) }
      var autoTranscodeButtonEnable by remember { mutableStateOf(false) }
      var flvSplitSize by remember { mutableStateOf("") }
      var flvSplitSizeError by remember { mutableStateOf(false) }
      var flvSplitSizeButtonEnbale by remember { mutableStateOf(false) }
      var isRecDanmu by remember { mutableStateOf(false) }
      var isRecDanmuButtonEnable by remember { mutableStateOf(false) }
      val systemConfigScope = rememberCoroutineScope()
      LaunchedEffect(true) {
        systemConfigFlow.collect { configs ->
          configs.forEach { config ->
            try {
              when (values()[config.Key]) {
                IsAutoTranscod -> {
                  logger.info("[ServerConfigCard] 识别到自动转码配置 -> ${config.Key}, ${config.Value}")
                  isAutoTranscod = config.Value.toBoolean()
                  autoTranscodeButtonEnable = true
                }
                FlvSplitSize -> {
                  logger.info("[ServerConfigCard] 识别到自动分割配置 -> ${config.Key}, ${config.Value}")
                  flvSplitSize = config.Value
                  flvSplitSizeButtonEnbale = true
                }
                IsRecDanmu -> {
                  logger.info("[ServerConfigCard] 识别到弹幕录制配置 -> ${config.Key}, ${config.Value}")
                  isRecDanmu = config.Value.toBoolean()
                  isRecDanmuButtonEnable = true
                }
                else -> {}
              }
            } catch (e: ArrayIndexOutOfBoundsException) {
              logger.warn("[ServerConfigCard] 不正确的 Config Key -> ${config.Key}")
            }
          }
        }
      }
      Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "服务器参数配置", style = MaterialTheme.typography.headlineSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(text = "自动转码", style = MaterialTheme.typography.bodySmall)
          Checkbox(
            checked = isAutoTranscod, onCheckedChange = {
              autoTranscodeButtonEnable = false
              logger.debug("[ServerConfigCard] 开始修改自动转码")
              systemConfigScope.launch(Dispatchers.IO) {
                try {
                  logger.debug("[ServerConfigCard] 准备发送修改自动转码请求")
                  systemCmdWithBoolean("Config_Transcod", "state", it)
                  isAutoTranscod = it
                  logger.info("[ServerConfigCard] 修改自动转码成功")
                } catch (e: APIError) {
                  logger.warn("[ServerConfigCard] 修改自动转码发生API错误 -> ${e.code}")
                } catch (e: Exception) {
                  logger.warn("[ServerConfigCard] 修改自动转码发生预料外错误 -> ${e.javaClass.name} ,${e.message}")
                }
                autoTranscodeButtonEnable = true
              }
            }, enabled = autoTranscodeButtonEnable
          )
          Text(text = "录制弹幕", style = MaterialTheme.typography.bodySmall)
          Checkbox(
            checked = isRecDanmu, onCheckedChange = {
              isRecDanmuButtonEnable = false
              logger.debug("[ServerConfigCard] 开始修改录制弹幕")
              systemConfigScope.launch(Dispatchers.IO) {
                try {
                  logger.debug("[ServerConfigCard] 准备发送修改录制弹幕请求")
                  systemCmdWithBoolean("Config_DanmuRec", "state", it)
                  isRecDanmu = it
                  logger.info("[ServerConfigCard] 修改录制弹幕成功")
                } catch (e: APIError) {
                  logger.warn("[ServerConfigCard] 修改录制弹幕发生API错误 -> ${e.code}")
                } catch (e: Exception) {
                  logger.warn("[ServerConfigCard] 修改录制弹幕发生预料外错误 -> ${e.javaClass.name} ,${e.message}")
                }
                isRecDanmuButtonEnable = true
              }
            }, enabled = isRecDanmuButtonEnable
          )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Column(Modifier.weight(.8f)) {
            OutlinedTextField(
              value = flvSplitSize,
              onValueChange = {
                flvSplitSize = it.filter { char -> char.isDigit() }
                if (flvSplitSize.toLong() < 10485760) {
                  flvSplitSizeError = true
                  flvSplitSizeButtonEnbale = false
                } else {
                  flvSplitSizeError = false
                  flvSplitSizeButtonEnbale = true
                }
              },
              label = { androidx.compose.material.Text(text = "自动切片大小 (Bytes)") },
              isError = flvSplitSizeError,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
          }
          Column(Modifier.weight(.2f)) {
            TextButton(
              onClick = {
                flvSplitSizeButtonEnbale = false
                logger.debug("[ServerConfigCard] 开始修改自动切片大小")
                systemConfigScope.launch(Dispatchers.IO) {
                  try {
                    logger.debug("[ServerConfigCard] 准备发送修改自动切片大小请求")
                    systemCmdWithLong("Config_FileSplit", "state", flvSplitSize.toLong())
                    logger.info("[ServerConfigCard] 修改自动切片大小成功")
                  } catch (e: APIError) {
                    logger.warn("[ServerConfigCard] 修改自动切片大小发生API错误 -> ${e.code}")
                  } catch (e: Exception) {
                    logger.warn("[ServerConfigCard] 修改自动切片大小发生预料外错误 -> ${e.javaClass.name} ,${e.message}")
                  }
                  flvSplitSizeButtonEnbale = true
                }
              }, enabled = flvSplitSizeButtonEnbale
            ) {
              Text(text = "保存")
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClientConfigCard() {
  val logger = remember { LocalLogger() }
  OutlinedCard(modifier = Modifier.fillMaxWidth()) {
    LaunchedEffect(true) {
      logger.info("[ClientConfigCard] 卡片加载")
    }
    Column(modifier = Modifier.padding(16.dp)) {
      Text(text = "客户端设置", style = MaterialTheme.typography.headlineSmall)
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "后台通知推送", style = MaterialTheme.typography.bodySmall)
        Checkbox(checked = notification, onCheckedChange = {
          logger.debug("[ClientConfigCard] 修改后台通知推送状态 -> $it")
          notification = it
          saveConfig()
        })
      }
      Text(text = "深色模式", style = MaterialTheme.typography.bodyLarge)
      Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = darkMode == true, onClick = {
          logger.debug("[ClientConfigCard] 修改后台通知推送状态 -> ${true}")
          darkMode = true
          saveConfig()
        })
        Text("开", style = MaterialTheme.typography.bodySmall)
      }
      Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = darkMode == false, onClick = {
          logger.debug("[ClientConfigCard] 修改后台通知推送状态 -> ${false}")
          darkMode = false
          saveConfig()
        })
        Text("关", style = MaterialTheme.typography.bodySmall)
      }
      Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = darkMode == null, onClick = {
          logger.debug("[ClientConfigCard] 修改后台通知推送状态 -> ${null}")
          darkMode = null
          saveConfig()
        })
        Text("跟随系统", style = MaterialTheme.typography.bodySmall)
      }
    }
  }
}