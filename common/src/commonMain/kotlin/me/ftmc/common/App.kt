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
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import me.ftmc.common.pages.AboutPage
import me.ftmc.common.pages.IndexPage
import me.ftmc.common.pages.LogPage
import me.ftmc.common.pages.StatusPage
import me.ftmc.common.pages.addFakeRoom
import kotlin.math.ln

val screenTypeChangeWidth = 800.dp
var currentScreenWidth = screenTypeChangeWidth

enum class TabList(val tabName: String, val tabIcon: ImageVector) {
  Index("??????", Icons.Filled.Home),
  Status("??????", Icons.Filled.PieChart),
  Log("??????", Icons.Filled.ListAlt),
  About("??????", Icons.Filled.Info)
}

enum class ConnectStatus(val statusString: String) {
  CONNECT("????????????"),
  DISCONNECT("?????????"),
  COOKIE_TIMEOUT("??????????????????"),
  LOGIN_FAILED("??????????????????"),
  SIG_FAILED("????????????"),
  CMD_FAILED("????????????"),
  NET_ERROR("????????????"),
  UNKNOWN_ERROR("????????????")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
  var tabSelected by rememberSaveable { mutableStateOf(TabList.Index) }
  val logger = remember { LocalLogger() }
  LaunchedEffect(true) {
    loadConfig()
    logger.info("[Main] ????????????")
  }
  val currentPrimaryColor = MaterialTheme.colorScheme.primary
  val currentSurfaceColor = MaterialTheme.colorScheme.surface
  val alpha = ((4.5f * ln(4f)) + 2f) / 100f
  val elevatedSurfaceColor = currentPrimaryColor.copy(alpha = alpha).compositeOver(currentSurfaceColor)
  Scaffold(topBar = {
    CenterAlignedTopAppBar(
      title = { Text(tabSelected.tabName) },
      modifier = Modifier.topBarModifier(),
      colors = if (currentScreenWidth >= screenTypeChangeWidth) {
        TopAppBarDefaults.centerAlignedTopAppBarColors()
      } else {
        TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = elevatedSurfaceColor
        )
      }
    )
  }, bottomBar = {
    AnimatedVisibility(currentScreenWidth < screenTypeChangeWidth) {
      NavigationBar(modifier = Modifier.bottomBarModifier()) {
        TabList.values().forEach {
          NavigationRailItem(selected = tabSelected == it, onClick = {
            tabSelected = it
            logger.debug("[Main] ????????? ${it.tabName} ?????????")
          }, icon = { Icon(it.tabIcon, it.tabName) }, label = { Text(it.tabName) })
        }
      }
    }
  }, floatingActionButton = {
    AnimatedVisibility(tabSelected == TabList.Status,
      enter = slideIn { IntOffset(it.width / 2, it.height / 2) } + fadeIn(),
      exit = slideOut { IntOffset(it.width / 2, it.height / 2) } + fadeOut()) {
      FloatingActionButton(onClick = {
        logger.debug("[Main] ????????????????????????")
        addFakeRoom()
      }) {
        Icon(Icons.Filled.Add, "????????????")
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
        TabList.Log -> LogPage()
        TabList.About -> AboutPage()
      }
    }
  }
}

fun saveWindowsSizeType(width: Dp) {
  currentScreenWidth = width
}
