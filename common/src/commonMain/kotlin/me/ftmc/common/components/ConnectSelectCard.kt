package me.ftmc.common.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import me.ftmc.common.LocalLogger
import me.ftmc.common.Server
import me.ftmc.common.globalConfigObject
import me.ftmc.common.saveConfig
import me.ftmc.common.selectedServer
import me.ftmc.common.selectedServerName


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectSelectCard(
  connectSelectExpanded: Boolean,
  editingModeUpdate: (String) -> Unit,
  settingSaveUpdater: () -> Unit
) {
  AnimatedVisibility(
    connectSelectExpanded,
    enter = expandIn(expandFrom = Alignment.TopCenter) + fadeIn(),
    exit = shrinkOut(shrinkTowards = Alignment.TopCenter) + fadeOut()
  ) {
    val logger = remember { LocalLogger() }
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
      LaunchedEffect(true) {
        logger.info("[ConnectSelectCard] ????????????")
      }
      Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "???????????????", style = MaterialTheme.typography.headlineSmall)
        val tempServerMap = remember { mutableStateMapOf<String, Server>() }
        LaunchedEffect(true) {
          tempServerMap.clear()
          globalConfigObject.serverListWithID.forEach { (key, value) -> tempServerMap[key] = value }
        }
        tempServerMap.forEach { (uuid, rowServer) ->
          Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(.7f)) {
              Text(text = rowServer.name, style = MaterialTheme.typography.bodySmall)
            }
            Column(modifier = Modifier.weight(.1f), horizontalAlignment = Alignment.CenterHorizontally) {
              IconButton(onClick = {
                selectedServer = globalConfigObject.serverListWithID[uuid] ?: Server()
                globalConfigObject.selectedUUID = uuid
                selectedServerName = selectedServer.name
                saveConfig()
                settingSaveUpdater()
                logger.debug("[ConnectSelectCard] ?????????????????? -> ${rowServer.name}")
              }) {
                Icon(Icons.Filled.Check, "???????????????")
              }
            }
            Column(modifier = Modifier.weight(.1f), horizontalAlignment = Alignment.CenterHorizontally) {
              IconButton(onClick = {
                editingModeUpdate(uuid)
                logger.debug("[ConnectSelectCard] ??????????????? -> ${rowServer.name}")
              }) {
                Icon(Icons.Filled.Edit, "???????????????")
              }
            }
            var removeWaiting by remember { mutableStateOf(false) }
            LaunchedEffect(removeWaiting) {
              delay(3000L)
              if (rowServer in tempServerMap.values) {
                removeWaiting = false
              }
            }
            Column(modifier = Modifier.weight(.1f), horizontalAlignment = Alignment.CenterHorizontally) {
              IconButton(onClick = {
                if (removeWaiting) {
                  globalConfigObject.serverListWithID.remove(uuid)
                  if (globalConfigObject.serverListWithID.isEmpty() || globalConfigObject.selectedUUID == uuid) {
                    selectedServer = Server()
                    globalConfigObject.selectedUUID = ""
                    selectedServerName = "????????????"
                  }
                  tempServerMap.remove(uuid)
                  logger.debug("[ConnectSelectCard] ?????????????????? -> ${rowServer.name}")
                } else {
                  removeWaiting = true
                }
              }) {
                Icon(
                  imageVector = if (removeWaiting) {
                    Icons.Filled.Warning
                  } else {
                    Icons.Filled.Delete
                  }, "???????????????"
                )
              }
            }
          }
        }
      }
    }
  }
}