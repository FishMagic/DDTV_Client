package me.ftmc.common

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant

class LogObject(val level: String, val time: Long, val message: String)

object LocalLogger {
  var loggerBuket = mutableListOf<LogObject>()
  private val backendLogger: Logger = LoggerFactory.getLogger("FrontEnd")

  @Synchronized
  fun info(message: String) {
    backendLogger.info(message)
    loggerBuket.add(LogObject("INFO", Instant.now().epochSecond, message))
  }

  @Synchronized
  fun warn(message: String) {
    backendLogger.warn(message)
    loggerBuket.add(LogObject("WARN", Instant.now().epochSecond, message))
  }

  @Synchronized
  fun debug(message: String) {
    backendLogger.debug(message)
    loggerBuket.add(LogObject("DEBUG", Instant.now().epochSecond, message))
  }
}