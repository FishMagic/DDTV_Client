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
import me.ftmc.common.backend.roomCmdRoomAllInfoData

enum class NotificationChannels(
  val channelId: String, val channelName: String, val importance: Int, val priority: Int
) {
  FOREGROUND("foreground", "服务状态", NotificationManager.IMPORTANCE_MIN, NotificationCompat.PRIORITY_MIN),
  LIVE_STATUS("live_status", "直播状态", NotificationManager.IMPORTANCE_HIGH, NotificationCompat.PRIORITY_MAX),
  RECORD_STATUS("record_status", "录制状态", NotificationManager.IMPORTANCE_HIGH, NotificationCompat.PRIORITY_MAX)
}

class MonitorWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
  private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

  override suspend fun doWork(): Result {
    val roomsState = mutableStateMapOf<Long, MutableList<Boolean>>()
    val logger = LocalLogger()
    logger.info("[MonitorWorker] 后台服务启动")
    setForeground(getForegroundInfo())
    while (true) {
      try {
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
                      .setSmallIcon(android.R.drawable.ic_dialog_info)
                      .setContentText(notificationMessage).setPriority(priority)
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
                      .setSmallIcon(android.R.drawable.ic_dialog_info)
                      .setContentText(notificationMessage).setPriority(priority)
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
      } catch (_: Exception) {
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
        .setContentText("客户端后台运行中").setOngoing(true).addAction(android.R.drawable.ic_delete, "停止后台服务", cancelIntent)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .build()

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