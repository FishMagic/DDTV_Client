package me.ftmc.common.backend

import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.time.Instant

val systemInfoFlow = flow {
  val logger = LoggerFactory.getLogger("systemInfoFlow")
  val cmd = "System_Info"
  while (true) {
    if (url == "" || accessKeyId == "" || accessKeySecret == "") {
      delay(1000L)
      continue
    }
    val nowTime = Instant.now().epochSecond
    logger.debug("[systemInfoFlow] 发送获取系统信息请求")
    val httpResponse: HttpResponse = httpClient.submitForm(url = getRequestURL(cmd), formParameters = Parameters.build {
      append("accesskeyid", accessKeyId)
      append("cmd", cmd)
      append("time", nowTime.toString())
      append("sig", getSig(cmd, nowTime))
    })
    logger.debug("[systemInfoFlow] 系统信息响应成功")
    try {
      val responseData: SystemInfoResponse = httpResponse.receive()
      logger.debug("[systemInfoFlow] 系统信息解析成功")
      emit(responseData.data)
    } catch (_: NoTransformationFoundException) {
      logger.warn("[systemInfoFlow] 系统信息解析失败，尝试解析错误信息")
      val errorResponse: String = httpResponse.receive()
      val apiErrorObject = Json.decodeFromString<StringDataResponse>(errorResponse)
      logger.debug("[systemInfoFlow] 错误信息解析成功")
      throw APIError(apiErrorObject.code)
    }
    delay(5000L)
  }
}.catch {
  val logger = LoggerFactory.getLogger("systemInfoFlow")
  if (it is RedirectResponseException) {
    logger.warn("[systemInfoFlow] 发现302重定向")
    val redirectURL = it.response.headers["Location"]
    if (redirectURL != null) {
      logger.debug("[systemInfoFlow] 发送获取错误信息请求")
      val errorResponse: StringDataResponse = httpClient.get(urlString = "${url}${redirectURL}")
      logger.debug("[systemInfoFlow] 解析错误信息成功")
      throw APIError(errorResponse.code)
    }
  } else {
    logger.warn("[systemInfoFlow] 发生预料外错误 -> ${it.message}")
    throw it
  }
}.flowOn(Dispatchers.IO)