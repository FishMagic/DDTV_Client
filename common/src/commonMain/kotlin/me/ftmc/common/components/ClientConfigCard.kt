package me.ftmc.common.components

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
import androidx.compose.material3.RadioButton
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
import me.ftmc.common.LocalLogLevel
import me.ftmc.common.LocalLogger
import me.ftmc.common.darkMode
import me.ftmc.common.notification
import me.ftmc.common.saveConfig


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientConfigCard() {
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
      Row(verticalAlignment = Alignment.CenterVertically) {
        var tempLogMaxSize by remember { mutableStateOf(LocalLogger.maxSize.toString()) }
        Column(Modifier.weight(.8f)) {
          OutlinedTextField(
            value = tempLogMaxSize,
            onValueChange = {
              tempLogMaxSize = it.filter { char -> char.isDigit() }
            },
            label = { androidx.compose.material.Text(text = "日志条目上限") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
          )
        }
        Column(Modifier.weight(.2f)) {
          TextButton(
            onClick = {
              logger.debug("[ClientConfigCard] 修改日志条目上限 -> $tempLogMaxSize")
              LocalLogger.maxSize = tempLogMaxSize.toInt()
              saveConfig()
            }
          ) {
            Text(text = "保存")
          }
        }
      }
      Text(text = "深色模式", style = MaterialTheme.typography.bodyLarge)
      Row {
        Row(verticalAlignment = Alignment.CenterVertically) {
          RadioButton(selected = darkMode == true, onClick = {
            logger.debug("[ClientConfigCard] 修改深色模式状态 -> ${true}")
            darkMode = true
            saveConfig()
          })
          Text("开", style = MaterialTheme.typography.bodySmall)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          RadioButton(selected = darkMode == false, onClick = {
            logger.debug("[ClientConfigCard] 修改深色模式状态 -> ${false}")
            darkMode = false
            saveConfig()
          })
          Text("关", style = MaterialTheme.typography.bodySmall)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          RadioButton(selected = darkMode == null, onClick = {
            logger.debug("[ClientConfigCard] 修改深色模式状态 -> ${null}")
            darkMode = null
            saveConfig()
          })
          Text("跟随系统", style = MaterialTheme.typography.bodySmall)
        }
      }
      Text(text = "日志等级", style = MaterialTheme.typography.bodyLarge)
      Column {
        Row {
          Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = LocalLogger.logLevel >= LocalLogLevel.DEBUG, onClick = {
              LocalLogger.logLevel = LocalLogLevel.DEBUG
              logger.debug("[ClientConfigCard] 修改日志级别状态 -> DEBUG")
              saveConfig()
            })
            Text("DEBUG", style = MaterialTheme.typography.bodySmall)
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = LocalLogger.logLevel == LocalLogLevel.WARNING, onClick = {
              logger.debug("[ClientConfigCard] 修改日志级别状态 -> WARNING")
              LocalLogger.logLevel = LocalLogLevel.WARNING
              saveConfig()
            })
            Text("WARNING", style = MaterialTheme.typography.bodySmall)
          }
        }
        Row {
          Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = LocalLogger.logLevel == LocalLogLevel.INFO, onClick = {
              logger.debug("[ClientConfigCard] 修改日志级别状态 -> INFO")
              LocalLogger.logLevel = LocalLogLevel.INFO
              saveConfig()
            })
            Text("INFO", style = MaterialTheme.typography.bodySmall)
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = LocalLogger.logLevel == LocalLogLevel.NONE, onClick = {
              logger.debug("[ClientConfigCard] 修改日志级别状态 -> NONE")
              LocalLogger.logLevel = LocalLogLevel.NONE
              saveConfig()
            })
            Text("NONE", style = MaterialTheme.typography.bodySmall)
          }
        }
      }
    }
  }
}