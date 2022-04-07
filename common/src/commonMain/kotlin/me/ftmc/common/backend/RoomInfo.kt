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
import me.ftmc.common.RecRecordingInfoLiteResponse
import me.ftmc.common.RoomAllInfoResponse
import me.ftmc.common.StringDataResponse
import me.ftmc.common.accessKeyId
import me.ftmc.common.accessKeySecret
import me.ftmc.common.getRequestURL
import me.ftmc.common.getSig
import me.ftmc.common.httpClient
import me.ftmc.common.url
import org.slf4j.LoggerFactory
import java.time.Instant

val roomAllInfoFlow = flow {
  LoggerFactory.getLogger("roomAllInfoFlow")
  val cmd = "Room_AllInfo"
  while (true) {
    if (url == "" || accessKeyId == "" || accessKeySecret == "") {
      throw APIError(-1)
    }
    val nowTime = Instant.now().epochSecond
    LocalLogger.debug("[roomAllInfoFlow] 发送获取房间信息请求")
    val httpResponse: HttpResponse = httpClient.submitForm(url = getRequestURL(cmd), formParameters = Parameters.build {
      append("accesskeyid", accessKeyId)
      append("cmd", cmd)
      append("time", nowTime.toString())
      append("sig", getSig(cmd, nowTime))
    })
    LocalLogger.debug("[roomAllInfoFlow] 房间信息响应成功")
    try {
      val responseData: RoomAllInfoResponse = httpResponse.receive()
      LocalLogger.debug("[roomAllInfoFlow] 房间信息解析成功")
      emit(responseData.data)
    } catch (_: NoTransformationFoundException) {
      val errorResponse: String = httpResponse.receive()
      val apiErrorObject = Json.decodeFromString<StringDataResponse>(errorResponse)
      throw APIError(apiErrorObject.code)
    }

    delay(10000L)
  }
}.catch {
  LoggerFactory.getLogger("roomAllInfoFlow")
  if (it is RedirectResponseException) {
    LocalLogger.warn("[roomAllInfoFlow] 发现302重定向")
    val redirectURL = it.response.headers["Location"]
    if (redirectURL != null) {
      LocalLogger.debug("[roomAllInfoFlow] 发送获取错误信息请求")
      val errorResponse: StringDataResponse = httpClient.get(urlString = "$url${redirectURL}")
      LocalLogger.debug("[roomAllInfoFlow] 解析错误信息成功")
      throw APIError(errorResponse.code)
    }
  } else {
    LocalLogger.warn("[roomAllInfoFlow] 发生预料外错误 -> ${it.message}")
    throw it
  }
}.flowOn(Dispatchers.IO)

val recordInfoFlow = flow {
  LoggerFactory.getLogger("recordInfoFlow")
  val cmd = "Rec_RecordingInfo_Lite"
  while (true) {
    if (url == "" || accessKeyId == "" || accessKeySecret == "") {
      throw APIError(-1)
    }
    val nowTime = Instant.now().epochSecond
    LocalLogger.debug("[recordInfoFlow] 发送获取录制状态请求")
    val httpResponse: HttpResponse = httpClient.submitForm(url = getRequestURL(cmd), formParameters = Parameters.build {
      append("accesskeyid", accessKeyId)
      append("cmd", cmd)
      append("time", nowTime.toString())
      append("sig", getSig(cmd, nowTime))
    })
    LocalLogger.debug("[recordInfoFlow] 录制状态响应成功")
    try {
      val responseData: RecRecordingInfoLiteResponse = httpResponse.receive()
      LocalLogger.debug("[recordInfoFlow] 录制状态解析成功")
      emit(responseData.data)
    } catch (_: NoTransformationFoundException) {
      val errorResponse: String = httpResponse.receive()
      val apiErrorObject = Json.decodeFromString<StringDataResponse>(errorResponse)
      throw APIError(apiErrorObject.code)
    }

    delay(10000L)
  }
}.catch {
  LoggerFactory.getLogger("recordInfoFlow")
  if (it is RedirectResponseException) {
    LocalLogger.warn("[recordInfoFlow] 发现302重定向")
    val redirectURL = it.response.headers["Location"]
    if (redirectURL != null) {
      LocalLogger.debug("[recordInfoFlow] 发送获取错误信息请求")
      val errorResponse: StringDataResponse = httpClient.get(urlString = "$url${redirectURL}")
      LocalLogger.debug("[recordInfoFlow] 解析错误信息成功")
      throw APIError(errorResponse.code)
    }
  } else {
    LocalLogger.warn("[recordInfoFlow] 发生预料外错误 -> ${it.message}")
    throw it
  }
}.flowOn(Dispatchers.IO)