package me.ftmc.common.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import me.ftmc.common.ConnectStatus
import me.ftmc.common.LocalLogger
import me.ftmc.common.backend.APIError
import me.ftmc.common.backend.getRequestURL
import me.ftmc.common.backend.httpClient
import me.ftmc.common.backend.httpCmdWithoutResult
import me.ftmc.common.backend.loginStateFlow
import me.ftmc.common.byteArrayToImageBitmap


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginInfoCard(connectStatus: ConnectStatus) {
  AnimatedVisibility(
    connectStatus == ConnectStatus.CONNECT,
    enter = expandIn(expandFrom = Alignment.TopCenter) + fadeIn(),
    exit = shrinkOut(shrinkTowards = Alignment.TopCenter) + fadeOut()
  ) {
    val logger = remember { LocalLogger() }
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
      LaunchedEffect(true) {
        logger.info("[LoginInfoCard] ????????????")
      }
      val loginStatusCardScope = rememberCoroutineScope()
      var loginCode by remember { mutableStateOf(0) }
      var qrCodeExpanded by remember { mutableStateOf(false) }
      LaunchedEffect(loginCode) {
        loginStateFlow.catch { }.collect {
          loginCode = it.LoginState
          qrCodeExpanded = it.LoginState != 1 && it.LoginState != 2
          logger.debug("[LoginInfoCard] ?????????????????? -> $loginCode")
        }
      }
      Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Bilibili ??????", style = MaterialTheme.typography.headlineSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "???????????????${
              when (loginCode) {
                0 -> "?????????"
                1 -> "?????????"
                2 -> "????????????"
                3 -> "?????????"
                else -> "??????"
              }
            }", style = MaterialTheme.typography.bodySmall
          )
          AnimatedVisibility(
            loginCode != 1, enter = fadeIn(), exit = fadeOut()
          ) {
            TextButton(onClick = { qrCodeExpanded = !qrCodeExpanded }) {
              Text(text = "???????????????")
            }
          }
          AnimatedVisibility(
            loginCode == 1 || loginCode == 2, enter = fadeIn(), exit = fadeOut()
          ) {
            var logoutButtonEnable by remember { mutableStateOf(true) }
            TextButton(onClick = {
              logger.info("[LoginInfoCard] ??????????????????")
              logoutButtonEnable = false
              loginStatusCardScope.launch(Dispatchers.IO) {
                try {
                  logger.debug("[LoginInfoCard] ????????????????????????????????????")
                  val cmd = "Login_Reset"
                  httpCmdWithoutResult(cmd, from = "LoginInfoCard")
                  logger.info("[LoginInfoCard] ??????????????????????????????")
                } catch (e: APIError) {
                  logger.warn("[LoginInfoCard] ????????????????????????API?????? -> ${e.errorType.msg}")
                } catch (e: Exception) {
                  logger.warn("[LoginInfoCard] ??????????????????????????????????????? -> ${e.javaClass.name} ,${e.message}")
                  logger.errorCatch(e)
                }
                logoutButtonEnable = true
                loginCode = 3
              }
            }, enabled = logoutButtonEnable) {
              Text(text = "????????????")
            }
          }
        }
        AnimatedVisibility(
          qrCodeExpanded,
          enter = expandIn(expandFrom = Alignment.TopCenter) + fadeIn(),
          exit = shrinkOut(shrinkTowards = Alignment.TopCenter) + fadeOut()
        ) {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            var imageLoading by remember { mutableStateOf(true) }
            var imageBitmap: ImageBitmap? by remember { mutableStateOf(null) }
            LaunchedEffect(true) {
              while (true) {
                imageLoading = true
                try {
                  val imageByteArray: ByteArray = httpClient.get(urlString = getRequestURL("loginqr")).body()
                  imageBitmap = byteArrayToImageBitmap(imageByteArray)
                  imageLoading = false
                  delay(30000L)
                } catch (e: Exception) {
                  delay(500L)
                }
              }
            }
            if (imageLoading || imageBitmap == null) {
              CircularProgressIndicator(modifier = Modifier.size(256.dp))
            } else {
              Image(bitmap = imageBitmap!!, contentDescription = null, modifier = Modifier.size(256.dp))
            }
          }
        }
      }
    }
  }
}