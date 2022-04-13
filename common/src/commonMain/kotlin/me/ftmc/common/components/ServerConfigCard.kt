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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Checkbox
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.ftmc.common.ConfigKeys
import me.ftmc.common.ConnectStatus
import me.ftmc.common.LocalLogger
import me.ftmc.common.backend.APIError
import me.ftmc.common.backend.systemCmdWithBoolean
import me.ftmc.common.backend.systemCmdWithLong
import me.ftmc.common.backend.systemConfigFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerConfigCard(connectStatus: ConnectStatus) {
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
      var autoTranscodeButtonEnable by remember {
        mutableStateOf(
          false
        )
      }
      var flvSplitSize by remember { mutableStateOf("") }
      var flvSplitSizeError by remember {
        mutableStateOf(
          false
        )
      }
      var flvSplitSizeButtonEnable by remember {
        mutableStateOf(
          false
        )
      }
      var flvSplitSizeFocused by remember {
        mutableStateOf(
          false
        )
      }
      var isRecDanmu by remember { mutableStateOf(false) }
      var isRecDanmuButtonEnable by remember {
        mutableStateOf(
          false
        )
      }
      val systemConfigScope = rememberCoroutineScope()
      LaunchedEffect(true) {
        systemConfigFlow.catch { }.collect { configs ->
          configs.forEach { config ->
            try {
              when (ConfigKeys.values()[config.Key]) {
                ConfigKeys.IsAutoTranscod -> {
                  logger.info("[ServerConfigCard] 识别到自动转码配置 -> ${config.Key}, ${config.Value}")
                  isAutoTranscod = config.Value.toBoolean()
                  autoTranscodeButtonEnable = true
                }
                ConfigKeys.FlvSplitSize -> {
                  logger.info("[ServerConfigCard] 识别到自动分割配置 -> ${config.Key}, ${config.Value}")
                  if (!flvSplitSizeFocused) {
                    flvSplitSize = config.Value
                    flvSplitSizeButtonEnable = true
                  }
                }
                ConfigKeys.IsRecDanmu -> {
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
                  logger.warn("[ServerConfigCard] 修改自动转码发生API错误 -> ${e.errorType.msg}")
                } catch (e: Exception) {
                  logger.warn("[ServerConfigCard] 修改自动转码发生预料外错误 -> ${e.javaClass.name} ,${e.message}")
                  logger.errorCatch(e)
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
                  logger.warn("[ServerConfigCard] 修改录制弹幕发生API错误 -> ${e.errorType.msg}")
                } catch (e: Exception) {
                  logger.warn("[ServerConfigCard] 修改录制弹幕发生预料外错误 -> ${e.javaClass.name} ,${e.message}")
                  logger.errorCatch(e)
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
                if ((flvSplitSize != "") && (flvSplitSize.toLong() != 0L) && (flvSplitSize.toLong() < 10485760)) {
                  flvSplitSizeError = true
                  flvSplitSizeButtonEnable = false
                } else {
                  flvSplitSizeError = false
                  flvSplitSizeButtonEnable = true
                }
              },
              label = { androidx.compose.material.Text(text = "自动切片大小 (Bytes)") },
              isError = flvSplitSizeError,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              modifier = Modifier.onFocusChanged {
                flvSplitSizeFocused = it.isFocused
              }
            )
          }
          Column(Modifier.weight(.2f)) {
            TextButton(
              onClick = {
                flvSplitSizeButtonEnable = false
                logger.debug("[ServerConfigCard] 开始修改自动切片大小")
                systemConfigScope.launch(Dispatchers.IO) {
                  try {
                    logger.debug("[ServerConfigCard] 准备发送修改自动切片大小请求")
                    systemCmdWithLong(
                      "Config_FileSplit",
                      "state",
                      if (flvSplitSize != "") {
                        flvSplitSize.toLong()
                      } else 0L
                    )
                    logger.info("[ServerConfigCard] 修改自动切片大小成功")
                  } catch (e: APIError) {
                    logger.warn("[ServerConfigCard] 修改自动切片大小发生API错误 -> ${e.errorType.msg}")
                  } catch (e: Exception) {
                    logger.warn("[ServerConfigCard] 修改自动切片大小发生预料外错误 -> ${e.javaClass.name} ,${e.message}")
                    logger.errorCatch(e)
                  }
                  flvSplitSizeButtonEnable = true
                }
              }, enabled = flvSplitSizeButtonEnable
            ) {
              Text(text = "保存")
            }
          }
        }
      }
    }
  }
}