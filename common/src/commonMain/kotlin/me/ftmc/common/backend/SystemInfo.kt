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
import java.time.Instant

val systemInfoFlow = flow {
  val cmd = "System_Info"
  while (true) {
    if (url == "" || accessKeyId == "" || accessKeySecret == "") {
      delay(1000L)
      continue
    }
    val nowTime = Instant.now().epochSecond
    val httpResponse: HttpResponse = httpClient.submitForm(url = getRequestURL(cmd), formParameters = Parameters.build {
      append("accesskeyid", accessKeyId)
      append("cmd", cmd)
      append("time", nowTime.toString())
      append("sig", getSig(cmd, nowTime))
    })
    if (httpResponse.status.value == 200) {
      try {
        val responseData: SystemInfoResponse = httpResponse.receive()
        emit(responseData.data)
      } catch (_: NoTransformationFoundException) {
        val errorResponse: String = httpResponse.receive()
        val apiErrorObject = Json.decodeFromString<StringDataResponse>(errorResponse)
        throw APIError(apiErrorObject.code)
      }
    }
    delay(5000L)
  }
}
  .catch {
    if (it is RedirectResponseException) {
      val redirectURL = it.response.headers["Location"]
      if (redirectURL != null) {
        val errorResponse: StringDataResponse = httpClient.get(urlString = "${url}${redirectURL}")
        throw APIError(errorResponse.code)
      }
    } else {
      throw it
    }
  }
  .flowOn(Dispatchers.IO)