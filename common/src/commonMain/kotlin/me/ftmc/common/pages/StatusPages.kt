package me.ftmc.common.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.mutableStateListOf
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
import me.ftmc.common.backend.APIError
import me.ftmc.common.backend.RecRecordingInfoLiteData
import me.ftmc.common.backend.RoomAllInfoData
import me.ftmc.common.backend.formatDataUnit
import me.ftmc.common.backend.formatLongTime
import me.ftmc.common.backend.recordInfoFlow
import me.ftmc.common.backend.roomAllInfoFlow
import me.ftmc.common.backend.roomCmdWithUID
import me.ftmc.common.backend.roomCmdWithUIDAndBoolean
import me.ftmc.common.currentScreenWidth
import me.ftmc.common.screenTypeChangeWidth
import java.lang.StrictMath.max

open class Room

data class RealRoom(
  val uid: Long,
  val username: String,
  val roomId: Int,
  val title: String,
  val liveStatus: Int,
  val isAutoRec: Boolean,
  val isRecDanmu: Boolean,
  val isDownload: Boolean
) : Room()

data class FakeRoom(val index: Int = 0) : Room()

lateinit var addFakeRoom: () -> Unit

@Composable
fun StatusPage() {
  val roomList = remember { mutableStateListOf<Room>() }
  val waitingRoomList = remember { mutableStateListOf<FakeRoom>() }
  var roomWaiting by remember { mutableStateOf(false) }
  var roomAddCancel by remember { mutableStateOf(false) }
  var roomAddSuccess by remember { mutableStateOf(false) }
  val childRoomList1 = remember { mutableStateListOf<Room>() }
  val childRoomList2 = remember { mutableStateListOf<Room>() }
  var columnExpanded1 by remember { mutableStateOf(0) }
  var columnExpanded2 by remember { mutableStateOf(0) }
  var columnReal1 by remember { mutableStateOf(0) }
  var columnReal2 by remember { mutableStateOf(0) }
  var columnFake1 by remember { mutableStateOf(0) }
  var columnFake2 by remember { mutableStateOf(0) }
  var apiUsable by remember { mutableStateOf(true) }

  @Synchronized
  fun roomListChange(newRoomList: List<RoomAllInfoData> = listOf()) {

    fun addChildList(index: Int, value: Room) {
      if (index % 2 == 0) {
        childRoomList1.add(value)
        if (value is RealRoom) columnReal1++
        if (value is FakeRoom) columnFake1++
      } else {
        childRoomList2.add(value)
        if (value is RealRoom) columnReal1++
        if (value is FakeRoom) columnFake2++
      }
    }

    val realRoomList = mutableListOf<RealRoom>()
    roomList.forEach { existRoom -> if (existRoom is RealRoom) realRoomList.add(existRoom) }
    roomList.removeAll(realRoomList)
    val fakeRoomList = mutableListOf<Room>()
    roomList.forEach { existRoom -> fakeRoomList.add(existRoom) }
    roomList.clear()
    childRoomList1.clear()
    childRoomList2.clear()
    columnReal1 = 0
    columnReal2 = 0
    columnFake1 = 0
    columnFake2 = 0
    var realIndex = 0
    if (newRoomList.isNotEmpty()) {
      newRoomList.forEach { room ->
        if (room.uid != null && room.uname != null && room.room_id != null && room.title != null && room.live_status != null && room.IsAutoRec != null && room.IsRecDanmu != null && room.IsDownload != null) {
          val tempRoom = RealRoom(
            uid = room.uid,
            username = room.uname,
            roomId = room.room_id,
            title = room.title,
            liveStatus = room.live_status,
            isAutoRec = room.IsAutoRec,
            isRecDanmu = room.IsRecDanmu,
            isDownload = room.IsDownload
          )
          roomList.add(tempRoom)
          addChildList(realIndex, tempRoom)
          realIndex++
        }
      }
    } else {
      realRoomList.forEach {
        roomList.add(it)
        addChildList(realIndex, it)
        realIndex++
      }
    }
    if (fakeRoomList.isEmpty()) {
      waitingRoomList.forEach { fakeRoom -> fakeRoomList.add(fakeRoom) }
    }
    waitingRoomList.clear()
    if (!roomAddCancel && !roomAddSuccess) {
      fakeRoomList.forEach { room ->
        roomList.add(room)
        if (realIndex % 2 == 0) {
          childRoomList1.add(room)
          columnFake1++
        } else {
          childRoomList2.add(room)
          columnFake2++
        }
        realIndex++
      }
    }
    roomAddCancel = false
    roomWaiting = false
    roomAddSuccess = false
  }
  LaunchedEffect(apiUsable) {
    roomAllInfoFlow.catch {
    }.collect { newRoomList ->
      roomListChange(newRoomList)
    }
  }
  LaunchedEffect(roomWaiting, roomAddCancel) {
    roomListChange()
  }
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
    val scrollState = rememberScrollState()
    addFakeRoom = {
      waitingRoomList.add(FakeRoom())
      roomWaiting = true
    }
    Column(
      modifier = if (currentScreenWidth >= screenTypeChangeWidth) {
        Modifier.width(350.dp).padding(start = 16.dp)
      } else {
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp)
      }.verticalScroll(scrollState)
    ) {
      for (room in if (currentScreenWidth >= screenTypeChangeWidth) childRoomList1 else roomList) {
        RoomCard(room,
          expandedUpdater = { if (it) columnExpanded1++ else columnExpanded1-- },
          cancelUpdater = { roomAddCancel = true }) {
          roomAddSuccess = it
        }
        Spacer(Modifier.height(8.dp))
      }
      if (currentScreenWidth < screenTypeChangeWidth) {
        Spacer(Modifier.height(90.dp))
      } else {
        Spacer(Modifier.height((max(0, columnExpanded2 - columnExpanded1) * 70).dp))
      }
    }
    if (currentScreenWidth >= screenTypeChangeWidth) {
      Spacer(Modifier.width(8.dp))
      Column(modifier = Modifier.width(350.dp).padding(end = 16.dp).verticalScroll(scrollState)) {
        for (room in childRoomList2) {
          RoomCard(room,
            expandedUpdater = { if (it) columnExpanded2++ else columnExpanded2-- },
            cancelUpdater = { roomAddCancel = true }) {
            roomAddSuccess = it
          }
          Spacer(Modifier.height(8.dp))
        }
        if (roomList.size % 2 != 0) {
          Spacer(Modifier.height(175.dp))
        }
        if (columnFake1 > columnFake2) {
          Spacer(Modifier.height(150.dp))
        }
        if (columnReal1 > columnReal2) {
          Spacer(Modifier.height(175.dp))
        }
        Spacer(Modifier.height((max(0, columnExpanded1 - columnExpanded2) * 70).dp))
      }
    }
  }
}

