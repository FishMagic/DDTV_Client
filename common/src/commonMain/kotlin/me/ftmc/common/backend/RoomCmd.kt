package me.ftmc.common.backend

import me.ftmc.common.LocalLogger
import me.ftmc.common.RoomAllInfoData
import me.ftmc.common.RoomAllInfoResponse
import me.ftmc.common.StringDataResponse

suspend fun roomCmdRoomAllInfoData(): List<RoomAllInfoData> {
  val logger = LocalLogger()
  val cmd = "Room_AllInfo"
  logger.debug("[roomCmdRoomAllInfoData] 发送房间操作请求 -> $cmd")
  val httpResponse: RoomAllInfoResponse = httpCmd(cmd, from = "roomCmd")
  logger.debug("[roomCmdRoomAllInfoData] 房间信息响应成功")
  return httpResponse.data
}

suspend fun roomCmdWithUID(cmd: String, uid: String) {
  val logger = LocalLogger()
  logger.debug("[roomCmdWithUID] 发送房间操作请求 -> $cmd, $uid")
  val httpResponse: StringDataResponse = httpCmd(cmd, mutableMapOf(Pair("UID", uid)), "roomCmdWithUID")
  logger.debug("[roomCmdWithUID] 房间操作响应成功")
  if (httpResponse.code == 0) {
    logger.debug("[roomCmdWithUID] 操作成功")
  } else {
    logger.warn("[roomCmdWithUID] 操作失败 -> ${httpResponse.massage}")
    throw APIError(httpResponse.code)
  }
}

suspend fun roomCmdWithUIDAndBoolean(cmd: String, uid: String, key: String, value: Boolean) {
  val logger = LocalLogger()
  logger.debug("[roomCmdWithUIDAndBoolean] 发送房间操作请求 -> $cmd, $uid, $key, $value")
  val httpResponse: StringDataResponse =
    httpCmd(cmd, mutableMapOf(Pair("UID", uid), Pair(key, value.toString())), "roomCmdWithUIDAndBoolean")
  logger.debug("[roomCmdWithUIDAndBoolean] 房间操作响应成功")
  if (httpResponse.code == 0) {
    logger.debug("[roomCmdWithUIDAndBoolean] 操作成功")
  } else {
    logger.warn("[roomCmdWithUIDAndBoolean] 操作失败 -> ${httpResponse.massage}")
    throw  APIError(httpResponse.code)
  }
}