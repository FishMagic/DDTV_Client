package me.ftmc.common

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.ftmc.common.backend.MessageDigestUtils

var darkMode: Boolean? by mutableStateOf(null)
var notification: Boolean by mutableStateOf(true)
var selectedServerName by mutableStateOf("无服务器")

var globalConfigObject = ConfigClass()
var selectedServer = Server()

fun getSig(cmd: String, nowTime: Long): String {
  val strToSig =
    "accesskeyid=${selectedServer.accessKeyId};accesskeysecret=${selectedServer.accessKeySecret};cmd=${cmd.lowercase()};time=${nowTime};"
  return MessageDigestUtils.sha1(strToSig).uppercase()
}

val formatLongTime: (Long) -> String = { longTime ->
  val dataTimeFormat = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
  dataTimeFormat.format(LocalDateTime.ofEpochSecond(longTime, 0, ZoneOffset.ofHours(8)))
}

fun formatDataUnit(byte: Long): String {
  return if (byte < 1024L) {
    String.format("%s B", byte.toString())
  } else if (byte < 1024L * 1024L) {
    String.format("%.2f KiB", byte.toFloat() / 1024)
  } else if (byte < 1024L * 1024L * 1024L) {
    String.format("%.2f MiB", (byte / 1024L).toFloat() / 1024)
  } else if (byte < 1024L * 1024L * 1024L * 1024L) {
    String.format("%.2f GiB", (byte / 1024L / 1024L).toFloat() / 1024)
  } else if (byte < 1024L * 1024L * 1024L * 1024L * 1024L) {
    String.format("%.2f TiB", (byte / 1024L / 1024L / 1024L).toFloat() / 1024)
  } else {
    String.format("%.2f PiB", (byte / 1024L / 1024L / 1024L / 1024L).toFloat() / 1024)
  }
}

enum class ConfigKeys {
  RoomListConfig,
  DownloadPath,
  TmpPath,
  DownloadDirectoryName,
  DownloadFileName,
  TranscodParmetrs,
  IsAutoTranscod,
  WEB_API_SSL,
  pfxFileName,
  pfxPasswordFileName,
  DefaultVolume,
  GUI_FirstStart,
  WEB_FirstStart,
  RecQuality,
  PlayQuality,
  IsRecDanmu,
  IsRecGift,
  IsRecGuard,
  IsRecSC,
  IsFlvSplit,
  FlvSplitSize,
  WebUserName,
  WebPassword,
  AccessKeyId,
  AccessKeySecret,
  ServerAID,
  ServerName,
  ClientAID,
  InitializationStatus,
  HideIconState,
  AccessControlAllowOrigin,
  AccessControlAllowCredentials,
  DoNotSleepWhileDownloading,
  CookieDomain,
  Shell
}

@Serializable
data class ConfigClass(
  val serverList: MutableList<Server> = mutableListOf(),
  val serverListWithID: MutableMap<String, Server> = mutableMapOf(),
  var selectedUUID: String = "",
  var darkMode: Boolean? = null,
  var notification: Boolean = true,
  var logLevel: LocalLogLevel = LocalLogLevel.ALL,
  var logMaxSize: Int = 300
)

@Serializable
data class Server(
  var name: String = "",
  var url: String = "http://",
  var accessKeyId: String = "",
  var accessKeySecret: String = "",
  var selected: Boolean = false
) {
  fun isEmpty(): Boolean {
    return url != "" && accessKeyId != "" && accessKeySecret != ""
  }

  fun isNotEmpty(): Boolean {
    return !isEmpty()
  }

  fun copy(): Server {
    return Server(this.name, this.url, this.accessKeyId, this.accessKeySecret)
  }

  override fun equals(other: Any?): Boolean {
    return if (other is Server) {
      this.url == other.url
    } else {
      false
    }
  }

  override fun hashCode(): Int {
    return url.hashCode()
  }
}

@Serializable
data class SystemInfoResponse(
  val cmd: String, val code: Int, val `data`: SystemInfoData, val massage: String
)

@Serializable
data class SystemInfoData(
  val DDTVCore_Ver: String,
  val Room_Quantity: Int,
  val ServerAID: String,
  val ServerName: String,
  val download_Info: SystemInfoDownloadInfo,
  val os_Info: SystemInfoOsInfo
)

@Serializable
data class SystemInfoDownloadInfo(
  val Completed_Downloads: Int, val Downloading: Int
)

@Serializable
data class SystemInfoOsInfo(
  val AppCore_Ver: String,
  val Associated_Users: String,
  val Current_Directory: String,
  val Memory_Usage: Int,
  val OS_Tpye: String,
  val OS_Ver: String,
  val Runtime_Ver: String,
  val UserInteractive: Boolean,
  val WebCore_Ver: String
)

