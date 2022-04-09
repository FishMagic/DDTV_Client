package me.ftmc.common.backend

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import me.ftmc.common.LocalLogger
import me.ftmc.common.StringDataResponse
import me.ftmc.common.accessKeyId
import me.ftmc.common.accessKeySecret
import me.ftmc.common.getSig
import me.ftmc.common.url
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.time.Instant

enum class APIErrorType(val code: Int, val msg: String) {
  UNKNOWN_ERROR(0, "未知错误"),
  SERVER_CONFIG_NULL(-1, "服务器配置为空"),
  NETWORK_CONNECT_FAILED(-2, "网络连接错误"),
  API_NOT_FOUND(-3, "API不存在"),
  SEVER_INTERNAL_ERROR(-4, "服务器内部错误"),
  COOKIE_TIMEOUT(6000, "cookie过期"),
  LOGIN_FAILED(6001, "登录失败"),
  SIG_FAILED(6002, "密钥错误"),
  CMD_FAILED(7000, "命令执行错误")
}

class APIError(val errorType: APIErrorType) : RuntimeException(errorType.msg) {
  constructor(code: Int) : this(
    when (code) {
      -1 -> APIErrorType.SERVER_CONFIG_NULL
      -2 -> APIErrorType.NETWORK_CONNECT_FAILED
      -3 -> APIErrorType.API_NOT_FOUND
      -4 -> APIErrorType.SEVER_INTERNAL_ERROR
      6000 -> APIErrorType.COOKIE_TIMEOUT
      6001 -> APIErrorType.LOGIN_FAILED
      6002 -> APIErrorType.SIG_FAILED
      7000 -> APIErrorType.CMD_FAILED
      else -> APIErrorType.UNKNOWN_ERROR
    }
  )
}

val httpClient = HttpClient {
  install(JsonFeature) {
    serializer = KotlinxSerializer()
  }
}

val getRequestURL: (String) -> String = { cmd -> "$url/api/$cmd" }

suspend inline fun <reified T> httpCmd(cmd: String, extraParameters: Map<String, String> = mapOf(), from: String): T {
  val logger = LocalLogger()
  if (url == "" || accessKeyId == "" || accessKeySecret == "") {
    logger.warn("[httpCmd] 服务器参数为空，退出执行命令")
    throw APIError(APIErrorType.SERVER_CONFIG_NULL)
  }
  val nowTime = Instant.now().epochSecond
  logger.debug("[httpCmd] 发送HTTP请求 -> $from")
  try {
    val httpResponse: HttpResponse = httpClient.submitForm(url = getRequestURL(cmd), formParameters = Parameters.build {
      append("accesskeyid", accessKeyId)
      append("cmd", cmd)
      append("time", nowTime.toString())
      append("sig", getSig(cmd, nowTime))
      extraParameters.forEach { (key, value) ->
        append(key, value)
      }
    })
    logger.info("[httpCmd] HTTP响应成功")
    try {
      val resultObject: T = httpResponse.receive()
      if (resultObject is StringDataResponse) {
        if (resultObject.code != 0) {
          throw APIError(resultObject.code)
        }
      }
      return resultObject
    } catch (_: NoTransformationFoundException) {
      logger.warn("[httpCmd] 解析失败，尝试解析错误信息")
      val errorResponse: String = httpResponse.receive()
      val apiErrorObject = Json.decodeFromString<StringDataResponse>(errorResponse)
      logger.debug("[httpCmd] 错误信息解析成功")
      throw APIError(apiErrorObject.code)
    }
  } catch (e: Throwable) {
    httpErrorProcessor(e, from)
  }
  throw APIError(APIErrorType.UNKNOWN_ERROR)
}

suspend fun httpCmdWithoutResult(
  cmd: String,
  extraParameters: Map<String, String> = mapOf(),
  from: String
): HttpResponse {
  return httpCmd(cmd, extraParameters, from)
}

fun httpErrorProcessor(e: Throwable, from: String) {
  val logger = LocalLogger()
  when (e) {
    is SocketTimeoutException -> {
      logger.warn("[httpErrorProcessor] 网络连接超时 -> $from, ${e.javaClass.name} ,${e.message}")
      throw APIError(APIErrorType.NETWORK_CONNECT_FAILED)
    }
    is ConnectException -> {
      logger.warn("[httpErrorProcessor] 网络连接异常 -> $from, ${e.javaClass.name} ,${e.message}")
      throw APIError(APIErrorType.NETWORK_CONNECT_FAILED)
    }
    is SocketException -> {
      logger.warn("[httpErrorProcessor] 会话连接异常 -> $from, ${e.javaClass.name} ,${e.message}")
      throw APIError(APIErrorType.NETWORK_CONNECT_FAILED)
    }
    is APIError -> {
      logger.warn("[httpErrorProcessor] 发生API错误 -> $from, ${e.errorType.msg}")
      throw APIError(APIErrorType.NETWORK_CONNECT_FAILED)
    }
    else -> {
      logger.warn("[httpErrorProcessor] 发生预料外错误 -> $from, ${e.javaClass.name} ,${e.message}")
      throw APIError(APIErrorType.UNKNOWN_ERROR)
    }
  }
}