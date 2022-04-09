package me.ftmc.common.backend

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import me.ftmc.common.LocalLogger
import me.ftmc.common.LoginStateResponse

val loginStateFlow = flow {
  val logger = LocalLogger()
  val cmd = "Login_State"
  while (true) {
    logger.debug("[loginStateFlow] 发送获取登录状态请求")
    val httpResponse: LoginStateResponse = httpCmd(cmd, from = "loginStateFlow")
    logger.debug("[loginStateFlow] 登录状态响应成功")
    emit(httpResponse.data)
    if (httpResponse.data.LoginState == 1) {
      delay(30000L)
    } else {
      delay(1000L)
    }
  }
}.flowOn(Dispatchers.IO)