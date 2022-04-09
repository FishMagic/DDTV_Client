package me.ftmc.common.backend

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import me.ftmc.common.LocalLogger
import me.ftmc.common.SystemConfigResponse

val systemConfigFlow = flow {
  val logger = LocalLogger()
  val cmd = "System_Config"
  while (true) {
    logger.debug("[systemConfigFlow] 发送获取服务器配置请求")
    val httpResponse: SystemConfigResponse = httpCmd(cmd, from = "systemConfigFlow")
    logger.debug("[systemConfigFlow] 服务器配置响应成功")
    emit(httpResponse.data)
    delay(10000L)
  }
}.flowOn(Dispatchers.IO)