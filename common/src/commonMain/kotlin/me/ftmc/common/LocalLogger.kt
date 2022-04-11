package me.ftmc.common

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.FileWriter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlinx.serialization.Serializable
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Serializable
class LogObject(val level: String, val time: Long, val message: String) {
  override fun toString(): String {
    return "[${this.level}] ${formatLongTime(this.time)}: ${this.message}"
  }
}

@Serializable
enum class LocalLogLevel(val levelInt: Int) {
  NONE(0),
  INFO(1),
  WARNING(2),
  DEBUG(3),
  ALL(4)
}

class LocalLogger {
  companion object {
    private val backendLogger: Logger = LoggerFactory.getLogger("FrontEnd")
    val loggerBuket = mutableListOf<LogObject>()
    var maxSize by mutableStateOf(100)
    var logFile = createLogFile(
      DateTimeFormatter.ofPattern("yyyy-MM-dd-hh-mm-ss")
        .format(LocalDateTime.ofEpochSecond(Instant.now().epochSecond, 0, ZoneOffset.ofHours(8)))
    )
    var logLevel by mutableStateOf(LocalLogLevel.ALL)
  }

  @Synchronized
  fun info(message: String) {
    if (logLevel >= LocalLogLevel.INFO) {
      synchronized(loggerBuket) {
        if (maxSize > 0 && loggerBuket.size >= maxSize) {
          loggerBuket.removeAt(0)
        }
        backendLogger.info(message)
        val logObject = LogObject("INFO", Instant.now().epochSecond, message)
        loggerBuket.add(logObject)
        val writer = FileWriter(logFile, true)
        writer.write(logObject.toString() + "\n")
        writer.flush()
        writer.close()
      }
    }
  }

  @Synchronized
  fun warn(message: String) {
    if (logLevel >= LocalLogLevel.WARNING) {
      synchronized(loggerBuket) {
        if (maxSize > 0 && loggerBuket.size >= maxSize) {
          loggerBuket.removeAt(0)
        }
        backendLogger.warn(message)
        val logObject = LogObject("WARN", Instant.now().epochSecond, message)
        loggerBuket.add(logObject)
        val writer = FileWriter(logFile, true)
        writer.write(logObject.toString() + "\n")
        writer.flush()
        writer.close()
      }
    }
  }

  @Synchronized
  fun debug(message: String) {
    if (logLevel >= LocalLogLevel.DEBUG) {
      synchronized(loggerBuket) {
        if (maxSize > 0 && loggerBuket.size >= maxSize) {
          loggerBuket.removeAt(0)
        }
        backendLogger.debug(message)
        val logObject = LogObject("DEBUG", Instant.now().epochSecond, message)
        loggerBuket.add(logObject)
        val writer = FileWriter(logFile, true)
        writer.write(logObject.toString() + "\n")
        writer.flush()
        writer.close()
      }
    }
  }

  @Synchronized
  fun errorCatch(e: Throwable) {
    val writer = FileWriter(logFile, true)
    writer.write(e.stackTrace.toString() + "\n")
    writer.flush()
    writer.close()
  }
}