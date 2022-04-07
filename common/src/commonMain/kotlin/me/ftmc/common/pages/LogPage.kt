package me.ftmc.common.pages

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.ftmc.common.LocalLogger
import me.ftmc.common.currentScreenWidth
import me.ftmc.common.formatLongTime
import me.ftmc.common.navigationBarsHeightModifier
import me.ftmc.common.screenTypeChangeWidth

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
  val scrollState = rememberLazyListState()
  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(16.dp),
    state = scrollState
  ) {
    itemsIndexed(LocalLogger.loggerBuket) { index, log ->
      Text("[${log.level}] ${formatLongTime(log.time)}: ${log.message}", style = MaterialTheme.typography.bodySmall)
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
