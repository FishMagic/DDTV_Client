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

val loginStatusFlow = flow {
  val cmd = "System_QueryUserState"
  while (true) {
    if (url == "" || accessKeyId == "" || accessKeySecret == "") {
      delay(1000L)
      continue
    }
    val nowTime = Instant.now().epochSecond
    val httpResponse: HttpResponse = httpClient.submitForm(
      url = getRequestURL(cmd),
      formParameters = Parameters.build {
        append("accesskeyid", accessKeyId)
        append("cmd", cmd)
        append("time", nowTime.toString())
        append("sig", getSig(cmd, nowTime))
      }
    )
    if (httpResponse.status.value == 200) {
      try{
        val responseData: BooleanDataResponse = httpResponse.receive()
        emit(responseData.data)
        if (responseData.data) {
          delay(300000L)
        } else {
          delay(1000L)
        }
      } catch (_: NoTransformationFoundException) {
        val errorResponse: String = httpResponse.receive()
        val apiErrorObject = Json.decodeFromString<StringDataResponse>(errorResponse)
        throw APIError(apiErrorObject.code)
      }
    }
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