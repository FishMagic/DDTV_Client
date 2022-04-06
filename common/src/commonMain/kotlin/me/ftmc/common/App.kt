package me.ftmc.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import me.ftmc.common.pages.IndexPage
import me.ftmc.common.pages.StatusPage

val screenTypeChangeWidth = 800.dp
var currentScreenWidth = screenTypeChangeWidth

enum class TabList(val tabName: String, val tabIcon: ImageVector) {
  Index("首页", Icons.Filled.Home),
  Status("状态", Icons.Filled.PieChart),
  Settings("设置", Icons.Filled.Settings),
  About("关于", Icons.Filled.Info)
}

enum class ConnectStatus(val statusString: String) {
  CONNECT("连接成功"),
  DISCONNECT("未连接"),
  COOKIE_TIMEOUT("登录信息失效"),
  LOGIN_FAILED("登录验证失败"),
  SIG_FAILED("密钥错误"),
  CMD_FAILED("操作失败"),
  NET_ERROR("网络错误"),
  UNKNOWN_ERROR("未知错误")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
  var tabSelected by remember { mutableStateOf(TabList.Index) }
  loadConfig()
  Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text(tabSelected.tabName) }) }, bottomBar = {
    AnimatedVisibility(currentScreenWidth < screenTypeChangeWidth) {
      NavigationBar {
        TabList.values().forEach {
          NavigationRailItem(selected = tabSelected == it,
            onClick = { tabSelected = it },
            icon = { Icon(it.tabIcon, it.tabName) },
            label = { Text(it.tabName) })
        }
      }
    }
  }, floatingActionButton = {
    AnimatedVisibility(tabSelected == TabList.Status,
      enter = slideIn { IntOffset(it.width / 2, it.height / 2) } + fadeIn(),
      exit = slideOut { IntOffset(it.width / 2, it.height / 2) } + fadeOut()) {
      FloatingActionButton(onClick = {}) {
        Icon(Icons.Filled.Add, "添加房间")
      }
    }
  }) {
    Row(modifier = Modifier.fillMaxSize()) {
      AnimatedVisibility(currentScreenWidth >= screenTypeChangeWidth) {
        NavigationRail {
          TabList.values().forEach {
            NavigationRailItem(selected = tabSelected == it,
              onClick = { tabSelected = it },
              icon = { Icon(it.tabIcon, it.tabName) },
              label = { Text(it.tabName) })
          }
        }
      }
      when (tabSelected) {
        TabList.Index -> IndexPage()
        TabList.Status -> StatusPage()
      }
    }
  }
}

fun saveWindowsSizeType(width: Dp) {
  currentScreenWidth = width
}
