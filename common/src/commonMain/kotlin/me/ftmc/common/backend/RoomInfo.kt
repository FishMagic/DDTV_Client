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

val roomAllInfoFlow = flow {
  val logger = LoggerFactory.getLogger("roomAllInfoFlow")
  val cmd = "Room_AllInfo"
  while (true) {
    if (url == "" || accessKeyId == "" || accessKeySecret == "") {
      delay(1000L)
      continue
    }
    val nowTime = Instant.now().epochSecond
    logger.debug("[roomAllInfoFlow] 发送获取房间信息请求")
    val httpResponse: HttpResponse = httpClient.submitForm(url = getRequestURL(cmd), formParameters = Parameters.build {
      append("accesskeyid", accessKeyId)
      append("cmd", cmd)
      append("time", nowTime.toString())
      append("sig", getSig(cmd, nowTime))
    })
    logger.debug("[roomAllInfoFlow] 房间信息响应成功")
    try {
      val responseData: RoomAllInfoResponse = httpResponse.receive()
      logger.debug("[roomAllInfoFlow] 房间信息解析成功")
      emit(responseData.data)
    } catch (_: NoTransformationFoundException) {
      val errorResponse: String = httpResponse.receive()
      val apiErrorObject = Json.decodeFromString<StringDataResponse>(errorResponse)
      throw APIError(apiErrorObject.code)
    }

    delay(1000L)
  }
}.catch {
  val logger = LoggerFactory.getLogger("roomAllInfoFlow")
  if (it is RedirectResponseException) {
    logger.warn("[roomAllInfoFlow] 发现302重定向")
    val redirectURL = it.response.headers["Location"]
    if (redirectURL != null) {
      logger.debug("[roomAllInfoFlow] 发送获取错误信息请求")
      val errorResponse: StringDataResponse = httpClient.get(urlString = "${url}${redirectURL}")
      logger.debug("[roomAllInfoFlow] 解析错误信息成功")
      throw APIError(errorResponse.code)
    }
  } else {
    logger.warn("[roomAllInfoFlow] 发生预料外错误 -> ${it.message}")
    throw it
  }
}.flowOn(Dispatchers.IO)

val recordInfoFlow = flow {
  val logger = LoggerFactory.getLogger("recordInfoFlow")
  val cmd = "Rec_RecordingInfo_Lite"
  while (true) {
    if (url == "" || accessKeyId == "" || accessKeySecret == "") {
      delay(1000L)
      continue
    }
    val nowTime = Instant.now().epochSecond
    logger.debug("[recordInfoFlow] 发送获取录制状态请求")
    val httpResponse: HttpResponse = httpClient.submitForm(url = getRequestURL(cmd), formParameters = Parameters.build {
      append("accesskeyid", accessKeyId)
      append("cmd", cmd)
      append("time", nowTime.toString())
      append("sig", getSig(cmd, nowTime))
    })
    logger.debug("[recordInfoFlow] 录制状态响应成功")
    try {
      val responseData: RecRecordingInfoLiteResponse = httpResponse.receive()
      logger.debug("[recordInfoFlow] 录制状态解析成功")
      emit(responseData.data)
    } catch (_: NoTransformationFoundException) {
      val errorResponse: String = httpResponse.receive()
      val apiErrorObject = Json.decodeFromString<StringDataResponse>(errorResponse)
      throw APIError(apiErrorObject.code)
    }

    delay(1000L)
  }
}.catch {
  val logger = LoggerFactory.getLogger("recordInfoFlow")
  if (it is RedirectResponseException) {
    logger.warn("[recordInfoFlow] 发现302重定向")
    val redirectURL = it.response.headers["Location"]
    if (redirectURL != null) {
      logger.debug("[recordInfoFlow] 发送获取错误信息请求")
      val errorResponse: StringDataResponse = httpClient.get(urlString = "${url}${redirectURL}")
      logger.debug("[recordInfoFlow] 解析错误信息成功")
      throw APIError(errorResponse.code)
    }
  } else {
    logger.warn("[recordInfoFlow] 发生预料外错误 -> ${it.message}")
    throw it
  }
}.flowOn(Dispatchers.IO)