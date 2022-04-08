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
import me.ftmc.common.BooleanDataResponse
import me.ftmc.common.LocalLogger
import me.ftmc.common.LoginStateResponse
import me.ftmc.common.StringDataResponse
import me.ftmc.common.accessKeyId
import me.ftmc.common.accessKeySecret
import me.ftmc.common.getRequestURL
import me.ftmc.common.getSig
import me.ftmc.common.httpClient
import me.ftmc.common.url
import java.time.Instant

@Deprecated("服务器端接口已废弃", replaceWith = ReplaceWith("loginStateFlow"))
val loginStatusFlow = flow {
  val logger = LocalLogger()
  val cmd = "System_QueryUserState"
  while (true) {
    if (url == "" || accessKeyId == "" || accessKeySecret == "") {
      throw APIError(-1)
    }
    val nowTime = Instant.now().epochSecond
    logger.debug("[loginStatusFlow] 发送获取登录状态请求")
    val httpResponse: HttpResponse = httpClient.submitForm(url = getRequestURL(cmd), formParameters = Parameters.build {
      append("accesskeyid", accessKeyId)
      append("cmd", cmd)
      append("time", nowTime.toString())
      append("sig", getSig(cmd, nowTime))
    })
    logger.debug("[loginStatusFlow] 登录状态响应成功")
    try {
      val responseData: BooleanDataResponse = httpResponse.receive()
      logger.debug("[loginStatusFlow] 登录状态解析成功")
      emit(responseData.data)
      if (responseData.data) {
        delay(300000L)
      } else {
        break
      }
    } catch (_: NoTransformationFoundException) {
      logger.warn("[loginStatusFlow] 登录状态解析失败，尝试解析错误信息")
      val errorResponse: String = httpResponse.receive()
      val apiErrorObject = Json.decodeFromString<StringDataResponse>(errorResponse)
      logger.debug("[loginStatusFlow] 错误信息解析成功")
      throw APIError(apiErrorObject.code)
    }
  }
}.catch {
  val logger = LocalLogger()
  if (it is RedirectResponseException) {
    logger.warn("[loginStatusFlow] 发现302重定向")
    val redirectURL = it.response.headers["Location"]
    if (redirectURL != null) {
      logger.debug("[loginStatusFlow] 发送获取错误信息请求")
      val errorResponse: StringDataResponse = httpClient.get(urlString = "$url${redirectURL}")
      logger.debug("[loginStatusFlow] 解析错误信息成功")
      throw APIError(errorResponse.code)
    }
  } else {
    logger.warn("[loginStatusFlow] 发生预料外错误 -> ${it.message}")
    throw it
  }
}.flowOn(Dispatchers.IO)

val loginStateFlow = flow {
  val logger = LocalLogger()
  val cmd = "Login_State"
  while (true) {
    if (url == "" || accessKeyId == "" || accessKeySecret == "") {
      throw APIError(-1)
    }
    val nowTime = Instant.now().epochSecond
    logger.debug("[loginStateFlow] 发送获取登录状态请求")
    val httpResponse: HttpResponse = httpClient.submitForm(url = getRequestURL(cmd), formParameters = Parameters.build {
      append("accesskeyid", accessKeyId)
      append("cmd", cmd)
      append("time", nowTime.toString())
      append("sig", getSig(cmd, nowTime))
    })
    logger.debug("[loginStateFlow] 登录状态响应成功")
    try {
      val responseData: LoginStateResponse = httpResponse.receive()
      logger.debug("[loginStateFlow] 登录状态解析成功")
      emit(responseData.data)
      if (responseData.data.LoginState == 1) {
        delay(30000L)
      } else {
        delay(1000L)
      }
    } catch (_: NoTransformationFoundException) {
      logger.warn("[loginStateFlow] 登录状态解析失败，尝试解析错误信息")
      val errorResponse: String = httpResponse.receive()
      val apiErrorObject = Json.decodeFromString<StringDataResponse>(errorResponse)
      logger.debug("[loginStateFlow] 错误信息解析成功")
      throw APIError(apiErrorObject.code)
    }
  }
}.catch {
  val logger = LocalLogger()
  if (it is RedirectResponseException) {
    logger.warn("[loginStateFlow] 发现302重定向")
    val redirectURL = it.response.headers["Location"]
    if (redirectURL != null) {
      logger.debug("[loginStateFlow] 发送获取错误信息请求")
      val errorResponse: StringDataResponse = httpClient.get(urlString = "$url${redirectURL}")
      logger.debug("[loginStateFlow] 解析错误信息成功")
      throw APIError(errorResponse.code)
    }
  } else {
    logger.warn("[loginStateFlow] 发生预料外错误 -> ${it.message}")
    throw it
  }
}.flowOn(Dispatchers.IO)