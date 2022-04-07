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
import me.ftmc.common.APIError
import me.ftmc.common.LocalLogger
import me.ftmc.common.StringDataResponse
import me.ftmc.common.SystemConfigResponse
import me.ftmc.common.accessKeyId
import me.ftmc.common.accessKeySecret
import me.ftmc.common.getRequestURL
import me.ftmc.common.getSig
import me.ftmc.common.httpClient
import me.ftmc.common.url
import java.time.Instant

val systemConfigFlow = flow {
  val logger = LocalLogger()
  val cmd = "System_Config"
  while (true) {
    if (url == "" || accessKeyId == "" || accessKeySecret == "") {
      throw APIError(-1)
    }
    val nowTime = Instant.now().epochSecond
    logger.debug("[systemConfigFlow] 发送获取服务器配置请求")
    val httpResponse: HttpResponse = httpClient.submitForm(url = getRequestURL(cmd), formParameters = Parameters.build {
      append("accesskeyid", accessKeyId)
      append("cmd", cmd)
      append("time", nowTime.toString())
      append("sig", getSig(cmd, nowTime))
    })
    logger.debug("[systemConfigFlow] 服务器配置响应成功")
    try {
      val responseData: SystemConfigResponse = httpResponse.receive()
      logger.debug("[systemConfigFlow] 服务器配置解析成功")
      emit(responseData.data)
      delay(10000L)
    } catch (_: NoTransformationFoundException) {
      logger.warn("[systemConfigFlow] 服务器配置解析失败，尝试解析错误信息")
      val errorResponse: String = httpResponse.receive()
      val apiErrorObject = Json.decodeFromString<StringDataResponse>(errorResponse)
      logger.debug("[systemConfigFlow] 错误信息解析成功")
      throw APIError(apiErrorObject.code)
    }
  }
}.catch {
  val logger = LocalLogger()
  if (it is RedirectResponseException) {
    logger.warn("[systemConfigFlow] 发现302重定向")
    val redirectURL = it.response.headers["Location"]
    if (redirectURL != null) {
      logger.debug("[systemConfigFlow] 发送获取错误信息请求")
      val errorResponse: StringDataResponse = httpClient.get(urlString = "$url${redirectURL}")
      logger.debug("[systemConfigFlow] 解析错误信息成功")
      throw APIError(errorResponse.code)
    }
  } else {
    logger.warn("[systemConfigFlow] 发生预料外错误 -> ${it.message}")
    throw it
  }
}.flowOn(Dispatchers.IO)