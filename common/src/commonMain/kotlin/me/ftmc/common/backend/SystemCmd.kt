package me.ftmc.common.backend

import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import me.ftmc.common.APIError
import me.ftmc.common.LocalLogger
import me.ftmc.common.StringDataResponse
import me.ftmc.common.accessKeyId
import me.ftmc.common.accessKeySecret
import me.ftmc.common.getRequestURL
import me.ftmc.common.getSig
import me.ftmc.common.httpClient
import me.ftmc.common.url
import org.slf4j.LoggerFactory
import java.time.Instant

suspend fun systemCmdWithBoolean(cmd: String, key: String, value: Boolean): String {
  LoggerFactory.getLogger("roomCmdWithUIDAndBoolean")
  if (url == "" || accessKeyId == "" || accessKeySecret == "") {
    return "操作失败：服务器数据为空"
  }
  val nowTime = Instant.now().epochSecond
  try {
    LocalLogger.debug("[systemCmdWithBoolean] 发送系统操作请求 -> $cmd, $key, $value")
    val httpResponse: HttpResponse = httpClient.submitForm(url = getRequestURL(cmd), formParameters = Parameters.build {
      append("accesskeyid", accessKeyId)
      append("cmd", cmd)
      append("time", nowTime.toString())
      append("sig", getSig(cmd, nowTime))
      append(key, value.toString())
    })
    LocalLogger.debug("[systemCmdWithBoolean] 系统操作响应成功")
    val responseData: StringDataResponse = httpResponse.receive()
    return if (responseData.code == 0) {
      LocalLogger.debug("[systemCmdWithBoolean] 操作成功")
      "操作成功"
    } else {
      LocalLogger.warn("[systemCmdWithBoolean] 操作失败 -> ${responseData.massage}")
      responseData.massage
    }
  } catch (e: RedirectResponseException) {
    LocalLogger.warn("[systemCmdWithBoolean] 发现302重定向")
    val redirectURL = e.response.headers["Location"]
    if (redirectURL != null) {
      LocalLogger.debug("[systemCmdWithBoolean] 发送获取错信息请求")
      val errorResponse: StringDataResponse = httpClient.get(urlString = "$url${redirectURL}")
      LocalLogger.debug("[systemCmdWithBoolean] 解析错误信息成功")
      throw APIError(errorResponse.code)
    }
  } catch (e: Exception) {
    LocalLogger.warn("[systemCmdWithBoolean] 发生预料外错误 -> ${e.message}")
    throw e
  }
  LocalLogger.warn("[systemCmdWithBoolean] 操作失败")
  return "操作失败：未知原因"
}

suspend fun systemCmdWithLong(cmd: String, key: String, value: Long): String {
  LoggerFactory.getLogger("systemCmdWithLong")
  if (url == "" || accessKeyId == "" || accessKeySecret == "") {
    return "操作失败：服务器数据为空"
  }
  val nowTime = Instant.now().epochSecond
  try {
    LocalLogger.debug("[systemCmdWithLong] 发送系统操作请求 -> $cmd, $key, $value")
    val httpResponse: HttpResponse = httpClient.submitForm(url = getRequestURL(cmd), formParameters = Parameters.build {
      append("accesskeyid", accessKeyId)
      append("cmd", cmd)
      append("time", nowTime.toString())
      append("sig", getSig(cmd, nowTime))
      append(key, value.toString())
    })
    LocalLogger.debug("[systemCmdWithLong] 系统操作响应成功")
    val responseData: StringDataResponse = httpResponse.receive()
    return if (responseData.code == 0) {
      LocalLogger.debug("[systemCmdWithLong] 操作成功")
      "操作成功"
    } else {
      LocalLogger.warn("[systemCmdWithLong] 操作失败 -> ${responseData.massage}")
      responseData.massage
    }
  } catch (e: RedirectResponseException) {
    LocalLogger.warn("[systemCmdWithLong] 发现302重定向")
    val redirectURL = e.response.headers["Location"]
    if (redirectURL != null) {
      LocalLogger.debug("[systemCmdWithLong] 发送获取错信息请求")
      val errorResponse: StringDataResponse = httpClient.get(urlString = "$url${redirectURL}")
      LocalLogger.debug("[systemCmdWithLong] 解析错误信息成功")
      throw APIError(errorResponse.code)
    }
  } catch (e: Exception) {
    LocalLogger.warn("[systemCmdWithLong] 发生预料外错误 -> ${e.message}")
    throw e
  }
  LocalLogger.warn("[systemCmdWithLong] 操作失败")
  return "操作失败：未知原因"
}