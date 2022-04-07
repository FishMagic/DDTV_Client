package me.ftmc.common.backend

import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import me.ftmc.common.APIError
import me.ftmc.common.StringDataResponse
import me.ftmc.common.accessKeyId
import me.ftmc.common.accessKeySecret
import me.ftmc.common.getRequestURL
import me.ftmc.common.getSig
import me.ftmc.common.httpClient
import me.ftmc.common.url
import org.slf4j.LoggerFactory
import java.time.Instant

suspend fun roomCmdWithUID(cmd: String, uid: String): String {
  val logger = LoggerFactory.getLogger("roomCmdWithUID")
  if (url == "" || accessKeyId == "" || accessKeySecret == "") {
    return "操作失败：服务器参数为空"
  }
  val nowTime = Instant.now().epochSecond
  try {
    logger.debug("[roomCmdWithUID] 发送房间操作请求 -> $cmd, $uid")
    val httpResponse: HttpResponse = httpClient.submitForm(url = getRequestURL(cmd), formParameters = Parameters.build {
      append("accesskeyid", accessKeyId)
      append("cmd", cmd)
      append("time", nowTime.toString())
      append("sig", getSig(cmd, nowTime))
      append("UID", uid)
    })
    logger.debug("[roomCmdWithUID] 房间操作响应成功")
    val responseData: StringDataResponse = httpResponse.receive()
    return if (responseData.code == 0) {
      logger.debug("[roomCmdWithUID] 操作成功")
      "操作成功"
    } else {
      logger.warn("[roomCmdWithUID] 操作失败 -> ${responseData.massage}")
      responseData.massage
    }
  } catch (e: RedirectResponseException) {
    logger.warn("[roomCmdWithUID] 发现302重定向")
    val redirectURL = e.response.headers["Location"]
    if (redirectURL != null) {
      logger.debug("[roomCmdWithUID] 发送获取错信息请求")
      val errorResponse: StringDataResponse = httpClient.get(urlString = "${url}${redirectURL}")
      logger.debug("[roomCmdWithUID] 解析错误信息成功")
      throw APIError(errorResponse.code)
    }
  } catch (e: Exception) {
    logger.warn("[roomCmdWithUID] 发生预料外错误 -> ${e.message}")
    return "操作失败：${e.message}"
    throw e
  }
  logger.warn("[roomCmdWithUID] 操作失败")
  return "操作失败：未知原因"
}

suspend fun roomCmdWithUIDAndBoolean(cmd: String, uid: String, key: String, value: Boolean): String {
  val logger = LoggerFactory.getLogger("roomCmdWithUIDAndBoolean")
  if (url == "" || accessKeyId == "" || accessKeySecret == "") {
    return "操作失败：服务器数据为空"
  }
  val nowTime = Instant.now().epochSecond
  try {
    logger.debug("[roomCmdWithUIDAndBoolean] 发送房间操作请求 -> $cmd, $uid, $key, $value")
    val httpResponse: HttpResponse = httpClient.submitForm(url = getRequestURL(cmd), formParameters = Parameters.build {
      append("accesskeyid", accessKeyId)
      append("cmd", cmd)
      append("time", nowTime.toString())
      append("sig", getSig(cmd, nowTime))
      append("UID", uid)
      append(key, value.toString())
    })
    logger.debug("[roomCmdWithUIDAndBoolean] 房间操作响应成功")
    val responseData: StringDataResponse = httpResponse.receive()
    return if (responseData.code == 0) {
      logger.debug("[roomCmdWithUIDAndBoolean] 操作成功")
      "操作成功"
    } else {
      logger.warn("[roomCmdWithUIDAndBoolean] 操作失败 -> ${responseData.massage}")
      responseData.massage
    }
  } catch (e: RedirectResponseException) {
    logger.warn("[roomCmdWithUIDAndBoolean] 发现302重定向")
    val redirectURL = e.response.headers["Location"]
    if (redirectURL != null) {
      logger.debug("[roomCmdWithUIDAndBoolean] 发送获取错信息请求")
      val errorResponse: StringDataResponse = httpClient.get(urlString = "${url}${redirectURL}")
      logger.debug("[roomCmdWithUIDAndBoolean] 解析错误信息成功")
      throw APIError(errorResponse.code)
    }
  } catch (e: Exception) {
    logger.warn("[roomCmdWithUIDAndBoolean] 发生预料外错误 -> ${e.message}")
    throw e
  }
  logger.warn("[roomCmdWithUIDAndBoolean] 操作失败")
  return "操作失败：未知原因"
}