package me.ftmc.common.backend

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import me.ftmc.common.LocalLogger
import me.ftmc.common.SystemInfoResponse

val systemInfoFlow = flow {
  val logger = LocalLogger()
  val cmd = "System_Info"
  while (true) {
    logger.debug("[systemInfoFlow] 发送获取系统信息请求")
    val httpResponse: SystemInfoResponse = httpCmd(cmd, from = "systemInfoFlow")
    logger.debug("[systemInfoFlow] 系统信息响应成功")
    logger.debug("[systemInfoFlow] 系统信息解析成功")
    emit(httpResponse.data)
    delay(5000L)
  }
}.flowOn(Dispatchers.IO)