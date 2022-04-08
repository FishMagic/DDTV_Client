package me.ftmc.common

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant

@Serializable
class LogObject(val level: String, val time: Long, val message: String)

class LocalLogger {
  companion object {
    private val backendLogger: Logger = LoggerFactory.getLogger("FrontEnd")
    val loggerBuket = mutableListOf<LogObject>()
    var maxSize by mutableStateOf(100)
  }

  @Synchronized
  fun info(message: String) {
    synchronized(loggerBuket) {
      if (maxSize > 0 && loggerBuket.size >= maxSize) {
        loggerBuket.removeAt(0)
      }
      backendLogger.info(message)
      loggerBuket.add(LogObject("INFO", Instant.now().epochSecond, message))
    }
  }

  @Synchronized
  fun warn(message: String) {
    synchronized(loggerBuket) {
      if (maxSize > 0 && loggerBuket.size >= maxSize) {
        loggerBuket.removeAt(0)
      }
      backendLogger.warn(message)
      loggerBuket.add(LogObject("WARN", Instant.now().epochSecond, message))
    }
  }

  @Synchronized
  fun debug(message: String) {
    synchronized(loggerBuket) {
      if (maxSize > 0 && loggerBuket.size >= maxSize) {
        loggerBuket.removeAt(0)
      }
      backendLogger.debug(message)
      loggerBuket.add(LogObject("DEBUG", Instant.now().epochSecond, message))
    }
  }
}