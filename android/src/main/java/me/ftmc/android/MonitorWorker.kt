package me.ftmc.android

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.runtime.mutableStateMapOf
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import me.ftmc.common.LocalLogger
import me.ftmc.common.backend.APIError
import me.ftmc.common.backend.APIErrorType.API_NOT_FOUND
import me.ftmc.common.backend.APIErrorType.CMD_FAILED
import me.ftmc.common.backend.APIErrorType.COOKIE_TIMEOUT
import me.ftmc.common.backend.APIErrorType.LOGIN_FAILED
import me.ftmc.common.backend.APIErrorType.NETWORK_CONNECT_FAILED
import me.ftmc.common.backend.APIErrorType.SERVER_CONFIG_NULL
import me.ftmc.common.backend.APIErrorType.SEVER_INTERNAL_ERROR
import me.ftmc.common.backend.APIErrorType.SIG_FAILED
import me.ftmc.common.backend.APIErrorType.UNKNOWN_ERROR
import me.ftmc.common.backend.loginStateCmd
import me.ftmc.common.backend.roomCmdRoomAllInfoData

enum class NotificationChannels(
  val channelId: String, val channelName: String, val importance: Int, val priority: Int
) {
  FOREGROUND("foreground", "服务状态", NotificationManager.IMPORTANCE_MIN, NotificationCompat.PRIORITY_MIN),
  LIVE_STATUS("live_status", "直播状态", NotificationManager.IMPORTANCE_HIGH, NotificationCompat.PRIORITY_MAX),
  RECORD_STATUS("record_status", "录制状态", NotificationManager.IMPORTANCE_HIGH, NotificationCompat.PRIORITY_MAX),
  SERVER_WARNING("warning", "警告", NotificationManager.IMPORTANCE_HIGH, NotificationCompat.PRIORITY_MAX),
  LOGIN_STATE_CHANGE("login_state", "登录状态", NotificationManager.IMPORTANCE_HIGH, NotificationCompat.PRIORITY_MAX)
}

class MonitorWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
  private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

  override suspend fun doWork(): Result {
    val logger = LocalLogger()
    val roomsState = mutableStateMapOf<Long, MutableList<Boolean>>()
    var nowLoginStatus = -1
    logger.info("[MonitorWorker] 后台服务启动")
    setForeground(getForegroundInfo())
    while (true) {
      try {
        logger.debug("[MonitorWorker] 发送获取登录状态请求")
        val httpResponse = loginStateCmd()
        logger.debug("[MonitorWorker] 登录状态响应成功")
        val newLoginState = httpResponse.data.LoginState
        if (nowLoginStatus < 0) {
          nowLoginStatus = newLoginState
        } else {
          if (nowLoginStatus != newLoginState) {
            logger.info("[MonitorWorker] 检测到登录状态改变 -> $newLoginState")
            val channelId = NotificationChannels.LOGIN_STATE_CHANGE.channelId
            val notificationTitle = "DDTV 登录状态"
            val priority = NotificationChannels.LOGIN_STATE_CHANGE.priority
            val notificationMessage = when (newLoginState) {
              0 -> "未登录"
              1 -> "已登陆"
              2 -> "登陆失效"
              3 -> "登陆中"
              else -> "未知"
            }
            val notification =
              NotificationCompat.Builder(applicationContext, channelId).setContentTitle(notificationTitle)
                .setSmallIcon(R.drawable.ic_ddtv).setContentText(notificationMessage).setPriority(priority)
            with(NotificationManagerCompat.from(applicationContext)) {
              notify("登录状态", 5, notification.build())
            }
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
                logger.debug("[MonitorWorker] 检测到开播状态变化 -> ${uid}, $liveStatus")
                try {
                  val channelId = NotificationChannels.LIVE_STATUS.channelId
                  val notificationTitle = "开播状态变化"
                  val notificationMessage = "${username}${if (liveStatus == 1) "开播了" else "下播了"}"
                  val priority = NotificationChannels.LIVE_STATUS.priority
                  val notification =
                    NotificationCompat.Builder(applicationContext, channelId).setContentTitle(notificationTitle)
                      .setSmallIcon(R.drawable.ic_ddtv).setContentText(notificationMessage).setPriority(priority)
                  with(NotificationManagerCompat.from(applicationContext)) {
                    notify(notificationMessage, uid.toInt(), notification.build())
                  }
                  logger.debug("[MonitorWorker] 直播状态变化通知发送成功 -> ${uid}, $liveStatus")
                } catch (e: Exception) {
                  logger.debug("[MonitorWorker] 直播状态变化通知发送失败 -> ${e.message}")
                }
              }
              if (roomState[1] != isDownload) {
                logger.debug("[MonitorWorker] 检测到录制状态变化 -> ${uid}, $isDownload")
                try {
                  val channelId = NotificationChannels.RECORD_STATUS.channelId
                  val notificationTitle = "录制状态变化"
                  val notificationMessage = "${username}${if (isDownload) "录制开始" else "录制结束"}"
                  val priority = NotificationChannels.RECORD_STATUS.priority
                  val notification =
                    NotificationCompat.Builder(applicationContext, channelId).setContentTitle(notificationTitle)
                      .setSmallIcon(R.drawable.ic_ddtv).setContentText(notificationMessage).setPriority(priority)
                  with(NotificationManagerCompat.from(applicationContext)) {
                    notify(notificationMessage, uid.toInt(), notification.build())
                  }
                  logger.debug("[MonitorWorker] 录制状态变化通知发送成功 -> ${uid}, $liveStatus")
                } catch (e: Exception) {
                  logger.debug("[MonitorWorker] 录制状态变化通知发送失败 -> ${e.message}")
                }
              }
              roomState[0] = liveStatus == 1
              roomState[1] = isDownload
            }
          }
        }
        delay(30000L)
      } catch (e: Exception) {
        val channelId = NotificationChannels.SERVER_WARNING.channelId
        val notificationTitle = "DDTV 客户端警告"
        val priority = NotificationChannels.SERVER_WARNING.priority
        val notificationMessage = if (e is APIError) {
          when (e.errorType) {
            UNKNOWN_ERROR -> "连接发生未知错误"
            SERVER_CONFIG_NULL -> "服务器配置为空"
            NETWORK_CONNECT_FAILED -> "连接服务器失败"
            API_NOT_FOUND -> "API接口无效"
            SEVER_INTERNAL_ERROR -> "服务器发生内部错误"
            COOKIE_TIMEOUT -> "cookie过期"
            LOGIN_FAILED -> "登录失败"
            SIG_FAILED -> "签名无效"
            CMD_FAILED -> "命令执行错误"
          }
        } else {
          logger.errorCatch(e)
          return Result.failure()
        }
        val notification = NotificationCompat.Builder(applicationContext, channelId).setContentTitle(notificationTitle)
          .setSmallIcon(R.drawable.ic_ddtv).setContentText(notificationMessage).setPriority(priority)
        with(NotificationManagerCompat.from(applicationContext)) {
          notify(notificationMessage, 2, notification.build())
        }
        return Result.failure()
      }
    }
  }

  override suspend fun getForegroundInfo(): ForegroundInfo {
    val id = NotificationChannels.FOREGROUND.channelId
    val priority = NotificationChannels.FOREGROUND.priority
    val title = "DDTV客户端服务状态"
    val cancelIntent = WorkManager.getInstance(applicationContext).createCancelPendingIntent(getId())

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      createChannel()
    }

    val notification =
      NotificationCompat.Builder(applicationContext, id).setPriority(priority).setContentTitle(title).setTicker(title)
        .setContentText("客户端后台运行中").setOngoing(true).addAction(0, "停止后台服务", cancelIntent)
        .setSmallIcon(R.drawable.ic_ddtv).build()

    return ForegroundInfo(0, notification)
  }

  private fun createChannel() {
    NotificationChannels.values().forEach {
      val channelId = it.channelId
      val descriptionText = it.channelName
      val importance = it.importance
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val mChannel = NotificationChannel(channelId, descriptionText, importance)
        notificationManager.createNotificationChannel(mChannel)
      }
    }
  }
}