package me.ftmc.common.backend

import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.Instant

suspend fun roomCmdWithUID(cmd: String, uid: String): String {
  if (url == "" || accessKeyId == "" || accessKeySecret == "") {
    return "操作失败：服务器参数为空"
  }
  val nowTime = Instant.now().epochSecond
  try {
    val httpResponse: HttpResponse = httpClient.submitForm(
      url = getRequestURL(cmd),
      formParameters = Parameters.build {
        append("accesskeyid", accessKeyId)
        append("cmd", cmd)
        append("time", nowTime.toString())
        append("sig", getSig(cmd, nowTime))
        append("UID", uid)
      }
    )
    if (httpResponse.status.value == 200) {
      try {
        val responseData: StringDataResponse = httpResponse.receive()
        return "操作成功：等待刷新"
      } catch (_: NoTransformationFoundException) {
        val errorResponse: String = httpResponse.receive()
        val apiErrorObject = Json.decodeFromString<StringDataResponse>(errorResponse)
        throw APIError(apiErrorObject.code, apiErrorObject.massage)
      }
    }
  } catch (e: RedirectResponseException) {
    val redirectURL = e.response.headers["Location"]
    if (redirectURL != null) {
      val errorResponse: StringDataResponse = httpClient.get(urlString = "${url}${redirectURL}")
      throw APIError(errorResponse.code)
    }
  }
  return "操作失败：未知原因"
}

suspend fun roomCmdWithUIDAndBoolean(cmd: String, uid: String, key: String, value: Boolean): String {
  if (url == "" || accessKeyId == "" || accessKeySecret == "") {
    return "操作失败：服务器数据为空"
  }
  val nowTime = Instant.now().epochSecond
  try {
    val httpResponse: HttpResponse = httpClient.submitForm(
      url = getRequestURL(cmd),
      formParameters = Parameters.build {
        append("accesskeyid", accessKeyId)
        append("cmd", cmd)
        append("time", nowTime.toString())
        append("sig", getSig(cmd, nowTime))
        append("UID", uid)
        append(key, value.toString())
      }
    )
    if (httpResponse.status.value == 200) {
      try {
        val responseData: StringDataResponse = httpResponse.receive()
        return "操作成功：等待刷新"
      } catch (_: NoTransformationFoundException) {
        val errorResponse: String = httpResponse.receive()
        val apiErrorObject = Json.decodeFromString<StringDataResponse>(errorResponse)
        throw APIError(apiErrorObject.code, apiErrorObject.massage)
      }
    }
  } catch (e: RedirectResponseException) {
    val redirectURL = e.response.headers["Location"]
    if (redirectURL != null) {
      val errorResponse: StringDataResponse = httpClient.get(urlString = "${url}${redirectURL}")
      throw APIError(errorResponse.code)
    }
  }
  return "操作失败：未知原因"
}