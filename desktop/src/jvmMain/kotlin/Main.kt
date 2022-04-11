import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberTrayState
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import me.ftmc.android.ui.theme.AppTheme
import me.ftmc.common.App
import me.ftmc.common.LocalLogger
import me.ftmc.common.backend.APIError
import me.ftmc.common.backend.APIErrorType
import me.ftmc.common.backend.loginStateCmd
import me.ftmc.common.backend.roomCmdRoomAllInfoData
import me.ftmc.common.darkMode
import me.ftmc.common.notification
import me.ftmc.common.screenTypeChangeWidth

fun main() = application {
  var windowShow by remember { mutableStateOf(true) }
  val trayState = rememberTrayState()
  val logger = remember { LocalLogger() }
  LaunchedEffect(true) {
    val typeFile = File(".type")
    withContext(Dispatchers.IO) {
      if (!typeFile.exists()) {
        typeFile.createNewFile()
      }
      val typeFOS = FileOutputStream(typeFile)
      typeFOS.write("client".toByteArray())
      typeFOS.flush()
      typeFOS.close()
    }
  }
  if (windowShow) {
    Window(
      onCloseRequest = {
        if (notification) {
          windowShow = false
        } else {
          exitApplication()
        }
      },
      state = WindowState(size = DpSize(screenTypeChangeWidth, 650.dp)),
      resizable = false,
      title = "DDTV 客户端",
      icon = painterResource("icon.png")
    ) {
      logger.info("[Window] 窗口加载")
      AppTheme(darkTheme = darkMode ?: isSystemInDarkTheme()) {
        App()
      }
      logger.info("[Window] 窗口加载完成")
    }
  }
  if (!windowShow) {
    Tray(icon = painterResource("icon.png"), state = trayState, onAction = { windowShow = true }) {
      logger.info("[Tray] 托盘区加载")
      val roomsState = remember { mutableStateMapOf<Long, MutableList<Boolean>>() }
      var nowLoginStatus by remember { mutableStateOf(-1) }
      LaunchedEffect(true) {
        logger.info("[Tray] 后台服务启动")
        while (true) {
          try {
            logger.debug("[Tray] 发送获取登录状态请求")
            val httpResponse = loginStateCmd()
            logger.debug("[Tray] 登录状态响应成功")
            val newLoginState = httpResponse.data.LoginState
            if (nowLoginStatus < 0) {
              nowLoginStatus = newLoginState
            } else {
              if (nowLoginStatus != newLoginState) {
                logger.info("[Tray] 检测到登录状态改变 -> $newLoginState")
                val notificationTitle = "DDTV 登录状态"
                val notificationMessage = when (newLoginState) {
                  0 -> "未登录"
                  1 -> "已登陆"
                  2 -> "登陆失效"
                  3 -> "登陆中"
                  else -> "未知"
                }
                trayState.sendNotification(Notification(notificationTitle, notificationMessage))
                nowLoginStatus = newLoginState
              }
            }
            if (httpResponse.data.LoginState != 1) {
              delay(1000L)
              continue
            }
            val roomInfoList = withContext(Dispatchers.IO) { roomCmdRoomAllInfoData() }
            roomInfoList.forEach { roomInfo ->
              val uid = roomInfo.uid
              val username = roomInfo.uname
              val liveStatus = roomInfo.live_status
              val isDownload = roomInfo.IsDownload
              val roomState = roomsState[uid]
              if (uid != null && liveStatus != null && isDownload != null && username != null) {
                if (roomState == null) {
                  roomsState[uid] = mutableListOf(liveStatus == 1, isDownload)
                } else {
                  if (roomState[0] != (liveStatus == 1)) {
                    logger.debug("[Tray] 检测到开播状态变化 -> ${uid}, $liveStatus")
                    try {
                      val notificationTitle = "开播状态变化"
                      val notificationMessage = "${username}${if (liveStatus == 1) "开播了" else "下播了"}"
                      trayState.sendNotification(Notification(notificationTitle, notificationMessage))
                      logger.debug("[Tray] 直播状态变化通知发送成功 -> ${uid}, $liveStatus")
                    } catch (e: Exception) {
                      logger.debug("[Tray] 直播状态变化通知发送失败 -> ${e.message}")
                    }
                  }
                  if (roomState[1] != isDownload) {
                    logger.debug("[Tray] 检测到录制状态变化 -> ${uid}, $isDownload")
                    try {
                      val notificationTitle = "录制状态变化"
                      val notificationMessage = "${username}${if (isDownload) "录制开始" else "录制结束"}"
                      trayState.sendNotification(Notification(notificationTitle, notificationMessage))
                      logger.debug("[Tray] 录制状态变化通知发送成功 -> ${uid}, $liveStatus")
                    } catch (e: Exception) {
                      logger.debug("[Tray] 录制状态变化通知发送失败 -> ${e.message}")
                    }
                  }
                  roomState[0] = liveStatus == 1
                  roomState[1] = isDownload
                }
              }
            }
            delay(30000L)
          } catch (e: Exception) {
            val notificationTitle = "DDTV 客户端警告"
            val notificationMessage = if (e is APIError) {
              when (e.errorType) {
                APIErrorType.UNKNOWN_ERROR -> "连接发生未知错误"
                APIErrorType.SERVER_CONFIG_NULL -> "服务器配置为空"
                APIErrorType.NETWORK_CONNECT_FAILED -> "连接服务器失败"
                APIErrorType.API_NOT_FOUND -> "API接口无效"
                APIErrorType.SEVER_INTERNAL_ERROR -> "服务器发生内部错误"
                APIErrorType.COOKIE_TIMEOUT -> "cookie过期"
                APIErrorType.LOGIN_FAILED -> "登录失败"
                APIErrorType.SIG_FAILED -> "签名无效"
                APIErrorType.CMD_FAILED -> "命令执行错误"
              }
            } else {
              "DDTV 客户端发生未知错误"
            }
            trayState.sendNotification(Notification(notificationTitle, notificationMessage))
            break
          }
        }
      }
      Item(text = "主界面") {
        windowShow = true
      }
      Item(text = "退出", onClick = ::exitApplication)
      logger.info("[Tray] 托盘区加载完成")
    }
  }
}