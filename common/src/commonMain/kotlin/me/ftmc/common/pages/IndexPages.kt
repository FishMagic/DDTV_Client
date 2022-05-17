package me.ftmc.common.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.ftmc.common.ConnectStatus
import me.ftmc.common.LocalLogger
import me.ftmc.common.components.ClientConfigCard
import me.ftmc.common.components.ConnectEditCard
import me.ftmc.common.components.ConnectSelectCard
import me.ftmc.common.components.ConnectStatusCard
import me.ftmc.common.components.LoginInfoCard
import me.ftmc.common.components.ServerConfigCard
import me.ftmc.common.currentScreenWidth
import me.ftmc.common.navigationBarsHeightModifier
import me.ftmc.common.screenTypeChangeWidth

@Composable
fun IndexPage() {
  val logger = remember { LocalLogger() }
  var connectStatus by remember { mutableStateOf(ConnectStatus.DISCONNECT) }
  var apiUsable by remember { mutableStateOf(true) }
  LaunchedEffect(true) {
    logger.info("[Index] 页面加载")
  }
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
    Column(
      modifier = if (currentScreenWidth >= screenTypeChangeWidth) {
        Modifier.width(350.dp).padding(start = 16.dp, bottom = 16.dp)
      } else {
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp)
      }.verticalScroll(rememberScrollState())
    ) {
      var connectEditExpanded by remember { mutableStateOf(false) }
      var connectSelectExpanded by remember { mutableStateOf(false) }
      var editingUUID: String? by remember { mutableStateOf(null) }
      ConnectStatusCard(
        apiUsable,
        connectStatus,
        connectEditExpanded,
        connectSelectExpanded,
        apiUsableUpdater = { apiUsable = it },
        connectStatusUpdater = { connectStatus = it },
        connectAddExpandedUpdater = {
          connectEditExpanded = it
          connectSelectExpanded = false
          editingUUID = null
        },
        connectSelectExpandedUpdater = {
          connectSelectExpanded = it
          connectEditExpanded = false
        })
      if (connectEditExpanded) {
        Spacer(Modifier.height(8.dp))
      }
      ConnectEditCard(connectEditExpanded, editingUUID) {
        apiUsable = true
        connectEditExpanded = false
        editingUUID = null
      }
      if (connectSelectExpanded) {
        Spacer(Modifier.height(8.dp))
      }
      ConnectSelectCard(connectSelectExpanded, {
        editingUUID = it
        connectEditExpanded = true
        connectSelectExpanded = false
      }) {
        apiUsable = true
        connectSelectExpanded = false
      }
      if (currentScreenWidth < screenTypeChangeWidth) {
        if (connectStatus == ConnectStatus.CONNECT) {
          Spacer(Modifier.height(8.dp))
        }
        LoginInfoCard(connectStatus)
        if (connectStatus == ConnectStatus.CONNECT) {
          Spacer(Modifier.height(8.dp))
        }
        ServerConfigCard(connectStatus)
      }
      Spacer(Modifier.height(8.dp))
      ClientConfigCard()
      if (currentScreenWidth < screenTypeChangeWidth) {
        Spacer(Modifier.height(90.dp))
        Spacer(Modifier.navigationBarsHeightModifier())
      }
    }
    if (currentScreenWidth >= screenTypeChangeWidth) {
      Spacer(modifier = Modifier.width(8.dp))
      Column(modifier = Modifier.width(350.dp).padding(end = 16.dp)) {
        LoginInfoCard(connectStatus)
        if (connectStatus == ConnectStatus.CONNECT) {
          Spacer(Modifier.height(8.dp))
        }
        ServerConfigCard(connectStatus)
      }
    }
  }
}