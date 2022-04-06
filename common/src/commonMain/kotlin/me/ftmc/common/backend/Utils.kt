package me.ftmc.common.backend

import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

var darkMode: Boolean? = null

var url = ""
var accessKeyId = ""
var accessKeySecret = ""

val httpClient = HttpClient {
  install(JsonFeature) {
    serializer = KotlinxSerializer()
  }
}

val getRequestURL: (String) -> String = { cmd -> "$url/api/$cmd" }

fun getSig(cmd: String, nowTime: Long): String {
  val strToSig = "accesskeyid=${accessKeyId};accesskeysecret=${accessKeySecret};cmd=${cmd.lowercase()};time=${nowTime};"
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

class APIError(val code: Int, val msg: String = "") : RuntimeException()

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
  val EndTime: Long,
  val FilePath: String,
  val RoomId: String,
  val StartTime: Long,
  val Title: String,
  val Token: String,
  val TotalDownloadCount: Long,
  val Uid: Long
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
  val CreationTime: String?,
  val DanmuFile: String?,
  @SerialName("Description") val DescriptionDescription: String?,
  val DownloadedFileInfo: DownloadedFileInfo?,
  val DownloadedLog: String?,
  val DownloadingList: String?,
  val IsAutoRec: Boolean?,
  val IsCliping: Boolean?,
  val IsDownload: Boolean?,
  val IsRecDanmu: Boolean?,
  val IsRemind: Boolean?,
  val IsUserCancel: Boolean?,
  val Like: Boolean?,
  val Shell: String?,
  val area: Int?,
  val area_name: String?,
  val area_v2_id: Int?,
  val area_v2_name: String?,
  val area_v2_parent_id: Int?,
  val area_v2_parent_name: String?,
  val attention: Int?,
  val broadcast_type: Int?,
  val cover_from_user: String?,
  val description: String?,
  val encrypted: Boolean?,
  val face: String?,
  val hidden_till: String?,
  val is_hidden: Boolean?,
  val is_locked: Boolean?,
  val is_portrait: Boolean?,
  val is_sp: Int?,
  val keyframe: String?,
  val level: Int?,
  val live_status: Int?,
  val live_time: Long?,
  val lock_till: String?,
  val need_p2p: Int?,
  val online: Int?,
  val pwd_verified: Boolean?,
  val roomStatus: Int?,
  val roomWebSocket: String?,
  val room_id: Int?,
  val room_shield: Int?,
  val roundStatus: Int?,
  val sex: String?,
  val short_id: Int?,
  val sign: String?,
  val special_type: Int?,
  val tag_name: String?,
  val tags: String?,
  val title: String?,
  val uid: Long?,
  val uname: String?,
  val url: String?
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