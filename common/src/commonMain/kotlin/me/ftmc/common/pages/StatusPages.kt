package me.ftmc.common.pages

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import me.ftmc.common.LocalLogger
import me.ftmc.common.RoomAllInfoData
import me.ftmc.common.backend.roomAllInfoFlow
import me.ftmc.common.components.RoomCard
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
  val logger = remember { LocalLogger() }
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
  LaunchedEffect(true) {
    logger.debug("[StatusPage] 页面加载")
  }
  LaunchedEffect(true) {
    roomAllInfoFlow.catch {}.collect { newRoomList ->
      logger.debug("[StatusPage] 接收房间信息成功，数量${newRoomList.size}")
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
      logger.debug("[StatusPage] 接收添加房间请求")
    }
    Column(
      modifier = if (currentScreenWidth >= screenTypeChangeWidth) {
        Modifier.width(350.dp).padding(start = 16.dp)
      } else {
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp)
      }.verticalScroll(scrollState)
    ) {
      for (room in if (currentScreenWidth >= screenTypeChangeWidth) childRoomList1 else roomList) {
        RoomCard(room, expandedUpdater = { if (it) columnExpanded1++ else columnExpanded1-- }, cancelUpdater = {
          roomAddCancel = true
          logger.debug("[StatusPage] 卡片上报取消添加房间")
        }) {
          roomAddSuccess = it
          logger.debug("[StatusPage] 卡片上报添加房间成功")
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
          RoomCard(room, expandedUpdater = { if (it) columnExpanded2++ else columnExpanded2-- }, cancelUpdater = {
            roomAddCancel = true
            logger.debug("[StatusPage] 卡片上报取消添加房间")
          }) {
            roomAddSuccess = it
            logger.debug("[StatusPage] 卡片上报添加房间成功")
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