package me.ftmc.common.backend

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import me.ftmc.common.LocalLogger
import me.ftmc.common.RecRecordingInfoLiteResponse
import me.ftmc.common.RoomAllInfoResponse

val roomAllInfoFlow = flow {
  val logger = LocalLogger()
  val cmd = "Room_AllInfo"
  while (true) {
    logger.debug("[roomAllInfoFlow] 发送获取房间信息请求")
    val httpResponse: RoomAllInfoResponse = httpCmd(cmd, from = "roomAllInfoFlow")
    logger.debug("[roomAllInfoFlow] 房间信息响应成功")
    emit(httpResponse.data)
    delay(10000L)
  }
}.flowOn(Dispatchers.IO)

val recordInfoFlow = flow {
  val logger = LocalLogger()
  val cmd = "Rec_RecordingInfo_Lite"
  while (true) {
    logger.debug("[recordInfoFlow] 发送获取录制状态请求")
    val httpResponse: RecRecordingInfoLiteResponse = httpCmd(cmd, from = "Rec_RecordingInfo_Lite")
    logger.debug("[recordInfoFlow] 录制状态响应成功")
    emit(httpResponse.data)
    delay(10000L)
  }
}.flowOn(Dispatchers.IO)