package me.ftmc.common.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File
import me.ftmc.common.LocalLogger
import me.ftmc.common.currentScreenWidth
import me.ftmc.common.getLogFileList
import me.ftmc.common.getPlatformName
import me.ftmc.common.navigationBarsHeightModifier
import me.ftmc.common.removeAllLog
import me.ftmc.common.screenTypeChangeWidth
import me.ftmc.common.shareLogFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogPage() {
  if (currentScreenWidth >= screenTypeChangeWidth) {
    OutlinedCard(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp).fillMaxSize()) {
      LogList()
    }
  } else {
    LogList()
  }
}

@Composable
private fun LogList() {
  Column(
    modifier = Modifier.fillMaxSize().padding(16.dp)
  ) {
    var logList by remember { mutableStateOf(false) }
    var listRefresh by remember { mutableStateOf(false) }
    Row(horizontalArrangement = Arrangement.Center) {
      OutlinedButton(onClick = { logList = !logList }) {
        Text(text = if (logList) "本次日志" else "日志列表")
      }

      if (logList) {
        Spacer(Modifier.width(4.dp))
        OutlinedButton(onClick = {
          removeAllLog()
          listRefresh = !listRefresh
        }) {
          Text(text = "清空日志文件")
        }
      }
    }
    Spacer(Modifier.height(4.dp))
    if (logList) {
      val scrollState = rememberLazyListState()
      val fileList = mutableStateListOf<File>()
      LaunchedEffect(listRefresh) {
        getLogFileList()?.forEach { fileList.add(it) }
      }
      LazyColumn(state = scrollState) {
        items(fileList) { file ->
          Row {
            Column(modifier = Modifier.weight(.9f)) {
              Text(text = file.name)
            }
            if (getPlatformName() == "Android") {
              Column(modifier = Modifier.weight(.1f)) {
                IconButton(onClick = { shareLogFile(file) }) {
                  Icon(Icons.Filled.Share, "分享")
                }
              }
            }
          }
        }
        item {
          if (currentScreenWidth < screenTypeChangeWidth) {
            Spacer(Modifier.height(90.dp))
            Spacer(Modifier.navigationBarsHeightModifier())
          }
        }
      }
    } else {
      val scrollState = rememberLazyListState()
      LazyColumn(
        state = scrollState
      ) {
        itemsIndexed(LocalLogger.loggerBuket) { index, log ->
          Text(log.toString(), style = MaterialTheme.typography.bodySmall)
          LaunchedEffect(true) {
            scrollState.animateScrollToItem(index)
          }
        }
        item {
          if (currentScreenWidth < screenTypeChangeWidth) {
            Spacer(Modifier.height(90.dp))
            Spacer(Modifier.navigationBarsHeightModifier())
          }
        }
      }
    }
  }
}
