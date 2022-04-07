package me.ftmc.common.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.ftmc.common.currentScreenWidth
import me.ftmc.common.navigationBarsHeightModifier
import me.ftmc.common.screenTypeChangeWidth

private const val version = "1.0.0 Alpha"

@Composable
fun AboutPage() {
  Row(
    modifier = Modifier.fillMaxSize().padding(16.dp),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(text = "非官方", style = MaterialTheme.typography.headlineSmall)
      Text(text = "DDTV客户端", style = MaterialTheme.typography.headlineLarge)
      Text(text = "基于官方WEB API", style = MaterialTheme.typography.headlineMedium)
      Text(text = "Version: $version", style = MaterialTheme.typography.bodyLarge)
      if (currentScreenWidth >= screenTypeChangeWidth) {
        Spacer(Modifier.height(120.dp))
      } else {
        Spacer(Modifier.height(90.dp))
        Spacer(Modifier.navigationBarsHeightModifier())
      }
      if (currentScreenWidth >= screenTypeChangeWidth) {
        Spacer(Modifier.width(80.dp))
      }
    }
  }
}