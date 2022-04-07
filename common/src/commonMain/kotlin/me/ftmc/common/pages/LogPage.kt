package me.ftmc.common.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.ftmc.common.LogObject
import me.ftmc.common.currentScreenWidth
import me.ftmc.common.formatLongTime
import me.ftmc.common.screenTypeChangeWidth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogPage(logBuket: MutableList<LogObject>) {
  if (currentScreenWidth >= screenTypeChangeWidth) {
    OutlinedCard(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp).fillMaxSize()) {
      LogList(logBuket, 16.dp)
    }
  } else {
    LogList(logBuket, 0.dp)
  }
}

@Composable
private fun LogList(logBuket: MutableList<LogObject>, topPadding: Dp) {
  val scrollState = rememberScrollState()
  val scrollScope = rememberCoroutineScope()
  Column(
    modifier = Modifier.fillMaxSize().padding(start = 16.dp, top = topPadding, end = 16.dp, bottom = 16.dp)
      .verticalScroll(scrollState)
  ) {
    logBuket.forEach {
      Text("[${it.level}] ${formatLongTime(it.time)}: ${it.message}", style = MaterialTheme.typography.bodySmall)
      scrollScope.launch {
        scrollState.scrollTo(scrollState.maxValue)
      }
    }
    if (currentScreenWidth < screenTypeChangeWidth) {
      Spacer(Modifier.height(90.dp))
    }
  }
}
