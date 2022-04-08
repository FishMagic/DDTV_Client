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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import me.ftmc.android.ui.theme.AppTheme
import me.ftmc.common.App
import me.ftmc.common.LocalLogger
import me.ftmc.common.backend.roomAllInfoFlow
import me.ftmc.common.darkMode
import me.ftmc.common.screenTypeChangeWidth

fun main() = application {
  var windowShow by remember { mutableStateOf(true) }
  val trayState = rememberTrayState()
  val logger = remember { LocalLogger() }
  if (windowShow) {
    Window(
      onCloseRequest = { windowShow = false },
      state = WindowState(size = DpSize(screenTypeChangeWidth, 650.dp)),
      resizable = false,
      title = "DDTV 客户端",
      icon = painterResource("app_icon.png")
    ) {
      logger.info("[Window] 窗口加载")
      AppTheme(darkTheme = darkMode ?: isSystemInDarkTheme()) {
        App()
      }
    }
  }
  if (!windowShow) {
    Tray(icon = painterResource("tray_icon.png"), state = trayState, onAction = { windowShow = true }) {
      logger.info("[Tray] 托盘区加载")
      val roomsState = remember { mutableStateMapOf<Long, MutableList<Boolean>>() }
      LaunchedEffect(true) {
        roomAllInfoFlow.catch { }.collect { roomInfoList ->
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
                  val title = "开播状态变化"
                  val message = "${username}${if (liveStatus == 1) "开播了" else "下播了"}"
                  try {
                    trayState.sendNotification(Notification(title, message))
                    logger.debug("[Tray] 直播状态变化通知发送成功 -> ${uid}, $liveStatus")
                  } catch (e: Exception) {
                    logger.debug("[Tray] 直播状态变化通知发送失败 -> ${e.javaClass.name}, ${e.message}")
                  }
                }
                if (roomState[1] != isDownload) {
                  logger.debug("[Tray] 检测到录制状态变化 -> ${uid}, $isDownload")
                  val title = "录制状态变化"
                  val message = "${username}${if (isDownload) "录制开始" else "录制结束"}"
                  try {
                    trayState.sendNotification(Notification(title, message))
                    logger.debug("[Tray] 录制状态变化通知发送成功 -> ${uid}, $liveStatus")
                  } catch (e: Exception) {
                    logger.debug("[Tray] 录制状态变化通知发送失败 -> ${e.javaClass.name}, ${e.message}")
                  }
                }
                roomState[0] = liveStatus == 1
                roomState[1] = isDownload
              }
            }
          }
        }
      }
      Item(text = "主界面") {
        windowShow = true
      }
      Item(text = "退出", onClick = ::exitApplication)
    }
  }
}