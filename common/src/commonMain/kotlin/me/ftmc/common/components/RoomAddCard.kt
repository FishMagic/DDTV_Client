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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.ftmc.common.LocalLogger
import me.ftmc.common.backend.APIError
import me.ftmc.common.backend.roomCmdWithUID


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomAddCard(cancelUpdater: () -> Unit, addSuccessUpdater: (Boolean) -> Unit) {
  val logger = remember { LocalLogger() }
  var cardShow by remember { mutableStateOf(false) }
  LaunchedEffect(true) {
    delay(1L)
    cardShow = true
  }
  AnimatedVisibility(
    cardShow,
    enter = expandIn(expandFrom = Alignment.TopCenter) + fadeIn(),
    exit = shrinkOut(shrinkTowards = Alignment.TopCenter) + fadeOut()
  ) {
    logger.debug("[RoomAddCard] 卡片加载")
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(16.dp)) {
        var newUID by remember { mutableStateOf("") }
        var buttonEnable by remember { mutableStateOf(true) }
        OutlinedTextField(value = newUID,
          onValueChange = { newUID = it },
          label = { androidx.compose.material.Text(text = "UID") })
        Row(verticalAlignment = Alignment.CenterVertically) {
          var addStatus by remember { mutableStateOf("等待操作") }
          val editCardScope = rememberCoroutineScope()
          TextButton(onClick = {
            logger.debug("[RoomAddCard] 开始添加房间 -> $newUID")
            buttonEnable = false
            editCardScope.launch(Dispatchers.IO) {
              addStatus = run {
                try {
                  logger.debug("[RoomAddCard] 准备发送添加请求")
                  roomCmdWithUID("Room_Add", newUID)
                  addSuccessUpdater(true)
                  logger.info("[RoomAddCard] 添加成功")
                  "添加成功"
                } catch (e: APIError) {
                  buttonEnable = true
                  logger.warn("[RoomAddCard] 发生 API 请求错误 -> ${e.errorType.msg}")
                  buttonEnable = true
                  e.errorType.msg
                } catch (e: Exception) {
                  logger.warn("[RoomAddCard] 发生预料外错误 -> ${e.javaClass.name} ,${e.message}")
                  logger.errorCatch(e)
                  buttonEnable = true
                  "操作失败：未知错误"
                }
              }
            }
          }, enabled = buttonEnable) {
            Text(text = "添加", style = MaterialTheme.typography.bodySmall)
          }
          TextButton(onClick = { cancelUpdater() }) {
            Text(text = "取消")
          }
          Text(text = addStatus, style = MaterialTheme.typography.bodySmall)
        }
      }
    }
  }
}