@Serializable
data class BooleanDataResponse(
  val cmd: String, val code: Int, val `data`: Boolean, val massage: String
)

@Serializable
data class SystemConfigResponse(
  val cmd: String, val code: Int, val `data`: List<SystemConfigData>, val massage: String
)

@Serializable
data class SystemConfigData(
  val Enabled: Boolean, val Group: Int, val Key: Int, val Value: String
)


@Serializable
data class StringListDataResponse(
  val cmd: String, val code: Int, val `data`: List<String>, val massage: String
)

@Serializable
data class StringDataResponse(
  val cmd: String, val code: Int, val `data`: String, val massage: String
)

@Serializable
data class FileGetTypeFileListResponse(
  val cmd: String, val code: Int, val `data`: FileGetTypeFileListData, val massage: String
)

@Serializable
data class FileGetTypeFileListData(
  val fileLists: List<FileGetTypeFileListFileLists>
)

@Serializable
data class FileGetTypeFileListFileLists(
  val Type: String, val files: List<String>
)

@Serializable
data class LoginResponse(
  val cmd: String, val code: Int, val `data`: LoginData, val massage: String
)

@Serializable
data class LoginData(
  val Cookie: String
)

@Serializable
data class RecRecordingInfoLiteResponse(
  val cmd: String, val code: Int, val `data`: List<RecRecordingInfoLiteData>, val massage: String
)

@Serializable
data class RecRecordingInfoLiteData(
  val EndTime: Long?,
  val FilePath: String?,
  val RoomId: String?,
  val StartTime: Long?,
  val Title: String?,
  val Token: String?,
  val TotalDownloadCount: Long?,
  val Uid: Long?
)

@Serializable
data class RecRecordCompleteInfonLiteResponse(
  val cmd: String, val code: Int, val `data`: List<RecRecordCompleteInfonLiteData>, val massage: String
)

@Serializable
data class RecRecordCompleteInfonLiteData(
  val EndTime: Int,
  val FilePath: String,
  val RoomId: String,
  val StartTime: Int,
  val Title: String,
  val Token: String,
  val TotalDownloadCount: Long,
  val Uid: Int
)

@Serializable
data class RoomAllInfoResponse(
  val cmd: String?, val code: Int?, val `data`: List<RoomAllInfoData>, val massage: String?
)

@Serializable
data class RoomAllInfoData(
  val CreationTime: String? = null,
  val DanmuFile: String? = null,
  @SerialName("Description") val DescriptionDescription: String? = null,
  val DownloadedFileInfo: DownloadedFileInfo? = null,
  val DownloadedLog: String? = null,
  val DownloadingList: String? = null,
  val IsAutoRec: Boolean? = null,
  val IsCliping: Boolean? = null,
  val IsDownload: Boolean? = null,
  val IsRecDanmu: Boolean? = null,
  val IsRemind: Boolean? = null,
  val IsUserCancel: Boolean? = null,
  val Like: Boolean? = null,
  val Shell: String? = null,
  val area: Int? = null,
  val area_name: String? = null,
  val area_v2_id: Int? = null,
  val area_v2_name: String? = null,
  val area_v2_parent_id: Int? = null,
  val area_v2_parent_name: String? = null,
  val attention: Int? = null,
  val broadcast_type: Int? = null,
  val cover_from_user: String? = null,
  val description: String? = null,
  val encrypted: Boolean? = null,
  val face: String? = null,
  val hidden_till: String? = null,
  val is_hidden: Boolean? = null,
  val is_locked: Boolean? = null,
  val is_portrait: Boolean? = null,
  val is_sp: Int? = null,
  val keyframe: String? = null,
  val level: Int? = null,
  val live_status: Int? = null,
  val live_time: Long? = null,
  val lock_till: String? = null,
  val need_p2p: Int? = null,
  val online: Int? = null,
  val pwd_verified: Boolean? = null,
  val roomStatus: Int? = null,
  val roomWebSocket: String? = null,
  val room_id: Int? = null,
  val room_shield: Int? = null,
  val roundStatus: Int? = null,
  val sex: String? = null,
  val short_id: Int? = null,
  val sign: String? = null,
  val special_type: Int? = null,
  val tag_name: String? = null,
  val tags: String? = null,
  val title: String? = null,
  val uid: Long? = null,
  val uname: String? = null,
  val url: String? = null
)

@Serializable
data class DownloadedFileInfo(
  val DanMuFile: String?,
  val FlvFile: String?,
  val GiftFile: String?,
  val GuardFile: String?,
  val Mp4File: String?,
  val SCFile: String?
)

@Serializable
data class LoginStateResponse(
  val cmd: String, val code: Int, val `data`: LoginStateData, val massage: String
)

@Serializable
data class LoginStateData(
  val LoginState: Int
)