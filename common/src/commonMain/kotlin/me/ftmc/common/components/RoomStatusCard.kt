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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.ftmc.common.LocalLogger
import me.ftmc.common.RecRecordingInfoLiteData
import me.ftmc.common.backend.APIError
import me.ftmc.common.backend.recordInfoFlow
import me.ftmc.common.backend.roomCmdWithUID
import me.ftmc.common.backend.roomCmdWithUIDAndBoolean
import me.ftmc.common.formatDataUnit
import me.ftmc.common.formatLongTime
import me.ftmc.common.pages.RealRoom


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomStatusCard(room: RealRoom, expandedUpdater: (Boolean) -> Unit) {
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
    logger.debug("[RoomStatusCard-${room.uid}] ????????????")
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
      var liveInfoExpanded by remember { mutableStateOf(false) }
      Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "${room.username} (${room.uid})", style = MaterialTheme.typography.bodyLarge, maxLines = 1)
        Text(text = "${room.title} (${room.roomId})", style = MaterialTheme.typography.bodyMedium, maxLines = 1)
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(text = "???????????????", style = MaterialTheme.typography.bodySmall)
          TextButton(
            onClick = {
              liveInfoExpanded = !liveInfoExpanded
              expandedUpdater(liveInfoExpanded)
            }, enabled = room.isDownload
          ) {
            Text(text = if (room.liveStatus == 0 || room.liveStatus == 2) "?????????" else "?????????")
          }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          val roomSetScope = rememberCoroutineScope()
          var buttonEnable by remember { mutableStateOf(true) }
          Text("???????????????", style = MaterialTheme.typography.bodySmall)
          var isAutoRec by remember { mutableStateOf(room.isAutoRec) }
          Checkbox(checked = isAutoRec, onCheckedChange = {
            buttonEnable = false
            logger.debug("[RoomStatusCard-${room.uid}] ?????????????????????????????? -> $it")
            roomSetScope.launch(Dispatchers.IO) {
              try {
                logger.debug("[RoomStatusCard-${room.uid}] ????????????????????????????????????")
                roomCmdWithUIDAndBoolean("Room_AutoRec", room.uid.toString(), "IsAutoRec", it)
                isAutoRec = it
                logger.info("[RoomStatusCard-${room.uid}] ??????????????????????????????")
              } catch (e: APIError) {
                logger.warn("[RoomStatusCard-${room.uid}] ??????????????????????????????API?????? -> ${e.errorType.msg}")
              } catch (e: Exception) {
                logger.warn("[RoomStatusCard-${room.uid}] ????????????????????????????????????????????? -> ${e.javaClass.name} ,${e.message}")
                logger.errorCatch(e)
              }
              buttonEnable = true
            }
          }, enabled = buttonEnable)
          Text("???????????????", style = MaterialTheme.typography.bodySmall)
          var isRecDanmu by remember { mutableStateOf(room.isRecDanmu) }
          Checkbox(checked = isRecDanmu, onCheckedChange = {
            buttonEnable = false
            logger.debug("[RoomStatusCard-${room.uid}] ?????????????????????????????? -> $it")
            roomSetScope.launch(Dispatchers.IO) {
              try {
                logger.debug("[RoomStatusCard-${room.uid}] ??????????????????????????????????????????")
                roomCmdWithUIDAndBoolean("Room_DanmuRec", room.uid.toString(), "IsRecDanmu", it)
                isRecDanmu = it
                logger.info("[RoomStatusCard-${room.uid}] ??????????????????????????????")
              } catch (e: APIError) {
                logger.warn("[RoomStatusCard-${room.uid}] ??????????????????????????????API?????? -> ${e.errorType.msg}")
              } catch (e: Exception) {
                logger.warn("[RoomStatusCard-${room.uid}] ????????????????????????????????????????????? -> ${e.javaClass.name} ,${e.message}")
                logger.errorCatch(e)
              }
              buttonEnable = true
            }
          }, enabled = buttonEnable)
          var roomDeleted by remember { mutableStateOf(false) }
          var roomDeletedWaiting by remember { mutableStateOf(false) }
          LaunchedEffect(roomDeletedWaiting) {
            if (roomDeletedWaiting) {
              delay(3000L)
              roomDeletedWaiting = false
            }
          }
          IconButton(onClick = {
            if (roomDeletedWaiting) {
              buttonEnable = false
              logger.debug("[RoomStatusCard-${room.uid}] ??????????????????")
              roomSetScope.launch(Dispatchers.IO) {
                try {
                  logger.debug("[RoomStatusCard-${room.uid}] ??????????????????????????????")
                  roomCmdWithUID("Room_Del", room.uid.toString())
                  roomDeleted = true
                  logger.info("[RoomStatusCard-${room.uid}] ??????????????????")
                } catch (e: APIError) {
                  buttonEnable = true
                  logger.warn("[RoomStatusCard-${room.uid}] ??????????????????API?????? -> ${e.errorType.msg}")
                } catch (e: Exception) {
                  buttonEnable = true
                  logger.warn("[RoomStatusCard-${room.uid}] ????????????????????????????????? -> ${e.javaClass.name} ,${e.message}")
                  logger.errorCatch(e)
                }
              }
            } else {
              roomDeletedWaiting = true
            }
          }, enabled = buttonEnable) {
            Icon(
              imageVector = if (roomDeletedWaiting) {
                Icons.Filled.Warning
              } else if (roomDeleted) {
                Icons.Filled.Done
              } else {
                Icons.Filled.Delete
              }, "????????????"
            )
          }
        }
        AnimatedVisibility(
          liveInfoExpanded,
          enter = expandIn(expandFrom = Alignment.TopCenter) + fadeIn(),
          exit = shrinkOut(shrinkTowards = Alignment.TopCenter) + fadeOut()
        ) {
          var totalDownloadCount by remember { mutableStateOf(0L) }
          var startTime by remember { mutableStateOf(0L) }
          LaunchedEffect(true) {
            recordInfoFlow.catch { }.collect {
              val checkedRoom: RecRecordingInfoLiteData? =
                run {
                  for (recordInfoRoom in it) {
                    if (recordInfoRoom.Uid == room.uid) {
                      logger.debug("[RoomStatusCard-${room.uid}] ????????????????????????")
                      return@run recordInfoRoom
                    }
                  }
                  logger.warn("[RoomStatusCard-${room.uid}] ?????????????????????")
                  return@run null
                }
              if (checkedRoom == null) {
                liveInfoExpanded = false
              } else {
                startTime = checkedRoom.StartTime ?: 0
                totalDownloadCount = checkedRoom.TotalDownloadCount ?: 0
              }
            }
          }
          Column(modifier = Modifier.fillMaxWidth()) {
            Divider(modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically) {
              Column {
                Text(text = "???????????????${formatLongTime(startTime)}", style = MaterialTheme.typography.bodySmall)
                Text(text = "??????????????????${formatDataUnit(totalDownloadCount)}", style = MaterialTheme.typography.bodySmall)
              }
              var cancelButtonEnable by remember { mutableStateOf(true) }
              val downloadInfoScope = rememberCoroutineScope()
              IconButton(onClick = {
                cancelButtonEnable = false
                logger.debug("[RoomStatusCard-${room.uid}] ??????????????????")
                downloadInfoScope.launch(Dispatchers.IO) {
                  try {
                    logger.debug("[RoomStatusCard-${room.uid}] ??????????????????????????????")
                    roomCmdWithUID("Rec_CancelDownload", room.uid.toString())
                    logger.info("[RoomStatusCard-${room.uid}] ??????????????????")
                    liveInfoExpanded = false
                  } catch (e: APIError) {
                    logger.warn("[RoomStatusCard-${room.uid}] ??????????????????API?????? -> ${e.errorType.msg}")
                  } catch (e: Exception) {
                    logger.warn("[RoomStatusCard-${room.uid}] ????????????????????????????????? -> ${e.javaClass.name} ,${e.message}")
                    logger.errorCatch(e)
                  }
                  cancelButtonEnable = true
                }
              }, enabled = cancelButtonEnable) {
                Icon(Icons.Filled.Cancel, "????????????")
              }
            }
          }
        }
      }
    }
  }
}