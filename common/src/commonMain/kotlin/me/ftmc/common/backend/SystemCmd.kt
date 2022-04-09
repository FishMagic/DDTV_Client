package me.ftmc.common.backend

import me.ftmc.common.LocalLogger
import me.ftmc.common.StringDataResponse

suspend fun systemCmdWithBoolean(cmd: String, key: String, value: Boolean) {
  val logger = LocalLogger()
  logger.debug("[systemCmdWithBoolean] 发送系统操作请求 -> $cmd, $key, $value")
  val httpResponse: StringDataResponse =
    httpCmd(cmd, mutableMapOf(Pair(key, value.toString())), from = "systemCmdWithBoolean")
  logger.debug("[systemCmdWithBoolean] 系统操作响应成功")
  if (httpResponse.code == 0) {
    logger.debug("[systemCmdWithBoolean] 操作成功")
  } else {
    logger.warn("[systemCmdWithBoolean] 操作失败 -> ${httpResponse.massage}")
  }
}

suspend fun systemCmdWithLong(cmd: String, key: String, value: Long) {
  val logger = LocalLogger()
  logger.debug("[systemCmdWithLong] 发送系统操作请求 -> $cmd, $key, $value")
  val httpResponse: StringDataResponse =
    httpCmd(cmd, mutableMapOf(Pair(key, value.toString())), from = "systemCmdWithLong")
  logger.debug("[systemCmdWithLong] 系统操作响应成功")
  if (httpResponse.code == 0) {
    logger.debug("[systemCmdWithLong] 操作成功")
  } else {
    logger.warn("[systemCmdWithLong] 操作失败 -> ${httpResponse.massage}")
  }
}