@Composable
private fun RoomCard(
  room: Room, expandedUpdater: (Boolean) -> Unit, cancelUpdater: () -> Unit, addSuccessUpdater: (Boolean) -> Unit
) {
  if (room is RealRoom) {
    RoomStatusCard(room, expandedUpdater)
  } else {
    RoomAddCard(cancelUpdater, addSuccessUpdater)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomAddCard(cancelUpdater: () -> Unit, addSuccessUpdater: (Boolean) -> Unit) {
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
            buttonEnable = false
            editCardScope.launch(Dispatchers.IO) {
              addStatus = try {
                addSuccessUpdater(true)
                roomCmdWithUID("Room_Add", newUID)
              } catch (e: APIError) {
                buttonEnable = true
                e.msg
              } catch (_: Exception) {
                "操作失败：未知错误"
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomStatusCard(room: RealRoom, expandedUpdater: (Boolean) -> Unit) {
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
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
      var liveInfoExpanded by remember { mutableStateOf(false) }
      Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "${room.username} (${room.uid})", style = MaterialTheme.typography.bodyLarge)
        Text(text = "${room.title} (${room.roomId})", style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(text = "开播状态：", style = MaterialTheme.typography.bodySmall)
          TextButton(
            onClick = {
              liveInfoExpanded = !liveInfoExpanded
              expandedUpdater(liveInfoExpanded)
            }, enabled = room.isDownload
          ) {
            Text(text = if (room.liveStatus == 0 || room.liveStatus == 2) "未开播" else "已开播")
          }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          val roomSetScope = rememberCoroutineScope()
          var buttonEnable by remember { mutableStateOf(true) }
          Text("自动录制：", style = MaterialTheme.typography.bodySmall)
          Checkbox(checked = room.isAutoRec, onCheckedChange = {
            buttonEnable = false
            roomSetScope.launch(Dispatchers.IO) {
              try {
                roomCmdWithUIDAndBoolean("Room_AutoRec", room.uid.toString(), "IsAutoRec", it)
              } catch (_: Exception) {
              }
              buttonEnable = true
            }
          }, enabled = buttonEnable)
          Text("录制弹幕：", style = MaterialTheme.typography.bodySmall)
          Checkbox(checked = room.isRecDanmu, onCheckedChange = {
            buttonEnable = false
            roomSetScope.launch(Dispatchers.IO) {
              try {
                roomCmdWithUIDAndBoolean("Room_DanmuRec", room.uid.toString(), "IsRecDanmu", it)
              } catch (_: Exception) {
              }
              buttonEnable = true
            }
          }, enabled = buttonEnable)
          IconButton(onClick = {
            buttonEnable = false
            roomSetScope.launch(Dispatchers.IO) {
              try {
                roomCmdWithUID("Room_Del", room.uid.toString())
              } catch (_: Exception) {
              }
              buttonEnable = true
            }
          }, enabled = buttonEnable) {
            Icon(Icons.Filled.Delete, "删除房间")
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
            recordInfoFlow.collect {
              val checkedRoom: RecRecordingInfoLiteData? = run {
                for (recordInfoRoom in it) {
                  if (recordInfoRoom.Uid == room.uid) {
                    return@run recordInfoRoom
                  }
                }
                return@run null
              }
              if (checkedRoom == null) {
                liveInfoExpanded = false
              } else {
                startTime = checkedRoom.StartTime
                totalDownloadCount = checkedRoom.TotalDownloadCount
              }
            }
          }
          Column(modifier = Modifier.fillMaxWidth()) {
            Divider(modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically) {
              Column {
                Text(text = "开始时间：${formatLongTime(startTime)}", style = MaterialTheme.typography.bodySmall)
                Text(text = "总下载大小：${formatDataUnit(totalDownloadCount)}", style = MaterialTheme.typography.bodySmall)
              }
              var cancelButtonEnable by remember { mutableStateOf(true) }
              val downloadInfoScope = rememberCoroutineScope()
              IconButton(onClick = {
                cancelButtonEnable = false
                downloadInfoScope.launch(Dispatchers.IO) {
                  try {
                    roomCmdWithUID("Rec_CancelDownload", room.uid.toString())
                  } catch (_: Exception) {
                  }
                  cancelButtonEnable = true
                }
              }, enabled = cancelButtonEnable) {
                Icon(Icons.Filled.Cancel, "取消录制")
              }
            }
          }
        }
      }
    }
  }
}